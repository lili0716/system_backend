package com.artdesign.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class SystemController {

    @GetMapping("/user/list")
    public Map<String, Object> getUserList(@RequestParam(required = false) Map<String, Object> params) {
        System.out.println("Get user list request received: " + params);
        
        // 直接返回硬编码的用户列表
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1L);
        user1.put("avatar", "");
        user1.put("status", "1");
        user1.put("userName", "admin");
        user1.put("userGender", "男");
        user1.put("nickName", "管理员");
        user1.put("userPhone", "13800138000");
        user1.put("userEmail", "admin@example.com");
        user1.put("userRoles", List.of("admin"));
        user1.put("createBy", "system");
        user1.put("createTime", new java.util.Date().toString());
        user1.put("updateBy", "system");
        user1.put("updateTime", new java.util.Date().toString());
        users.add(user1);
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 2L);
        user2.put("avatar", "");
        user2.put("status", "1");
        user2.put("userName", "user1");
        user2.put("userGender", "女");
        user2.put("nickName", "用户1");
        user2.put("userPhone", "13800138001");
        user2.put("userEmail", "user1@example.com");
        user2.put("userRoles", List.of("user"));
        user2.put("createBy", "system");
        user2.put("createTime", new java.util.Date().toString());
        user2.put("updateBy", "system");
        user2.put("updateTime", new java.util.Date().toString());
        users.add(user2);
        
        Map<String, Object> userList = new HashMap<>();
        userList.put("records", users);
        userList.put("current", 1);
        userList.put("size", 10);
        userList.put("total", users.size());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", userList);
        
        return result;
    }

    @GetMapping("/role/list")
    public Map<String, Object> getRoleList(@RequestParam(required = false) Map<String, Object> params) {
        System.out.println("Get role list request received: " + params);
        
        // 直接返回硬编码的角色列表
        List<Map<String, Object>> roles = new ArrayList<>();
        
        Map<String, Object> role1 = new HashMap<>();
        role1.put("roleId", 1L);
        role1.put("roleName", "管理员");
        role1.put("roleCode", "admin");
        role1.put("description", "系统管理员");
        role1.put("enabled", true);
        role1.put("createTime", new java.util.Date().toString());
        roles.add(role1);
        
        Map<String, Object> role2 = new HashMap<>();
        role2.put("roleId", 2L);
        role2.put("roleName", "普通用户");
        role2.put("roleCode", "user");
        role2.put("description", "普通用户");
        role2.put("enabled", true);
        role2.put("createTime", new java.util.Date().toString());
        roles.add(role2);
        
        Map<String, Object> roleList = new HashMap<>();
        roleList.put("records", roles);
        roleList.put("current", 1);
        roleList.put("size", 10);
        roleList.put("total", roles.size());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", roleList);
        
        return result;
    }

}