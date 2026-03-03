package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.dto.SalaryStatisticsDTO;
import com.artdesign.backend.service.SalaryStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/salary/statistics")
public class SalaryStatisticsController {

    @Autowired
    private SalaryStatisticsService salaryStatisticsService;

    @GetMapping
    public Result<org.springframework.data.domain.Page<SalaryStatisticsDTO>> getMonthlyStatistics(
            @RequestParam String month,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(
                salaryStatisticsService.calculateMonthlySalaryWithPage(month, employeeId, departmentId, page, size));
    }
}
