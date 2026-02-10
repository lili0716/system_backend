package com.artdesign.backend.service;

import com.artdesign.backend.dto.SalaryStatisticsDTO;

import java.util.List;

public interface SalaryStatisticsService {
    /**
     * Calculate monthly salary statistics for all users
     * 
     * @param month YYYY-MM
     * @return List of salary statistics
     */
    List<SalaryStatisticsDTO> calculateMonthlySalary(String month);
}
