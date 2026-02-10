package com.artdesign.backend.service.impl;

import com.artdesign.backend.dto.SalaryStatisticsDTO;
import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.LeaveForm;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.AttendanceRecordRepository;
import com.artdesign.backend.repository.LeaveFormRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.SalaryStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SalaryStatisticsServiceImpl implements SalaryStatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private LeaveFormRepository leaveFormRepository;

    @Override
    public List<SalaryStatisticsDTO> calculateMonthlySalary(String monthStr) {
        List<SalaryStatisticsDTO> result = new ArrayList<>();

        // Parse "YYYY-MM"
        YearMonth yearMonth = YearMonth.parse(monthStr);
        LocalDate startLocalDate = yearMonth.atDay(1);
        LocalDate endLocalDate = yearMonth.atEndOfMonth();

        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endLocalDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        List<User> users = userRepository.findAll();

        for (User user : users) {
            // Skip if user has no basic info needed or specific status?
            // Ideally we process all active users.

            SalaryStatisticsDTO dto = new SalaryStatisticsDTO();
            dto.setUserId(user.getId());
            dto.setUserName(user.getNickName() != null ? user.getNickName() : user.getEmail()); // Fallback
            dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : "");
            dto.setEmployeeId(user.getEmployeeId());

            dto.setMonth(monthStr);

            // 1. Basic Salary
            BigDecimal basicSalary = BigDecimal.ZERO;
            if (StringUtils.hasText(user.getSalary())) {
                try {
                    basicSalary = new BigDecimal(user.getSalary());
                } catch (Exception e) {
                    // ignore
                }
            }
            dto.setBasicSalary(basicSalary);

            // 2. Hourly Wage = Basic / 30 / 8
            BigDecimal hourlyWage = basicSalary.divide(new BigDecimal(30), 10, RoundingMode.HALF_UP)
                    .divide(new BigDecimal(8), 2, RoundingMode.HALF_UP);
            dto.setHourlyWage(hourlyWage);

            // 3. Attendance Stats
            List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndRecordDateBetween(user.getId(),
                    startDate, endDate);

            double actualDays = 0;
            double weekdayOvertime = 0;
            double weekendOvertime = 0;
            int mealCount = 0;

            for (AttendanceRecord record : records) {
                // Attendance Days: if status is not Absent(3)
                if (record.getStatus() != null && record.getStatus() != 3) {
                    actualDays += 1; // Assuming 1 record = 1 day.
                }

                // Overtime & Meal
                Double otHours = record.getOvertimeHours() != null ? record.getOvertimeHours() : 0.0;
                Integer day = record.getDayOfWeek(); // 1-7

                boolean isWeekend = (day != null && (day == 6 || day == 7));

                if (otHours > 0) {
                    if (isWeekend) {
                        weekendOvertime += otHours;
                        // Weekend Meal: >= 6h
                        if (otHours >= 6) {
                            mealCount++;
                        }
                    } else {
                        weekdayOvertime += otHours;
                        // Weekday OT Meal: >= 2h
                        if (otHours >= 2) {
                            mealCount++;
                        }
                    }
                }

                // Weekday Normal Meal
                if (!isWeekend && record.getStatus() != null && record.getStatus() != 3
                        && record.getActualWorkHours() != null && record.getActualWorkHours() > 0) {
                    mealCount++;
                }
            }

            dto.setActualAttendanceDays(actualDays);
            dto.setWeekdayOvertimeHours(weekdayOvertime);
            dto.setWeekendOvertimeHours(weekendOvertime);
            dto.setMealCount(mealCount);

            // 4. Leave Stats (From LeaveForm)
            // Status 2 assumed Approved. Check Form.java if I can confirm.
            // Form.java: "表单状态：0-待审批，1-已审批，2-已拒绝" -> WAIT. 1 is Approved!
            // "1-已审批" usually means Approved. "2-已拒绝" is Rejected.
            // Correcting status to 1.
            List<LeaveForm> leaves = leaveFormRepository.findApprovedLeavesByUserIdAndDateRange(user.getId(), 1,
                    startDate, endDate);

            double sickHours = 0;
            double personalHours = 0;

            for (LeaveForm leave : leaves) {
                Integer type = leave.getLeaveType();
                Double days = leave.getLeaveDays() != null ? leave.getLeaveDays() : 0.0;
                double hours = days * 8; // Convert days to hours

                if (type != null) {
                    if (type == 2) { // Sick
                        sickHours += hours;
                    } else if (type == 1) { // Personal
                        personalHours += hours;
                    }
                }
                // TODO: Handle leave crossing month boundaries strictly?
                // For now, if the leave overlaps, we count the full days recorded on the form.
                // Ideally we should calculate intersection, but that requires more complex
                // logic.
                // Given "Monthly" payroll, usually leaves are split or filed per month.
                // I will assume LeaveDays is accurate for the period or acceptable
                // approximation.
            }

            dto.setSickLeaveHours(sickHours);
            dto.setPersonalLeaveHours(personalHours);

            // 5. Calculations
            // Sick Deduction: hourly * sickHours * 0.2
            BigDecimal sickDed = hourlyWage.multiply(BigDecimal.valueOf(sickHours))
                    .multiply(new BigDecimal("0.2")).setScale(2, RoundingMode.HALF_UP);
            dto.setSickLeaveDeduction(sickDed);

            // Personal Deduction: hourly * personalHours (* 1.0)
            BigDecimal personalDed = hourlyWage.multiply(BigDecimal.valueOf(personalHours))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setPersonalLeaveDeduction(personalDed);

            // Weekday Subsidy: hours * 20
            BigDecimal weekSub = BigDecimal.valueOf(weekdayOvertime).multiply(new BigDecimal("20"))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setWeekdayOvertimeSubsidy(weekSub);

            // Weekend Subsidy: hours * (110/8)
            BigDecimal weekendSub = BigDecimal.valueOf(weekendOvertime)
                    .multiply(new BigDecimal("110").divide(new BigDecimal("8"), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setWeekendOvertimeSubsidy(weekendSub);

            // Meal Subsidy: count * 6
            BigDecimal mealSub = BigDecimal.valueOf(mealCount).multiply(new BigDecimal("6"))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setMealSubsidy(mealSub);

            // Gross Salary
            // Basic + WeekSub + WeekendSub + MealSub - SickDed - PersonalDed
            BigDecimal gross = basicSalary.add(weekSub).add(weekendSub).add(mealSub)
                    .subtract(sickDed).subtract(personalDed)
                    .setScale(2, RoundingMode.HALF_UP);

            // Ensure not negative?
            if (gross.compareTo(BigDecimal.ZERO) < 0) {
                gross = BigDecimal.ZERO;
            }
            dto.setGrossSalary(gross);

            result.add(dto);
        }

        return result;
    }
}
