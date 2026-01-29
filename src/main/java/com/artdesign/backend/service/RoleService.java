package com.artdesign.backend.service;

import com.artdesign.backend.entity.Role;
import com.artdesign.backend.entity.Permission;
import java.util.List;
import java.util.Map;

public interface RoleService {

    List<Role> findAll();

    Role findById(Long id);

    Role save(Role role);

    void deleteById(Long id);
    
    // 分页查询角色列表
    Map<String, Object> getRoleList(Map<String, Object> params);

    // 权限相关方法
    List<Permission> getPermissionsByRoleId(Long roleId);
    
    List<Long> getRolePermissionIds(Long roleId);
    
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

}