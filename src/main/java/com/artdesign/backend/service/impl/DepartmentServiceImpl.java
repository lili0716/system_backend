package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Department;
import com.artdesign.backend.repository.DepartmentRepository;
import com.artdesign.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id).orElse(null);
    }

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public List<Department> findByParentId(Long parentId) {
        List<Department> allDepartments = departmentRepository.findAll();
        return allDepartments.stream()
                .filter(dept -> parentId == null ? dept.getParent() == null : parentId.equals(dept.getParent() != null ? dept.getParent().getId() : null))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDepartmentTree() {
        List<Department> allDepartments = departmentRepository.findAll();
        Map<Long, Department> deptMap = allDepartments.stream()
                .collect(Collectors.toMap(Department::getId, dept -> dept));

        // 构建部门树
        List<Map<String, Object>> treeNodes = new ArrayList<>();
        for (Department dept : allDepartments) {
            if (dept.getParent() == null) {
                // 根部门
                Map<String, Object> node = buildDeptNode(dept, deptMap);
                treeNodes.add(node);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", treeNodes);
        return result;
    }

    private Map<String, Object> buildDeptNode(Department dept, Map<Long, Department> deptMap) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", dept.getId());
        node.put("name", dept.getName());
        node.put("code", dept.getCode());
        node.put("description", dept.getDescription());
        node.put("sort", dept.getSort());
        node.put("enabled", dept.getEnabled());
        node.put("leaderId", dept.getLeaderId());
        node.put("leaderName", dept.getLeaderName());

        // 查找子部门
        List<Map<String, Object>> children = new ArrayList<>();
        for (Department childDept : deptMap.values()) {
            if (childDept.getParent() != null && childDept.getParent().getId().equals(dept.getId())) {
                Map<String, Object> childNode = buildDeptNode(childDept, deptMap);
                children.add(childNode);
            }
        }
        node.put("children", children);
        return node;
    }

}
