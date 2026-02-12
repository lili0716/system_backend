package com.artdesign.backend.controller;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.entity.UserCredential;
import com.artdesign.backend.service.UserService;
import com.artdesign.backend.service.DepartmentService;
import com.artdesign.backend.service.PositionService;
import com.artdesign.backend.service.RoleService;
import com.artdesign.backend.service.DataSyncService;
import com.artdesign.backend.service.SalaryService;
import com.artdesign.backend.entity.Salary;
import com.artdesign.backend.dto.DataSyncDTO;
import com.artdesign.backend.repository.UserCredentialRepository;
import com.artdesign.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private DataSyncService dataSyncService;
    
    @Autowired
    private SalaryService salaryService;

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
            // 从 user_credentials 表查密码
            UserCredential credential = userCredentialRepository.findByEmployeeId(employeeId);
            String storedPassword = null;

            if (credential != null) {
                storedPassword = credential.getPassword();
            } else {
                // 兼容：如果 user_credentials 表没有记录，降级读取 users 表旧密码
                storedPassword = user.getPassword();
            }

            // Check password
            if (storedPassword == null || !password.equals(storedPassword)) {
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

            // 获取角色编码列表
            List<String> roleCodes = user.getRoles().stream()
                    .map(com.artdesign.backend.entity.Role::getRoleCode)
                    .collect(Collectors.toList());

            // 生成 JWT Token（包含 employeeId 和 roles）
            String token = jwtUtil.generateToken(user.getEmployeeId(), roleCodes);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("refreshToken", ""); // 暂时不实现刷新 token
            data.put("employeeId", user.getEmployeeId());
            data.put("userId", user.getId());
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
        System.out.println("Logout request received. Token: " + (token != null ? "***" : "null"));
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }

    // 获取用户信息接口
    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从 JWT Token 中解析工号
            String employeeId = null;
            if (token != null) {
                employeeId = jwtUtil.getEmployeeId(token);
            }

            if (employeeId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("msg", "Token 无效或已过期");
                return result;
            }

            User user = userService.findByEmployeeId(employeeId);

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
                    .collect(Collectors.toList());
            data.put("roles", roleCodes);
            data.put("userId", user.getId());
            data.put("userName", user.getNickName());
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
        } catch (Exception e) {
            System.out.println("Error in getUserInfo: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "获取用户信息失败: " + e.getMessage());
            return result;
        }
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

    // 获取部门列表接口
    @GetMapping("/department/list")
    public Map<String, Object> getDepartmentList() {
        System.out.println("Get department list request received");

        try {
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
        // 如果前端传了密码，保存到 user_credentials 表
        String password = user.getPassword();
        User savedUser = userService.save(user);
        if (password != null && !password.isEmpty() && savedUser.getEmployeeId() != null) {
            UserCredential credential = userCredentialRepository.findByEmployeeId(savedUser.getEmployeeId());
            if (credential == null) {
                credential = new UserCredential(savedUser.getEmployeeId(), password);
            } else {
                credential.setPassword(password);
            }
            userCredentialRepository.save(credential);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", savedUser);
        return result;
    }

    @PutMapping("/users")
    public Map<String, Object> update(@RequestBody User user) {
        User updatedUser = userService.save(user);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", updatedUser);
        return result;
    }

    @PutMapping("/users/salary")
    public Map<String, Object> updateSalary(@RequestBody Map<String, Object> params) {
        System.out.println("Update salary request: " + params);
        Map<String, Object> result = new HashMap<>();
        try {
            Object idObj = params.get("id");
            if (idObj == null) {
                result.put("code", 400);
                result.put("msg", "ID is missing");
                return result;
            }
            Long id = Long.valueOf(idObj.toString());
            String salary = params.get("salary") != null ? params.get("salary").toString() : "";
            System.out.println("Updating salary for user " + id + " to " + salary);

            User user = userService.findById(id);
            if (user != null) {
                System.out.println("User found: " + user.getNickName());
                
                // 查找或创建薪资信息
                Salary userSalary = salaryService.findByUserId(id);
                if (userSalary == null) {
                    userSalary = new Salary();
                    userSalary.setUser(user);
                    userSalary.setStatus("1");
                    userSalary.setCreateBy("system");
                    userSalary.setCreateTime(new java.util.Date());
                }
                
                // 更新薪资信息
                userSalary.setAmount(salary);
                userSalary.setCurrentSalary(salary);
                userSalary.setUpdateBy("system");
                userSalary.setUpdateTime(new java.util.Date());
                
                Salary savedSalary = salaryService.save(userSalary);
                System.out.println("Salary saved. New salary: " + savedSalary.getAmount());

                result.put("code", 200);
                result.put("msg", "Salary updated successfully");
            } else {
                System.out.println("User not found with id: " + id);
                result.put("code", 404);
                result.put("msg", "User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating salary: " + e.getMessage());
            result.put("code", 500);
            result.put("msg", "Failed to update salary: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteById(@PathVariable Long id) {
        // 删除用户相关的薪资信息
        try {
            salaryService.deleteByUserId(id);
            System.out.println("Salary deleted for user: " + id);
        } catch (Exception e) {
            System.out.println("Error deleting salary: " + e.getMessage());
        }
        
        // 删除用户
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
    
    /**
     * 数据同步接口
     * @param syncDTO 数据同步配置
     * @return 同步结果
     */
    @PostMapping("/users/sync")
    public Map<String, Object> syncUsers(@RequestBody DataSyncDTO syncDTO) {
        return dataSyncService.syncEmployeeData(syncDTO);
    }
    
    /**
     * 测试数据库连接接口
     * @param syncDTO 数据库连接配置
     * @return 连接测试结果
     */
    @PostMapping("/users/test-connection")
    public Map<String, Object> testConnection(@RequestBody DataSyncDTO syncDTO) {
        return dataSyncService.testConnection(syncDTO);
    }
}