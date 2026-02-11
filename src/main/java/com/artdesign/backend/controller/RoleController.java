package com.artdesign.backend.controller;

import com.artdesign.backend.entity.Role;
import com.artdesign.backend.service.RoleService;
import com.artdesign.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private com.artdesign.backend.service.UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取所有角色
    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.findAll();
    }

    // 根据ID获取角色
    @GetMapping("/{id}")
    public Role getRoleById(@PathVariable Long id) {
        return roleService.findById(id);
    }

    // 新增角色
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleService.save(role);
    }

    // 修改角色
    @PutMapping("/{id}")
    public Role updateRole(@PathVariable Long id, @RequestBody Role role,
            @RequestHeader(value = "Authorization", required = false) String token) {
        checkPermission(id, token);
        role.setRoleId(id);
        return roleService.save(role);
    }

    // 删除角色
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        checkPermission(id, token);
        roleService.deleteById(id);
    }

    // 为角色分配权限
    @PostMapping("/{roleId}/permissions")
    public void assignPermissionsToRole(@PathVariable Long roleId, @RequestBody List<Long> permissionIds,
            @RequestHeader(value = "Authorization", required = false) String token) {
        checkPermission(roleId, token);
        roleService.assignPermissionsToRole(roleId, permissionIds);
    }

    // 获取角色的权限
    @GetMapping("/{roleId}/permissions")
    public List<Long> getRolePermissions(@PathVariable Long roleId) {
        return roleService.getRolePermissionIds(roleId);
    }

    // 获取角色的菜单权限（返回Route Names）
    @GetMapping("/{roleId}/menu-permissions")
    public java.util.Map<String, Object> getRoleMenuPermissions(@PathVariable Long roleId) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", roleService.getRoleMenuPermissions(roleId));
        return result;
    }

    // 为角色分配菜单权限
    @PostMapping("/{roleId}/menu-permissions")
    public java.util.Map<String, Object> assignMenuPermissionsToRole(@PathVariable Long roleId,
            @RequestBody List<String> routeNames,
            @RequestHeader(value = "Authorization", required = false) String token) {
        checkPermission(roleId, token);
        roleService.assignMenuPermissionsToRole(roleId, routeNames);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 200);
        result.put("msg", "权限分配成功");
        return result;
    }

    private void checkPermission(Long targetRoleId, String token) {
        // 1. 获取目标角色
        Role targetRole = roleService.findById(targetRoleId);
        if (targetRole == null)
            return;

        // 2. 从 JWT 中识别当前用户
        String employeeId = null;
        if (token != null) {
            employeeId = jwtUtil.getEmployeeId(token);
        }

        if (employeeId == null) {
            throw new RuntimeException("Unauthorized: Token 无效或已过期");
        }

        com.artdesign.backend.entity.User currentUser = userService.findByEmployeeId(employeeId);
        if (currentUser == null) {
            throw new RuntimeException("Unauthorized: User not found");
        }

        // 3. 检查当前用户的角色权限
        java.util.List<String> userRoleCodes = currentUser.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(java.util.stream.Collectors.toList());

        // 检查是否为超级管理员
        boolean isSuperAdmin = userRoleCodes.contains("R_SUPER");
        if (isSuperAdmin) {
            // 超级管理员可以修改任何角色
            return;
        }

        // 检查是否为管理员
        boolean isAdmin = userRoleCodes.contains("R_ADMIN");
        if (isAdmin) {
            // 管理员只能修改非管理员角色
            if (Boolean.TRUE.equals(targetRole.getIsAdmin())) {
                throw new RuntimeException("Permission Denied: 管理员只能修改普通用户的权限。");
            }
            return;
        }

        // 非管理员不能修改任何角色
        throw new RuntimeException("Permission Denied: 只有管理员才能修改角色权限。");
    }

}
