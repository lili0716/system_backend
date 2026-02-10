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
                routes = routeRepository.findByParentIsNull();
            } else {
                // 非管理员：查看部门路由
                if (user.getDepartment() != null && user.getDepartment().getRoutes() != null
                        && !user.getDepartment().getRoutes().isEmpty()) {
                    routes = new ArrayList<>(user.getDepartment().getRoutes());
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

}
