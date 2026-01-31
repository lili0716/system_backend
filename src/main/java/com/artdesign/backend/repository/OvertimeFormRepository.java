package com.artdesign.backend.repository;

import com.artdesign.backend.entity.OvertimeForm;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;

public interface OvertimeFormRepository extends JpaRepository<OvertimeForm, Long> {

    @Query("SELECT f FROM OvertimeForm f WHERE f.applicant.id = ?1 AND f.status = 1 AND f.startTime >= ?2 AND f.endTime <= ?3")
    List<OvertimeForm> findApprovedFormsByApplicantAndDateRange(Long applicantId, Date startOfDay, Date endOfDay);
}
