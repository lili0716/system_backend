package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.service.AttendanceReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 考勤报表控制器 (日报 / 月报)
 */
@RestController
@RequestMapping("/attendance/report")
public class AttendanceReportController {

    @Autowired
    private AttendanceReportService attendanceReportService;

    /**
     * 获取考勤日报
     */
    @GetMapping("/daily")
    public Result<Map<String, Object>> getDailyReport(
            @RequestParam String date,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String employeeIds,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return Result.success(attendanceReportService.getDailyReport(date, deptId, employeeIds, page, pageSize));
    }

    /**
     * 导出考勤日报
     */
    @GetMapping("/daily/export")
    public ResponseEntity<byte[]> exportDailyReport(
            @RequestParam String date,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String employeeIds) {

        byte[] excelData = attendanceReportService.exportDailyReport(date, deptId, employeeIds);

        String fileName = "考勤日报_" + date + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    /**
     * 获取考勤月报
     */
    @GetMapping("/monthly")
    public Result<Map<String, Object>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String employeeIds,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return Result
                .success(attendanceReportService.getMonthlyReport(year, month, deptId, employeeIds, page, pageSize));
    }

    /**
     * 导出考勤月报
     */
    @GetMapping("/monthly/export")
    public ResponseEntity<byte[]> exportMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String employeeIds) {

        byte[] excelData = attendanceReportService.exportMonthlyReport(year, month, deptId, employeeIds);

        String fileName = "考勤月报_" + year + "-" + String.format("%02d", month) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
