package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.entity.Form;
import com.artdesign.backend.entity.PunchCardForm;
import com.artdesign.backend.entity.BusinessTripForm;
import com.artdesign.backend.entity.FieldWorkForm;
import com.artdesign.backend.entity.LeaveForm;
import com.artdesign.backend.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forms")
public class FormController {

    @Autowired
    private FormService formService;

    // 获取所有表单
    @GetMapping
    public Result<List<Form>> getAllForms() {
        return Result.success(formService.findAll());
    }

    // 根据ID获取表单
    @GetMapping("/{id}")
    public Result<Form> getFormById(@PathVariable Long id) {
        return Result.success(formService.findById(id));
    }

    // 分页查询表单
    @PostMapping("/list")
    public Result<Map<String, Object>> getFormList(@RequestBody Map<String, Object> params) {
        return Result.success(formService.getFormList(params));
    }

    // 审批表单
    @PostMapping("/{id}/approve")
    public Result<Void> approveForm(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String comment = (String) params.get("comment");
        Long approverId = params.get("approverId") != null ? Long.valueOf(params.get("approverId").toString()) : null;
        formService.approveForm(id, status, comment, approverId);
        return Result.success();
    }

    // 补打卡表单相关接口
    @PostMapping("/punch-card")
    public Result<PunchCardForm> createPunchCardForm(@RequestBody PunchCardForm form) {
        return Result.success(formService.savePunchCardForm(form));
    }

    @GetMapping("/punch-card/{id}")
    public Result<PunchCardForm> getPunchCardFormById(@PathVariable Long id) {
        return Result.success(formService.findPunchCardFormById(id));
    }

    // 出差表单相关接口
    @PostMapping("/business-trip")
    public Result<BusinessTripForm> createBusinessTripForm(@RequestBody BusinessTripForm form) {
        return Result.success(formService.saveBusinessTripForm(form));
    }

    @GetMapping("/business-trip/{id}")
    public Result<BusinessTripForm> getBusinessTripFormById(@PathVariable Long id) {
        return Result.success(formService.findBusinessTripFormById(id));
    }

    // 外勤表单相关接口
    @PostMapping("/field-work")
    public Result<FieldWorkForm> createFieldWorkForm(@RequestBody FieldWorkForm form) {
        return Result.success(formService.saveFieldWorkForm(form));
    }

    @GetMapping("/field-work/{id}")
    public Result<FieldWorkForm> getFieldWorkFormById(@PathVariable Long id) {
        return Result.success(formService.findFieldWorkFormById(id));
    }

    // 请假表单相关接口
    @PostMapping("/leave")
    public Result<LeaveForm> createLeaveForm(@RequestBody LeaveForm form) {
        return Result.success(formService.saveLeaveForm(form));
    }

    @GetMapping("/leave/{id}")
    public Result<LeaveForm> getLeaveFormById(@PathVariable Long id) {
        return Result.success(formService.findLeaveFormById(id));
    }

    // 根据申请人查询表单
    @GetMapping("/applicant/{applicantId}")
    public Result<List<Form>> getFormsByApplicantId(@PathVariable Long applicantId) {
        return Result.success(formService.findByApplicantId(applicantId));
    }

    // 根据审批人查询表单
    @GetMapping("/approver/{approverId}")
    public Result<List<Form>> getFormsByApproverId(@PathVariable Long approverId) {
        return Result.success(formService.findByApproverId(approverId));
    }

    // 根据状态查询表单
    @GetMapping("/status/{status}")
    public Result<List<Form>> getFormsByStatus(@PathVariable Integer status) {
        return Result.success(formService.findByStatus(status));
    }

    // 撤销表单
    @PostMapping("/{id}/revoke")
    public Result<Void> revokeForm(@PathVariable Long id) {
        formService.revokeForm(id);
        return Result.success();
    }

}
