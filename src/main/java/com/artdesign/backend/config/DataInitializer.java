package com.artdesign.backend.config;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.entity.Role;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.artdesign.backend.repository.AttendanceRuleRepository attendanceRuleRepository;

    @Override
    public void run(String... args) throws Exception {
        // 初始化角色
        initializeRoles();

        // 初始化用户
        initializeUsers();

        // 初始化考勤规则
        initializeAttendanceRules();
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
            roleRepository.save(superRole);

            // 创建管理员角色
            Role adminRole = new Role();
            adminRole.setRoleName("管理员");
            adminRole.setRoleCode("R_ADMIN");
            adminRole.setDescription("拥有系统大部分权限");
            adminRole.setEnabled(true);
            roleRepository.save(adminRole);

            // 创建普通用户角色
            Role userRole = new Role();
            userRole.setRoleName("普通用户");
            userRole.setRoleCode("R_USER");
            userRole.setDescription("拥有系统基础权限");
            userRole.setEnabled(true);
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
            superUser.setPassword("123456"); // 实际项目中应该加密密码
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

            // 创建管理员用户
            User adminUser = new User();
            adminUser.setEmployeeId("20951");
            adminUser.setPassword("123456"); // 实际项目中应该加密密码
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

            // 创建普通用户
            User normalUser = new User();
            normalUser.setEmployeeId("20952");
            normalUser.setPassword("123456"); // 实际项目中应该加密密码
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

            System.out.println("用户初始化完成");
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
}
