package com.artdesign.backend.controller;

import com.artdesign.backend.util.JwtUtil;
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

    @org.springframework.beans.factory.annotation.Autowired
    private com.artdesign.backend.service.UserService userService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.artdesign.backend.repository.RouteRepository routeRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/routes")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> getRoutes(jakarta.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        // 使用 JWT 解析工号
        String employeeId = null;
        if (token != null) {
            employeeId = jwtUtil.getEmployeeId(token);
        }

        com.artdesign.backend.entity.User user = null;
        if (employeeId != null) {
            user = userService.findByEmployeeId(employeeId);
        }
        List<com.artdesign.backend.entity.Route> routes = new ArrayList<>();

        if (user != null) {
            // 动态判断是否为管理员：查询用户角色的 isAdmin 字段
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> Boolean.TRUE.equals(r.getIsAdmin()));

            if (isAdmin) {
                // 管理员看到所有顶级路由
                try (java.io.PrintWriter out = new java.io.PrintWriter("debug_routes.txt")) {
                    out.println("User " + employeeId + " is Admin. Fetching all top-level routes.");
                    routes = routeRepository.findByParentIsNull();
                    out.println("Found top-level routes: " + routes.size());
                    for (com.artdesign.backend.entity.Route r : routes) {
                        out.println("Route: " + r.getName() + ", Path: " + r.getPath() + ", Roles: "
                                + (r.getMeta() != null ? r.getMeta().getRoles() : "null"));
                        if (r.getChildren() != null) {
                            out.println("  Children of " + r.getName() + ": " + r.getChildren().size());
                            for (com.artdesign.backend.entity.Route c : r.getChildren()) {
                                out.println("    - " + c.getName() + " (" + c.getPath() + "), Roles: "
                                        + (c.getMeta() != null ? c.getMeta().getRoles() : "null"));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 非管理员：查看部门路由
                if (user.getDepartment() != null && user.getDepartment().getRoutes() != null
                    && !user.getDepartment().getRoutes().isEmpty()) {
                    // 只获取一级菜单（父菜单为 null 的路由）
                    // 一级菜单会自动包含它们的子菜单
                    List<com.artdesign.backend.entity.Route> allDeptRoutes = new ArrayList<>(user.getDepartment().getRoutes());
                    // 过滤出一级菜单
                    routes = allDeptRoutes.stream()
                            .filter(route -> route.getParent() == null)
                            .collect(java.util.stream.Collectors.toList());
                }

                // 如果没有路由（或部门没有配置路由），分配默认路由：Dashboard + Form
                if (routes.isEmpty()) {
                    com.artdesign.backend.entity.Route dashboard = routeRepository.findAll().stream()
                            .filter(r -> "Dashboard".equals(r.getName()))
                            .findFirst().orElse(null);
                    if (dashboard != null) {
                        routes.add(dashboard);
                    }

                    com.artdesign.backend.entity.Route form = routeRepository.findAll().stream()
                            .filter(r -> "Form".equals(r.getName()))
                            .findFirst().orElse(null);
                    if (form != null) {
                        routes.add(form);
                    }
                } else {
                    // 确保 Dashboard 始终存在
                    boolean hasDashboard = routes.stream().anyMatch(r -> "Dashboard".equals(r.getName()));
                    if (!hasDashboard) {
                        com.artdesign.backend.entity.Route dash = routeRepository.findAll().stream()
                                .filter(r -> "Dashboard".equals(r.getName()))
                                .findFirst().orElse(null);
                        if (dash != null)
                            routes.add(0, dash);
                    }
                }
            }
        }

        // 转换为结果 map
        List<Map<String, Object>> resultRoutes = convertRoutes(routes);

        // 后处理：非管理员过滤表单审批菜单
        boolean isDeptLeader = false;
        if (user != null && user.getDepartment() != null) {
            Long userId = user.getId();
            Long leaderId = user.getDepartment().getLeaderId();
            if (leaderId != null) {
                isDeptLeader = userId.equals(leaderId);
            }
            if (!isDeptLeader && user.getPosition() != null) {
                String posCode = user.getPosition().getCode();
                if ("DEPT_MANAGER".equals(posCode) || "GM".equals(posCode)) {
                    isDeptLeader = true;
                }
            }
        }

        // 动态判断管理员
        boolean finalIsAdmin = user != null && user.getRoles().stream()
                .anyMatch(r -> Boolean.TRUE.equals(r.getIsAdmin()));

        if (!finalIsAdmin) {
            filterFormApproval(resultRoutes, isDeptLeader);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", resultRoutes);

        return result;
    }

    private List<Map<String, Object>> convertRoutes(List<com.artdesign.backend.entity.Route> sourceRoutes) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (sourceRoutes == null)
            return list;

        // 排序
        sourceRoutes.sort((r1, r2) -> {
            int sort1 = (r1.getMeta() != null && r1.getMeta().getSort() != null) ? r1.getMeta().getSort() : 0;
            int sort2 = (r2.getMeta() != null && r2.getMeta().getSort() != null) ? r2.getMeta().getSort() : 0;
            return Integer.compare(sort1, sort2);
        });

        for (com.artdesign.backend.entity.Route r : sourceRoutes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("path", r.getPath());
            map.put("name", r.getName());
            map.put("component", r.getComponent());

            if (r.getMeta() != null) {
                Map<String, Object> meta = new HashMap<>();
                meta.put("title", r.getMeta().getTitle());
                meta.put("icon", r.getMeta().getIcon());
                meta.put("keepAlive", r.getMeta().getKeepAlive());
                meta.put("roles", r.getMeta().getRoles());
                meta.put("fixedTab", r.getMeta().getFixedTab());
                meta.put("sort", r.getMeta().getSort()); // 顺便把 sort 也返回给前端，方便前端调试或使用
                map.put("meta", meta);
            }

            if (r.getChildren() != null && !r.getChildren().isEmpty()) {
                map.put("children", convertRoutes(r.getChildren()));
            }

            list.add(map);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void filterFormApproval(List<Map<String, Object>> routes, boolean isLeader) {
        if (routes == null)
            return;

        for (Map<String, Object> route : routes) {
            String name = (String) route.get("name");
            String path = (String) route.get("path");

            if ("Form".equals(name) || "/form".equals(path)) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) route.get("children");
                if (children != null) {
                    if (!isLeader) {
                        children.removeIf(child -> {
                            String cName = (String) child.get("name");
                            String cPath = (String) child.get("path");
                            return "FormApproval".equals(cName) || "approval".equals(cPath)
                                    || "/form/approval".equals(cPath);
                        });
                    }
                }
            }

            List<Map<String, Object>> children = (List<Map<String, Object>>) route.get("children");
            if (children != null) {
                filterFormApproval(children, isLeader);
            }
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/routes")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> createRoute(
            @org.springframework.web.bind.annotation.RequestBody com.artdesign.backend.entity.Route route) {
        // 如果有 parentId，设置 parent
        if (route.getParent() != null && route.getParent().getId() != null) {
            com.artdesign.backend.entity.Route parent = routeRepository.findById(route.getParent().getId())
                    .orElse(null);
            route.setParent(parent);
        } else {
            route.setParent(null);
        }

        com.artdesign.backend.entity.Route savedRoute = routeRepository.save(route);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", savedRoute);
        return result;
    }

    @org.springframework.web.bind.annotation.PutMapping("/routes/{id}")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> updateRoute(@org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody com.artdesign.backend.entity.Route routeDetails) {
        com.artdesign.backend.entity.Route route = routeRepository.findById(id).orElse(null);
        if (route != null) {
            route.setName(routeDetails.getName());
            route.setPath(routeDetails.getPath());
            route.setComponent(routeDetails.getComponent());
            route.setMeta(routeDetails.getMeta());

            // 更新 parent (仅当请求显式包含 parent 时更新，避免丢失原有层级)
            if (routeDetails.getParent() != null) {
                if (routeDetails.getParent().getId() != null) {
                    com.artdesign.backend.entity.Route parent = routeRepository
                            .findById(routeDetails.getParent().getId()).orElse(null);
                    route.setParent(parent);
                } else {
                    // 如果传了一个空的 parent 对象，视为移动到根节点?
                    // 或者我们约定 parent: { id: null } 表示移动到根？
                    // 暂时保留原状，如果仅仅是 metadata update，通常 JSON 不反序列化 parent 字段，则 getParent 为 null
                    // 如果 explicit null, Jackson sets it to null?
                    // 这里我们假设如果不传 parent，则不修改 parent。
                    // 只有当 routeDetails.getParent() 不为 null 才处理。
                    // 如果想置空 parent (移动到根)，前端需要传 parent: { id: null } ?
                    // Route 里的 parent 是对象。
                    // 简单起见：如果 id 不存在，视为根节点。
                    route.setParent(null);
                }
            }

            routeRepository.save(route);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/routes/{id}")
    public Map<String, Object> deleteRoute(@org.springframework.web.bind.annotation.PathVariable Long id) {
        routeRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }
}
