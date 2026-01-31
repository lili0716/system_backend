package com.artdesign.backend.service;

import com.artdesign.backend.entity.Form;
import com.artdesign.backend.entity.PunchCardForm;
import com.artdesign.backend.entity.BusinessTripForm;
import com.artdesign.backend.entity.FieldWorkForm;
import com.artdesign.backend.entity.LeaveForm;
import java.util.List;
import java.util.Map;

public interface FormService {

    // 通用表单方法
    List<Form> findAll();

    Form findById(Long id);

    Form save(Form form);

    void deleteById(Long id);

    // 根据申请人查询
    List<Form> findByApplicantId(Long applicantId);

    // 根据审批人查询
    List<Form> findByApproverId(Long approverId);

    // 根据状态查询
    List<Form> findByStatus(Integer status);

    // 分页查询表单
    Map<String, Object> getFormList(Map<String, Object> params);

    // 审批表单
    void approveForm(Long formId, Integer status, String comment, Long approverId);

    // 补打卡表单方法
    PunchCardForm savePunchCardForm(PunchCardForm form);

    PunchCardForm findPunchCardFormById(Long id);

    // 出差表单方法
    BusinessTripForm saveBusinessTripForm(BusinessTripForm form);

    BusinessTripForm findBusinessTripFormById(Long id);

    // 外勤表单方法
    FieldWorkForm saveFieldWorkForm(FieldWorkForm form);

    FieldWorkForm findFieldWorkFormById(Long id);

    // 请假表单方法
    LeaveForm saveLeaveForm(LeaveForm form);

    LeaveForm findLeaveFormById(Long id);

    // 加班表单
    com.artdesign.backend.entity.OvertimeForm saveOvertimeForm(com.artdesign.backend.entity.OvertimeForm form);

    com.artdesign.backend.entity.OvertimeForm findOvertimeFormById(Long id);

    // 撤销表单
    void revokeForm(Long formId);

}
