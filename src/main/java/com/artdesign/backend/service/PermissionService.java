package com.artdesign.backend.service;

import com.artdesign.backend.entity.Permission;
import java.util.List;
import java.util.Map;

public interface PermissionService {

    List<Permission> findAll();

    Permission findById(Long id);

    Permission save(Permission permission);

    void deleteById(Long id);

    List<Permission> findByParentId(Long parentId);

    Map<String, Object> getPermissionTree();

    List<Permission> findByRoleId(Long roleId);

}
