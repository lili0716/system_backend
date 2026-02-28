package com.artdesign.backend.service;

import java.util.Map;

/**
 * 考勤报表服务接口
 */
public interface AttendanceReportService {

    /**
     * 获取考勤日报
     * 
     * @param date        考勤日期(yyyy-MM-dd)
     * @param deptId      部门ID
     * @param employeeIds 员工工号列表（逗号分隔）
     * @param page        页码
     * @param pageSize    每页条数
     * @return 分页和统计结果
     */
    Map<String, Object> getDailyReport(String date, Long deptId, String employeeIds, Integer page, Integer pageSize);

    /**
     * 导出考勤日报
     * 
     * @param date        考勤日期
     * @param deptId      部门ID
     * @param employeeIds 员工列表
     * @return 字节流
     */
    byte[] exportDailyReport(String date, Long deptId, String employeeIds);

    /**
     * 获取考勤月报
     * 
     * @param year        年份
     * @param month       月份
     * @param deptId      部门ID
     * @param employeeIds 员工列表
     * @param page        页码
     * @param pageSize    每页数量
     * @return 分页和复杂月度横向统计结果
     */
    Map<String, Object> getMonthlyReport(int year, int month, Long deptId, String employeeIds, Integer page,
            Integer pageSize);

    /**
     * 导出考勤月报
     * 
     * @param year        年份
     * @param month       月份
     * @param deptId      部门ID
     * @param employeeIds 员工列表
     * @return 注入颜色的 POI 字节流
     */
    byte[] exportMonthlyReport(int year, int month, Long deptId, String employeeIds);
}
