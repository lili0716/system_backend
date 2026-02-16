package com.artdesign.backend.repository;

import com.artdesign.backend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // 根据用户ID查询考勤记录
    List<AttendanceRecord> findByUserId(Long userId);

    // 根据用户ID和日期范围查询
    List<AttendanceRecord> findByUserIdAndRecordDateBetween(Long userId, Date startDate, Date endDate);

    // 根据用户ID和日期查询
    AttendanceRecord findByUserIdAndRecordDate(Long userId, Date recordDate);

    // 根据状态查询
    List<AttendanceRecord> findByStatus(Integer status);

    // 根据用户ID和状态查询
    List<AttendanceRecord> findByUserIdAndStatus(Long userId, Integer status);

    // 分页查询考勤记录
    @Query(value = "SELECT ar FROM AttendanceRecord ar WHERE 1=1 " +
            "AND (:userId IS NULL OR ar.user.id = :userId) " +
            "AND (:status IS NULL OR ar.status = :status) " +
            "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
            "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
            "ORDER BY ar.recordDate DESC")
    List<AttendanceRecord> findAttendanceRecordsWithParams(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

}
