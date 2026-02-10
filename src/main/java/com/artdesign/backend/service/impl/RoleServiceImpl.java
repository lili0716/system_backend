package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Role;
import com.artdesign.backend.entity.Permission;
import com.artdesign.backend.repository.RoleRepository;
import com.artdesign.backend.repository.PermissionRepository;
import com.artdesign.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private com.artdesign.backend.repository.RouteRepository routeRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getRoleList(Map<String, Object> params) {
        // 获取分页参数
        int current = params.getOrDefault("current", 1) instanceof Number ? ((Number) params.get("current")).intValue()
                : 1;
        int size = params.getOrDefault("size", 10) instanceof Number ? ((Number) params.get("size")).intValue() : 10;

        // 创建分页对象
        Pageable pageable = PageRequest.of(current - 1, size);

        // 执行分页查询
        Page<Role> rolePage = roleRepository.findAll(pageable);

        // 构建响应结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", rolePage.getContent());
        result.put("current", current);
        result.put("size", size);
        result.put("total", rolePage.getTotalElements());

        return result;
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        return role != null ? role.getPermissions() : new ArrayList<>();
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null && role.getPermissions() != null) {
            return role.getPermissions().stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }

    @Override
    public List<String> getRoleMenuPermissions(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null)
            return new ArrayList<>();
        String roleCode = role.getRoleCode();
        if (roleCode == null)
            return new ArrayList<>();

        // Fetch all routes
        List<com.artdesign.backend.entity.Route> allRoutes = routeRepository.findAll();
        List<String> allowedRouteNames = new ArrayList<>();

        for (com.artdesign.backend.entity.Route route : allRoutes) {
            if (route.getMeta() != null && route.getMeta().getRoles() != null) {
                if (route.getMeta().getRoles().contains(roleCode)) {
                    allowedRouteNames.add(route.getName());
                }
            }
        }
        return allowedRouteNames;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void assignMenuPermissionsToRole(Long roleId, List<String> routeNames) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }
        String roleCode = role.getRoleCode();

        List<com.artdesign.backend.entity.Route> allRoutes = routeRepository.findAll();
        for (com.artdesign.backend.entity.Route route : allRoutes) {
            if (route.getMeta() == null)
                continue;

            List<String> roles = route.getMeta().getRoles();
            if (roles == null) {
                roles = new ArrayList<>();
                route.getMeta().setRoles(roles);
            }

            boolean shouldHave = routeNames.contains(route.getName());
            boolean currentlyHas = roles.contains(roleCode);

            if (shouldHave && !currentlyHas) {
                roles.add(roleCode);
            } else if (!shouldHave && currentlyHas) {
                roles.remove(roleCode);
            }
        }
        routeRepository.saveAll(allRoutes);
    }

}