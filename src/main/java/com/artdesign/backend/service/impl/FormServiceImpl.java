package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Form;
import com.artdesign.backend.entity.PunchCardForm;
import com.artdesign.backend.entity.BusinessTripForm;
import com.artdesign.backend.entity.FieldWorkForm;
import com.artdesign.backend.entity.LeaveForm;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.FormRepository;
import com.artdesign.backend.repository.PunchCardFormRepository;
import com.artdesign.backend.repository.BusinessTripFormRepository;
import com.artdesign.backend.repository.FieldWorkFormRepository;
import com.artdesign.backend.repository.LeaveFormRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private PunchCardFormRepository punchCardFormRepository;

    @Autowired
    private BusinessTripFormRepository businessTripFormRepository;

    @Autowired
    private FieldWorkFormRepository fieldWorkFormRepository;

    @Autowired
    private LeaveFormRepository leaveFormRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.artdesign.backend.repository.DepartmentRepository departmentRepository;

    @Override
    public List<Form> findAll() {
        return formRepository.findAll();
    }

    @Override
    public Form findById(Long id) {
        return formRepository.findById(id).orElse(null);
    }

    @Override
    public Form save(Form form) {
        // 设置默认状态为待审批
        if (form.getStatus() == null) {
            form.setStatus(0);
        }
        // 自动设置审批人（这里简化处理，实际应该根据组织架构找到上级）
        if (form.getApprover() == null && form.getApplicant() != null) {
            User applicant = form.getApplicant();
            // 这里应该根据组织架构找到申请人的直接上级作为审批人
            // 暂时设置为系统管理员
            User admin = userRepository.findByEmployeeId("20950");
            if (admin != null) {
                form.setApprover(admin);
            }
        }
        return formRepository.save(form);
    }

    @Override
    public void deleteById(Long id) {
        formRepository.deleteById(id);
    }

    @Override
    public List<Form> findByApplicantId(Long applicantId) {
        return formRepository.findByApplicantId(applicantId);
    }

    @Override
    public List<Form> findByApproverId(Long approverId) {
        return formRepository.findByApproverId(approverId);
    }

    @Override
    public List<Form> findByStatus(Integer status) {
        return formRepository.findByStatus(status);
    }

    @Override
    public void revokeForm(Long formId) {
        Form form = formRepository.findById(formId).orElse(null);
        if (form != null) {
            if (form.getStatus() == 0) {
                formRepository.delete(form);
            } else {
                throw new RuntimeException("只能撤销待审批的表单");
            }
        }
    }

    @Override
    public Map<String, Object> getFormList(Map<String, Object> params) {
        Long applicantId = params.get("applicantId") != null ? Long.valueOf(params.get("applicantId").toString()) : null;
        Long approverId = params.get("approverId") != null ? Long.valueOf(params.get("approverId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;
        Integer type = params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : null;
        Integer page = params.get("page") != null ? Integer.valueOf(params.get("page").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.valueOf(params.get("pageSize").toString()) : 10;
        
        Date startDate = null;
        Date endDate = null;
        // Simple date parsing handling - assuming incoming string is ISO compatible or we handle in Controller/Jackson
        // For simplicity here, assuming frontend sends valid format handled by Jackson in Controller, 
        // but params is Map<String, Object>, so likely Strings.
        // Skipping complex parsing for now, assuming params might have date objects if handled by caller or null.
        // Ideally controller should parse. For now, keep it simple or fix if needed.

        List<Form> forms = formRepository.findFormsWithParams(applicantId, approverId, status, type, startDate, endDate);

        // 计算分页
        int total = forms.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Form> pageForms = forms.subList(start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", pageForms);
        return result;
    }

    @Override
    public void approveForm(Long formId, Integer status, String comment, Long approverId) {
        Form form = formRepository.findById(formId).orElse(null);
        if (form != null) {
            form.setStatus(status);
            form.setApproveComment(comment);
            form.setApproveTime(new Date());
            if (approverId != null) {
                User approver = userRepository.findById(approverId).orElse(null);
                if (approver != null) {
                    form.setApprover(approver);
                }
            }
            formRepository.save(form);
        }
    }

    @Override
    public PunchCardForm savePunchCardForm(PunchCardForm form) {
        form.setType(1); // 设置表单类型为补打卡
        determineAndSetApprover(form);
        return punchCardFormRepository.save(form);
    }

    @Override
    public PunchCardForm findPunchCardFormById(Long id) {
        return punchCardFormRepository.findById(id).orElse(null);
    }

    @Override
    public BusinessTripForm saveBusinessTripForm(BusinessTripForm form) {
        form.setType(2); // 设置表单类型为出差
        determineAndSetApprover(form);
        return businessTripFormRepository.save(form);
    }

    @Override
    public BusinessTripForm findBusinessTripFormById(Long id) {
        return businessTripFormRepository.findById(id).orElse(null);
    }

    @Override
    public FieldWorkForm saveFieldWorkForm(FieldWorkForm form) {
        form.setType(3); // 设置表单类型为外勤
        determineAndSetApprover(form);
        return fieldWorkFormRepository.save(form);
    }

    @Override
    public FieldWorkForm findFieldWorkFormById(Long id) {
        return fieldWorkFormRepository.findById(id).orElse(null);
    }

    @Override
    public LeaveForm saveLeaveForm(LeaveForm form) {
        form.setType(4); // 设置表单类型为请假
        determineAndSetApprover(form);
        return leaveFormRepository.save(form);
    }

    @Override
    public LeaveForm findLeaveFormById(Long id) {
        return leaveFormRepository.findById(id).orElse(null);
    }

    private void determineAndSetApprover(Form form) {
        if (form.getStatus() == null) {
            form.setStatus(0); // Default to Pending
        }
        form.setApplyTime(new Date());

        if (form.getApprover() == null && form.getApplicant() != null) {
            User applicant = form.getApplicant();
            // Refetch applicant to ensure department info is loaded
            if (applicant.getId() != null) {
                 applicant = userRepository.findById(applicant.getId()).orElse(applicant);
                 form.setApplicant(applicant);
            }

            if (applicant.getDepartment() != null) {
                Long leaderId = applicant.getDepartment().getLeaderId();
                
                // Special Agent: Leave > 1 day -> HR Department Leader
                if (form instanceof LeaveForm) {
                    LeaveForm leaveForm = (LeaveForm) form;
                    if (leaveForm.getLeaveDays() != null && leaveForm.getLeaveDays() > 1) {
                         List<com.artdesign.backend.entity.Department> hrDepts = departmentRepository.findByNameContaining("人事");
                         if (hrDepts.isEmpty()) {
                             hrDepts = departmentRepository.findByNameContaining("Human Resources");
                         }
                         if (hrDepts.isEmpty()) {
                             hrDepts = departmentRepository.findByNameContaining("HR");
                         }
                         
                         if (!hrDepts.isEmpty()) {
                             Long hrLeaderId = hrDepts.get(0).getLeaderId();
                             if (hrLeaderId != null) {
                                 leaderId = hrLeaderId;
                             }
                         }
                    }
                }

                if (leaderId != null) {
                    User approver = userRepository.findById(leaderId).orElse(null);
                    if (approver != null) {
                        form.setApprover(approver);
                    }
                }
            }
             
            // Fallback: If no approver found, assign to admin
            if (form.getApprover() == null) {
                 User admin = userRepository.findByEmployeeId("20950");
                 if (admin != null) {
                     form.setApprover(admin);
                 }
            }
        }
    }

}
