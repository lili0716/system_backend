package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance_record")
@EntityListeners(AuditingEntityListener.class)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 员工
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 打卡日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recordDate;

    // 上班打卡时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date workInTime;

    // 下班打卡时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date workOutTime;

    // 实际工时
    private Double actualWorkHours;

    // 考勤状态：0-正常，1-迟到，2-早退，3-缺勤，4-加班
    private Integer status;

    // 迟到分钟数
    private Integer lateMinutes;

    // 早退分钟数
    private Integer earlyLeaveMinutes;

    // 星期几（1-7，1代表周一，7代表周日）
    private Integer dayOfWeek;

    // 备注
    private String remark;

    // 关联的考勤文件
    @ManyToOne
    @JoinColumn(name = "file_id")
    private AttendanceFile attendanceFile;

    // 创建时间
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 是否已补卡修正
    private Boolean isCorrected = false;

    // 补卡次数
    private Integer correctionCount = 0;

    // 加班工时
    private Double overtimeHours = 0.0;

    // 是否加班异常（早退）
    private Boolean overtimeException = false;

    public AttendanceRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
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

    public Double getActualWorkHours() {
        return actualWorkHours;
    }

    public void setActualWorkHours(Double actualWorkHours) {
        this.actualWorkHours = actualWorkHours;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public Integer getEarlyLeaveMinutes() {
        return earlyLeaveMinutes;
    }

    public void setEarlyLeaveMinutes(Integer earlyLeaveMinutes) {
        this.earlyLeaveMinutes = earlyLeaveMinutes;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public AttendanceFile getAttendanceFile() {
        return attendanceFile;
    }

    public void setAttendanceFile(AttendanceFile attendanceFile) {
        this.attendanceFile = attendanceFile;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsCorrected() {
        return isCorrected;
    }

    public void setIsCorrected(Boolean isCorrected) {
        this.isCorrected = isCorrected;
    }

    public Integer getCorrectionCount() {
        return correctionCount;
    }

    public void setCorrectionCount(Integer correctionCount) {
        this.correctionCount = correctionCount;
    }

    public Double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(Double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public Boolean getOvertimeException() {
        return overtimeException;
    }

    public void setOvertimeException(Boolean overtimeException) {
        this.overtimeException = overtimeException;
    }
}
