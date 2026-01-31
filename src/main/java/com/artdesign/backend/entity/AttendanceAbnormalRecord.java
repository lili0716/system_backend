package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

/**
 * 异常考勤记录实体
 * 记录员工的异常打卡信息，包括上班异常、下班异常、缺勤等
 */
@Entity
@Table(name = "attendance_abnormal_record")
@EntityListeners(AuditingEntityListener.class)
public class AttendanceAbnormalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 关联的考勤记录
    @ManyToOne
    @JoinColumn(name = "attendance_record_id")
    private AttendanceRecord attendanceRecord;

    // 员工
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 异常日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recordDate;

    // 异常类型：1-上班异常(迟到), 2-下班异常(早退), 3-缺勤(无打卡记录)
    private Integer abnormalType;

    // 原始打卡时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date originalTime;

    // 应打卡时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date expectedTime;

    // 差异分钟数
    private Integer diffMinutes;

    // 是否已修正
    private Boolean isCorrected = false;

    // 修正的补卡表单ID
    private Long correctedByFormId;

    // 创建时间
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public AttendanceAbnormalRecord() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttendanceRecord getAttendanceRecord() {
        return attendanceRecord;
    }

    public void setAttendanceRecord(AttendanceRecord attendanceRecord) {
        this.attendanceRecord = attendanceRecord;
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

    public Integer getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(Integer abnormalType) {
        this.abnormalType = abnormalType;
    }

    public Date getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(Date originalTime) {
        this.originalTime = originalTime;
    }

    public Date getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(Date expectedTime) {
        this.expectedTime = expectedTime;
    }

    public Integer getDiffMinutes() {
        return diffMinutes;
    }

    public void setDiffMinutes(Integer diffMinutes) {
        this.diffMinutes = diffMinutes;
    }

    public Boolean getIsCorrected() {
        return isCorrected;
    }

    public void setIsCorrected(Boolean isCorrected) {
        this.isCorrected = isCorrected;
    }

    public Long getCorrectedByFormId() {
        return correctedByFormId;
    }

    public void setCorrectedByFormId(Long correctedByFormId) {
        this.correctedByFormId = correctedByFormId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
