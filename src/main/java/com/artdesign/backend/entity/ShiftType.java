package com.artdesign.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shift_types")
public class ShiftType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 班次名称，如"早班"、"晚班"、"休息"
    private String name;

    // 班次代码，如"MORNING"、"NIGHT"、"REST"
    private String code;

    // 班次颜色（十六进制）
    private String color;

    // 上班时间，如"09:00"
    private String workStart;

    // 下班时间，如"18:00"
    private String workEnd;

    // 是否为休息
    private Boolean isRest = false;

    // 是否为默认班次（工作日分配）
    private Boolean isDefault = false;

    // 备注
    private String remark;

    public ShiftType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(String workEnd) {
        this.workEnd = workEnd;
    }

    public Boolean getIsRest() {
        return isRest;
    }

    public void setIsRest(Boolean isRest) {
        this.isRest = isRest;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
