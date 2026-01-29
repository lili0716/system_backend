package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "field_work_form")
public class FieldWorkForm extends Form {

    // 外勤日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date workDate;

    // 开始时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date startTime;

    // 结束时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date endTime;

    // 外勤地点
    private String location;

    // 外勤内容
    private String content;

    // 备注
    private String remark;

}
