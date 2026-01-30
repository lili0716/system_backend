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
        
        // 1. 工作台菜单
        Map<String, Object> dashboardRoute = new HashMap<>();
        dashboardRoute.put("path", "/dashboard");
        dashboardRoute.put("name", "Dashboard");
        dashboardRoute.put("component", "/index/index");
        
        Map<String, Object> dashboardMeta = new HashMap<>();
        dashboardMeta.put("title", "工作台");
        dashboardMeta.put("icon", "ri:pie-chart-line");
        dashboardMeta.put("roles", List.of("R_SUPER", "R_ADMIN"));
        dashboardRoute.put("meta", dashboardMeta);
        
        List<Map<String, Object>> dashboardChildren = new ArrayList<>();
        
        // 控制台
        Map<String, Object> consoleRoute = new HashMap<>();
        consoleRoute.put("path", "console");
        consoleRoute.put("name", "Console");
        consoleRoute.put("component", "/dashboard/console");
        
        Map<String, Object> consoleMeta = new HashMap<>();
        consoleMeta.put("title", "控制台");
        consoleMeta.put("keepAlive", false);
        consoleMeta.put("fixedTab", true);
        consoleRoute.put("meta", consoleMeta);
        dashboardChildren.add(consoleRoute);
        
        dashboardRoute.put("children", dashboardChildren);
        routes.add(dashboardRoute);
        
        // 2. 组织架构菜单
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
        departmentRoute.put("path", "dept");
        departmentRoute.put("name", "Department");
        departmentRoute.put("component", "/system/dept");
        
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
        
        // 3. 系统管理菜单
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
        
        // 表单申请
        Map<String, Object> applicationRoute = new HashMap<>();
        applicationRoute.put("path", "application");
        applicationRoute.put("name", "FormApplication");
        applicationRoute.put("component", "/form/application");
        
        Map<String, Object> applicationMeta = new HashMap<>();
        applicationMeta.put("title", "表单申请");
        applicationMeta.put("keepAlive", true);
        applicationMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER"));
        applicationRoute.put("meta", applicationMeta);
        formChildren.add(applicationRoute);
        
        // 表单审批
        Map<String, Object> approvalRoute = new HashMap<>();
        approvalRoute.put("path", "approval");
        approvalRoute.put("name", "FormApproval");
        approvalRoute.put("component", "/form/approval");
        
        Map<String, Object> approvalMeta = new HashMap<>();
        approvalMeta.put("title", "表单审批");
        approvalMeta.put("keepAlive", true);
        // Only leaders/admin should see this ideally, but handled by frontend visibility often. 
        // Backend role check: R_SUPER, R_ADMIN. For dynamic leaders, we might just expose to all and let frontend hide empty lists?
        // Or if we want strict role control: R_SUPER, R_ADMIN.
        // User request says: "display form approval function based on whether user is leader".
        // The current hardcoded mock uses static roles. Let's add R_USER too, so everyone can access (and see empty list if not leader), 
        // OR better, keep it broad here and let the specific permissions logic handle visibility if we had it.
        // Given current simple role system, adding all so they can access the page, 
        // but the page itself will filter data.
        approvalMeta.put("roles", List.of("R_SUPER", "R_ADMIN", "R_USER")); 
        approvalRoute.put("meta", approvalMeta);
        formChildren.add(approvalRoute);
        
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
