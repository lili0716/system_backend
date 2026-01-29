package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
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

}
