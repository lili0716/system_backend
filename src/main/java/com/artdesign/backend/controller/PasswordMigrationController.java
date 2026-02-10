package com.artdesign.backend.controller;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.entity.UserCredential;
import com.artdesign.backend.repository.UserCredentialRepository;
import com.artdesign.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一次性密码迁移控制器
 * 将 users 表中的旧密码迁移到 user_credentials 表
 * 迁移完成后可删除此控制器
 */
@RestController
@RequestMapping("/init")
public class PasswordMigrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @GetMapping("/migrate-passwords")
    public Map<String, Object> migratePasswords() {
        Map<String, Object> result = new HashMap<>();
        int migrated = 0;
        int skipped = 0;

        try {
            List<User> allUsers = userService.findAll();

            for (User user : allUsers) {
                String employeeId = user.getEmployeeId();
                if (employeeId == null || employeeId.isEmpty()) {
                    skipped++;
                    continue;
                }

                // 检查是否已经迁移过
                UserCredential existing = userCredentialRepository.findByEmployeeId(employeeId);
                if (existing != null) {
                    skipped++;
                    continue;
                }

                // 从 users 表获取旧密码
                String password = user.getPassword();
                if (password == null || password.isEmpty()) {
                    // 如果没有密码，设置默认密码 123456
                    password = "123456";
                }

                UserCredential credential = new UserCredential(employeeId, password);
                userCredentialRepository.save(credential);
                migrated++;
            }

            result.put("code", 200);
            result.put("msg", "密码迁移完成");
            result.put("migrated", migrated);
            result.put("skipped", skipped);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "迁移失败: " + e.getMessage());
        }

        return result;
    }
}
