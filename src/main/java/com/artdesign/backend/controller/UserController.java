package com.artdesign.backend.controller;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.service.UserService;
import com.artdesign.backend.service.DepartmentService;
import com.artdesign.backend.service.PositionService;
import com.artdesign.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private RoleService roleService;

    // 测试接口
    @GetMapping("/test")
    public String test() {
        System.out.println("Test request received");
        return "Hello, World!";
    }

    // 登录接口
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        System.out.println("Login request received: " + params);
        
        // 直接返回登录成功，不验证用户名和密码
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock-token-admin");
        data.put("refreshToken", "mock-refresh-token-admin");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", data);
        
        return result;
    }

    // 获取用户信息接口
    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo() {
        System.out.println("Get user info request received");
        
        // 直接返回用户信息对象，符合前端期望的格式
        Map<String, Object> data = new HashMap<>();
        data.put("buttons", new ArrayList<>());
        data.put("roles", List.of("admin"));
        data.put("userId", 1L);
        data.put("userName", "admin");
        data.put("email", "admin@example.com");
        data.put("avatar", "");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", data);
        
        return result;
    }

    // 获取用户列表接口
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

    // 获取角色列表接口
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

    // 获取部门列表接口
    @GetMapping("/department/list")
    public Map<String, Object> getDepartmentList() {
        System.out.println("Get department list request received");
        
        try {
            // 调用服务获取部门树
            Map<String, Object> departmentTree = departmentService.getDepartmentTree();
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", departmentTree);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "获取部门列表失败");
            return result;
        }
    }

    // 获取职位列表接口
    @GetMapping("/position/list")
    public Map<String, Object> getPositionList() {
        System.out.println("Get position list request received");
        
        try {
            // 调用服务获取职位列表
            List<Map<String, Object>> positions = new ArrayList<>();
            
            Map<String, Object> position1 = new HashMap<>();
            position1.put("id", 1L);
            position1.put("name", "总经理");
            position1.put("code", "GM");
            position1.put("description", "公司总经理");
            positions.add(position1);
            
            Map<String, Object> position2 = new HashMap<>();
            position2.put("id", 2L);
            position2.put("name", "部门经理");
            position2.put("code", "DM");
            position2.put("description", "部门经理");
            positions.add(position2);
            
            Map<String, Object> position3 = new HashMap<>();
            position3.put("id", 3L);
            position3.put("name", "普通员工");
            position3.put("code", "EE");
            position3.put("description", "普通员工");
            positions.add(position3);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", positions);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "获取职位列表失败");
            return result;
        }
    }

    @GetMapping("/users")
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/users")
    public User save(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/users")
    public User update(@RequestBody User user) {
        return userService.save(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteById(@PathVariable Long id) {
        userService.deleteById(id);
    }

}