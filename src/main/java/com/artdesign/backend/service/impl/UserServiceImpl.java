package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            // 创建默认用户
            createDefaultUsers();
            users = userRepository.findAll();
        }
        return users;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getUserList(Map<String, Object> params) {
        // 获取分页参数
        int current = 1;
        int size = 10;
        try {
            if (params.get("current") != null) {
                current = Integer.parseInt(params.get("current").toString());
            }
            if (params.get("size") != null) {
                size = Integer.parseInt(params.get("size").toString());
            }
        } catch (NumberFormatException e) {
            // Use defaults
        }

        // 确保有默认用户数据
        if (userRepository.findAll().isEmpty()) {
            createDefaultUsers();
        }

        // 创建分页对象，使用默认排序
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.ASC, "nickName"));

        // 构建查询条件
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            // 姓名模糊查询
            if (params.get("nickName") != null && !params.get("nickName").toString().isEmpty()) {
                predicates.add(cb.like(root.get("nickName"), "%" + params.get("nickName") + "%"));
            }

            // 工号模糊查询
            if (params.get("employeeId") != null && !params.get("employeeId").toString().isEmpty()) {
                predicates.add(cb.like(root.get("employeeId"), "%" + params.get("employeeId") + "%"));
            }

            // 性别精确查询
            if (params.get("userGender") != null && !params.get("userGender").toString().isEmpty()) {
                predicates.add(cb.equal(root.get("userGender"), params.get("userGender")));
            }

            // 在职时长查询
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);

            if (params.get("tenureMin") != null && !params.get("tenureMin").toString().isEmpty()) {
                try {
                    int minYears = Integer.parseInt(params.get("tenureMin").toString());
                    Calendar minCal = (Calendar) calendar.clone();
                    minCal.add(Calendar.YEAR, -minYears);
                    predicates.add(cb.lessThanOrEqualTo(root.get("hireDate"), minCal.getTime()));
                } catch (NumberFormatException e) {
                    // ignore invalid number
                }
            }

            if (params.get("tenureMax") != null && !params.get("tenureMax").toString().isEmpty()) {
                try {
                    int maxYears = Integer.parseInt(params.get("tenureMax").toString());
                    Calendar maxCal = (Calendar) calendar.clone();
                    maxCal.add(Calendar.YEAR, -maxYears);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("hireDate"), maxCal.getTime()));
                } catch (NumberFormatException e) {
                    // ignore invalid number
                }
            }

            // 默认只查询未删除的或者在职/离职状态
            if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            // 执行分页查询
            Page<User> userPage = userRepository.findAll(spec, pageable);

            // 构建只包含指定字段的用户列表
            List<Map<String, Object>> userList = new java.util.ArrayList<>();
            for (User user : userPage.getContent()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickName", user.getNickName());
                userMap.put("userGender", user.getUserGender());
                userMap.put("email", user.getEmail());
                userMap.put("employeeId", user.getEmployeeId());
                userMap.put("createTime", user.getCreateTime());
                userMap.put("status", user.getStatus());
                userMap.put("hireDate", user.getHireDate());
                userMap.put("leaveDate", user.getLeaveDate());
                userMap.put("updateTime", user.getUpdateTime());
                userMap.put("remark", user.getRemark());
                userMap.put("idCard", user.getIdCard());
                
                // 避免直接关联查询，只返回部门ID和名称
                if (user.getDepartment() != null) {
                    userMap.put("departmentId", user.getDepartment().getId());
                    userMap.put("departmentName", user.getDepartment().getName());
                }

                // 计算并返回tenure (年，保留1位小数)
                if (user.getHireDate() != null) {
                    long diffInMillies = Math.abs(new Date().getTime() - user.getHireDate().getTime());
                    double years = diffInMillies / (1000.0 * 60 * 60 * 24 * 365.25);
                    userMap.put("tenure", String.format("%.1f", years));
                } else {
                    userMap.put("tenure", "0.0");
                }

                userList.add(userMap);
            }

            // 构建响应结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", userList);
            result.put("current", current);
            result.put("size", size);
            result.put("total", userPage.getTotalElements());

            return result;
        } catch (Exception e) {
            // 捕获并记录异常
            e.printStackTrace();
            // 返回错误信息
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("records", new java.util.ArrayList<>());
            errorResult.put("current", current);
            errorResult.put("size", size);
            errorResult.put("total", 0);
            return errorResult;
        }
    }

    // 创建默认用户数据
    private void createDefaultUsers() {
        // 创建默认用户 - 简化版本
        // 创建默认用户 - 简化版本
        User admin = new User();
        admin.setEmployeeId("20950");
        admin.setPassword("123456");
        admin.setEmail("20950@example.com");
        admin.setNickName("超级管理员");
        admin.setUserPhone("13800138000");
        admin.setUserGender("男");
        admin.setStatus("1");
        admin.setAvatar("");
        admin.setCreateBy("system");
        admin.setCreateTime(new java.util.Date());
        admin.setUpdateBy("system");
        admin.setUpdateTime(new java.util.Date());
        userRepository.save(admin);

        User user1 = new User();
        user1.setEmployeeId("20952");
        user1.setPassword("123456");
        user1.setEmail("20952@example.com");
        user1.setNickName("普通用户");
        user1.setUserPhone("13800138002");
        user1.setUserGender("女");
        user1.setStatus("1");
        user1.setAvatar("");
        user1.setCreateBy("system");
        user1.setCreateTime(new java.util.Date());
        user1.setUpdateBy("system");
        user1.setUpdateTime(new java.util.Date());
        userRepository.save(user1);
    }

    @Override
    public Page<User> searchActiveUsers(String keyword, int page, int size) {
        if (userRepository.findAll().isEmpty()) {
            createDefaultUsers();
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        String searchKey = "%" + (keyword == null ? "" : keyword) + "%";
        return userRepository.searchActiveUsers("1", searchKey, pageable);
    }

}