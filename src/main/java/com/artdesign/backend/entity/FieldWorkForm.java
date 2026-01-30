package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

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

    public FieldWorkForm() {}

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
