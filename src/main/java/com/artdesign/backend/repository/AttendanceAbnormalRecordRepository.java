package com.artdesign.backend.repository;

import com.artdesign.backend.entity.AttendanceAbnormalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AttendanceAbnormalRecordRepository extends JpaRepository<AttendanceAbnormalRecord, Long> {

    // 根据用户ID查询异常记录
    List<AttendanceAbnormalRecord> findByUserId(Long userId);

    // 查询用户未修正的异常记录
    List<AttendanceAbnormalRecord> findByUserIdAndIsCorrectedFalse(Long userId);

    // 查询用户已修正的异常记录
    List<AttendanceAbnormalRecord> findByUserIdAndIsCorrectedTrue(Long userId);

    // 根据考勤记录ID查询异常记录
    List<AttendanceAbnormalRecord> findByAttendanceRecordId(Long attendanceRecordId);

    // 根据日期范围查询用户的异常记录
    @Query("SELECT a FROM AttendanceAbnormalRecord a WHERE a.user.id = :userId AND a.recordDate BETWEEN :startDate AND :endDate")
    List<AttendanceAbnormalRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // 根据异常类型查询
    List<AttendanceAbnormalRecord> findByAbnormalType(Integer abnormalType);

    // 批量查询
    List<AttendanceAbnormalRecord> findByIdIn(List<Long> ids);

    // 根据用户ID和记录日期查询
    List<AttendanceAbnormalRecord> findByUserIdAndRecordDate(Long userId, Date recordDate);
}
