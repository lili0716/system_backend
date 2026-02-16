package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

@Data
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

    // 创建时间
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 更新时间
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
