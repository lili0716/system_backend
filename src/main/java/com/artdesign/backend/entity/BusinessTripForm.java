package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

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

    public BusinessTripForm() {}

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
}
