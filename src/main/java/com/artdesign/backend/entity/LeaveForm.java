package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "leave_form")
public class LeaveForm extends Form {

    // 请假类型：1-事假，2-病假，3-产假，4-婚假，5-丧假，6-年假，7-调休
    private Integer leaveType;

    // 开始日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    // 结束日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    // 开始时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date startTime;

    // 结束时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date endTime;

    // 请假天数
    private Double leaveDays;

    // 请假原因
    private String reason;

    // 备注
    private String remark;

    public LeaveForm() {}

    public Integer getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(Integer leaveType) {
        this.leaveType = leaveType;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Double getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(Double leaveDays) {
        this.leaveDays = leaveDays;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
