package com.artdesign.backend.repository;

import com.artdesign.backend.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    // 根据用户ID查询薪资信息
    Salary findByUserId(Long userId);

    // 根据用户ID删除薪资信息
    void deleteByUserId(Long userId);

}
