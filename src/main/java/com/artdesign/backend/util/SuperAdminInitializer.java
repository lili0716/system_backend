package com.artdesign.backend.util;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.entity.UserCredential;
import com.artdesign.backend.entity.Role;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.repository.UserCredentialRepository;
import com.artdesign.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SuperAdminInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private RoleRepository roleRepository;

    public void initializeSuperAdmin() {
        System.out.println("开始初始化超级管理员账号...");

        // 1. 检查并创建超级管理员角色
        Role superRole = roleRepository.findByRoleCode("R_SUPER");
        if (superRole == null) {
            System.out.println("创建超级管理员角色...");
            superRole = new Role();
            superRole.setRoleName("超级管理员");
            superRole.setRoleCode("R_SUPER");
            superRole.setDescription("拥有系统所有权限");
            superRole.setEnabled(true);
            superRole.setIsAdmin(true);
            superRole.setCreateTime(new Date());
            superRole = roleRepository.save(superRole);
            System.out.println("超级管理员角色创建成功: " + superRole.getRoleId());
        } else {
            System.out.println("超级管理员角色已存在: " + superRole.getRoleId());
        }

        // 2. 检查并创建超级管理员用户
        User superUser = userRepository.findByEmployeeId("SuperAdmin");
        if (superUser == null) {
            System.out.println("创建超级管理员用户...");
            superUser = new User();
            superUser.setEmployeeId("SuperAdmin");
            superUser.setEmail("superadmin@example.com");
            superUser.setNickName("超级管理员");
            superUser.setUserPhone("13800138000");
            superUser.setUserGender("男");
            superUser.setStatus("1");
            superUser.setCreateBy("system");
            superUser.setCreateTime(new Date());
            superUser.setUpdateBy("system");
            superUser.setUpdateTime(new Date());
            superUser.setRoles(List.of(superRole));
            superUser = userRepository.save(superUser);
            System.out.println("超级管理员用户创建成功: " + superUser.getId());

            // 3. 创建用户凭证（密码）
            saveCredential("SuperAdmin", "123456");
        } else {
            System.out.println("超级管理员用户已存在: " + superUser.getId());
            // 确保角色正确
            if (!superUser.getRoles().contains(superRole)) {
                System.out.println("更新超级管理员用户角色...");
                superUser.setRoles(List.of(superRole));
                superUser = userRepository.save(superUser);
                System.out.println("超级管理员用户角色更新成功");
            }
            // 确保密码正确
            saveCredential("SuperAdmin", "123456");
        }

        System.out.println("超级管理员账号初始化完成！");
        System.out.println("账号: SuperAdmin");
        System.out.println("密码: 123456");
    }

    private void saveCredential(String employeeId, String password) {
        UserCredential existing = userCredentialRepository.findByEmployeeId(employeeId);
        if (existing == null) {
            System.out.println("创建用户凭证...");
            UserCredential credential = new UserCredential(employeeId, password);
            userCredentialRepository.save(credential);
            System.out.println("用户凭证创建成功");
        } else {
            System.out.println("用户凭证已存在，检查密码...");
            if (!password.equals(existing.getPassword())) {
                System.out.println("更新用户密码...");
                existing.setPassword(password);
                userCredentialRepository.save(existing);
                System.out.println("用户密码更新成功");
            } else {
                System.out.println("用户密码正确，无需更新");
            }
        }
    }

    public static void main(String[] args) {
        // 测试方法
        System.out.println("超级管理员初始化工具");
        System.out.println("使用方法: 在Spring Boot应用启动后通过依赖注入使用");
    }
}
