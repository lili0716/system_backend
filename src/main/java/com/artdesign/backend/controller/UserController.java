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

    @Autowired
    private com.artdesign.backend.service.AttendanceService attendanceService;

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
            List<String> roleCodes = user.getRoles().stream()
                    .map(com.artdesign.backend.entity.Role::getRoleCode)
                    .collect(java.util.stream.Collectors.toList());
            data.put("roles", roleCodes);

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

    // 退出登录接口
    @PostMapping("/auth/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        System.out.println("Logout request received. Token: " + token);
        // Mock logout - in real scenario, invalidate token in Redis
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
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
        List<String> roleCodes = user.getRoles().stream()
                .map(com.artdesign.backend.entity.Role::getRoleCode)
                .collect(java.util.stream.Collectors.toList());
        data.put("roles", roleCodes);
        data.put("userId", user.getId());
        data.put("userName", user.getNickName()); // Frontend expects userName for display
        data.put("employeeId", user.getEmployeeId());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());

        // 获取并增加考勤规则信息
        try {
            com.artdesign.backend.entity.AttendanceRule rule = attendanceService.getEffectiveRule(user);
            if (rule != null) {
                data.put("attendanceRule", rule);
            }
        } catch (Exception e) {
            System.out.println("Error fetching attendance rule: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", data);

        return result;
    }

    // 获取用户列表接口
    @GetMapping("/user/list")
    public Map<String, Object> getUserList(@RequestParam(required = false) Map<String, Object> params) {
        Map<String, Object> data = userService.getUserList(params);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", data);
        return result;
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
    public Map<String, Object> save(@RequestBody User user) {
        User savedUser = userService.save(user);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", savedUser);
        return result;
    }

    @PutMapping("/users")
    public Map<String, Object> update(@RequestBody User user) {
        // Safe update: fetch existing first to preserve password etc if not provided
        // But for full edit dialog, we assume full data.
        // For partial updates, we should use specific endpoints or PATCH.
        // Current frontend sends full data for standard edit.
        User updatedUser = userService.save(user);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", updatedUser);
        return result;
    }

    @PutMapping("/users/salary")
    public Map<String, Object> updateSalary(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String salary = params.get("salary") != null ? params.get("salary").toString() : "";

            User user = userService.findById(id);
            if (user != null) {
                user.setSalary(salary);
                userService.save(user);
                result.put("code", 200);
                result.put("msg", "Salary updated successfully");
            } else {
                result.put("code", 404);
                result.put("msg", "User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "Failed to update salary: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", null);
        return result;
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