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
@Table(name = "form")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(AuditingEntityListener.class)
public abstract class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 申请人
    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private User applicant;

    // 申请时间
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    // 表单状态：0-待审批，1-已审批，2-已拒绝
    private Integer status;

    // 审批人
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    // 审批时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    // 审批意见
    private String approveComment;

    // 表单类型：1-补打卡，2-出差，3-外勤，4-请假
    private Integer type;

    // 表单标题
    private String title;

    // 表单描述
    private String description;

}
