package com.artdesign.backend.dto;

import com.artdesign.backend.entity.User;
import lombok.Data;

import java.math.BigDecimal;

public class SalaryStatisticsDTO {
    private Long userId;
    private String userName;
    private String departmentName;
    private String employeeId;

    // Month YYYY-MM
    private String month;

    // Attendance Stats
    private Double shouldAttendanceDays = 0.0;
    private Double actualAttendanceDays = 0.0;

    // Detailed Stats
    private Double sickLeaveHours = 0.0; // 病假时数
    private Double personalLeaveHours = 0.0; // 事假时数
    private Double weekdayOvertimeHours = 0.0; // 平时加班时数
    private Double weekendOvertimeHours = 0.0; // 周末加班时数
    private Integer mealCount = 0; // 餐补次数

    // Salary Calculation Items
    private BigDecimal basicSalary = BigDecimal.ZERO; // 基本工资
    private BigDecimal hourlyWage = BigDecimal.ZERO; // 小时薪资

    private BigDecimal sickLeaveDeduction = BigDecimal.ZERO; // 病假扣款
    private BigDecimal personalLeaveDeduction = BigDecimal.ZERO; // 事假扣款

    private BigDecimal weekdayOvertimeSubsidy = BigDecimal.ZERO; // 平时加班补贴
    private BigDecimal weekendOvertimeSubsidy = BigDecimal.ZERO; // 周末加班补贴
    private BigDecimal mealSubsidy = BigDecimal.ZERO; // 餐补金额

    // Final Result
    private BigDecimal grossSalary = BigDecimal.ZERO; // 应发工资

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Double getShouldAttendanceDays() {
        return shouldAttendanceDays;
    }

    public void setShouldAttendanceDays(Double shouldAttendanceDays) {
        this.shouldAttendanceDays = shouldAttendanceDays;
    }

    public Double getActualAttendanceDays() {
        return actualAttendanceDays;
    }

    public void setActualAttendanceDays(Double actualAttendanceDays) {
        this.actualAttendanceDays = actualAttendanceDays;
    }

    public Double getSickLeaveHours() {
        return sickLeaveHours;
    }

    public void setSickLeaveHours(Double sickLeaveHours) {
        this.sickLeaveHours = sickLeaveHours;
    }

    public Double getPersonalLeaveHours() {
        return personalLeaveHours;
    }

    public void setPersonalLeaveHours(Double personalLeaveHours) {
        this.personalLeaveHours = personalLeaveHours;
    }

    public Double getWeekdayOvertimeHours() {
        return weekdayOvertimeHours;
    }

    public void setWeekdayOvertimeHours(Double weekdayOvertimeHours) {
        this.weekdayOvertimeHours = weekdayOvertimeHours;
    }

    public Double getWeekendOvertimeHours() {
        return weekendOvertimeHours;
    }

    public void setWeekendOvertimeHours(Double weekendOvertimeHours) {
        this.weekendOvertimeHours = weekendOvertimeHours;
    }

    public Integer getMealCount() {
        return mealCount;
    }

    public void setMealCount(Integer mealCount) {
        this.mealCount = mealCount;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getHourlyWage() {
        return hourlyWage;
    }

    public void setHourlyWage(BigDecimal hourlyWage) {
        this.hourlyWage = hourlyWage;
    }

    public BigDecimal getSickLeaveDeduction() {
        return sickLeaveDeduction;
    }

    public void setSickLeaveDeduction(BigDecimal sickLeaveDeduction) {
        this.sickLeaveDeduction = sickLeaveDeduction;
    }

    public BigDecimal getPersonalLeaveDeduction() {
        return personalLeaveDeduction;
    }

    public void setPersonalLeaveDeduction(BigDecimal personalLeaveDeduction) {
        this.personalLeaveDeduction = personalLeaveDeduction;
    }

    public BigDecimal getWeekdayOvertimeSubsidy() {
        return weekdayOvertimeSubsidy;
    }

    public void setWeekdayOvertimeSubsidy(BigDecimal weekdayOvertimeSubsidy) {
        this.weekdayOvertimeSubsidy = weekdayOvertimeSubsidy;
    }

    public BigDecimal getWeekendOvertimeSubsidy() {
        return weekendOvertimeSubsidy;
    }

    public void setWeekendOvertimeSubsidy(BigDecimal weekendOvertimeSubsidy) {
        this.weekendOvertimeSubsidy = weekendOvertimeSubsidy;
    }

    public BigDecimal getMealSubsidy() {
        return mealSubsidy;
    }

    public void setMealSubsidy(BigDecimal mealSubsidy) {
        this.mealSubsidy = mealSubsidy;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }
}
