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
    public Role updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return roleService.save(role);
    }

    // 删除角色
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteById(id);
    }

    // 为角色分配权限
    @PostMapping("/{roleId}/permissions")
    public void assignPermissionsToRole(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissionsToRole(roleId, permissionIds);
    }

    // 获取角色的权限
    @GetMapping("/{roleId}/permissions")
    public List<Long> getRolePermissions(@PathVariable Long roleId) {
        return roleService.getRolePermissionIds(roleId);
    }

}
