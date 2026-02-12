package com.artdesign.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;

import java.util.Date;

@Entity
@Table(name = "salaries")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 与用户的一对一关系
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // 薪资金额
    private String amount;

    // 薪资类型（如：月薪、年薪等）
    private String salaryType;

    // 发放周期
    private String paymentCycle;

    // 入职薪资
    private String initialSalary;

    // 当前薪资
    private String currentSalary;

    // 上次调薪日期
    private Date lastAdjustmentDate;

    // 下次调薪日期
    private Date nextAdjustmentDate;

    // 薪资状态
    private String status;

    // 创建信息
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    // 备注
    private String remark;

    public Salary() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(String salaryType) {
        this.salaryType = salaryType;
    }

    public String getPaymentCycle() {
        return paymentCycle;
    }

    public void setPaymentCycle(String paymentCycle) {
        this.paymentCycle = paymentCycle;
    }

    public String getInitialSalary() {
        return initialSalary;
    }

    public void setInitialSalary(String initialSalary) {
        this.initialSalary = initialSalary;
    }

    public String getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(String currentSalary) {
        this.currentSalary = currentSalary;
    }

    public Date getLastAdjustmentDate() {
        return lastAdjustmentDate;
    }

    public void setLastAdjustmentDate(Date lastAdjustmentDate) {
        this.lastAdjustmentDate = lastAdjustmentDate;
    }

    public Date getNextAdjustmentDate() {
        return nextAdjustmentDate;
    }

    public void setNextAdjustmentDate(Date nextAdjustmentDate) {
        this.nextAdjustmentDate = nextAdjustmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
