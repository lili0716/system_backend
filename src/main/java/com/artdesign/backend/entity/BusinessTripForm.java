package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "business_trip_form")
public class BusinessTripForm extends Form {

    // 出差开始日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    // 出差结束日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    // 出差地点
    private String location;

    // 出差目的
    private String purpose;

    // 交通工具
    private String transport;

    // 预计费用
    private Double estimatedCost;

}
