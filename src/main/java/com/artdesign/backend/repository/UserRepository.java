package com.artdesign.backend.repository;

import com.artdesign.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmployeeId(String employeeId);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.status = :status AND (u.nickName LIKE :keyword OR u.employeeId LIKE :keyword)")
    org.springframework.data.domain.Page<User> searchActiveUsers(@org.springframework.data.repository.query.Param("status") String status, @org.springframework.data.repository.query.Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

}