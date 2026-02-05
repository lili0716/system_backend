package com.artdesign.backend.controller;

import com.artdesign.backend.entity.Role;
import com.artdesign.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private com.artdesign.backend.service.UserService userService;

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

    private void checkPermission(Long targetRoleId, String token) {
        // 1. Get Target Role
        Role targetRole = roleService.findById(targetRoleId);
        if (targetRole == null)
            return;

        // If target is NOT superadmin, anyone (who has access to this API) can edit
        if (!"superadmin".equals(targetRole.getRoleCode())) {
            return;
        }

        // 2. Identify Current User
        String employeeId = null;
        if (token != null && token.contains("mock-token-")) {
            employeeId = token.replace("Bearer ", "").replace("mock-token-", "").trim();
        }

        if (employeeId == null) {
            throw new RuntimeException("Unauthorized: No token provided");
        }

        com.artdesign.backend.entity.User currentUser = userService.findByEmployeeId(employeeId);
        if (currentUser == null) {
            throw new RuntimeException("Unauthorized: User not found");
        }

        // 3. Check if Current User is Superadmin
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> "superadmin".equals(r.getRoleCode()));

        if (!isSuperAdmin) {
            throw new RuntimeException("Permission Denied: Only superadmin can modify superadmin role.");
        }
    }

}
