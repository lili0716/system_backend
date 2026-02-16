package com.artdesign.backend.controller;

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
@RequestMapping("/api/forms")
public class FormController {

    @Autowired
    private FormService formService;

    // 获取所有表单
    @GetMapping
    public List<Form> getAllForms() {
        return formService.findAll();
    }

    // 根据ID获取表单
    @GetMapping("/{id}")
    public Form getFormById(@PathVariable Long id) {
        return formService.findById(id);
    }

    // 分页查询表单
    @PostMapping("/list")
    public Map<String, Object> getFormList(@RequestBody Map<String, Object> params) {
        return formService.getFormList(params);
    }

    // 审批表单
    @PostMapping("/{id}/approve")
    public void approveForm(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String comment = (String) params.get("comment");
        Long approverId = params.get("approverId") != null ? Long.valueOf(params.get("approverId").toString()) : null;
        formService.approveForm(id, status, comment, approverId);
    }

    // 补打卡表单相关接口
    @PostMapping("/punch-card")
    public PunchCardForm createPunchCardForm(@RequestBody PunchCardForm form) {
        return formService.savePunchCardForm(form);
    }

    @GetMapping("/punch-card/{id}")
    public PunchCardForm getPunchCardFormById(@PathVariable Long id) {
        return formService.findPunchCardFormById(id);
    }

    // 出差表单相关接口
    @PostMapping("/business-trip")
    public BusinessTripForm createBusinessTripForm(@RequestBody BusinessTripForm form) {
        return formService.saveBusinessTripForm(form);
    }

    @GetMapping("/business-trip/{id}")
    public BusinessTripForm getBusinessTripFormById(@PathVariable Long id) {
        return formService.findBusinessTripFormById(id);
    }

    // 外勤表单相关接口
    @PostMapping("/field-work")
    public FieldWorkForm createFieldWorkForm(@RequestBody FieldWorkForm form) {
        return formService.saveFieldWorkForm(form);
    }

    @GetMapping("/field-work/{id}")
    public FieldWorkForm getFieldWorkFormById(@PathVariable Long id) {
        return formService.findFieldWorkFormById(id);
    }

    // 请假表单相关接口
    @PostMapping("/leave")
    public LeaveForm createLeaveForm(@RequestBody LeaveForm form) {
        return formService.saveLeaveForm(form);
    }

    @GetMapping("/leave/{id}")
    public LeaveForm getLeaveFormById(@PathVariable Long id) {
        return formService.findLeaveFormById(id);
    }

    // 根据申请人查询表单
    @GetMapping("/applicant/{applicantId}")
    public List<Form> getFormsByApplicantId(@PathVariable Long applicantId) {
        return formService.findByApplicantId(applicantId);
    }

    // 根据审批人查询表单
    @GetMapping("/approver/{approverId}")
    public List<Form> getFormsByApproverId(@PathVariable Long approverId) {
        return formService.findByApproverId(approverId);
    }

    // 根据状态查询表单
    @GetMapping("/status/{status}")
    public List<Form> getFormsByStatus(@PathVariable Integer status) {
        return formService.findByStatus(status);
    }

}
