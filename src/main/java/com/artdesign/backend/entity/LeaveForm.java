package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
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

}
