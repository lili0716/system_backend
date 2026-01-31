package com.artdesign.backend.repository;

import com.artdesign.backend.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AttendanceRecordRepository
                extends JpaRepository<AttendanceRecord, Long>, JpaSpecificationExecutor<AttendanceRecord> {

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
                        @Param("endDate") Date endDate);

        // 按部门ID和日期范围查询考勤记录
        @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.department.id = :departmentId " +
                        "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
                        "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
                        "ORDER BY ar.recordDate DESC, ar.user.employeeId ASC")
        List<AttendanceRecord> findByDepartmentIdAndDateRange(
                        @Param("departmentId") Long departmentId,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        // 按部门ID和日期范围分页查询考勤记录
        @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.department.id = :departmentId " +
                        "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
                        "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
                        "ORDER BY ar.recordDate DESC, ar.user.employeeId ASC")
        Page<AttendanceRecord> findByDepartmentIdAndDateRangePaged(
                        @Param("departmentId") Long departmentId,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        Pageable pageable);

        // 按工号列表和日期范围查询考勤记录
        @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.employeeId IN :employeeIds " +
                        "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
                        "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
                        "ORDER BY ar.recordDate DESC, ar.user.employeeId ASC")
        List<AttendanceRecord> findByEmployeeIdsAndDateRange(
                        @Param("employeeIds") List<String> employeeIds,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        // 按工号列表和日期范围分页查询考勤记录
        @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.employeeId IN :employeeIds " +
                        "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
                        "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
                        "ORDER BY ar.recordDate DESC, ar.user.employeeId ASC")
        Page<AttendanceRecord> findByEmployeeIdsAndDateRangePaged(
                        @Param("employeeIds") List<String> employeeIds,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        Pageable pageable);

        // 综合查询（部门+工号+日期范围）分页
        @Query("SELECT ar FROM AttendanceRecord ar WHERE 1=1 " +
                        "AND (:departmentId IS NULL OR ar.user.department.id = :departmentId) " +
                        "AND (:hasEmployeeIds = false OR ar.user.employeeId IN :employeeIds) " +
                        "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
                        "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
                        "ORDER BY ar.recordDate DESC, ar.user.employeeId ASC")
        Page<AttendanceRecord> findByQueryConditions(
                        @Param("departmentId") Long departmentId,
                        @Param("hasEmployeeIds") boolean hasEmployeeIds,
                        @Param("employeeIds") List<String> employeeIds,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        Pageable pageable);

        // 综合查询（部门+工号+日期范围）全量查询（用于导出）
        @Query("SELECT ar FROM AttendanceRecord ar WHERE 1=1 " +
                        "AND (:departmentId IS NULL OR ar.user.department.id = :departmentId) " +
                        "AND (:hasEmployeeIds = false OR ar.user.employeeId IN :employeeIds) " +
                        "AND (:startDate IS NULL OR ar.recordDate >= :startDate) " +
                        "AND (:endDate IS NULL OR ar.recordDate <= :endDate) " +
                        "ORDER BY ar.recordDate DESC, ar.user.employeeId ASC")
        List<AttendanceRecord> findAllByQueryConditions(
                        @Param("departmentId") Long departmentId,
                        @Param("hasEmployeeIds") boolean hasEmployeeIds,
                        @Param("employeeIds") List<String> employeeIds,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

}
