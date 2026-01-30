package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "form")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(AuditingEntityListener.class)
public abstract class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "form_seq_gen")
    @SequenceGenerator(name = "form_seq_gen", sequenceName = "form_seq_v2", initialValue = 1000, allocationSize = 1)
    private Long id;

    // 申请人
    @ManyToOne
    @JoinColumn(name = "applicant_id")
    @JsonIgnoreProperties({"roles", "password"})
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
    @JsonIgnoreProperties({"roles", "password"})
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

    public Form() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public String getApproveComment() {
        return approveComment;
    }

    public void setApproveComment(String approveComment) {
        this.approveComment = approveComment;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
