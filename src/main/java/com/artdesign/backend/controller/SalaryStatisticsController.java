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
    public Result<List<SalaryStatisticsDTO>> getMonthlyStatistics(
            @RequestParam String month,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Long departmentId) {
        return Result.success(salaryStatisticsService.calculateMonthlySalary(month, employeeId, departmentId));
    }
}
