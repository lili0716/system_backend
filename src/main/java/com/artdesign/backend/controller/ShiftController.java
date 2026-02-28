package com.artdesign.backend.controller;

import com.artdesign.backend.entity.ShiftSchedule;
import com.artdesign.backend.entity.ShiftType;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.ShiftScheduleRepository;
import com.artdesign.backend.repository.ShiftTypeRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.HolidayService;
import com.artdesign.backend.util.JwtUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

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
            @RequestParam(required = false) String employeeIds,
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

        Set<String> empParamsSet = null;
        if (employeeIds != null && !employeeIds.trim().isEmpty()) {
            empParamsSet = java.util.Arrays.stream(employeeIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        final Set<String> targetIds = empParamsSet;

        allEmployees = allEmployees.stream()
                .filter(u -> u.getStatus() == null || "1".equals(u.getStatus()) || !"0".equals(u.getStatus()))
                .filter(u -> targetIds == null || targetIds.contains(u.getEmployeeId()))
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

        // 只查询当页员工的排班数据，减少无效数据量 (使用 IN 条件避免海量回表读取)
        List<String> pageEmployeeIdList = pageEmployees.stream()
                .map(User::getEmployeeId)
                .collect(Collectors.toList());

        List<ShiftSchedule> schedules;
        if (pageEmployeeIdList.isEmpty()) {
            schedules = new java.util.ArrayList<>();
        } else {
            schedules = shiftScheduleRepository.findByYearAndMonthAndEmployeeIdIn(year, month, pageEmployeeIdList);
        }

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

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Autowired
    private com.artdesign.backend.service.ShiftGenerateService shiftGenerateService;

    // ===== 异步长连接带进度条生成月排班 (Redis 分布式锁防护) =====
    @GetMapping(value = "/generate", produces = "text/event-stream;charset=UTF-8")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter generateMonthScheduleParams(
            @RequestParam int year, @RequestParam int month,
            jakarta.servlet.http.HttpServletRequest request) {

        String operator = "system";
        String token = request.getHeader("Authorization");
        if (token != null) {
            String empIdStr = jwtUtil.getEmployeeId(token.startsWith("Bearer ") ? token.substring(7) : token);
            if (empIdStr != null && !empIdStr.isEmpty())
                operator = empIdStr;
        }

        // SSE 断开超时：设定为 6 分钟极长待机时长。
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                360000L);

        // 1. 全局 Redis 并发排他锁
        String lockKey = "lock:shift_generate";
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "running", 15,
                java.util.concurrent.TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(locked)) {
            try {
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error")
                        .data(Map.of("code", 409, "msg", "系统正在全力为您所在的单位生成排班数据，请稍后刷新重试以免造成并发瘫痪。")));
                emitter.complete();
            } catch (Exception e) {
            }
            return emitter;
        }

        try {
            // 确保有班次类型
            List<ShiftType> types = shiftTypeRepository.findAll();
            if (types.isEmpty()) {
                initDefaultShiftTypes();
                types = shiftTypeRepository.findAll();
            }

            ShiftType workType = types.stream().filter(t -> Boolean.TRUE.equals(t.getIsDefault())).findFirst()
                    .orElse(types.stream().filter(t -> !Boolean.TRUE.equals(t.getIsRest())).findFirst().orElse(null));
            ShiftType restType = types.stream().filter(t -> Boolean.TRUE.equals(t.getIsRest())).findFirst()
                    .orElse(null);

            if (workType == null || restType == null) {
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error")
                        .data(Map.of("code", 500, "msg", "班次类型配置不完整，请先配置工作班次和休息班次")));
                emitter.complete();
                redisTemplate.delete(lockKey); // 前置出错解锁
                return emitter;
            }

            // 获取所有在职员工
            List<User> employees = userRepository.findAll().stream()
                    .filter(u -> u.getStatus() == null || !"0".equals(u.getStatus()))
                    .collect(Collectors.toList());

            // 批量预查询该月所有「手动」排班字典
            List<ShiftSchedule> manualSchedules = shiftScheduleRepository.findByYearAndMonth(year, month)
                    .stream().filter(s -> "MANUAL".equals(s.getSource())).collect(Collectors.toList());
            Set<String> manualKeys = manualSchedules.stream()
                    .map(s -> s.getEmployeeId() + "_" + s.getDay())
                    .collect(Collectors.toSet());

            // 删除该月已有的自动生成排班（保留手动调班），以免覆盖出错
            shiftScheduleRepository.deleteAutoByYearAndMonth(year, month);

            // 预查当月全部节假日
            Set<String> holidayStrs = new HashSet<>();
            int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
            for (int d = 1; d <= daysInMonth; d++) {
                if (holidayService.isRestDay(year, month, d)) {
                    holidayStrs.add(String.format("%04d-%02d-%02d", year, month, d));
                }
            }

            // 交由异步线程执行，当前 HTTP 线程携带 emitter 返回脱壳
            shiftGenerateService.generateAsyncSchedules(
                    year, month, operator, employees, workType, restType,
                    manualKeys, holidayStrs, emitter, lockKey);

        } catch (Exception e) {
            try {
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error")
                        .data(Map.of("code", 500, "msg", "排班架构初始化出错: " + e.getMessage())));
            } catch (Exception se) {
            }
            redisTemplate.delete(lockKey);
            emitter.completeWithError(e);
        }

        return emitter;
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

    // ===== 获取导入模板 =====
    @GetMapping("/template")
    public void getScheduleTemplate(@RequestParam("year") int year, @RequestParam("month") int month,
            HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("排班导入模板");
            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("姓名");
            headerRow.createCell(1).setCellValue("工号");

            for (int day = 1; day <= daysInMonth; day++) {
                headerRow.createCell(day + 1).setCellValue(day + "号");
            }

            // 制作每天单元格的班别下拉限制
            List<ShiftType> allTypes = shiftTypeRepository.findAll();
            if (allTypes.isEmpty()) {
                initDefaultShiftTypes();
                allTypes = shiftTypeRepository.findAll();
            }
            String[] shiftNames = allTypes.stream().map(ShiftType::getName).toArray(String[]::new);
            if (shiftNames.length > 0) {
                org.apache.poi.ss.usermodel.DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                org.apache.poi.ss.usermodel.DataValidationConstraint constraint = validationHelper
                        .createExplicitListConstraint(shiftNames);
                // 给第2列至最后1列的前1000行附加下拉框
                org.apache.poi.ss.util.CellRangeAddressList addressList = new org.apache.poi.ss.util.CellRangeAddressList(
                        1, 1000, 2, daysInMonth + 1);
                org.apache.poi.ss.usermodel.DataValidation validation = validationHelper.createValidation(constraint,
                        addressList);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"schedule_template_" + year + "_" + month + ".xlsx\"");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ===== 导入排班 =====
    @PostMapping("/import")
    @Transactional
    public Map<String, Object> importSchedule(@RequestParam("file") MultipartFile file, @RequestParam("year") int year,
            @RequestParam("month") int month, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        String operator = "system";
        String token = request.getHeader("Authorization");
        if (token != null) {
            String empIdStr = jwtUtil.getEmployeeId(token.startsWith("Bearer ") ? token.substring(7) : token);
            if (empIdStr != null && !empIdStr.isEmpty()) {
                operator = empIdStr;
            }
        }

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // 获取当月天数
            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

            // 预加载所有的 ShiftType 并通过 name 建立映射
            List<ShiftType> allTypes = shiftTypeRepository.findAll();
            if (allTypes.isEmpty()) {
                initDefaultShiftTypes();
                allTypes = shiftTypeRepository.findAll();
            }
            Map<String, ShiftType> typeMap = allTypes.stream()
                    .collect(Collectors.toMap(t -> normalizeString(t.getName()), t -> t, (a, b) -> a));

            // 兜底班次
            ShiftType defaultRest = allTypes.stream().filter(t -> Boolean.TRUE.equals(t.getIsRest())).findFirst()
                    .orElse(null);

            // 预加载所有员工
            Map<String, User> userMap = userRepository.findAll().stream()
                    .collect(Collectors.toMap(User::getEmployeeId, u -> u, (a, b) -> a));

            List<Object[]> batchArgs = new ArrayList<>();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                Cell empIdCell = row.getCell(1);
                if (empIdCell == null)
                    continue;

                String empId = getCellValueAsString(empIdCell).trim();
                // 解决 POI 读取数字类型的异常 例如工号为 1001 被读成 1001.0
                if (empId.endsWith(".0")) {
                    empId = empId.substring(0, empId.length() - 2);
                }

                if (empId.isEmpty() || !userMap.containsKey(empId)) {
                    continue; // 跨过无效工号
                }

                // 读取 1~daysInMonth 号的排班
                for (int day = 1; day <= daysInMonth; day++) {
                    // C列(索引2)对应 1号, 依此类推 索引为 day + 1
                    Cell dayCell = row.getCell(day + 1);
                    String shiftName = getCellValueAsString(dayCell).trim();
                    if (shiftName.isEmpty())
                        continue; // 为空时不填覆盖该天的排班，视为保留原始原数据

                    String normalizedName = normalizeString(shiftName);
                    ShiftType matchType = typeMap.get(normalizedName);

                    if (matchType == null) {
                        matchType = defaultRest;
                    }

                    if (matchType != null) {
                        batchArgs.add(new Object[] {
                                empId, year, month, day, matchType.getId(),
                                "MANUAL", now, operator, now, operator
                        });
                    }
                }
            }

            String sql = "INSERT INTO shift_schedules (employee_id, year, month, day, shift_type_id, source, create_time, create_by, update_time, update_by) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (employee_id, year, month, day) " +
                    "DO UPDATE SET shift_type_id = EXCLUDED.shift_type_id, source = 'MANUAL', update_time = EXCLUDED.update_time, update_by = EXCLUDED.update_by";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Object[] args = batchArgs.get(i);
                    ps.setString(1, (String) args[0]);
                    ps.setInt(2, (Integer) args[1]);
                    ps.setInt(3, (Integer) args[2]);
                    ps.setInt(4, (Integer) args[3]);
                    ps.setLong(5, (Long) args[4]);
                    ps.setString(6, (String) args[5]);
                    ps.setTimestamp(7, (Timestamp) args[6]);
                    ps.setString(8, (String) args[7]);
                    ps.setTimestamp(9, (Timestamp) args[8]);
                    ps.setString(10, (String) args[9]);
                }

                @Override
                public int getBatchSize() {
                    return batchArgs.size();
                }
            });

            result.put("code", 200);
            result.put("msg", "成功导入 " + batchArgs.size() + " 条排班记录");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "解析 Excel 失败: " + e.getMessage());
        }
        return result;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String normalizeString(String s) {
        if (s == null)
            return "";
        return s.trim().toLowerCase();
    }
}
