package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.dto.AttendanceQueryDTO;
import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.AttendanceAbnormalRecord;
import com.artdesign.backend.entity.AttendanceFile;
import com.artdesign.backend.entity.AttendanceRule;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserRepository userRepository;

    // 考勤记录相关接口
    @GetMapping("/records")
    public Result<List<AttendanceRecord>> getAllRecords() {
        return Result.success(attendanceService.findAllRecords());
    }

    @GetMapping("/records/{id}")
    public Result<AttendanceRecord> getRecordById(@PathVariable Long id) {
        return Result.success(attendanceService.findRecordById(id));
    }

    @PostMapping("/records")
    public Result<AttendanceRecord> createRecord(@RequestBody AttendanceRecord record) {
        return Result.success(attendanceService.saveRecord(record));
    }

    @PutMapping("/records/{id}")
    public Result<AttendanceRecord> updateRecord(@PathVariable Long id, @RequestBody AttendanceRecord record) {
        record.setId(id);
        return Result.success(attendanceService.saveRecord(record));
    }

    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        attendanceService.deleteRecordById(id);
        return Result.success();
    }

    @PostMapping("/records/list")
    public Result<Map<String, Object>> getRecordList(@RequestBody Map<String, Object> params) {
        return Result.success(attendanceService.getRecordList(params));
    }

    @GetMapping("/records/user/{userId}")
    public Result<List<AttendanceRecord>> getRecordsByUserId(@PathVariable Long userId) {
        return Result.success(attendanceService.findRecordsByUserId(userId));
    }

    @PostMapping("/records/user/{userId}/date-range")
    public Result<List<AttendanceRecord>> getRecordsByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        Date startDate = (Date) params.get("startDate");
        Date endDate = (Date) params.get("endDate");
        return Result.success(attendanceService.findRecordsByUserIdAndDateRange(userId, startDate, endDate));
    }

    // 考勤查询接口
    @PostMapping("/records/query")
    public Result<Map<String, Object>> queryAttendanceRecords(@RequestBody AttendanceQueryDTO dto) {
        try {
            return Result.success(attendanceService.queryAttendanceRecords(dto));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorData = new java.util.HashMap<>();
            errorData.put("error", e.getMessage());
            errorData.put("type", e.getClass().getName());
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    // 考勤记录详情
    @GetMapping("/records/detail/{id}")
    public Result<Map<String, Object>> getAttendanceRecordDetail(@PathVariable Long id) {
        return Result.success(attendanceService.getAttendanceRecordDetail(id));
    }

    // 导出考勤记录Excel
    @PostMapping("/records/export")
    public ResponseEntity<byte[]> exportAttendanceRecords(@RequestBody AttendanceQueryDTO dto) {
        byte[] excelData = attendanceService.exportAttendanceRecords(dto);

        String fileName = "考勤记录_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // 员工搜索（下拉搜索框用）
    @GetMapping("/users/search")
    public Result<List<Map<String, Object>>> searchEmployees(@RequestParam String keyword) {
        List<User> users = userRepository.searchEmployees(keyword, PageRequest.of(0, 20));
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", u.getEmployeeId());
            map.put("label", u.getNickName() + " (" + u.getEmployeeId() + ")");
            map.put("employeeId", u.getEmployeeId());
            map.put("nickName", u.getNickName());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    // 考勤文件相关接口 - 返回解析结果
    @PostMapping("/files/upload")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("uploaderId") Long uploaderId) {
        return Result.success(attendanceService.uploadAndParseAttendanceFile(file, uploaderId));
    }

    @GetMapping("/files")
    public Result<List<AttendanceFile>> getAllFiles() {
        return Result.success(attendanceService.findAllFiles());
    }

    @GetMapping("/files/{id}")
    public Result<AttendanceFile> getFileById(@PathVariable Long id) {
        return Result.success(attendanceService.findFileById(id));
    }

    @PostMapping("/files/{id}/parse")
    public Result<Map<String, Object>> parseFile(@PathVariable Long id) {
        return Result.success(attendanceService.parseAttendanceFile(id));
    }

    // 导出失败记录Excel
    @PostMapping("/files/failed-export")
    public ResponseEntity<byte[]> exportFailedRecords(@RequestBody List<Map<String, Object>> failedRecords) {
        byte[] excelData = attendanceService.generateFailedRecordsExcel(failedRecords);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "failed_records.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // 异常考勤记录接口
    @GetMapping("/abnormal-records/user/{userId}")
    public Result<List<AttendanceAbnormalRecord>> getAbnormalRecordsByUserId(@PathVariable Long userId) {
        return Result.success(attendanceService.findAbnormalRecordsByUserId(userId));
    }

    @GetMapping("/abnormal-records/uncorrected/{userId}")
    public Result<List<AttendanceAbnormalRecord>> getUncorrectedAbnormalRecords(@PathVariable Long userId) {
        return Result.success(attendanceService.findUncorrectedAbnormalRecords(userId));
    }

    // 考勤规则相关接口
    @GetMapping("/rules")
    public Result<List<AttendanceRule>> getAllRules() {
        return Result.success(attendanceService.findAllRules());
    }

    @GetMapping("/rules/{id}")
    public Result<AttendanceRule> getRuleById(@PathVariable Long id) {
        return Result.success(attendanceService.findRuleById(id));
    }

    @PostMapping("/rules")
    public Result<AttendanceRule> createRule(@RequestBody AttendanceRule rule) {
        return Result.success(attendanceService.saveRule(rule));
    }

    @PutMapping("/rules/{id}")
    public Result<AttendanceRule> updateRule(@PathVariable Long id, @RequestBody AttendanceRule rule) {
        rule.setId(id);
        return Result.success(attendanceService.saveRule(rule));
    }

    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        attendanceService.deleteRuleById(id);
        return Result.success();
    }

    @GetMapping("/rules/current")
    public Result<AttendanceRule> getCurrentRule() {
        return Result.success(attendanceService.getCurrentRule());
    }

    @PostMapping("/rules/query")
    public Result<List<AttendanceRule>> queryRules(@RequestBody Map<String, Object> params) {
        String ruleName = params.get("ruleName") != null ? params.get("ruleName").toString() : null;
        Boolean singleWeekOff = params.get("singleWeekOff") != null
                ? Boolean.valueOf(params.get("singleWeekOff").toString())
                : null;
        return Result.success(attendanceService.findRulesByCondition(ruleName, singleWeekOff));
    }

    @GetMapping("/rules/department/{departmentId}")
    public Result<List<AttendanceRule>> getRulesByDepartment(@PathVariable Long departmentId) {
        return Result.success(attendanceService.findRulesByDepartmentId(departmentId));
    }

}
