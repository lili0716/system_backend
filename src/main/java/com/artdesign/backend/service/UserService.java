package com.artdesign.backend.service;

import com.artdesign.backend.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    User findByEmployeeId(String employeeId);

    User save(User user);

    void deleteById(Long id);
    
    // 新增方法：分页查询用户列表
    Map<String, Object> getUserList(Map<String, Object> params);

    // 搜索在职员工
    org.springframework.data.domain.Page<User> searchActiveUsers(String keyword, int page, int size);

}