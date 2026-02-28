package com.artdesign.backend.service.impl;

import com.artdesign.backend.dto.AttendanceQueryDTO;
import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.AttendanceAbnormalRecord;
import com.artdesign.backend.entity.AttendanceFile;
import com.artdesign.backend.entity.AttendanceRule;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.AttendanceRecordRepository;
import com.artdesign.backend.repository.AttendanceAbnormalRecordRepository;
import com.artdesign.backend.repository.AttendanceFileRepository;
import com.artdesign.backend.repository.AttendanceRuleRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.AttendanceService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private AttendanceAbnormalRecordRepository abnormalRecordRepository;

    @Autowired
    private AttendanceFileRepository attendanceFileRepository;

    @Autowired
    private AttendanceRuleRepository attendanceRuleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.artdesign.backend.repository.OvertimeFormRepository overtimeFormRepository;

    @Autowired
    private com.artdesign.backend.repository.LeaveFormRepository leaveFormRepository;

    @Autowired
    private com.artdesign.backend.repository.ShiftScheduleRepository shiftScheduleRepository;

    // 文件保存路径 (使用绝对路径避免 Tomcat 内置 temp 目录漂移)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/attendance/";

    @Override
    public List<AttendanceRecord> findAllRecords() {
        return attendanceRecordRepository.findAll();
    }

    @Override
    public AttendanceRecord findRecordById(Long id) {
        return attendanceRecordRepository.findById(id).orElse(null);
    }

    @Override
    public AttendanceRecord saveRecord(AttendanceRecord record) {
        AttendanceRule rule = getEffectiveRule(record.getUser());
        double stdHours = (rule != null && rule.getStandardWorkHours() != null) ? rule.getStandardWorkHours() : 8.0;

        // 计算基础实际工时（最多不能超过标准工时）
        if (record.getWorkInTime() != null && record.getWorkOutTime() != null) {
            double worked = calculateWorkHours(record.getWorkInTime(), record.getWorkOutTime());
            record.setActualWorkHours(Math.min(worked, stdHours));
        }

        // 计算初始考勤状态
        if (record.getWorkInTime() != null || record.getWorkOutTime() != null) {
            if (rule != null) {
                record.setStatus(calculateAttendanceStatus(record.getWorkInTime(), record.getWorkOutTime(), rule));
                calculateLateAndEarlyMinutes(record, rule);
            }
        }

        // 依次执行请假冲销与加班核算叠加
        if (rule != null) {
            applyLeaveLogic(record, rule);
        }
        applyOvertimeLogic(record);

        return attendanceRecordRepository.save(record);
    }

    @Override
    public void deleteRecordById(Long id) {
        attendanceRecordRepository.deleteById(id);
    }

    @Override
    public List<AttendanceRecord> findRecordsByUserId(Long userId) {
        return attendanceRecordRepository.findByUserId(userId);
    }

    @Override
    public List<AttendanceRecord> findRecordsByUserIdAndDateRange(Long userId, Date startDate, Date endDate) {
        return attendanceRecordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getRecordList(Map<String, Object> params) {
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;
        Date startDate = null;
        Date endDate = null;
        if (params.get("startDate") != null) {
            try {
                startDate = new SimpleDateFormat("yyyy-MM-dd").parse(params.get("startDate").toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (params.get("endDate") != null) {
            try {
                endDate = new SimpleDateFormat("yyyy-MM-dd").parse(params.get("endDate").toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Integer page = params.get("page") != null ? Integer.valueOf(params.get("page").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.valueOf(params.get("pageSize").toString()) : 10;

        List<AttendanceRecord> records = attendanceRecordRepository.findAttendanceRecordsWithParams(userId, status,
                startDate, endDate);

        // 计算分页
        int total = records.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<AttendanceRecord> pageRecords = records.subList(start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", pageRecords);
        return result;
    }

    @Override
    public Map<String, Object> uploadAndParseAttendanceFile(MultipartFile file, Long uploaderId) {
        Map<String, Object> result = new HashMap<>();
        AttendanceFile attendanceFile = new AttendanceFile();

        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            String filePath = UPLOAD_DIR + fileName;

            // 保存文件
            File dest = new File(filePath);
            file.transferTo(dest);

            // 设置文件信息
            attendanceFile.setFileName(fileName);
            attendanceFile.setOriginalFileName(originalFilename);
            attendanceFile.setFilePath(filePath);
            attendanceFile.setFileSize(file.getSize());
            attendanceFile.setFileType(file.getContentType());
            attendanceFile.setParseStatus(1); // 解析中

            // 设置上传人
            if (uploaderId != null) {
                User uploader = userRepository.findById(uploaderId).orElse(null);
                if (uploader != null) {
                    attendanceFile.setUploader(uploader);
                }
            }

            attendanceFile = attendanceFileRepository.save(attendanceFile);

            // 同步解析文件
            Map<String, Object> parseResult = parseAttendanceFile(attendanceFile.getId());
            result.putAll(parseResult);
            result.put("fileId", attendanceFile.getId());
            result.put("fileName", originalFilename);

        } catch (Exception e) {
            e.printStackTrace();
            attendanceFile.setParseStatus(3); // 解析失败
            attendanceFile.setParseResult("文件上传失败：" + e.getMessage());
            attendanceFileRepository.save(attendanceFile);
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public List<AttendanceFile> findAllFiles() {
        return attendanceFileRepository.findAll();
    }

    @Override
    public AttendanceFile findFileById(Long id) {
        return attendanceFileRepository.findById(id).orElse(null);
    }

    @Override
    public Map<String, Object> parseAttendanceFile(Long fileId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successRecords = new ArrayList<>();
        List<Map<String, Object>> failedRecords = new ArrayList<>();

        AttendanceFile file = attendanceFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            result.put("success", false);
            result.put("message", "文件不存在");
            return result;
        }

        try {
            file.setParseStatus(1); // 解析中
            attendanceFileRepository.save(file);

            // 移除全局考勤规则检查，改为针对每个用户查找生效规则
            // AttendanceRule rule = getCurrentRule();
            // ...

            // 读取Excel文件
            File excelFile = new File(file.getFilePath());
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            // 支持多种日期时间格式
            SimpleDateFormat[] dateTimeFormats = {
                    new SimpleDateFormat("yyyy/MM/dd HH:mm"), // 打卡设备格式
                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"), // 打卡设备格式（含秒）
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), // 标准格式
                    new SimpleDateFormat("yyyy-MM-dd HH:mm"), // 标准格式（无秒）
            };

            // 按员工+日期分组打卡记录
            // 格式: Map<employeeId, Map<date, List<punchTime>>>
            Map<String, Map<String, List<Date>>> punchRecords = new HashMap<>();

            // 读取表头，确定列索引
            Row headerRow = sheet.getRow(0);
            int employeeIdCol = -1; // 考勤号码/工号列
            int dateTimeCol = -1; // 时间日期列
            int dateCol = -1; // 日期列（旧格式）
            int timeCol = -1; // 打卡时间列（旧格式）

            if (headerRow != null) {
                for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                    String headerValue = getCellStringValue(headerRow.getCell(col));
                    if (headerValue != null) {
                        headerValue = headerValue.trim();
                        if ("考勤号码".equals(headerValue) || "工号".equals(headerValue) ||
                                "员工工号".equals(headerValue) || "员工号码".equals(headerValue)) {
                            employeeIdCol = col;
                        } else if ("时间日期".equals(headerValue) || "日期时间".equals(headerValue) ||
                                "打卡时间".equals(headerValue) || "签到时间".equals(headerValue)) {
                            dateTimeCol = col;
                        } else if ("日期".equals(headerValue)) {
                            dateCol = col;
                        } else if ("时间".equals(headerValue)) {
                            timeCol = col;
                        }
                    }
                }
            }

            // 如果没有找到列，使用默认位置（兼容旧格式）
            if (employeeIdCol == -1)
                employeeIdCol = 0;
            if (dateTimeCol == -1 && dateCol == -1) {
                dateCol = 1;
                timeCol = 2;
            }

            // 遍历行（跳过表头）
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null)
                    continue;

                try {
                    // 读取工号
                    String employeeId = getCellStringValue(row.getCell(employeeIdCol));
                    if (employeeId == null || employeeId.isEmpty())
                        continue;

                    Date punchTime = null;
                    String dateStr = null;

                    // 优先使用合并的时间日期列
                    if (dateTimeCol != -1) {
                        Cell dateTimeCell = row.getCell(dateTimeCol);
                        if (dateTimeCell != null) {
                            // 如果单元格是日期类型，直接获取
                            if (DateUtil.isCellDateFormatted(dateTimeCell)) {
                                punchTime = dateTimeCell.getDateCellValue();
                                dateStr = dateFormat.format(punchTime);
                            } else {
                                // 否则尝试解析字符串
                                String dateTimeStr = getCellStringValue(dateTimeCell);
                                if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
                                    for (SimpleDateFormat fmt : dateTimeFormats) {
                                        try {
                                            punchTime = fmt.parse(dateTimeStr);
                                            dateStr = dateFormat.format(punchTime);
                                            break;
                                        } catch (ParseException e) {
                                            // 尝试下一个格式
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // 使用分开的日期和时间列（旧格式）
                        dateStr = getCellStringValue(row.getCell(dateCol));
                        String timeStr = getCellStringValue(row.getCell(timeCol));

                        if (dateStr != null && timeStr != null) {
                            for (SimpleDateFormat fmt : dateTimeFormats) {
                                try {
                                    punchTime = fmt.parse(dateStr + " " + timeStr);
                                    break;
                                } catch (ParseException e) {
                                    // 尝试下一个格式
                                }
                            }
                        }
                    }

                    if (punchTime == null) {
                        failedRecords.add(createFailedRecord(rowNum, employeeId, dateStr, "打卡时间格式错误"));
                        continue;
                    }

                    // 添加到分组
                    punchRecords.computeIfAbsent(employeeId, k -> new HashMap<>())
                            .computeIfAbsent(dateStr, k -> new ArrayList<>())
                            .add(punchTime);

                } catch (Exception e) {
                    failedRecords.add(createFailedRecord(rowNum, "", "", "行数据解析失败: " + e.getMessage()));
                }
            }

            // 处理分组后的打卡记录
            for (Map.Entry<String, Map<String, List<Date>>> employeeEntry : punchRecords.entrySet()) {
                String employeeId = employeeEntry.getKey();
                User user = userRepository.findByEmployeeId(employeeId);

                if (user == null) {
                    for (String dateStr : employeeEntry.getValue().keySet()) {
                        failedRecords.add(createFailedRecord(0, employeeId, dateStr, "员工不存在"));
                    }
                    continue;
                }

                for (Map.Entry<String, List<Date>> dateEntry : employeeEntry.getValue().entrySet()) {
                    String dateStr = dateEntry.getKey();
                    List<Date> punchTimes = dateEntry.getValue();

                    try {
                        Date recordDate = dateFormat.parse(dateStr);

                        // 检查是否已存在该日期的记录
                        AttendanceRecord existingRecord = attendanceRecordRepository
                                .findByUserIdAndRecordDate(user.getId(), recordDate);
                        if (existingRecord != null) {
                            failedRecords.add(createFailedRecord(0, employeeId, dateStr, "该日期考勤记录已存在"));
                            continue;
                        }

                        // 获取用户生效的考勤规则
                        AttendanceRule rule = getEffectiveRule(user);
                        if (rule == null) {
                            failedRecords.add(createFailedRecord(0, employeeId, dateStr, "未找到适用的考勤规则"));
                            continue;
                        }

                        // 计算上下班打卡时间
                        Date workInTime = calculateWorkInTime(punchTimes, rule);
                        Date workOutTime = calculateWorkOutTime(punchTimes, rule);

                        // 创建考勤记录
                        AttendanceRecord record = new AttendanceRecord();
                        record.setUser(user);
                        record.setRecordDate(recordDate);
                        record.setWorkInTime(workInTime);
                        record.setWorkOutTime(workOutTime);
                        record.setAttendanceFile(file);

                        // 设置星期几 (1-7, 1=周一, 7=周日)
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(recordDate);
                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ... Saturday=7
                        // 转换成: Monday=1, ... Sunday=7
                        int normalizedDayOfWeek = (dayOfWeek == Calendar.SUNDAY) ? 7 : (dayOfWeek - 1);
                        record.setDayOfWeek(normalizedDayOfWeek);

                        // 计算工时和状态
                        double stdHours = (rule != null && rule.getStandardWorkHours() != null)
                                ? rule.getStandardWorkHours()
                                : 8.0;
                        if (workInTime != null && workOutTime != null) {
                            double worked = calculateWorkHours(workInTime, workOutTime);
                            record.setActualWorkHours(Math.min(worked, stdHours));
                        } else if (workInTime == null && workOutTime == null) {
                            record.setActualWorkHours(0.0);
                        } else {
                            // 有一个为空，估算工时
                            double partial = calculatePartialWorkHours(workInTime, workOutTime, rule);
                            record.setActualWorkHours(Math.min(partial, stdHours));
                        }

                        record.setStatus(calculateAttendanceStatus(workInTime, workOutTime, rule));

                        // 计算迟到/早退分钟数
                        calculateLateAndEarlyMinutes(record, rule);

                        // 应用请假与加班关联冲销计算
                        if (rule != null) {
                            applyLeaveLogic(record, rule);
                        }
                        applyOvertimeLogic(record);

                        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);

                        // 生成异常记录
                        List<AttendanceAbnormalRecord> abnormalRecords = generateAbnormalRecords(savedRecord, rule);
                        for (AttendanceAbnormalRecord abnormal : abnormalRecords) {
                            abnormalRecordRepository.save(abnormal);
                        }

                        Map<String, Object> successRecord = new HashMap<>();
                        successRecord.put("employeeId", employeeId);
                        successRecord.put("date", dateStr);
                        successRecord.put("status", record.getStatus());
                        successRecord.put("workHours", record.getActualWorkHours());
                        successRecords.add(successRecord);

                    } catch (Exception e) {
                        failedRecords.add(createFailedRecord(0, employeeId, dateStr, "处理失败: " + e.getMessage()));
                    }
                }
            }

            workbook.close();

            file.setParseStatus(2); // 解析成功
            file.setParseResult("解析完成，成功: " + successRecords.size() + " 条，失败: " + failedRecords.size() + " 条");
            attendanceFileRepository.save(file);

            result.put("success", true);
            result.put("successCount", successRecords.size());
            result.put("failedCount", failedRecords.size());
            result.put("successRecords", successRecords);
            result.put("failedRecords", failedRecords);

        } catch (Exception e) {
            e.printStackTrace();
            file.setParseStatus(3); // 解析失败
            file.setParseResult("解析失败：" + e.getMessage());
            attendanceFileRepository.save(file);

            result.put("success", false);
            result.put("message", "解析失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    /**
     * 计算上班打卡时间：取当天最早的打卡记录
     */
    private Date calculateWorkInTime(List<Date> punchTimes, AttendanceRule rule) {
        if (punchTimes == null || punchTimes.isEmpty())
            return null;
        return Collections.min(punchTimes);
    }

    /**
     * 计算下班打卡时间：取当天最晚的打卡记录（只有一次打卡时为空）
     */
    private Date calculateWorkOutTime(List<Date> punchTimes, AttendanceRule rule) {
        if (punchTimes == null || punchTimes.size() < 2)
            return null;
        Date max = Collections.max(punchTimes);
        Date min = Collections.min(punchTimes);
        if (max.equals(min))
            return null;
        return max;
    }

    /**
     * 计算部分工时（当只有上班或下班打卡时）
     */
    private Double calculatePartialWorkHours(Date workInTime, Date workOutTime, AttendanceRule rule) {
        double standardHours = rule.getStandardWorkHours() != null ? rule.getStandardWorkHours() : 8.0;

        if (workInTime == null && workOutTime == null) {
            return 0.0;
        }

        Calendar ruleCal = Calendar.getInstance();

        if (workInTime != null && workOutTime == null) {
            // 只有上班打卡，没有下班打卡
            ruleCal.setTime(rule.getWorkInTime());
            int ruleInMinutes = ruleCal.get(Calendar.HOUR_OF_DAY) * 60 + ruleCal.get(Calendar.MINUTE);

            Calendar punchCal = Calendar.getInstance();
            punchCal.setTime(workInTime);
            int punchMinutes = punchCal.get(Calendar.HOUR_OF_DAY) * 60 + punchCal.get(Calendar.MINUTE);

            int lateMinutes = Math.max(0, punchMinutes - ruleInMinutes);
            return Math.max(0, standardHours - lateMinutes / 60.0);
        }

        if (workInTime == null && workOutTime != null) {
            // 只有下班打卡，没有上班打卡
            ruleCal.setTime(rule.getWorkOutTime());
            int ruleOutMinutes = ruleCal.get(Calendar.HOUR_OF_DAY) * 60 + ruleCal.get(Calendar.MINUTE);

            Calendar punchCal = Calendar.getInstance();
            punchCal.setTime(workOutTime);
            int punchMinutes = punchCal.get(Calendar.HOUR_OF_DAY) * 60 + punchCal.get(Calendar.MINUTE);

            int earlyMinutes = Math.max(0, ruleOutMinutes - punchMinutes);
            return Math.max(0, standardHours - earlyMinutes / 60.0);
        }

        return standardHours;
    }

    /**
     * 计算迟到和早退分钟数
     */
    private void calculateLateAndEarlyMinutes(AttendanceRecord record, AttendanceRule rule) {
        Calendar ruleCal = Calendar.getInstance();

        // 计算迟到分钟数
        if (record.getWorkInTime() != null && rule.getWorkInTime() != null) {
            ruleCal.setTime(rule.getWorkInTime());
            int ruleInMinutes = ruleCal.get(Calendar.HOUR_OF_DAY) * 60 + ruleCal.get(Calendar.MINUTE);

            Calendar punchCal = Calendar.getInstance();
            punchCal.setTime(record.getWorkInTime());
            int punchMinutes = punchCal.get(Calendar.HOUR_OF_DAY) * 60 + punchCal.get(Calendar.MINUTE);

            int lateMinutes = punchMinutes - ruleInMinutes;
            record.setLateMinutes(Math.max(0, lateMinutes));
        } else if (record.getWorkInTime() == null) {
            record.setLateMinutes(null); // 无上班打卡记录
        }

        // 计算早退分钟数
        if (record.getWorkOutTime() != null && rule.getWorkOutTime() != null) {
            ruleCal.setTime(rule.getWorkOutTime());
            int ruleOutMinutes = ruleCal.get(Calendar.HOUR_OF_DAY) * 60 + ruleCal.get(Calendar.MINUTE);

            Calendar punchCal = Calendar.getInstance();
            punchCal.setTime(record.getWorkOutTime());
            int punchMinutes = punchCal.get(Calendar.HOUR_OF_DAY) * 60 + punchCal.get(Calendar.MINUTE);

            int earlyMinutes = ruleOutMinutes - punchMinutes;
            record.setEarlyLeaveMinutes(Math.max(0, earlyMinutes));
        } else if (record.getWorkOutTime() == null) {
            record.setEarlyLeaveMinutes(null); // 无下班打卡记录
        }
    }

    /**
     * 生成异常考勤记录
     */
    private List<AttendanceAbnormalRecord> generateAbnormalRecords(AttendanceRecord record, AttendanceRule rule) {
        List<AttendanceAbnormalRecord> abnormalRecords = new ArrayList<>();

        // 上班异常（迟到或无打卡）
        if (record.getWorkInTime() == null) {
            // 缺勤（无上班打卡）
            AttendanceAbnormalRecord abnormal = new AttendanceAbnormalRecord();
            abnormal.setUser(record.getUser());
            abnormal.setAttendanceRecord(record);
            abnormal.setRecordDate(record.getRecordDate());
            abnormal.setAbnormalType(3); // 缺勤
            abnormal.setExpectedTime(rule.getWorkInTime());
            abnormal.setIsCorrected(false);
            abnormalRecords.add(abnormal);
        } else if (record.getLateMinutes() != null && record.getLateMinutes() > 0) {
            // 迟到
            AttendanceAbnormalRecord abnormal = new AttendanceAbnormalRecord();
            abnormal.setUser(record.getUser());
            abnormal.setAttendanceRecord(record);
            abnormal.setRecordDate(record.getRecordDate());
            abnormal.setAbnormalType(1); // 上班异常
            abnormal.setOriginalTime(record.getWorkInTime());
            abnormal.setExpectedTime(rule.getWorkInTime());
            abnormal.setDiffMinutes(record.getLateMinutes());
            abnormal.setIsCorrected(false);
            abnormalRecords.add(abnormal);
        }

        // 下班异常（早退或无打卡）
        if (record.getWorkOutTime() == null && record.getWorkInTime() != null) {
            // 只有上班打卡，无下班打卡
            AttendanceAbnormalRecord abnormal = new AttendanceAbnormalRecord();
            abnormal.setUser(record.getUser());
            abnormal.setAttendanceRecord(record);
            abnormal.setRecordDate(record.getRecordDate());
            abnormal.setAbnormalType(2); // 下班异常
            abnormal.setExpectedTime(rule.getWorkOutTime());
            abnormal.setIsCorrected(false);
            abnormalRecords.add(abnormal);
        } else if (record.getEarlyLeaveMinutes() != null && record.getEarlyLeaveMinutes() > 0) {
            // 早退
            AttendanceAbnormalRecord abnormal = new AttendanceAbnormalRecord();
            abnormal.setUser(record.getUser());
            abnormal.setAttendanceRecord(record);
            abnormal.setRecordDate(record.getRecordDate());
            abnormal.setAbnormalType(2); // 下班异常
            abnormal.setOriginalTime(record.getWorkOutTime());
            abnormal.setExpectedTime(rule.getWorkOutTime());
            abnormal.setDiffMinutes(record.getEarlyLeaveMinutes());
            abnormal.setIsCorrected(false);
            abnormalRecords.add(abnormal);
        }

        return abnormalRecords;
    }

    /**
     * 创建失败记录
     */
    private Map<String, Object> createFailedRecord(int rowNum, String employeeId, String date, String reason) {
        Map<String, Object> failed = new HashMap<>();
        failed.put("rowNum", rowNum);
        failed.put("employeeId", employeeId);
        failed.put("date", date);
        failed.put("reason", reason);
        return failed;
    }

    @Override
    public byte[] generateFailedRecordsExcel(List<Map<String, Object>> failedRecords) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("导入失败记录");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("行号");
            headerRow.createCell(1).setCellValue("工号");
            headerRow.createCell(2).setCellValue("日期");
            headerRow.createCell(3).setCellValue("失败原因");

            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> failed : failedRecords) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(failed.get("rowNum") != null ? failed.get("rowNum").toString() : "");
                row.createCell(1)
                        .setCellValue(failed.get("employeeId") != null ? failed.get("employeeId").toString() : "");
                row.createCell(2).setCellValue(failed.get("date") != null ? failed.get("date").toString() : "");
                row.createCell(3).setCellValue(failed.get("reason") != null ? failed.get("reason").toString() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 异常考勤记录方法
    @Override
    public List<AttendanceAbnormalRecord> findAbnormalRecordsByUserId(Long userId) {
        return abnormalRecordRepository.findByUserId(userId);
    }

    @Override
    public List<AttendanceAbnormalRecord> findUncorrectedAbnormalRecords(Long userId) {
        return abnormalRecordRepository.findByUserIdAndIsCorrectedFalse(userId);
    }

    @Override
    public AttendanceAbnormalRecord saveAbnormalRecord(AttendanceAbnormalRecord record) {
        return abnormalRecordRepository.save(record);
    }

    @Override
    public List<AttendanceAbnormalRecord> findAbnormalRecordsByIds(List<Long> ids) {
        return abnormalRecordRepository.findByIdIn(ids);
    }

    @Override
    public void correctAbnormalRecords(List<Long> abnormalRecordIds, Long formId) {
        List<AttendanceAbnormalRecord> records = abnormalRecordRepository.findByIdIn(abnormalRecordIds);
        Set<Long> correctedRecordIds = new HashSet<>();
        Set<Date> correctedDates = new HashSet<>();

        for (AttendanceAbnormalRecord abnormal : records) {
            abnormal.setIsCorrected(true);
            abnormal.setCorrectedByFormId(formId);
            abnormalRecordRepository.save(abnormal);

            // 标记考勤记录已修正
            AttendanceRecord attendanceRecord = abnormal.getAttendanceRecord();
            if (attendanceRecord != null && !correctedRecordIds.contains(attendanceRecord.getId())) {
                attendanceRecord.setIsCorrected(true);
                Integer count = attendanceRecord.getCorrectionCount();
                attendanceRecord.setCorrectionCount((count != null ? count : 0) + 1);
                attendanceRecordRepository.save(attendanceRecord);
                correctedRecordIds.add(attendanceRecord.getId());

                // 记录日期用于统计补卡次数
                correctedDates.add(abnormal.getRecordDate());
            }

            // 更新用户补卡次数
            User user = abnormal.getUser();
            if (user != null) {
                Integer userCount = user.getPunchCorrectionCount();
                user.setPunchCorrectionCount((userCount != null ? userCount : 0) + 1);
                userRepository.save(user); // Save updated user punch count
            }
        }
    }

    /**
     * 应用加班逻辑
     */
    private void applyOvertimeLogic(AttendanceRecord record) {
        if (record.getUser() == null || record.getRecordDate() == null)
            return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(record.getRecordDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfDay = cal.getTime();

        List<com.artdesign.backend.entity.OvertimeForm> forms = overtimeFormRepository
                .findApprovedFormsByApplicantAndDateRange(record.getUser().getId(), startOfDay, endOfDay);

        if (forms.isEmpty()) {
            record.setOvertimeHours(0.0);
            record.setOvertimeException(false);
            return;
        }

        com.artdesign.backend.entity.OvertimeForm form = forms.get(0);

        if (record.getWorkInTime() != null && record.getWorkOutTime() != null) {
            Date workIn = normalizeTime(record.getWorkInTime(), record.getRecordDate());
            Date workOut = normalizeTime(record.getWorkOutTime(), record.getRecordDate());
            if (workIn.after(workOut)) {
                Calendar c = Calendar.getInstance();
                c.setTime(workOut);
                c.add(Calendar.DAY_OF_MONTH, 1);
                workOut = c.getTime();
            }

            double hours = calculateOvertimeIntersection(workIn, workOut, form.getStartTime(), form.getEndTime());
            record.setOvertimeHours(hours);

            // 核心变动：将工时加上加班工时且在备注中标注加班时间
            if (hours > 0) {
                record.setActualWorkHours(
                        (record.getActualWorkHours() != null ? record.getActualWorkHours() : 0.0) + hours);
                String oldRm = record.getRemark() != null ? record.getRemark() + " | " : "";
                record.setRemark(oldRm + "加班时长: " + String.format("%.1f", hours) + "h");
            }

            if (workOut.getTime() < form.getEndTime().getTime() - 60 * 1000) {
                record.setOvertimeException(true);
            } else {
                record.setOvertimeException(false);
            }
        } else {
            record.setOvertimeHours(0.0);
            record.setOvertimeException(true);
        }
    }

    /**
     * 应用请假逻辑
     */
    private void applyLeaveLogic(AttendanceRecord record, AttendanceRule rule) {
        if (record.getUser() == null || record.getRecordDate() == null)
            return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(record.getRecordDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfDay = cal.getTime();

        List<com.artdesign.backend.entity.LeaveForm> forms = leaveFormRepository
                .findApprovedLeavesByUserIdAndDateRange(record.getUser().getId(), 1, startOfDay, endOfDay);

        if (forms.isEmpty())
            return;

        com.artdesign.backend.entity.LeaveForm form = forms.get(0);
        double stdHours = rule.getStandardWorkHours() != null ? rule.getStandardWorkHours() : 8.0;

        // 简单计算请假时长 (默认如果涵盖当天则用天数占比推算小时)
        double leaveDuration = stdHours;
        if (form.getLeaveDays() != null && form.getLeaveDays() < 1.0) {
            leaveDuration = form.getLeaveDays() * stdHours;
        }
        if (leaveDuration > stdHours)
            leaveDuration = stdHours;

        // 核心变动：有请假，用8h减去请假时间
        record.setActualWorkHours(stdHours - leaveDuration);

        // 核心变动：综合请假表数据综合判断考勤状态 (设定状态值为4为请假标志位) 冲销原本的迟到早退分钟缺漏
        record.setStatus(4);
        record.setLateMinutes(0);
        record.setEarlyLeaveMinutes(0);

        String typeName = "";
        switch (form.getLeaveType() != null ? form.getLeaveType() : 1) {
            case 1:
                typeName = "事假";
                break;
            case 2:
                typeName = "病假";
                break;
            case 3:
                typeName = "产假";
                break;
            case 4:
                typeName = "婚假";
                break;
            case 5:
                typeName = "丧假";
                break;
            case 6:
                typeName = "年假";
                break;
            case 7:
                typeName = "调休";
                break;
            default:
                typeName = "请假";
                break;
        }

        String oldRm = record.getRemark() != null ? record.getRemark() + " | " : "";
        record.setRemark(oldRm + typeName + ": " + String.format("%.1f", leaveDuration) + "h");
    }

    private Date normalizeTime(Date time, Date date) {
        if (time == null || date == null)
            return time;
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        // If year is already > 2000, assume it's correct (e.g. from Excel)
        if (timeCal.get(Calendar.YEAR) > 2000)
            return time;

        timeCal.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
        timeCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
        timeCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));

        return timeCal.getTime();
    }

    private double calculateOvertimeIntersection(Date workStart, Date workEnd, Date formStart, Date formEnd) {
        if (workStart == null || workEnd == null || formStart == null || formEnd == null) {
            return 0.0;
        }

        long start = Math.max(workStart.getTime(), formStart.getTime());
        long end = Math.min(workEnd.getTime(), formEnd.getTime());

        if (end <= start) {
            return 0.0;
        }

        long diffMillis = end - start;
        return diffMillis / (1000.0 * 60.0 * 60.0); // 毫秒转小时
    }

    @Override
    public List<AttendanceRule> findAllRules() {
        return attendanceRuleRepository.findAll();
    }

    @Override
    public AttendanceRule findRuleById(Long id) {
        return attendanceRuleRepository.findById(id).orElse(null);
    }

    @Override
    public AttendanceRule saveRule(AttendanceRule rule) {
        // 如果设置为启用，将其他规则设置为禁用
        if (rule.getEnabled() != null && rule.getEnabled()) {
            List<AttendanceRule> rules = attendanceRuleRepository.findByEnabled(true);
            for (AttendanceRule r : rules) {
                if (!r.getId().equals(rule.getId())) {
                    r.setEnabled(false);
                    attendanceRuleRepository.save(r);
                }
            }
        }
        return attendanceRuleRepository.save(rule);
    }

    @Override
    public void deleteRuleById(Long id) {
        AttendanceRule rule = attendanceRuleRepository.findById(id).orElse(null);
        if (rule != null && Boolean.TRUE.equals(rule.getIsDefault())) {
            throw new RuntimeException("默认考勤规则不可删除");
        }
        attendanceRuleRepository.deleteById(id);
    }

    @Override
    public AttendanceRule getCurrentRule() {
        List<AttendanceRule> rules = attendanceRuleRepository.findByEnabled(true);
        return rules.isEmpty() ? null : rules.get(0);
    }

    @Override
    public Double calculateWorkHours(Date workInTime, Date workOutTime) {
        long diff = workOutTime.getTime() - workInTime.getTime();
        return diff / (1000.0 * 60 * 60);
    }

    @Override
    public Integer calculateAttendanceStatus(Date workInTime, Date workOutTime, AttendanceRule rule) {
        if (workInTime == null && workOutTime == null) {
            return 3; // 缺勤
        }

        boolean late = false;
        boolean earlyLeave = false;

        // 检查迟到
        if (workInTime != null && rule.getWorkInTime() != null) {
            Calendar punchCal = Calendar.getInstance();
            punchCal.setTime(workInTime);
            int punchMinutes = punchCal.get(Calendar.HOUR_OF_DAY) * 60 + punchCal.get(Calendar.MINUTE);

            Calendar ruleCal = Calendar.getInstance();
            ruleCal.setTime(rule.getWorkInTime());
            int ruleMinutes = ruleCal.get(Calendar.HOUR_OF_DAY) * 60 + ruleCal.get(Calendar.MINUTE);

            if (punchMinutes > ruleMinutes) {
                late = true;
            }
        } else if (workInTime == null) {
            late = true; // 无上班打卡视为迟到
        }

        // 检查早退
        if (workOutTime != null && rule.getWorkOutTime() != null) {
            Calendar punchCal = Calendar.getInstance();
            punchCal.setTime(workOutTime);
            int punchMinutes = punchCal.get(Calendar.HOUR_OF_DAY) * 60 + punchCal.get(Calendar.MINUTE);

            Calendar ruleCal = Calendar.getInstance();
            ruleCal.setTime(rule.getWorkOutTime());
            int ruleMinutes = ruleCal.get(Calendar.HOUR_OF_DAY) * 60 + ruleCal.get(Calendar.MINUTE);

            if (punchMinutes < ruleMinutes) {
                earlyLeave = true;
            }
        } else if (workOutTime == null && workInTime != null) {
            earlyLeave = true; // 无下班打卡视为早退
        }

        if (late && earlyLeave) {
            return 1; // 迟到（同时迟到和早退按迟到处理）
        } else if (late) {
            return 1; // 迟到
        } else if (earlyLeave) {
            return 2; // 早退
        } else {
            return 0; // 正常
        }
    }

    @Override
    public List<AttendanceRule> findRulesByCondition(String ruleName, Boolean singleWeekOff) {
        if (ruleName != null && singleWeekOff != null) {
            return attendanceRuleRepository.findByRuleNameContainingAndSingleWeekOff(ruleName, singleWeekOff);
        } else if (ruleName != null) {
            return attendanceRuleRepository.findByRuleNameContaining(ruleName);
        } else if (singleWeekOff != null) {
            return attendanceRuleRepository.findBySingleWeekOff(singleWeekOff);
        } else {
            return attendanceRuleRepository.findAll();
        }
    }

    @Override
    public List<AttendanceRule> findRulesByDepartmentId(Long departmentId) {
        return attendanceRuleRepository.findByDepartmentId(departmentId);
    }

    @Override
    public Map<String, Object> queryAttendanceRecords(AttendanceQueryDTO dto) {
        Map<String, Object> result = new HashMap<>();

        // 处理查询条件
        final List<String> employeeIds = dto.getEmployeeIds();
        final Long departmentId = dto.getDepartmentId();
        final Date startDate = dto.getStartDate();
        final Date endDate = dto.getEndDate();

        // 分页参数 (日期倒序)
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "recordDate"));

        // 构建查询条件
        Specification<AttendanceRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 部门过滤
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("user").get("department").get("id"), departmentId));
            }

            // 员工ID过滤
            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("user").get("employeeId").in(employeeIds));
            }

            // 日期范围过滤
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        org.springframework.data.domain.Page<AttendanceRecord> pageResult = attendanceRecordRepository.findAll(spec,
                pageable);

        // 转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        for (AttendanceRecord record : pageResult.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());

            // 用户信息
            if (record.getUser() != null) {
                item.put("employeeName", record.getUser().getNickName());
                item.put("employeeId", record.getUser().getEmployeeId());
            }

            // 日期
            item.put("recordDate", record.getRecordDate() != null ? dateFormat.format(record.getRecordDate()) : null);
            item.put("dayOfWeek", record.getDayOfWeek());

            // 考勤状态
            item.put("status", record.getStatus());
            item.put("statusText", getStatusText(record.getStatus()));

            // 备注：补打卡/请假类型/空
            String remark = buildRemark(record);
            item.put("remark", remark);

            // 其他信息
            item.put("workInTime",
                    record.getWorkInTime() != null ? timeFormat.format(record.getWorkInTime()) : null);
            item.put("workOutTime",
                    record.getWorkOutTime() != null ? timeFormat.format(record.getWorkOutTime()) : null);
            item.put("actualWorkHours", record.getActualWorkHours());
            item.put("isCorrected", record.getIsCorrected());

            records.add(item);
        }

        result.put("rows", records);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", pageResult.getTotalPages());

        return result;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null)
            return "未知";
        switch (status) {
            case 0:
                return "正常";
            case 1:
                return "迟到";
            case 2:
                return "早退";
            case 3:
                return "缺勤";
            case 4:
                return "加班";
            default:
                return "未知";
        }
    }

    /**
     * 构建备注：补打卡显示"补打卡"，请假显示假别，正常为空
     */
    private String buildRemark(AttendanceRecord record) {
        // 检查是否已补卡
        if (record.getIsCorrected() != null && record.getIsCorrected()) {
            return "补打卡";
        }

        // 检查是否有请假（通过查询该日期该用户的请假表单）
        if (record.getUser() != null && record.getRecordDate() != null) {
            // 这里简化处理，如果需要可以关联查询LeaveForm
            // 暂时返回空，后续可扩展
        }

        return "";
    }

    /**
     * 获取请假类型文本
     */
    private String getLeaveTypeText(Integer leaveType) {
        if (leaveType == null)
            return "";
        switch (leaveType) {
            case 1:
                return "事假";
            case 2:
                return "病假";
            case 3:
                return "产假";
            case 4:
                return "婚假";
            case 5:
                return "丧假";
            case 6:
                return "年假";
            case 7:
                return "调休";
            default:
                return "其他假";
        }
    }

    @Override
    public byte[] exportAttendanceRecords(AttendanceQueryDTO dto) {
        try {
            // 获取全部符合条件的记录（不分页）

            // 构建查询条件
            Specification<AttendanceRecord> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // 部门过滤
                if (dto.getDepartmentId() != null) {
                    predicates.add(cb.equal(root.get("user").get("department").get("id"), dto.getDepartmentId()));
                }

                // 员工ID过滤
                List<String> validEmployeeIds = dto.getEmployeeIds();
                if (validEmployeeIds != null && !validEmployeeIds.isEmpty()) {
                    predicates.add(root.get("user").get("employeeId").in(validEmployeeIds));
                }

                // 日期范围过滤
                if (dto.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), dto.getStartDate()));
                }
                if (dto.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), dto.getEndDate()));
                }

                // 排序: 日期倒序, 工号正序 (使用CriteriaBuilder排序)
                query.orderBy(cb.desc(root.get("recordDate")), cb.asc(root.get("user").get("employeeId")));

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            List<AttendanceRecord> records = attendanceRecordRepository.findAll(spec);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("考勤记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = { "员工姓名", "工号", "日期", "考勤状态", "上班时间", "下班时间", "实际工时", "备注" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            int rowNum = 1;

            for (AttendanceRecord record : records) {
                Row row = sheet.createRow(rowNum++);

                // 员工姓名
                row.createCell(0)
                        .setCellValue(record.getUser() != null ? record.getUser().getNickName() : "");
                // 工号
                row.createCell(1)
                        .setCellValue(record.getUser() != null ? record.getUser().getEmployeeId() : "");
                // 日期
                row.createCell(2).setCellValue(
                        record.getRecordDate() != null ? dateFormat.format(record.getRecordDate()) : "");
                // 考勤状态
                row.createCell(3).setCellValue(getStatusText(record.getStatus()));
                // 上班时间
                row.createCell(4).setCellValue(
                        record.getWorkInTime() != null ? timeFormat.format(record.getWorkInTime()) : "");
                // 下班时间
                row.createCell(5).setCellValue(
                        record.getWorkOutTime() != null ? timeFormat.format(record.getWorkOutTime()) : "");
                // 实际工时
                row.createCell(6)
                        .setCellValue(record.getActualWorkHours() != null ? record.getActualWorkHours() : 0);
                // 备注
                row.createCell(7).setCellValue(buildRemark(record));
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public Map<String, Object> getAttendanceRecordDetail(Long recordId) {
        Map<String, Object> result = new HashMap<>();

        AttendanceRecord record = attendanceRecordRepository.findById(recordId).orElse(null);
        if (record == null) {
            result.put("success", false);
            result.put("message", "考勤记录不存在");
            return result;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        result.put("success", true);
        result.put("id", record.getId());

        // 用户信息
        if (record.getUser() != null) {
            result.put("employeeName", record.getUser().getNickName());
            result.put("employeeId", record.getUser().getEmployeeId());
            if (record.getUser().getDepartment() != null) {
                result.put("departmentName", record.getUser().getDepartment().getName());
            }
        }

        // 日期
        result.put("recordDate", record.getRecordDate() != null ? dateFormat.format(record.getRecordDate()) : null);

        // 打卡时间
        result.put("workInTime", record.getWorkInTime() != null ? timeFormat.format(record.getWorkInTime()) : null);
        result.put("workOutTime", record.getWorkOutTime() != null ? timeFormat.format(record.getWorkOutTime()) : null);

        // 工时
        result.put("actualWorkHours", record.getActualWorkHours());

        // 考勤状态
        result.put("status", record.getStatus());
        result.put("statusText", getStatusText(record.getStatus()));

        // 迟到/早退分钟数
        result.put("lateMinutes", record.getLateMinutes());
        result.put("earlyLeaveMinutes", record.getEarlyLeaveMinutes());

        // 补卡信息
        result.put("isCorrected", record.getIsCorrected());
        result.put("correctionCount", record.getCorrectionCount());

        // 备注
        result.put("remark", record.getRemark());
        result.put("displayRemark", buildRemark(record));

        // 异常记录
        if (record.getUser() != null) {
            List<AttendanceAbnormalRecord> abnormals = abnormalRecordRepository.findByUserIdAndRecordDate(
                    record.getUser().getId(), record.getRecordDate());
            List<Map<String, Object>> abnormalList = new ArrayList<>();
            for (AttendanceAbnormalRecord abnormal : abnormals) {
                Map<String, Object> ab = new HashMap<>();
                ab.put("id", abnormal.getId());
                ab.put("abnormalType", abnormal.getAbnormalType());
                ab.put("abnormalTypeText", getAbnormalTypeText(abnormal.getAbnormalType()));
                ab.put("expectedTime",
                        abnormal.getExpectedTime() != null ? timeFormat.format(abnormal.getExpectedTime()) : null);
                ab.put("originalTime",
                        abnormal.getOriginalTime() != null ? timeFormat.format(abnormal.getOriginalTime()) : null);
                ab.put("diffMinutes", abnormal.getDiffMinutes());
                ab.put("isCorrected", abnormal.getIsCorrected());
                abnormalList.add(ab);
            }
            result.put("abnormalRecords", abnormalList);
        }

        return result;
    }

    /**
     * 获取异常类型文本
     */
    private String getAbnormalTypeText(Integer abnormalType) {
        if (abnormalType == null)
            return "未知";
        switch (abnormalType) {
            case 1:
                return "上班异常";
            case 2:
                return "下班异常";
            case 3:
                return "缺勤";
            default:
                return "未知";
        }
    }

    @Override
    public AttendanceRule getEffectiveRule(User user) {
        if (user == null) {
            return getCurrentRule();
        }

        // 1. 个人规则 (Personal Rule)
        if (user.getAttendanceRuleId() != null) {
            AttendanceRule rule = attendanceRuleRepository.findById(user.getAttendanceRuleId()).orElse(null);
            if (rule != null && Boolean.TRUE.equals(rule.getEnabled())) {
                return rule;
            }
        }

        // 2. 部门规则 (Department Rule - Hierarchical)
        if (user.getDepartment() != null) {
            com.artdesign.backend.entity.Department dept = user.getDepartment();
            while (dept != null) {
                List<AttendanceRule> rules = attendanceRuleRepository.findByDepartmentIdAndEnabledTrue(dept.getId());
                if (!rules.isEmpty()) {
                    return rules.get(0);
                }
                dept = dept.getParent();
            }
        }

        // 3. 默认考勤规则 (Default Rule)
        AttendanceRule defaultRule = attendanceRuleRepository.findByIsDefaultTrue();
        if (defaultRule != null && Boolean.TRUE.equals(defaultRule.getEnabled())) {
            return defaultRule;
        }

        // 4. 旧的全局默认规则 (Global Default fallback)
        List<AttendanceRule> globalRules = attendanceRuleRepository.findByDepartmentIdIsNullAndEnabledTrue();
        if (!globalRules.isEmpty()) {
            return globalRules.get(0);
        }

        // Fallback
        return getCurrentRule();
    }

}
