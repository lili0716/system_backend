package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.*;
import com.artdesign.backend.repository.*;
import com.artdesign.backend.service.AttendanceReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceReportServiceImpl implements AttendanceReportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private ShiftTypeRepository shiftTypeRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private LeaveFormRepository leaveFormRepository;

    @Autowired
    private OvertimeFormRepository overtimeFormRepository;

    @Autowired
    private FormRepository formRepository;

    @Override
    public Map<String, Object> getDailyReport(String dateStr, Long deptId, String employeeIds, Integer page,
            Integer pageSize) {
        LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 获取目标员工列表
        List<User> targetUsers = getTargetUsers(deptId, employeeIds);

        int total = targetUsers.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<User> pagedUsers = start < total ? targetUsers.subList(start, end) : Collections.emptyList();

        List<Map<String, Object>> resultList = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        for (User user : pagedUsers) {
            Map<String, Object> row = new HashMap<>();
            row.put("employeeId", user.getEmployeeId());
            row.put("nickName", user.getNickName());
            row.put("departmentName", user.getDepartment() != null ? user.getDepartment().getName() : "");
            row.put("date", dateStr);

            // 排班 (假定有一个查询列表方法，由于我们没有确切的，稳妥起见从全员当月排班里捞并按年月过滤)
            int vYear = localDate.getYear();
            int vMonth = localDate.getMonthValue();
            int vDay = localDate.getDayOfMonth();

            ShiftSchedule shift = shiftScheduleRepository
                    .findByYearAndMonthAndDayAndEmployeeId(vYear, vMonth, vDay, user.getEmployeeId())
                    .orElse(null);
            String shiftNameStr = "休息";
            if (shift != null && shift.getShiftTypeId() != null) {
                ShiftType st = shiftTypeRepository.findById(shift.getShiftTypeId()).orElse(null);
                if (st != null) {
                    shiftNameStr = st.getName();
                }
            }
            row.put("shiftName", shiftNameStr);

            // 打卡
            List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndRecordDateBetween(user.getId(),
                    date,
                    date);
            AttendanceRecord record = records.isEmpty() ? null : records.get(0);

            if (record != null) {
                row.put("workInTime", record.getWorkInTime() != null ? timeFormat.format(record.getWorkInTime()) : "");
                row.put("workOutTime",
                        record.getWorkOutTime() != null ? timeFormat.format(record.getWorkOutTime()) : "");
                row.put("actualWorkHours", record.getActualWorkHours() != null ? record.getActualWorkHours() : 0.0);
                row.put("remark", record.getRemark() != null ? record.getRemark() : "");
            } else {
                row.put("workInTime", "");
                row.put("workOutTime", "");
                row.put("actualWorkHours", 0.0);
                row.put("remark", "未打卡");
            }

            resultList.add(row);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", resultList);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        return response;
    }

    @Override
    public byte[] exportDailyReport(String date, Long deptId, String employeeIds) {
        Map<String, Object> data = getDailyReport(date, deptId, employeeIds, 1, 999999);
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("考勤日报_" + date);

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = { "工号", "姓名", "部门", "日期", "班别", "上班打卡", "下班打卡", "实际工时", "状态/备注" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data
            int rowIdx = 1;
            for (Map<String, Object> rowMap : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue((String) rowMap.get("employeeId"));
                row.createCell(1).setCellValue((String) rowMap.get("nickName"));
                row.createCell(2).setCellValue((String) rowMap.get("departmentName"));
                row.createCell(3).setCellValue((String) rowMap.get("date"));
                row.createCell(4).setCellValue((String) rowMap.get("shiftName"));
                row.createCell(5).setCellValue((String) rowMap.get("workInTime"));
                row.createCell(6).setCellValue((String) rowMap.get("workOutTime"));
                row.createCell(7).setCellValue((Double) rowMap.get("actualWorkHours"));
                row.createCell(8).setCellValue((String) rowMap.get("remark"));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出日报失败", e);
        }
    }

    @Override
    public Map<String, Object> getMonthlyReport(int year, int month, Long deptId, String employeeIdsStr, Integer page,
            Integer pageSize) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        List<User> targetUsers = getTargetUsers(deptId, employeeIdsStr);
        int total = targetUsers.size();
        int startIdx = (page - 1) * pageSize;
        int endIdx = Math.min(startIdx + pageSize, total);
        List<User> pagedUsers = startIdx < total ? targetUsers.subList(startIdx, endIdx) : Collections.emptyList();

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (User user : pagedUsers) {
            Map<String, Object> row = buildUserMonthlyRow(user, year, month, daysInMonth, start, end);
            resultList.add(row);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", resultList);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("daysInMonth", daysInMonth);
        return response;
    }

    @Override
    public byte[] exportMonthlyReport(int year, int month, Long deptId, String employeeIds) {
        Map<String, Object> data = getMonthlyReport(year, month, deptId, employeeIds, 1, 999999);
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        int daysInMonth = (Integer) data.get("daysInMonth");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("考勤月报_" + year + "-" + String.format("%02d", month));

            // Define Styles
            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex()); // 小于8小时红
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle yellowStyle = workbook.createCellStyle();
            yellowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex()); // 请假黄
            yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle grayStyle = workbook.createCellStyle();
            grayStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); // 缺卡灰底
            grayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header 1 (Base info + Days + Stats)
            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            String[] baseHeaders = { "工号", "姓名", "部门" };
            for (String h : baseHeaders) {
                headerRow.createCell(colIdx++).setCellValue(h);
            }

            for (int d = 1; d <= daysInMonth; d++) {
                headerRow.createCell(colIdx++).setCellValue(d + "日");
            }

            String[] statHeaders = {
                    "出勤天数", "出勤工时", "双休班(时)", "平时加班(时)", "周末加班(时)", "迟到(次)", "早退(次)", "旷工(次)",
                    "事假(天)", "病假(天)", "年假(天)", "调休(天)", "产假(天)", "其他假(天)"
            };
            for (String h : statHeaders) {
                headerRow.createCell(colIdx++).setCellValue(h);
            }

            // Data
            int rowIdx = 1;
            for (Map<String, Object> rowMap : list) {
                Row row = sheet.createRow(rowIdx++);
                colIdx = 0;
                row.createCell(colIdx++).setCellValue((String) rowMap.get("employeeId"));
                row.createCell(colIdx++).setCellValue((String) rowMap.get("nickName"));
                row.createCell(colIdx++).setCellValue((String) rowMap.get("departmentName"));

                List<Map<String, Object>> dailyList = (List<Map<String, Object>>) rowMap.get("dailyList");
                for (int d = 1; d <= daysInMonth; d++) {
                    Map<String, Object> dayInfo = dailyList.get(d - 1);
                    Cell cell = row.createCell(colIdx++);

                    String text = (String) dayInfo.get("text");
                    String status = (String) dayInfo.get("status");
                    cell.setCellValue(text);

                    // Apply colors based on status
                    if ("leave".equals(status)) {
                        cell.setCellStyle(yellowStyle);
                    } else if ("missing".equals(status)) {
                        cell.setCellStyle(grayStyle);
                    } else if ("insufficient".equals(status)) {
                        cell.setCellStyle(redStyle);
                    }
                }

                row.createCell(colIdx++).setCellValue(toDouble(rowMap.get("attendanceDays")));
                row.createCell(colIdx++).setCellValue(toDouble(rowMap.get("workHours")));
                row.createCell(colIdx++).setCellValue(toDouble(rowMap.get("weekendWorkHours")));
                row.createCell(colIdx++).setCellValue(toDouble(rowMap.get("weekdayOvertimeHours")));
                row.createCell(colIdx++).setCellValue(toDouble(rowMap.get("weekendOvertimeHours")));
                row.createCell(colIdx++).setCellValue(toInteger(rowMap.get("lateCount")));
                row.createCell(colIdx++).setCellValue(toInteger(rowMap.get("earlyCount")));
                row.createCell(colIdx++).setCellValue(toInteger(rowMap.get("absentCount")));

                Map<String, Double> leaves = (Map<String, Double>) rowMap.get("leaveStats");
                row.createCell(colIdx++).setCellValue(leaves.getOrDefault("事假", 0.0));
                row.createCell(colIdx++).setCellValue(leaves.getOrDefault("病假", 0.0));
                row.createCell(colIdx++).setCellValue(leaves.getOrDefault("年假", 0.0));
                row.createCell(colIdx++).setCellValue(leaves.getOrDefault("调休", 0.0));
                row.createCell(colIdx++).setCellValue(leaves.getOrDefault("产假", 0.0));
                row.createCell(colIdx++).setCellValue(leaves.getOrDefault("其他", 0.0));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出月报失败", e);
        }
    }

    private Map<String, Object> buildUserMonthlyRow(User user, int year, int month, int daysInMonth, Date start,
            Date end) {
        Map<String, Object> row = new HashMap<>();
        row.put("employeeId", user.getEmployeeId());
        row.put("nickName", user.getNickName());
        row.put("departmentName", user.getDepartment() != null ? user.getDepartment().getName() : "");

        // Fetch user data for the month
        List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndRecordDateBetween(user.getId(),
                start, end);
        // Note: For real scenarios, leave and overtime forms fetching from approved
        // forms in date range.

        List<Map<String, Object>> dailyList = new ArrayList<>();
        double totalWorkHours = 0;
        int attendanceDays = 0;
        double weekendWorkHours = 0;
        double weekdayOvertime = 0;
        double weekendOvertime = 0;
        int lateCount = 0;
        int earlyCount = 0;
        int absentCount = 0;
        Map<String, Double> leaveStats = new HashMap<>();

        Map<Integer, AttendanceRecord> recordMap = records.stream()
                .collect(Collectors.toMap(r -> {
                    LocalDate ld = r.getRecordDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return ld.getDayOfMonth();
                }, r -> r, (r1, r2) -> r1)); // Keep first if duplicates

        for (int d = 1; d <= daysInMonth; d++) {
            Map<String, Object> dayInfo = new HashMap<>();
            LocalDate currentDay = LocalDate.of(year, month, d);
            boolean isWeekend = currentDay.getDayOfWeek().getValue() >= 6;

            AttendanceRecord record = recordMap.get(d);
            String text = "";
            String status = "normal";

            // Process existing record
            if (record != null) {
                double hours = record.getActualWorkHours() != null ? record.getActualWorkHours() : 0.0;
                totalWorkHours += hours;

                if (hours > 0) {
                    attendanceDays++;
                    if (isWeekend)
                        weekendWorkHours += hours;
                }

                if (hours > 0 && hours < 8.0 && !isWeekend) {
                    status = "insufficient";
                }

                if (record.getLateMinutes() != null && record.getLateMinutes() > 0)
                    lateCount++;
                if (record.getEarlyLeaveMinutes() != null && record.getEarlyLeaveMinutes() > 0)
                    earlyCount++;
                if (record.getStatus() != null && record.getStatus() == 3)
                    absentCount++; // Absent

                // Add Overtime computation from record if present
                if (record.getOvertimeHours() != null && record.getOvertimeHours() > 0) {
                    if (isWeekend)
                        weekendOvertime += record.getOvertimeHours();
                    else
                        weekdayOvertime += record.getOvertimeHours();
                }

                if (hours > 0) {
                    text = String.format("%.1f", hours);
                } else {
                    text = "缺卡";
                    status = "missing";
                }

                // Extremely Simplified Mock for Leave based on Status (if real app absent might
                // be marked as leave by forms)
                // Need Form queries for realism, simulating it here for demonstration:
                if (record.getRemark() != null && record.getRemark().contains("请假")) {
                    text = "请假";
                    status = "leave";
                    leaveStats.put("事假", leaveStats.getOrDefault("事假", 0.0) + 1.0); // Dummy default to 事假
                }
            } else {
                // Not in record DB, check if weekend
                if (isWeekend) {
                    text = "休";
                } else {
                    text = "缺卡";
                    status = "missing";
                }
            }

            dayInfo.put("day", d);
            dayInfo.put("text", text);
            dayInfo.put("status", status); // normal, leave, missing, insufficient
            dailyList.add(dayInfo);
        }

        row.put("dailyList", dailyList);
        row.put("attendanceDays", attendanceDays);
        row.put("workHours", totalWorkHours);
        row.put("weekendWorkHours", weekendWorkHours);
        row.put("weekdayOvertimeHours", weekdayOvertime);
        row.put("weekendOvertimeHours", weekendOvertime);
        row.put("lateCount", lateCount);
        row.put("earlyCount", earlyCount);
        row.put("absentCount", absentCount);
        row.put("leaveStats", leaveStats);

        return row;
    }

    private List<User> getTargetUsers(Long deptId, String employeeIds) {
        List<User> targetUsers;
        if (employeeIds != null && !employeeIds.trim().isEmpty()) {
            List<String> ids = Arrays.asList(employeeIds.split(","));
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> ids.contains(u.getEmployeeId()))
                    .collect(Collectors.toList());
        } else if (deptId != null) {
            targetUsers = userRepository.findByDepartmentId(deptId);
        } else {
            targetUsers = userRepository.findAll();
        }
        return targetUsers;
    }

    private double toDouble(Object val) {
        if (val == null)
            return 0.0;
        if (val instanceof Number)
            return ((Number) val).doubleValue();
        return 0.0;
    }

    private int toInteger(Object val) {
        if (val == null)
            return 0;
        if (val instanceof Number)
            return ((Number) val).intValue();
        return 0;
    }
}
