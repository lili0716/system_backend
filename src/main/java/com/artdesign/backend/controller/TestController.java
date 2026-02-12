package com.artdesign.backend.controller;

import com.artdesign.backend.util.SuperAdminInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SuperAdminInitializer superAdminInitializer;

    @GetMapping("/user/list")
    public String getUserList() {
        System.out.println("Test get user list request received");
        return "User list endpoint is working!";
    }

    @GetMapping("/role/list")
    public String getRoleList() {
        System.out.println("Test get role list request received");
        return "Role list endpoint is working!";
    }

    @GetMapping("/init-superadmin")
    public Map<String, Object> initializeSuperAdmin() {
        Map<String, Object> result = new HashMap<>();
        try {
            superAdminInitializer.initializeSuperAdmin();
            result.put("code", 200);
            result.put("msg", "超级管理员账号初始化成功");
            result.put("data", Map.of(
                "username", "SpuerAdmin",
                "password", "123456",
                "role", "超级管理员"
            ));
        } catch (Exception e) {
            System.err.println("初始化超级管理员账号失败: " + e.getMessage());
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "初始化超级管理员账号失败: " + e.getMessage());
        }
        return result;
    }

}