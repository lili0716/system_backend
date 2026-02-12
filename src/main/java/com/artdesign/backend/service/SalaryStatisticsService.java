package com.artdesign.backend.service;

import com.artdesign.backend.dto.SalaryStatisticsDTO;

import java.util.List;

public interface SalaryStatisticsService {
    /**
     * Calculate monthly salary statistics
     * 
     * @param month YYYY-MM
     * @param employeeId Employee ID (optional)
     * @param departmentId Department ID (optional)
     * @return List of salary statistics
     */
    List<SalaryStatisticsDTO> calculateMonthlySalary(String month, String employeeId, Long departmentId);
}
