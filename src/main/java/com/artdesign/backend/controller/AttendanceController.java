package com.artdesign.backend.controller;

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
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // 考勤记录相关接口
    @GetMapping("/records")
    public List<AttendanceRecord> getAllRecords() {
        return attendanceService.findAllRecords();
    }

    @GetMapping("/records/{id}")
    public AttendanceRecord getRecordById(@PathVariable Long id) {
        return attendanceService.findRecordById(id);
    }

    @PostMapping("/records")
    public AttendanceRecord createRecord(@RequestBody AttendanceRecord record) {
        return attendanceService.saveRecord(record);
    }

    @PutMapping("/records/{id}")
    public AttendanceRecord updateRecord(@PathVariable Long id, @RequestBody AttendanceRecord record) {
        record.setId(id);
        return attendanceService.saveRecord(record);
    }

    @DeleteMapping("/records/{id}")
    public void deleteRecord(@PathVariable Long id) {
        attendanceService.deleteRecordById(id);
    }

    @PostMapping("/records/list")
    public Map<String, Object> getRecordList(@RequestBody Map<String, Object> params) {
        return attendanceService.getRecordList(params);
    }

    @GetMapping("/records/user/{userId}")
    public List<AttendanceRecord> getRecordsByUserId(@PathVariable Long userId) {
        return attendanceService.findRecordsByUserId(userId);
    }

    @PostMapping("/records/user/{userId}/date-range")
    public List<AttendanceRecord> getRecordsByUserIdAndDateRange(
            @PathVariable Long userId, 
            @RequestBody Map<String, Object> params) {
        Date startDate = (Date) params.get("startDate");
        Date endDate = (Date) params.get("endDate");
        return attendanceService.findRecordsByUserIdAndDateRange(userId, startDate, endDate);
    }

    // 考勤文件相关接口
    @PostMapping("/files/upload")
    public AttendanceFile uploadFile(@RequestParam("file") MultipartFile file, 
                                     @RequestParam("uploaderId") Long uploaderId) {
        return attendanceService.uploadFile(file, uploaderId);
    }

    @GetMapping("/files")
    public List<AttendanceFile> getAllFiles() {
        return attendanceService.findAllFiles();
    }

    @GetMapping("/files/{id}")
    public AttendanceFile getFileById(@PathVariable Long id) {
        return attendanceService.findFileById(id);
    }

    @PostMapping("/files/{id}/parse")
    public void parseFile(@PathVariable Long id) {
        attendanceService.parseAttendanceFile(id);
    }

    // 考勤规则相关接口
    @GetMapping("/rules")
    public List<AttendanceRule> getAllRules() {
        return attendanceService.findAllRules();
    }

    @GetMapping("/rules/{id}")
    public AttendanceRule getRuleById(@PathVariable Long id) {
        return attendanceService.findRuleById(id);
    }

    @PostMapping("/rules")
    public AttendanceRule createRule(@RequestBody AttendanceRule rule) {
        return attendanceService.saveRule(rule);
    }

    @PutMapping("/rules/{id}")
    public AttendanceRule updateRule(@PathVariable Long id, @RequestBody AttendanceRule rule) {
        rule.setId(id);
        return attendanceService.saveRule(rule);
    }

    @DeleteMapping("/rules/{id}")
    public void deleteRule(@PathVariable Long id) {
        attendanceService.deleteRuleById(id);
    }

    @GetMapping("/rules/current")
    public AttendanceRule getCurrentRule() {
        return attendanceService.getCurrentRule();
    }

}
