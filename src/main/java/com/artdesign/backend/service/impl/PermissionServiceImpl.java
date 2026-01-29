package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Permission;
import com.artdesign.backend.repository.PermissionRepository;
import com.artdesign.backend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Override
    public Permission findById(Long id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @Override
    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Override
    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }

    @Override
    public List<Permission> findByParentId(Long parentId) {
        List<Permission> allPermissions = permissionRepository.findAll();
        return allPermissions.stream()
                .filter(perm -> parentId == null ? perm.getParentId() == null : parentId.equals(perm.getParentId()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        Map<Long, Permission> permMap = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getId, perm -> perm));

        // 构建权限树
        List<Map<String, Object>> treeNodes = new ArrayList<>();
        for (Permission perm : allPermissions) {
            if (perm.getParentId() == null) {
                // 根权限
                Map<String, Object> node = buildPermNode(perm, permMap);
                treeNodes.add(node);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", treeNodes);
        return result;
    }

    private Map<String, Object> buildPermNode(Permission perm, Map<Long, Permission> permMap) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", perm.getId());
        node.put("name", perm.getName());
        node.put("code", perm.getCode());
        node.put("description", perm.getDescription());
        node.put("type", perm.getType());
        node.put("path", perm.getPath());
        node.put("component", perm.getComponent());
        node.put("icon", perm.getIcon());
        node.put("sort", perm.getSort());
        node.put("enabled", perm.getEnabled());

        // 查找子权限
        List<Map<String, Object>> children = new ArrayList<>();
        for (Permission childPerm : permMap.values()) {
            if (childPerm.getParentId() != null && childPerm.getParentId().equals(perm.getId())) {
                Map<String, Object> childNode = buildPermNode(childPerm, permMap);
                children.add(childNode);
            }
        }
        node.put("children", children);
        return node;
    }

    @Override
    public List<Permission> findByRoleId(Long roleId) {
        // 这里需要实现根据角色ID查询权限的逻辑
        // 由于我们还没有实现角色与权限的关联查询，这里暂时返回空列表
        // 后续会在RoleService中实现完整的逻辑
        return new ArrayList<>();
    }

}
