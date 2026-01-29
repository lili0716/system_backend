package com.artdesign.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class RouteController {

    // 获取菜单列表接口
    @GetMapping("/routes")
    public Map<String, Object> getRoutes() {
        System.out.println("Get routes request received");
        
        // 构建菜单列表
        List<Map<String, Object>> routes = new ArrayList<>();
        
        // 1. 组织架构菜单
        Map<String, Object> organizationRoute = new HashMap<>();
        organizationRoute.put("path", "/organization");
        organizationRoute.put("name", "Organization");
        organizationRoute.put("component", "/index/index");
        
        Map<String, Object> organizationMeta = new HashMap<>();
        organizationMeta.put("title", "组织架构");
        organizationMeta.put("icon", "ri:building-line");
        organizationMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        organizationRoute.put("meta", organizationMeta);
        
        List<Map<String, Object>> organizationChildren = new ArrayList<>();
        
        // 部门管理
        Map<String, Object> departmentRoute = new HashMap<>();
        departmentRoute.put("path", "department");
        departmentRoute.put("name", "Department");
        departmentRoute.put("component", "/system/department");
        
        Map<String, Object> departmentMeta = new HashMap<>();
        departmentMeta.put("title", "部门管理");
        departmentMeta.put("keepAlive", true);
        departmentMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        departmentRoute.put("meta", departmentMeta);
        organizationChildren.add(departmentRoute);
        
        // 职位管理
        Map<String, Object> positionRoute = new HashMap<>();
        positionRoute.put("path", "position");
        positionRoute.put("name", "Position");
        positionRoute.put("component", "/system/position");
        
        Map<String, Object> positionMeta = new HashMap<>();
        positionMeta.put("title", "职位管理");
        positionMeta.put("keepAlive", true);
        positionMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        positionRoute.put("meta", positionMeta);
        organizationChildren.add(positionRoute);
        
        organizationRoute.put("children", organizationChildren);
        routes.add(organizationRoute);
        
        // 2. 系统管理菜单
        Map<String, Object> systemRoute = new HashMap<>();
        systemRoute.put("path", "/system");
        systemRoute.put("name", "System");
        systemRoute.put("component", "/index/index");
        
        Map<String, Object> systemMeta = new HashMap<>();
        systemMeta.put("title", "系统管理");
        systemMeta.put("icon", "ri:settings-2-line");
        systemMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        systemRoute.put("meta", systemMeta);
        
        List<Map<String, Object>> systemChildren = new ArrayList<>();
        
        // 用户管理
        Map<String, Object> userRoute = new HashMap<>();
        userRoute.put("path", "user");
        userRoute.put("name", "User");
        userRoute.put("component", "/system/user");
        
        Map<String, Object> userMeta = new HashMap<>();
        userMeta.put("title", "用户管理");
        userMeta.put("keepAlive", true);
        userMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        userRoute.put("meta", userMeta);
        systemChildren.add(userRoute);
        
        // 角色管理
        Map<String, Object> roleRoute = new HashMap<>();
        roleRoute.put("path", "role");
        roleRoute.put("name", "Role");
        roleRoute.put("component", "/system/role");
        
        Map<String, Object> roleMeta = new HashMap<>();
        roleMeta.put("title", "角色管理");
        roleMeta.put("keepAlive", true);
        roleMeta.put("roles", List.of("R_SUPER"));
        roleRoute.put("meta", roleMeta);
        systemChildren.add(roleRoute);
        
        // 菜单管理
        Map<String, Object> menuRoute = new HashMap<>();
        menuRoute.put("path", "menu");
        menuRoute.put("name", "Menus");
        menuRoute.put("component", "/system/menu");
        
        Map<String, Object> menuMeta = new HashMap<>();
        menuMeta.put("title", "菜单管理");
        menuMeta.put("keepAlive", true);
        menuMeta.put("roles", List.of("R_SUPER"));
        menuRoute.put("meta", menuMeta);
        systemChildren.add(menuRoute);
        
        systemRoute.put("children", systemChildren);
        routes.add(systemRoute);
        
        // 3. 表单管理菜单
        Map<String, Object> formRoute = new HashMap<>();
        formRoute.put("path", "/form");
        formRoute.put("name", "Form");
        formRoute.put("component", "/index/index");
        
        Map<String, Object> formMeta = new HashMap<>();
        formMeta.put("title", "表单管理");
        formMeta.put("icon", "ri:file-list-line");
        formMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        formRoute.put("meta", formMeta);
        
        List<Map<String, Object>> formChildren = new ArrayList<>();
        
        // 补打卡
        Map<String, Object> punchCardRoute = new HashMap<>();
        punchCardRoute.put("path", "punch-card");
        punchCardRoute.put("name", "PunchCard");
        punchCardRoute.put("component", "/attendance/form_application");
        
        Map<String, Object> punchCardMeta = new HashMap<>();
        punchCardMeta.put("title", "补打卡");
        punchCardMeta.put("keepAlive", true);
        punchCardMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        punchCardRoute.put("meta", punchCardMeta);
        formChildren.add(punchCardRoute);
        
        // 出差
        Map<String, Object> businessTripRoute = new HashMap<>();
        businessTripRoute.put("path", "business-trip");
        businessTripRoute.put("name", "BusinessTrip");
        businessTripRoute.put("component", "/attendance/form_application");
        
        Map<String, Object> businessTripMeta = new HashMap<>();
        businessTripMeta.put("title", "出差");
        businessTripMeta.put("keepAlive", true);
        businessTripMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        businessTripRoute.put("meta", businessTripMeta);
        formChildren.add(businessTripRoute);
        
        // 外勤
        Map<String, Object> fieldWorkRoute = new HashMap<>();
        fieldWorkRoute.put("path", "field-work");
        fieldWorkRoute.put("name", "FieldWork");
        fieldWorkRoute.put("component", "/attendance/form_application");
        
        Map<String, Object> fieldWorkMeta = new HashMap<>();
        fieldWorkMeta.put("title", "外勤");
        fieldWorkMeta.put("keepAlive", true);
        fieldWorkMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        fieldWorkRoute.put("meta", fieldWorkMeta);
        formChildren.add(fieldWorkRoute);
        
        // 请假
        Map<String, Object> leaveRoute = new HashMap<>();
        leaveRoute.put("path", "leave");
        leaveRoute.put("name", "Leave");
        leaveRoute.put("component", "/attendance/form_application");
        
        Map<String, Object> leaveMeta = new HashMap<>();
        leaveMeta.put("title", "请假");
        leaveMeta.put("keepAlive", true);
        leaveMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        leaveRoute.put("meta", leaveMeta);
        formChildren.add(leaveRoute);
        
        formRoute.put("children", formChildren);
        routes.add(formRoute);
        
        // 4. 考勤管理菜单
        Map<String, Object> attendanceRoute = new HashMap<>();
        attendanceRoute.put("path", "/attendance");
        attendanceRoute.put("name", "Attendance");
        attendanceRoute.put("component", "/index/index");
        
        Map<String, Object> attendanceMeta = new HashMap<>();
        attendanceMeta.put("title", "考勤管理");
        attendanceMeta.put("icon", "ri:time-line");
        attendanceMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        attendanceRoute.put("meta", attendanceMeta);
        
        List<Map<String, Object>> attendanceChildren = new ArrayList<>();
        
        // 考勤记录
        Map<String, Object> recordRoute = new HashMap<>();
        recordRoute.put("path", "record");
        recordRoute.put("name", "AttendanceRecord");
        recordRoute.put("component", "/attendance/record");
        
        Map<String, Object> recordMeta = new HashMap<>();
        recordMeta.put("title", "考勤记录");
        recordMeta.put("keepAlive", true);
        recordMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        recordRoute.put("meta", recordMeta);
        attendanceChildren.add(recordRoute);
        
        // 考勤查询
        Map<String, Object> queryRoute = new HashMap<>();
        queryRoute.put("path", "query");
        queryRoute.put("name", "AttendanceQuery");
        queryRoute.put("component", "/attendance/query");
        
        Map<String, Object> queryMeta = new HashMap<>();
        queryMeta.put("title", "考勤查询");
        queryMeta.put("keepAlive", true);
        queryMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        queryRoute.put("meta", queryMeta);
        attendanceChildren.add(queryRoute);
        
        // 考勤规则
        Map<String, Object> ruleRoute = new HashMap<>();
        ruleRoute.put("path", "rule");
        ruleRoute.put("name", "AttendanceRule");
        ruleRoute.put("component", "/attendance/rule");
        
        Map<String, Object> ruleMeta = new HashMap<>();
        ruleMeta.put("title", "考勤规则");
        ruleMeta.put("keepAlive", true);
        ruleMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        ruleRoute.put("meta", ruleMeta);
        attendanceChildren.add(ruleRoute);
        
        attendanceRoute.put("children", attendanceChildren);
        routes.add(attendanceRoute);
        
        // 构建响应结果
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", routes);
        
        return result;
    }

}
