package com.artdesign.backend.controller;

import com.artdesign.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
public class SystemController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/role/list")
    public Map<String, Object> getRoleList(@RequestParam(required = false) Map<String, Object> params) {
        System.out.println("Get role list request received: " + params);
        Map<String, Object> result = new HashMap<>();

        try {
            // 从数据库获取真实的角色列表
            Map<String, Object> roleList = roleService.getRoleList(params != null ? params : new HashMap<>());
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", roleList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "Failed to fetch role list: " + e.getMessage());
        }

        return result;
    }

}
