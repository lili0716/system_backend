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
        
        String employeeId = params.get("userName"); // Frontend sends 'userName'
        String password = params.get("password");
        
        if (employeeId == null || password == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("msg", "Invalid parameters");
            return result;
        }

        User user = userService.findByEmployeeId(employeeId);
        
        if (user != null) {
            // Check password
            if (!password.equals(user.getPassword())) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("msg", "工号或密码错误");
                return result;
            }
            
            // Check status
            if (!"1".equals(user.getStatus())) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 403);
                result.put("msg", "账号已禁用，请联系管理员");
                return result;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("token", "mock-token-" + user.getEmployeeId());
            data.put("refreshToken", "mock-refresh-token-" + user.getEmployeeId());
            data.put("employeeId", user.getEmployeeId());
            data.put("userId", user.getId());
            data.put("roles", List.of("admin")); // Hardcode role for now until RoleService is fully integrated
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", data);
            return result;
        } else {
             Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("msg", "该工号不存在");
            return result;
        }
    }

    // 获取用户信息接口
    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        System.out.println("Get user info request received. Token: " + token);
        
        // Mock get user from token (in real app, parse token)
        // For now, return a default admin or derive from token if possible
        String employeeId = "20950";
        if (token != null && token.contains("mock-token-")) {
            employeeId = token.replace("mock-token-", "").replace("Bearer ", "");
        }
        
        User user = userService.findByEmployeeId(employeeId);
        if (user == null) {
             // Fallback
             user = userService.findByEmployeeId("20950");
        }
        
        if (user == null) {
             Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("msg", "User not found");
            return result;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("buttons", new ArrayList<>());
        data.put("roles", List.of("admin")); // Mock role
        data.put("userId", user.getId());
        data.put("userName", user.getNickName()); // Frontend expects userName for display, verify what it expects
        data.put("employeeId", user.getEmployeeId());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", data);
        
        return result;
    }

    // 获取用户列表接口
    @GetMapping("/user/list")
    public Map<String, Object> getUserList(@RequestParam(required = false) Map<String, Object> params) {
         return userService.getUserList(params);
    }
    
    // 省略角色列表代码，保持不变...

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

    @GetMapping("/users/search")
    public Map<String, Object> searchUsers(@RequestParam(required = false, defaultValue = "") String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        org.springframework.data.domain.Page<User> userPage = userService.searchActiveUsers(keyword, page, size);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", userPage.getContent());
        data.put("total", userPage.getTotalElements());
        data.put("current", page);
        data.put("size", size);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", data);
        return result;
    }
}