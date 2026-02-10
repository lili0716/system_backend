package com.artdesign.backend.repository;

import com.artdesign.backend.entity.LeaveForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;

public interface LeaveFormRepository extends JpaRepository<LeaveForm, Long> {

    // 查询用户在指定时间段内的已通过请假申请
    @Query("SELECT l FROM LeaveForm l WHERE l.applicant.id = :userId " +
            "AND l.status = :status " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate")
    List<LeaveForm> findApprovedLeavesByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}
