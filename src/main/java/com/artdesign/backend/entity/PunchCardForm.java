package com.artdesign.backend.entity;

import jakarta.persistence.*;

/**
 * 补打卡表单实体
 * 关联异常考勤记录，支持多条异常记录一次性申请补卡
 */
@Entity
@Table(name = "punch_card_form")
public class PunchCardForm extends Form {

    // 关联的异常记录ID列表（逗号分隔）
    @Column(length = 500)
    private String abnormalRecordIds;

    // 补打卡原因
    @Column(length = 500)
    private String reason;

    public PunchCardForm() {
    }

    public String getAbnormalRecordIds() {
        return abnormalRecordIds;
    }

    public void setAbnormalRecordIds(String abnormalRecordIds) {
        this.abnormalRecordIds = abnormalRecordIds;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
