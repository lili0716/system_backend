package com.artdesign.backend.repository;

import com.artdesign.backend.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {

    // 根据申请人ID查询表单
    List<Form> findByApplicantId(Long applicantId);

    // 根据审批人ID查询表单
    List<Form> findByApproverId(Long approverId);

    // 根据状态查询表单
    List<Form> findByStatus(Integer status);

    // 根据类型查询表单
    List<Form> findByType(Integer type);

    // 根据申请人和状态查询
    List<Form> findByApplicantIdAndStatus(Long applicantId, Integer status);

    // 根据审批人和状态查询
    List<Form> findByApproverIdAndStatus(Long approverId, Integer status);

    // 分页查询表单
    @Query(value = "SELECT f FROM Form f WHERE 1=1 " +
            "AND (:applicantId IS NULL OR f.applicant.id = :applicantId) " +
            "AND (:status IS NULL OR f.status = :status) " +
            "AND (:type IS NULL OR f.type = :type) " +
            "ORDER BY f.applyTime DESC")
    List<Form> findFormsWithParams(
            @Param("applicantId") Long applicantId,
            @Param("status") Integer status,
            @Param("type") Integer type
    );

}
