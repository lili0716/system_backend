package com.artdesign.backend.config;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.entity.UserCredential;
import com.artdesign.backend.entity.Role;
import com.artdesign.backend.entity.Route;
import com.artdesign.backend.entity.RouteMeta;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.repository.UserCredentialRepository;
import com.artdesign.backend.repository.RoleRepository;
import com.artdesign.backend.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.artdesign.backend.repository.AttendanceRuleRepository attendanceRuleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 初始化角色
        initializeRoles();

        // 初始化用户
        initializeUsers();

        // 初始化考勤规则
        initializeAttendanceRules();

        // 初始化菜单
        initializeRoutes();

        // 修复：确保已有角色的 isAdmin 字段正确
        fixExistingRolesIsAdmin();
    }

    // 初始化角色
    private void initializeRoles() {
        // 检查是否已存在角色
        if (roleRepository.count() == 0) {
            // 创建超级管理员角色
            Role superRole = new Role();
            superRole.setRoleName("超级管理员");
            superRole.setRoleCode("R_SUPER");
            superRole.setDescription("拥有系统所有权限");
            superRole.setEnabled(true);
            superRole.setIsAdmin(true); // 标记为管理员
            roleRepository.save(superRole);

            // 创建管理员角色
            Role adminRole = new Role();
            adminRole.setRoleName("管理员");
            adminRole.setRoleCode("R_ADMIN");
            adminRole.setDescription("拥有系统大部分权限");
            adminRole.setEnabled(true);
            adminRole.setIsAdmin(true); // 标记为管理员
            roleRepository.save(adminRole);

            // 创建普通用户角色
            Role userRole = new Role();
            userRole.setRoleName("普通用户");
            userRole.setRoleCode("R_USER");
            userRole.setDescription("拥有系统基础权限");
            userRole.setEnabled(true);
            userRole.setIsAdmin(false); // 非管理员
            roleRepository.save(userRole);

            System.out.println("角色初始化完成");
        }
    }

    // 初始化用户
    private void initializeUsers() {
        // 检查是否已存在用户
        if (userRepository.count() == 0) {
            Date now = new Date();

            // 获取角色
            Role superRole = roleRepository.findByRoleCode("R_SUPER");
            Role adminRole = roleRepository.findByRoleCode("R_ADMIN");
            Role userRole = roleRepository.findByRoleCode("R_USER");

            // 创建超级管理员用户
            User superUser = new User();
            superUser.setEmployeeId("20950");
            superUser.setEmail("20950@example.com");
            superUser.setNickName("超级管理员");
            superUser.setUserPhone("13800138000");
            superUser.setUserGender("男");
            superUser.setStatus("1");
            superUser.setCreateBy("system");
            superUser.setCreateTime(now);
            superUser.setUpdateBy("system");
            superUser.setUpdateTime(now);
            if (superRole != null) {
                superUser.setRoles(List.of(superRole));
            }
            userRepository.save(superUser);
            // 密码存入 user_credentials 表
            saveCredential("20950", "123456");

            // 创建管理员用户
            User adminUser = new User();
            adminUser.setEmployeeId("20951");
            adminUser.setEmail("20951@example.com");
            adminUser.setNickName("管理员");
            adminUser.setUserPhone("13800138001");
            adminUser.setUserGender("女");
            adminUser.setStatus("1");
            adminUser.setCreateBy("system");
            adminUser.setCreateTime(now);
            adminUser.setUpdateBy("system");
            adminUser.setUpdateTime(now);
            if (adminRole != null) {
                adminUser.setRoles(List.of(adminRole));
            }
            userRepository.save(adminUser);
            saveCredential("20951", "123456");

            // 创建普通用户
            User normalUser = new User();
            normalUser.setEmployeeId("20952");
            normalUser.setEmail("20952@example.com");
            normalUser.setNickName("普通用户");
            normalUser.setUserPhone("13800138002");
            normalUser.setUserGender("男");
            normalUser.setStatus("1");
            normalUser.setCreateBy("system");
            normalUser.setCreateTime(now);
            normalUser.setUpdateBy("system");
            normalUser.setUpdateTime(now);
            if (userRole != null) {
                normalUser.setRoles(List.of(userRole));
            }
            userRepository.save(normalUser);
            saveCredential("20952", "123456");

            System.out.println("用户初始化完成");
        }
    }

    // 保存凭证到 user_credentials 表
    private void saveCredential(String employeeId, String password) {
        UserCredential existing = userCredentialRepository.findByEmployeeId(employeeId);
        if (existing == null) {
            UserCredential credential = new UserCredential(employeeId, password);
            userCredentialRepository.save(credential);
        }
    }

    // 修复已有角色的 isAdmin 字段（升级兼容）
    private void fixExistingRolesIsAdmin() {
        List<Role> allRoles = roleRepository.findAll();
        for (Role role : allRoles) {
            if (role.getIsAdmin() == null) {
                // 根据角色编码设置默认值（仅用于首次升级）
                String code = role.getRoleCode();
                if ("R_SUPER".equals(code) || "R_ADMIN".equals(code)
                        || "superadmin".equals(code) || "admin".equals(code)) {
                    role.setIsAdmin(true);
                } else {
                    role.setIsAdmin(false);
                }
                roleRepository.save(role);
            }
        }
    }

    // 初始化考勤规则
    private void initializeAttendanceRules() {
        com.artdesign.backend.entity.AttendanceRule defaultRule = attendanceRuleRepository.findByIsDefaultTrue();

        if (defaultRule == null) {
            try {
                // 检查是否存在同名规则
                com.artdesign.backend.entity.AttendanceRule existingRule = attendanceRuleRepository
                        .findByRuleName("默认考勤规则");
                if (existingRule != null) {
                    existingRule.setIsDefault(true);
                    attendanceRuleRepository.save(existingRule);
                    System.out.println("已将现有'默认考勤规则'标记为默认");
                } else {
                    com.artdesign.backend.entity.AttendanceRule rule = new com.artdesign.backend.entity.AttendanceRule();
                    rule.setRuleName("默认考勤规则");
                    rule.setDescription("系统默认考勤规则");
                    rule.setIsDefault(true);
                    rule.setEnabled(true);

                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
                    rule.setWorkInTime(timeFormat.parse("09:00:00"));
                    rule.setWorkOutTime(timeFormat.parse("18:00:00"));
                    rule.setStandardWorkHours(8.0);
                    rule.setLateThreshold(0);
                    rule.setEarlyLeaveThreshold(0);

                    attendanceRuleRepository.save(rule);
                    System.out.println("默认考勤规则初始化完成");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 初始化菜单
    private void initializeRoutes() {
        // 检查是否已存在运维管理菜单
        Route opsRoute = routeRepository.findByName("Ops");
        if (opsRoute == null) {
            // 创建运维管理一级菜单
            opsRoute = new Route();
            opsRoute.setName("Ops");
            opsRoute.setPath("/ops");
            opsRoute.setComponent("/index/index");

            // 创建运维管理菜单的meta数据
            RouteMeta opsMeta = new RouteMeta();
            opsMeta.setTitle("运维管理");
            opsMeta.setIcon("ri:server-line");
            opsMeta.setKeepAlive(false);
            opsMeta.setSort(6); // 设置排序，确保在合适的位置显示

            // 设置角色权限
            List<String> opsRoles = new ArrayList<>();
            opsRoles.add("R_SUPER");
            opsRoles.add("R_ADMIN");
            opsMeta.setRoles(opsRoles);

            opsRoute.setMeta(opsMeta);
            routeRepository.save(opsRoute);

            // 创建服务器运维二级菜单
            Route serverRoute = new Route();
            serverRoute.setName("ServerOps");
            serverRoute.setPath("server");
            serverRoute.setComponent("/ops/server-monitor");
            serverRoute.setParent(opsRoute);

            RouteMeta serverMeta = new RouteMeta();
            serverMeta.setTitle("服务器运维");
            serverMeta.setKeepAlive(true);
            serverMeta.setRoles(opsRoles);
            serverRoute.setMeta(serverMeta);
            routeRepository.save(serverRoute);

            // 创建系统日志二级菜单
            Route logsRoute = new Route();
            logsRoute.setName("SystemLogs");
            logsRoute.setPath("logs");
            logsRoute.setComponent("/ops/system-log");
            logsRoute.setParent(opsRoute);

            RouteMeta logsMeta = new RouteMeta();
            logsMeta.setTitle("系统日志");
            logsMeta.setKeepAlive(true);
            logsMeta.setRoles(opsRoles);
            logsRoute.setMeta(logsMeta);
            routeRepository.save(logsRoute);

            System.out.println("运维管理菜单初始化完成");
        }
    }
}
