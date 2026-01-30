package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance_rule")
@EntityListeners(AuditingEntityListener.class)
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 规则名称
    private String ruleName;

    // 规则描述
    private String description;

    // 上班规定时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date workInTime;

    // 下班规定时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date workOutTime;

    // 弹性上班打卡开始时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date flexibleWorkInStart;

    // 弹性上班打卡结束时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date flexibleWorkInEnd;

    // 弹性下班打卡开始时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date flexibleWorkOutStart;

    // 弹性下班打卡结束时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date flexibleWorkOutEnd;

    // 每日标准工时
    private Double standardWorkHours;

    // 迟到阈值（分钟）
    private Integer lateThreshold;

    // 早退阈值（分钟）
    private Integer earlyLeaveThreshold;

    // 是否启用
    private Boolean enabled;

    // 单双休设置：true为单休，false为双休
    private Boolean singleWeekOff;

    // 部门ID（可为空，为空表示全局规则）
    private Long departmentId;

    // 弹性打卡时间范围（分钟）
    private Integer flexibleTimeRange;

    // 创建时间
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 更新时间
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public AttendanceRule() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getWorkInTime() {
        return workInTime;
    }

    public void setWorkInTime(Date workInTime) {
        this.workInTime = workInTime;
    }

    public Date getWorkOutTime() {
        return workOutTime;
    }

    public void setWorkOutTime(Date workOutTime) {
        this.workOutTime = workOutTime;
    }

    public Date getFlexibleWorkInStart() {
        return flexibleWorkInStart;
    }

    public void setFlexibleWorkInStart(Date flexibleWorkInStart) {
        this.flexibleWorkInStart = flexibleWorkInStart;
    }

    public Date getFlexibleWorkInEnd() {
        return flexibleWorkInEnd;
    }

    public void setFlexibleWorkInEnd(Date flexibleWorkInEnd) {
        this.flexibleWorkInEnd = flexibleWorkInEnd;
    }

    public Date getFlexibleWorkOutStart() {
        return flexibleWorkOutStart;
    }

    public void setFlexibleWorkOutStart(Date flexibleWorkOutStart) {
        this.flexibleWorkOutStart = flexibleWorkOutStart;
    }

    public Date getFlexibleWorkOutEnd() {
        return flexibleWorkOutEnd;
    }

    public void setFlexibleWorkOutEnd(Date flexibleWorkOutEnd) {
        this.flexibleWorkOutEnd = flexibleWorkOutEnd;
    }

    public Double getStandardWorkHours() {
        return standardWorkHours;
    }

    public void setStandardWorkHours(Double standardWorkHours) {
        this.standardWorkHours = standardWorkHours;
    }

    public Integer getLateThreshold() {
        return lateThreshold;
    }

    public void setLateThreshold(Integer lateThreshold) {
        this.lateThreshold = lateThreshold;
    }

    public Integer getEarlyLeaveThreshold() {
        return earlyLeaveThreshold;
    }

    public void setEarlyLeaveThreshold(Integer earlyLeaveThreshold) {
        this.earlyLeaveThreshold = earlyLeaveThreshold;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getSingleWeekOff() {
        return singleWeekOff;
    }

    public void setSingleWeekOff(Boolean singleWeekOff) {
        this.singleWeekOff = singleWeekOff;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getFlexibleTimeRange() {
        return flexibleTimeRange;
    }

    public void setFlexibleTimeRange(Integer flexibleTimeRange) {
        this.flexibleTimeRange = flexibleTimeRange;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
