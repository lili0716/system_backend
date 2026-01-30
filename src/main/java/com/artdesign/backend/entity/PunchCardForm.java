package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "punch_card_form")
public class PunchCardForm extends Form {

    // 补打卡日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date punchDate;

    // 补打卡时间
    @JsonFormat(pattern = "HH:mm:ss")
    private Date punchTime;

    // 补打卡类型：1-上班，2-下班
    private Integer punchType;

    // 补打卡原因
    private String reason;

    // 补打卡地点
    private String location;

    public PunchCardForm() {}

    public Date getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(Date punchDate) {
        this.punchDate = punchDate;
    }

    public Date getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(Date punchTime) {
        this.punchTime = punchTime;
    }

    public Integer getPunchType() {
        return punchType;
    }

    public void setPunchType(Integer punchType) {
        this.punchType = punchType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
