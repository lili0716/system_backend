package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.AttendanceFile;
import com.artdesign.backend.entity.AttendanceRule;
import com.artdesign.backend.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

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

    // 考勤文件相关接口
    @PostMapping("/files/upload")
    public Result<AttendanceFile> uploadFile(@RequestParam("file") MultipartFile file, 
                                     @RequestParam("uploaderId") Long uploaderId) {
        return Result.success(attendanceService.uploadFile(file, uploaderId));
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
    public Result<Void> parseFile(@PathVariable Long id) {
        attendanceService.parseAttendanceFile(id);
        return Result.success();
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
        Boolean singleWeekOff = params.get("singleWeekOff") != null ? Boolean.valueOf(params.get("singleWeekOff").toString()) : null;
        return Result.success(attendanceService.findRulesByCondition(ruleName, singleWeekOff));
    }

    @GetMapping("/rules/department/{departmentId}")
    public Result<List<AttendanceRule>> getRulesByDepartment(@PathVariable Long departmentId) {
        return Result.success(attendanceService.findRulesByDepartmentId(departmentId));
    }

}
