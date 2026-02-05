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

    @org.springframework.beans.factory.annotation.Autowired
    private com.artdesign.backend.service.UserService userService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.artdesign.backend.repository.RouteRepository routeRepository;

    @GetMapping("/routes")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> getRoutes(jakarta.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        String employeeId = null;
        if (token != null && token.contains("mock-token-")) {
            employeeId = token.replace("Bearer ", "").replace("mock-token-", "").trim();
        }

        com.artdesign.backend.entity.User user = null;
        if (employeeId != null) {
            user = userService.findByEmployeeId(employeeId);
        }
        List<com.artdesign.backend.entity.Route> routes = new ArrayList<>();

        if (user != null) {
            // 1. Check if user is Super Admin or Admin
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> "superadmin".equals(r.getRoleCode()) || "admin".equals(r.getRoleCode()));

            if (isAdmin) {
                // Admins see all top-level routes
                routes = routeRepository.findByParentIsNull();
            } else {
                // Non-admins: check department routes
                if (user.getDepartment() != null && user.getDepartment().getRoutes() != null
                        && !user.getDepartment().getRoutes().isEmpty()) {
                    routes = new ArrayList<>(user.getDepartment().getRoutes());
                }

                // If no routes found (or dept has no routes), assign default routes: Dashboard
                // + Form
                if (routes.isEmpty()) {
                    // Find Dashboard
                    com.artdesign.backend.entity.Route dashboard = routeRepository.findAll().stream()
                            .filter(r -> "Dashboard".equals(r.getName()))
                            .findFirst().orElse(null);
                    if (dashboard != null) {
                        routes.add(dashboard);
                    }

                    // Find Form
                    com.artdesign.backend.entity.Route form = routeRepository.findAll().stream()
                            .filter(r -> "Form".equals(r.getName()))
                            .findFirst().orElse(null);
                    if (form != null) {
                        routes.add(form);
                    }
                } else {
                    // Ensure Dashboard is always present even if dept has other routes
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
        } else {
            // User not found
        }

        // Convert to result map
        List<Map<String, Object>> resultRoutes = convertRoutes(routes);

        // Post-processing: Filter specific menus for non-admins (or everyone based on
        // business rule)
        // Rule: "Form Approval" only visible if user is Department Leader
        boolean isDeptLeader = false;
        if (user != null && user.getDepartment() != null) {
            Long userId = user.getId();
            Long leaderId = user.getDepartment().getLeaderId();
            System.out.println("Checking Leader: UserID=" + userId + ", DeptLeaderID=" + leaderId);
            if (leaderId != null) {
                isDeptLeader = userId.equals(leaderId);
            }
            if (!isDeptLeader && user.getPosition() != null) {
                String posCode = user.getPosition().getCode();
                if ("DEPT_MANAGER".equals(posCode) || "GM".equals(posCode)) {
                    isDeptLeader = true;
                    // System.out.println("User is leader by Position Code: " + posCode);
                }
            }
        }
        // System.out.println("Is Dept Leader: " + isDeptLeader);

        // If USER is NOT admin (admins see everything), AND NOT leader, filter Form
        // Approval
        // Actually, requirement says "Only department leader sees Form Approval".
        // Does this apply to Admin too? Usually Admins see all. Let's assume Admin sees
        // all.
        boolean finalIsAdmin = user != null && user.getRoles().stream()
                .anyMatch(r -> "superadmin".equals(r.getRoleCode()) || "admin".equals(r.getRoleCode()));

        // System.out.println("Final Is Admin: " + finalIsAdmin);

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
            map.put("id", r.getId()); // Add ID
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

    private void filterFormApproval(List<Map<String, Object>> routes, boolean isLeader) {
        if (routes == null)
            return;

        for (Map<String, Object> route : routes) {
            String name = (String) route.get("name");
            String path = (String) route.get("path");

            // Check if this is the "Form" menu
            if ("Form".equals(name) || "/form".equals(path)) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) route.get("children");
                if (children != null) {
                    // Remove "Approval" if not leader
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

            // Recursively check children (though Form is top level, good practice)
            List<Map<String, Object>> children = (List<Map<String, Object>>) route.get("children");
            if (children != null) {
                filterFormApproval(children, isLeader);
            }
        }
    }

}
