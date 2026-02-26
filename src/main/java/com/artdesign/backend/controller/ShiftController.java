package com.artdesign.backend.controller;

import com.artdesign.backend.entity.ShiftSchedule;
import com.artdesign.backend.entity.ShiftType;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.ShiftScheduleRepository;
import com.artdesign.backend.repository.ShiftTypeRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
public class ShiftController {

    @Autowired
    private ShiftTypeRepository shiftTypeRepository;

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HolidayService holidayService;

    // ===== 班次类型管理 =====

    @GetMapping("/types")
    public Map<String, Object> getShiftTypes() {
        List<ShiftType> types = shiftTypeRepository.findAll();
        // 如果没有数据，初始化默认班次类型
        if (types.isEmpty()) {
            initDefaultShiftTypes();
            types = shiftTypeRepository.findAll();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", types);
        return result;
    }

    @PostMapping("/types")
    public Map<String, Object> createShiftType(@RequestBody ShiftType shiftType) {
        ShiftType saved = shiftTypeRepository.save(shiftType);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", saved);
        return result;
    }

    @PutMapping("/types/{id}")
    public Map<String, Object> updateShiftType(@PathVariable Long id, @RequestBody ShiftType shiftType) {
        shiftType.setId(id);
        ShiftType saved = shiftTypeRepository.save(shiftType);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", saved);
        return result;
    }

    @DeleteMapping("/types/{id}")
    public Map<String, Object> deleteShiftType(@PathVariable Long id) {
        shiftTypeRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "删除成功");
        return result;
    }

    // ===== 节假日数据 =====

    @GetMapping("/holidays")
    public Map<String, Object> getHolidays(@RequestParam int year, @RequestParam int month) {
        Set<String> holidays = holidayService.getHolidaysOfMonth(year, month);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", holidays);
        return result;
    }

    // ===== 月排班查询 =====

    /**
     * 获取某月排班数据，按员工分组
     * 返回格式：{ employees: [...], schedules: { employeeId: { day: shiftTypeId } } }
     */
    @GetMapping("/month")
    public Map<String, Object> getMonthSchedule(@RequestParam int year, @RequestParam int month,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> result = new HashMap<>();

        // 获取员工列表（按部门筛选）
        List<User> allEmployees;
        if (deptId != null) {
            allEmployees = userRepository.findAll().stream()
                    .filter(u -> u.getDepartment() != null && deptId.equals(u.getDepartment().getId()))
                    .collect(Collectors.toList());
        } else {
            allEmployees = userRepository.findAll();
        }
        allEmployees = allEmployees.stream()
                .filter(u -> u.getStatus() == null || "1".equals(u.getStatus()) || !"0".equals(u.getStatus()))
                .sorted(Comparator.comparing(User::getEmployeeId))
                .collect(Collectors.toList());

        // 分页截取当页员工
        int total = allEmployees.size();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pageEmployees = allEmployees.subList(fromIndex, toIndex);

        // 转员工基本信息（仅当页）
        List<Map<String, Object>> employeeList = pageEmployees.stream().map(u -> {
            Map<String, Object> emp = new HashMap<>();
            emp.put("employeeId", u.getEmployeeId());
            emp.put("nickName", u.getNickName());
            emp.put("departmentName", u.getDepartment() != null ? u.getDepartment().getName() : "");
            return emp;
        }).collect(Collectors.toList());

        // 只查询当页员工的排班数据，减少无效数据量
        Set<String> pageEmployeeIds = pageEmployees.stream()
                .map(User::getEmployeeId)
                .collect(Collectors.toSet());
        List<ShiftSchedule> schedules = shiftScheduleRepository.findByYearAndMonth(year, month)
                .stream()
                .filter(s -> pageEmployeeIds.contains(s.getEmployeeId()))
                .collect(Collectors.toList());

        // 构建 employeeId -> { day -> shiftTypeId } 的映射
        Map<String, Map<Integer, Long>> scheduleMap = new HashMap<>();
        for (ShiftSchedule s : schedules) {
            scheduleMap.computeIfAbsent(s.getEmployeeId(), k -> new HashMap<>())
                    .put(s.getDay(), s.getShiftTypeId());
        }

        // 获取班次类型
        List<ShiftType> shiftTypes = shiftTypeRepository.findAll();
        if (shiftTypes.isEmpty()) {
            initDefaultShiftTypes();
            shiftTypes = shiftTypeRepository.findAll();
        }

        // 当月节假日
        Set<String> holidays = holidayService.getHolidaysOfMonth(year, month);

        // 当月天数
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        result.put("code", 200);
        result.put("data", Map.of(
                "year", year,
                "month", month,
                "daysInMonth", daysInMonth,
                "employees", employeeList,
                "schedules", scheduleMap,
                "shiftTypes", shiftTypes,
                "holidays", holidays,
                "total", total,
                "page", page,
                "pageSize", pageSize));
        return result;
    }

    // ===== 生成月排班 =====

    @PostMapping("/generate")
    @Transactional
    public Map<String, Object> generateMonthSchedule(@RequestParam int year, @RequestParam int month) {
        Map<String, Object> result = new HashMap<>();

        // 确保有班次类型
        List<ShiftType> types = shiftTypeRepository.findAll();
        if (types.isEmpty()) {
            initDefaultShiftTypes();
            types = shiftTypeRepository.findAll();
        }

        // 找到默认工作班次和休息班次
        ShiftType workType = types.stream().filter(t -> Boolean.TRUE.equals(t.getIsDefault())).findFirst()
                .orElse(types.stream().filter(t -> !Boolean.TRUE.equals(t.getIsRest())).findFirst().orElse(null));
        ShiftType restType = types.stream().filter(t -> Boolean.TRUE.equals(t.getIsRest())).findFirst()
                .orElse(null);

        if (workType == null || restType == null) {
            result.put("code", 500);
            result.put("msg", "班次类型配置不完整，请先配置工作班次和休息班次");
            return result;
        }

        // 获取所有在职员工
        List<User> employees = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == null || !"0".equals(u.getStatus()))
                .collect(Collectors.toList());

        // 批量预查询该月所有「手动」排班，构建 Set<"employeeId_day"> 避免 N×31 次单条查询
        List<ShiftSchedule> manualSchedules = shiftScheduleRepository.findByYearAndMonth(year, month)
                .stream()
                .filter(s -> "MANUAL".equals(s.getSource()))
                .collect(Collectors.toList());
        Set<String> manualKeys = manualSchedules.stream()
                .map(s -> s.getEmployeeId() + "_" + s.getDay())
                .collect(Collectors.toSet());

        // 删除该月已有的自动生成排班（保留手动调班）
        shiftScheduleRepository.deleteAutoByYearAndMonth(year, month);

        // 生成排班
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        List<ShiftSchedule> toSave = new ArrayList<>();

        for (User emp : employees) {
            for (int day = 1; day <= daysInMonth; day++) {
                // 用预加载的 Set 判断是否已有手动排班，无需再查 DB
                if (manualKeys.contains(emp.getEmployeeId() + "_" + day)) {
                    continue;
                }
                boolean isRest = holidayService.isRestDay(year, month, day);
                ShiftSchedule schedule = new ShiftSchedule();
                schedule.setYear(year);
                schedule.setMonth(month);
                schedule.setDay(day);
                schedule.setEmployeeId(emp.getEmployeeId());
                schedule.setShiftTypeId(isRest ? restType.getId() : workType.getId());
                schedule.setSource("AUTO");
                toSave.add(schedule);
            }
        }

        shiftScheduleRepository.saveAll(toSave);

        result.put("code", 200);
        result.put("msg", "排班生成成功，共生成 " + toSave.size() + " 条记录");
        result.put("data", Map.of("count", toSave.size(), "year", year, "month", month));
        return result;
    }

    // ===== 单格调班 =====

    @PutMapping("/cell")
    @Transactional
    public Map<String, Object> updateCell(@RequestBody Map<String, Object> body) {
        int year = (int) body.get("year");
        int month = (int) body.get("month");
        int day = (int) body.get("day");
        String employeeId = (String) body.get("employeeId");
        Long shiftTypeId = Long.parseLong(body.get("shiftTypeId").toString());

        Optional<ShiftSchedule> existing = shiftScheduleRepository
                .findByYearAndMonthAndDayAndEmployeeId(year, month, day, employeeId);

        ShiftSchedule schedule = existing.orElse(new ShiftSchedule());
        schedule.setYear(year);
        schedule.setMonth(month);
        schedule.setDay(day);
        schedule.setEmployeeId(employeeId);
        schedule.setShiftTypeId(shiftTypeId);
        schedule.setSource("MANUAL");
        if (body.containsKey("remark")) {
            schedule.setRemark((String) body.get("remark"));
        }

        shiftScheduleRepository.save(schedule);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "调班成功");
        return result;
    }

    // ===== 内部方法：初始化默认班次类型 =====

    @Transactional
    public void initDefaultShiftTypes() {
        // 早班（默认工作班次）
        ShiftType morning = new ShiftType();
        morning.setName("早班");
        morning.setCode("MORNING");
        morning.setColor("#4ECDC4");
        morning.setWorkStart("09:00");
        morning.setWorkEnd("18:00");
        morning.setIsRest(false);
        morning.setIsDefault(true);
        morning.setRemark("标准工作班次");
        shiftTypeRepository.save(morning);

        // 休息
        ShiftType rest = new ShiftType();
        rest.setName("休息");
        rest.setCode("REST");
        rest.setColor("#FF9F43");
        rest.setWorkStart(null);
        rest.setWorkEnd(null);
        rest.setIsRest(true);
        rest.setIsDefault(false);
        rest.setRemark("休息日");
        shiftTypeRepository.save(rest);

        // 晚班
        ShiftType night = new ShiftType();
        night.setName("晚班");
        night.setCode("NIGHT");
        night.setColor("#A29BFE");
        night.setWorkStart("14:00");
        night.setWorkEnd("22:00");
        night.setIsRest(false);
        night.setIsDefault(false);
        night.setRemark("下午至晚间班次");
        shiftTypeRepository.save(night);
    }
}
