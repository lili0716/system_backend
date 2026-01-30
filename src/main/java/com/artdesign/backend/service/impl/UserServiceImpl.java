package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            // 创建默认用户
            createDefaultUsers();
            users = userRepository.findAll();
        }
        return users;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getUserList(Map<String, Object> params) {
        // 获取分页参数
        int current = params.getOrDefault("current", 1) instanceof Number ? ((Number) params.get("current")).intValue() : 1;
        int size = params.getOrDefault("size", 10) instanceof Number ? ((Number) params.get("size")).intValue() : 10;
        
        // 确保有默认用户数据
        if (userRepository.findAll().isEmpty()) {
            createDefaultUsers();
        }
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(current - 1, size);
        
        // 执行分页查询
        Page<User> userPage = userRepository.findAll(pageable);
        
        // 构建响应结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", userPage.getContent());
        result.put("current", current);
        result.put("size", size);
        result.put("total", userPage.getTotalElements());
        
        return result;
    }
    
    // 创建默认用户数据
    private void createDefaultUsers() {
        // 创建默认用户 - 简化版本
        // 创建默认用户 - 简化版本
        User admin = new User();
        admin.setEmployeeId("20950"); 
        admin.setPassword("123456");
        admin.setEmail("20950@example.com");
        admin.setNickName("超级管理员");
        admin.setUserPhone("13800138000");
        admin.setUserGender("男");
        admin.setStatus("1");
        admin.setAvatar("");
        admin.setCreateBy("system");
        admin.setCreateTime(new java.util.Date());
        admin.setUpdateBy("system");
        admin.setUpdateTime(new java.util.Date());
        userRepository.save(admin);
        
        User user1 = new User();
        user1.setEmployeeId("20952");
        user1.setPassword("123456");
        user1.setEmail("20952@example.com");
        user1.setNickName("普通用户");
        user1.setUserPhone("13800138002");
        user1.setUserGender("女");
        user1.setStatus("1");
        user1.setAvatar("");
        user1.setCreateBy("system");
        user1.setCreateTime(new java.util.Date());
        user1.setUpdateBy("system");
        user1.setUpdateTime(new java.util.Date());
        userRepository.save(user1);
    }

    @Override
    public Page<User> searchActiveUsers(String keyword, int page, int size) {
        if (userRepository.findAll().isEmpty()) {
            createDefaultUsers();
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        String searchKey = "%" + (keyword == null ? "" : keyword) + "%";
        return userRepository.searchActiveUsers("1", searchKey, pageable);
    }

}