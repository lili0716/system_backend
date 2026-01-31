package com.artdesign.backend.repository;

import com.artdesign.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByEmployeeId(String employeeId);

    @Query("SELECT u FROM User u WHERE u.status = :status AND (u.nickName LIKE :keyword OR u.employeeId LIKE :keyword)")
    Page<User> searchActiveUsers(@Param("status") String status, @Param("keyword") String keyword, Pageable pageable);

    // 按部门ID查询用户
    List<User> findByDepartmentId(Long departmentId);

    // 员工搜索（工号或姓名模糊匹配）- 用于下拉搜索框
    @Query("SELECT u FROM User u WHERE u.status = '1' AND (u.employeeId LIKE %:keyword% OR u.nickName LIKE %:keyword%)")
    List<User> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

}
