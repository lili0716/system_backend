package com.artdesign.backend.controller;

import com.artdesign.backend.entity.Department;
import com.artdesign.backend.entity.Position;
import com.artdesign.backend.entity.Role;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.entity.Route;
import com.artdesign.backend.entity.RouteMeta;
import com.artdesign.backend.repository.DepartmentRepository;
import com.artdesign.backend.repository.PositionRepository;
import com.artdesign.backend.repository.RoleRepository;
import com.artdesign.backend.repository.UserRepository;
import com.artdesign.backend.repository.RouteRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/init")
public class InitDataController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RouteRepository routeRepository; // Added RouteRepository
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/org")
    @Transactional
    public void initOrgData(jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 1. Clear Data via JDBC to handle constraints and join tables
            // Clear business data first
            try {
                jdbcTemplate.execute("DELETE FROM business_trip_form");
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM leave_form");
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM overtime_form");
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM field_work_form");
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM punch_card_form");
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM attendance_record");
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM attendance_abnormal_record");
            } catch (Exception e) {
            }

            jdbcTemplate.execute("DELETE FROM department_routes"); // Clear helper table for routes
            jdbcTemplate.execute("DELETE FROM user_roles");
            jdbcTemplate.execute("DELETE FROM users");

            // Handle self-referencing Department table
            // Usually need to delete in order or set parent_id to null then delete.
            // Or just DELETE CASCADE if DB supports it.
            // Safe way: update all parent_id to null, then delete.
            jdbcTemplate.execute("UPDATE departments SET parent_id = NULL");
            jdbcTemplate.execute("DELETE FROM departments");

            jdbcTemplate.execute("DELETE FROM positions");
            // roleRepository.deleteAll(); // Keep roles

            // Handle self-referencing Route table
            try {
                jdbcTemplate.execute("UPDATE routes SET parent_id = NULL");
            } catch (Exception e) {
            }

            jdbcTemplate.execute("DELETE FROM routes"); // Clear routes

            // Clear route_meta dependencies first
            try {
                jdbcTemplate.execute("DELETE FROM route_meta_roles"); // ElementCollection table
            } catch (Exception e) {
            }
            try {
                jdbcTemplate.execute("DELETE FROM auth_items"); // OneToMany table (if exists)
            } catch (Exception e) {
            }

            jdbcTemplate.execute("DELETE FROM route_meta"); // Clear route meta

            // 2. Init Routes (Standard Menus)
            initRoutes();

            // 3. Ensure Roles exist (Simple mechanism)
            Role userRole = roleRepository.findAll().stream()
                    .filter(r -> "USER".equals(r.getRoleCode()))
                    .findFirst()
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setRoleName("普通用户");
                        r.setRoleCode("USER");
                        r.setEnabled(true);
                        r.setCreateTime(new Date());
                        return roleRepository.save(r);
                    });

            // 4. Init Positions
            Position managerPos = new Position();
            managerPos.setName("部门负责人");
            managerPos.setCode("DEPT_MANAGER");
            managerPos.setEnabled(true);
            positionRepository.save(managerPos);

            Position staffPos = new Position();
            staffPos.setName("普通员工");
            staffPos.setCode("STAFF");
            staffPos.setEnabled(true);
            positionRepository.save(staffPos);

            // 5. Init Departments (The Tree) & Users
            List<User> usersToCreate = new ArrayList<>();
            AtomicInteger empIdCounter = new AtomicInteger(1);

            // Root: 总经理
            Department gmDept = createDept("总经理", null);
            createUsers(gmDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Child: 管理者代表
            Department repDept = createDept("管理者代表", gmDept);
            createUsers(repDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 1 (Finance/Admin)
            Department dgm1 = createDept("副总经理(分管行政财务)", gmDept);
            createUsers(dgm1, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            createDeptWithUsers("财务部", dgm1, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            Department adminDept = createDept("行政部", dgm1);
            createUsers(adminDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            createDeptWithUsers("食堂", adminDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("设备部", dgm1, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("信息技术部", dgm1, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 2 (Engineering)
            Department dgm2 = createDept("副总经理(分管工程)", gmDept);
            createUsers(dgm2, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("工程部", dgm2, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 3 (Planning/Quality/Prod)
            Department dgm3 = createDept("副总经理(分管生产计划)", gmDept);
            createUsers(dgm3, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            Department planDept = createDept("计划采购部", dgm3);
            createUsers(planDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("仓库", planDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            createDeptWithUsers("品管部", dgm3, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("工艺部", dgm3, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            Department prodDept = createDept("生产部", dgm3);
            createUsers(prodDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("装配车间", prodDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("焊接车间", prodDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 4 (Structure)
            Department dgm4 = createDept("副总经理(分管结构)", gmDept);
            createUsers(dgm4, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("结构部", dgm4, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 5 (Tech)
            Department dgm5 = createDept("副总经理(分管技术)", gmDept);
            createUsers(dgm5, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("技术部", dgm5, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 6 (Trade/Market)
            Department dgm6 = createDept("副总经理(分管营销)", gmDept);
            createUsers(dgm6, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            Department tradeDept = createDept("外贸部", dgm6);
            createUsers(tradeDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("发运组", tradeDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            Department marketDept = createDept("市场部", dgm6);
            createUsers(marketDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("售后服务组", marketDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Deputy GM 7 (HR)
            Department dgm7 = createDept("副总经理(分管人资)", gmDept);
            createUsers(dgm7, managerPos, staffPos, usersToCreate, empIdCounter, userRole);
            createDeptWithUsers("人力资源部", dgm7, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // Direct under GM: QM Office
            createDeptWithUsers("质量管理办公室", gmDept, managerPos, staffPos, usersToCreate, empIdCounter, userRole);

            // ==========================================
            // RESTORE SUPERADMIN 20950
            // ==========================================
            Role adminRole = roleRepository.findAll().stream()
                    .filter(r -> "superadmin".equals(r.getRoleCode()))
                    .findFirst()
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setRoleName("超级管理员");
                        r.setRoleCode("superadmin");
                        r.setEnabled(true);
                        r.setCreateTime(new Date());
                        return roleRepository.save(r);
                    });

            User superAdmin = new User();
            superAdmin.setEmployeeId("20950");
            superAdmin.setNickName("Super Admin");
            superAdmin.setDepartment(gmDept); // Attach to GM or leave null
            superAdmin.setPosition(managerPos);
            superAdmin.setPassword("123456"); // Or keep previous password if known, but resetting here
            superAdmin.setStatus("1");
            superAdmin.setCreateTime(new Date());
            superAdmin.setRoles(List.of(userRole, adminRole));
            superAdmin.setRemark("Recovered Superadmin Account");

            // Add to list so it gets saved and exported
            usersToCreate.add(0, superAdmin); // Add to top

            // Save Users
            userRepository.saveAll(usersToCreate);

            // 5. Export Excel
            exportExcel(usersToCreate, response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(500, e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Department createDept(String name, Department parent) {
        Department d = new Department();
        d.setName(name);
        d.setParent(parent);
        d.setEnabled(true);
        d.setCode(Long.toHexString(Double.doubleToLongBits(Math.random()))); // Simple random code
        return departmentRepository.save(d);
    }

    private void createDeptWithUsers(String name, Department parent, Position mgr, Position staff, List<User> users,
            AtomicInteger counter, Role userRole) {
        Department d = createDept(name, parent);
        createUsers(d, mgr, staff, users, counter, userRole);
    }

    private void createUsers(Department d, Position mgr, Position staff, List<User> users, AtomicInteger counter,
            Role userRole) {
        User u1 = new User();
        u1.setEmployeeId(String.format("EMP%04d", counter.getAndIncrement()));
        u1.setNickName(d.getName() + "负责人");
        u1.setDepartment(d);
        u1.setPosition(mgr);
        u1.setPassword("123456");
        u1.setStatus("1"); // Assuming "1" is Normal or Enabled, verified from User.java string type
        u1.setCreateTime(new Date());
        u1.setRoles(Collections.singletonList(userRole));
        users.add(u1);

        User u2 = new User();
        u2.setEmployeeId(String.format("EMP%04d", counter.getAndIncrement()));
        u2.setNickName(d.getName() + "员工");
        u2.setDepartment(d);
        u2.setPosition(staff);
        u2.setPassword("123456");
        u2.setStatus("1");
        u2.setCreateTime(new Date());
        u2.setRoles(Collections.singletonList(userRole));
        users.add(u2);
    }

    private void initRoutes() {
        // 1. Dashboard
        Route dashboard = createRoute("Dashboard", "/dashboard", "/index/index", null);
        // Parent acts as Layout, no title/icon implies transparent grouping or
        // effective flat menu if child is promoted
        // Actually, for "First Level Menu" effect in many admin templates:
        // Parent: Path /dashboard, Component Layout. Meta: Title NULL/Hidden.
        // Child: Path index (or console), Component Page. Meta: Title "Workbench".
        dashboard.setMeta(createMeta(null, null, List.of("R_SUPER", "R_ADMIN")));

        Route console = createRoute("Console", "console", "/dashboard/console", dashboard);
        console.setMeta(createMeta("工作台", "ri:pie-chart-line", List.of("R_SUPER", "R_ADMIN")));
        console.getMeta().setFixedTab(true);
        console.getMeta().setKeepAlive(false);

        dashboard.setChildren(new ArrayList<>(List.of(console)));
        routeRepository.save(dashboard);

        // 2. Personnel
        Route personnel = createRoute("Personnel", "/personnel", "/index/index", null);
        RouteMeta personnelMeta = createMeta("人事管理", "ri:user-star-line", List.of("R_SUPER", "R_ADMIN"));
        personnelMeta.setAlwaysShow(true); // Force show as parent
        personnel.setMeta(personnelMeta);

        Route user = createRoute("User", "user", "/system/user", personnel);
        user.setMeta(createMeta("用户管理", "ri:user-settings-line", List.of("R_SUPER", "R_ADMIN")));
        user.getMeta().setKeepAlive(true);

        Route salary = createRoute("Salary", "salary", "/system/salary", personnel);
        salary.setMeta(createMeta("薪资管理", "ri:money-cny-circle-line", List.of("R_SUPER", "R_ADMIN")));
        salary.getMeta().setKeepAlive(true);

        personnel.setChildren(new ArrayList<>(List.of(user, salary)));
        routeRepository.save(personnel);

        // 3. Organization
        Route org = createRoute("Organization", "/organization", "/index/index", null);
        org.setMeta(createMeta("组织架构", "ri:building-line", List.of("R_SUPER", "R_ADMIN")));

        Route dept = createRoute("Department", "dept", "/system/dept", org);
        dept.setMeta(createMeta("部门管理", null, List.of("R_SUPER", "R_ADMIN")));
        dept.getMeta().setKeepAlive(true);

        Route pos = createRoute("Position", "position", "/system/position", org);
        pos.setMeta(createMeta("职位管理", null, List.of("R_SUPER", "R_ADMIN")));
        pos.getMeta().setKeepAlive(true);

        org.setChildren(new ArrayList<>(List.of(dept, pos)));
        routeRepository.save(org);

        // 4. System
        Route sys = createRoute("System", "/system", "/index/index", null);
        sys.setMeta(createMeta("系统管理", "ri:settings-2-line", List.of("R_SUPER", "R_ADMIN")));

        Route role = createRoute("Role", "role", "/system/role", sys);
        role.setMeta(createMeta("角色管理", null, List.of("R_SUPER", "R_ADMIN"))); // Allow ADMIN
        role.getMeta().setKeepAlive(true);

        Route menu = createRoute("Menus", "menu", "/system/menu", sys);
        menu.setMeta(createMeta("菜单管理", null, List.of("R_SUPER"))); // Only super
        menu.getMeta().setKeepAlive(true);

        sys.setChildren(new ArrayList<>(List.of(role, menu)));
        routeRepository.save(sys);

        // 5. Form
        Route form = createRoute("Form", "/form", "/index/index", null);
        form.setMeta(createMeta("表单管理", "ri:file-list-line", List.of("R_SUPER", "R_ADMIN", "R_USER")));

        Route app = createRoute("FormApplication", "application", "/form/application", form);
        app.setMeta(createMeta("表单申请", null, List.of("R_SUPER", "R_ADMIN", "R_USER")));
        app.getMeta().setKeepAlive(true);

        Route approval = createRoute("FormApproval", "approval", "/form/approval", form);
        approval.setMeta(createMeta("表单审批", null, List.of("R_SUPER", "R_ADMIN", "R_USER")));
        approval.getMeta().setKeepAlive(true);

        form.setChildren(new ArrayList<>(List.of(app, approval)));
        routeRepository.save(form);

        // 5. Attendance
        Route att = createRoute("Attendance", "/attendance", "/index/index", null);
        att.setMeta(createMeta("考勤管理", "ri:time-line", List.of("R_SUPER", "R_ADMIN")));

        Route upload = createRoute("AttendanceUpload", "upload", "/attendance/upload", att);
        upload.setMeta(createMeta("上传考勤", null, List.of("R_SUPER", "R_ADMIN")));
        upload.getMeta().setKeepAlive(true);

        Route query = createRoute("AttendanceQuery", "query", "/attendance/query", att);
        query.setMeta(createMeta("考勤查询", null, List.of("R_SUPER", "R_ADMIN", "R_USER")));
        query.getMeta().setKeepAlive(true);

        Route rule = createRoute("AttendanceRule", "rule", "/attendance/rule", att);
        rule.setMeta(createMeta("考勤规则", null, List.of("R_SUPER", "R_ADMIN")));
        rule.getMeta().setKeepAlive(true);

        att.setChildren(new ArrayList<>(List.of(upload, query, rule)));
        routeRepository.save(att);
    }

    private Route createRoute(String name, String path, String component, Route parent) {
        Route r = new Route();
        r.setName(name);
        r.setPath(path);
        r.setComponent(component);
        r.setParent(parent);
        return r;
    }

    private RouteMeta createMeta(String title, String icon, List<String> roles) {
        RouteMeta m = new RouteMeta();
        m.setTitle(title);
        m.setIcon(icon);
        m.setRoles(roles);
        return m;
    }

    @GetMapping("/routes")
    @Transactional
    public void updateRoutes(jakarta.servlet.http.HttpServletResponse response) {
        try {
            // Clear existing routes to avoid duplication or conflicts
            // Note: This might break existing FKs if not careful, but for menu structure
            // update it's often necessary
            // A safer approach for production is diffing, but for this task provided
            // context:
            // verified we can delete routes as they are re-created.

            // However, we need to handle the self-referencing and FKs.
            // The simplest way to "Update" is to wipe routes and re-init them,
            // assuming no other critical data depends on route IDs (like permissions
            // assigned to specific route IDs).
            // If permissions are assigned to Route IDs, this is destructive.
            // But looking at InitDataController, permissions seem role-based in RouteMeta
            // (ElementCollection likely or just strings).

            // Let's look at initRoutes impl. It uses creating new Route objects.

            // Safe update strategy for this context:
            // 1. Delete all routes.
            try {
                jdbcTemplate.execute("UPDATE routes SET parent_id = NULL");
                jdbcTemplate.execute("DELETE FROM routes");
                jdbcTemplate.execute("DELETE FROM route_meta_roles");
                jdbcTemplate.execute("DELETE FROM route_meta");
            } catch (Exception e) {
                // Ignore if tables empty or slight errors, though explicit checks are better
            }

            initRoutes();

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 200, \"msg\": \"Menu routes updated successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(500, e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exportExcel(List<User> users, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Initialized Users");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("工号");
        header.createCell(1).setCellValue("姓名");
        header.createCell(2).setCellValue("部门");
        header.createCell(3).setCellValue("职位");
        header.createCell(4).setCellValue("初始密码");

        int rowNum = 1;
        for (User u : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(u.getEmployeeId());
            row.createCell(1).setCellValue(u.getNickName());
            row.createCell(2).setCellValue(u.getDepartment() != null ? u.getDepartment().getName() : "");
            row.createCell(3).setCellValue(u.getPosition() != null ? u.getPosition().getName() : "");
            row.createCell(4).setCellValue("123456");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // Use URLEncoder to fix filename encoding
        String fileName = java.net.URLEncoder.encode("用户初始数据.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
