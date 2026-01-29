package com.artdesign.backend.controller;

import com.artdesign.backend.entity.Permission;
import com.artdesign.backend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    // 获取所有权限
    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionService.findAll();
    }

    // 根据ID获取权限
    @GetMapping("/{id}")
    public Permission getPermissionById(@PathVariable Long id) {
        return permissionService.findById(id);
    }

    // 新增权限
    @PostMapping
    public Permission createPermission(@RequestBody Permission permission) {
        return permissionService.save(permission);
    }

    // 修改权限
    @PutMapping("/{id}")
    public Permission updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        return permissionService.save(permission);
    }

    // 删除权限
    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable Long id) {
        permissionService.deleteById(id);
    }

    // 根据父ID获取子权限
    @GetMapping("/parent/{parentId}")
    public List<Permission> getPermissionsByParentId(@PathVariable Long parentId) {
        return permissionService.findByParentId(parentId);
    }

    // 获取权限树
    @GetMapping("/tree")
    public Map<String, Object> getPermissionTree() {
        return permissionService.getPermissionTree();
    }

}
