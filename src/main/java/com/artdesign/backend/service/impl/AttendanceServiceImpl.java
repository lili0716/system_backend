package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.AttendanceFile;
import com.artdesign.backend.entity.AttendanceRule;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.AttendanceRecordRepository;
import com.artdesign.backend.repository.AttendanceFileRepository;
import com.artdesign.backend.repository.AttendanceRuleRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.AttendanceService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private AttendanceFileRepository attendanceFileRepository;

    @Autowired
    private AttendanceRuleRepository attendanceRuleRepository;

    @Autowired
    private UserRepository userRepository;

    // 文件保存路径
    private static final String UPLOAD_DIR = "uploads/attendance/";

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
        // 计算实际工时
        if (record.getWorkInTime() != null && record.getWorkOutTime() != null) {
            record.setActualWorkHours(calculateWorkHours(record.getWorkInTime(), record.getWorkOutTime()));
        }
        // 计算考勤状态
        if (record.getWorkInTime() != null || record.getWorkOutTime() != null) {
            AttendanceRule rule = getCurrentRule();
            if (rule != null) {
                record.setStatus(calculateAttendanceStatus(record.getWorkInTime(), record.getWorkOutTime(), rule));
            }
        }
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

        List<AttendanceRecord> records = attendanceRecordRepository.findAttendanceRecordsWithParams(userId, status, startDate, endDate);

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
    public AttendanceFile uploadFile(MultipartFile file, Long uploaderId) {
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
            attendanceFile.setParseStatus(0); // 未解析

            // 设置上传人
            if (uploaderId != null) {
                User uploader = userRepository.findById(uploaderId).orElse(null);
                if (uploader != null) {
                    attendanceFile.setUploader(uploader);
                }
            }

            attendanceFile = attendanceFileRepository.save(attendanceFile);
            
            // 保存文件ID到final变量，用于lambda表达式
            final Long fileId = attendanceFile.getId();

            // 异步解析文件
            new Thread(() -> {
                try {
                    parseAttendanceFile(fileId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            attendanceFile.setParseStatus(3); // 解析失败
            attendanceFile.setParseResult("文件上传失败：" + e.getMessage());
            attendanceFileRepository.save(attendanceFile);
        }
        return attendanceFile;
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
    public void parseAttendanceFile(Long fileId) {
        AttendanceFile file = attendanceFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return;
        }

        try {
            file.setParseStatus(1); // 解析中
            attendanceFileRepository.save(file);

            // 读取Excel文件
            File excelFile = new File(file.getFilePath());
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            // 解析Excel数据
            List<AttendanceRecord> records = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            // 遍历行（跳过表头）
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                try {
                    // 假设Excel格式：用户名、日期、上班时间、下班时间
                    String username = row.getCell(0).getStringCellValue();
                    Date recordDate = dateFormat.parse(row.getCell(1).getStringCellValue());
                    Date workInTime = timeFormat.parse(row.getCell(2).getStringCellValue());
                    Date workOutTime = timeFormat.parse(row.getCell(3).getStringCellValue());

                    // 查找用户
                    User user = userRepository.findByEmployeeId(username);
                    if (user != null) {
                        // 检查是否已存在该日期的记录
                        AttendanceRecord existingRecord = attendanceRecordRepository.findByUserIdAndRecordDate(user.getId(), recordDate);
                        if (existingRecord == null) {
                            AttendanceRecord record = new AttendanceRecord();
                            record.setUser(user);
                            record.setRecordDate(recordDate);
                            record.setWorkInTime(workInTime);
                            record.setWorkOutTime(workOutTime);
                            record.setAttendanceFile(file);
                            records.add(record);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }

            // 保存考勤记录
            for (AttendanceRecord record : records) {
                saveRecord(record);
            }

            file.setParseStatus(2); // 解析成功
            file.setParseResult("解析成功，生成了 " + records.size() + " 条考勤记录");
            attendanceFileRepository.save(file);

            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            file.setParseStatus(3); // 解析失败
            file.setParseResult("解析失败：" + e.getMessage());
            attendanceFileRepository.save(file);
        }
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
            if (workInTime.after(rule.getWorkInTime())) {
                late = true;
            }
        }

        // 检查早退
        if (workOutTime != null && rule.getWorkOutTime() != null) {
            if (workOutTime.before(rule.getWorkOutTime())) {
                earlyLeave = true;
            }
        }

        if (late && earlyLeave) {
            return 1; // 迟到
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

}
