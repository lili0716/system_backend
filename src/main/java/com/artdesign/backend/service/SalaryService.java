package com.artdesign.backend.service;

import com.artdesign.backend.entity.Salary;

public interface SalaryService {

    // 根据ID查询薪资信息
    Salary findById(Long id);

    // 根据用户ID查询薪资信息
    Salary findByUserId(Long userId);

    // 保存薪资信息
    Salary save(Salary salary);

    // 删除薪资信息
    void deleteById(Long id);

    // 根据用户ID删除薪资信息
    void deleteByUserId(Long userId);

}
