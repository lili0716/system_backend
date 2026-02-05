package com.artdesign.backend.controller;

import com.artdesign.backend.entity.Role;
import com.artdesign.backend.repository.RoleRepository;
import com.artdesign.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/options")
public class OptionsController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentService departmentService;

    // 获取角色下拉列表
    @GetMapping("/roles")
    public Map<String, Object> getRoleOptions() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Role> roles = roleRepository.findAll();
            // Transform to simple Label/Value structure if needed, or return entity
            // Using entity is consistent with existing frontend logic which expects Role
            // objects mostly
            // But UserDialog.vue uses: label="role.roleName" value="role.roleId"

            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", roles);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取部门树形下拉列表
    // Directly reuse service logic but wrap in standard result
    @GetMapping("/departments")
    public Map<String, Object> getDepartmentOptions() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> treeData = departmentService.getDepartmentTree();
            // treeData structure: { nodes: [...] }

            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", treeData);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
