package com.artdesign.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;

/**
 * 考勤查询DTO
 */
public class AttendanceQueryDTO {

    // 工号列表（最多3个）
    private List<String> employeeIds;

    // 部门ID
    private Long departmentId;

    // 开始日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    // 结束日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    // 当前页码
    private Integer page = 1;

    // 每页数量
    private Integer pageSize = 10;

    public AttendanceQueryDTO() {
    }

    public List<String> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(List<String> employeeIds) {
        // 限制最多3个工号
        if (employeeIds != null && employeeIds.size() > 3) {
            this.employeeIds = employeeIds.subList(0, 3);
        } else {
            this.employeeIds = employeeIds;
        }
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page != null && page > 0 ? page : 1;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;
    }
}
