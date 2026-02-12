package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Department;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.DepartmentRepository;
import com.artdesign.backend.repository.UserRepository;
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
    
    @Autowired
    private UserRepository userRepository;

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
                .filter(dept -> parentId == null ? dept.getParent() == null
                        : parentId.equals(dept.getParent() != null ? dept.getParent().getId() : null))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDepartmentTree() {
        List<Department> allDepartments = departmentRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        
        // 计算每个部门的员工数量
        Map<Long, Integer> employeeCountMap = new HashMap<>();
        for (User user : allUsers) {
            if (user.getDepartment() != null) {
                Long deptId = user.getDepartment().getId();
                employeeCountMap.put(deptId, employeeCountMap.getOrDefault(deptId, 0) + 1);
            }
        }

        // Transform to nodes map
        Map<Long, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> treeNodes = new ArrayList<>();

        // 1. Create all nodes
        for (Department dept : allDepartments) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", dept.getId());
            node.put("name", dept.getName());
            node.put("code", dept.getCode());
            node.put("description", dept.getDescription());
            node.put("sort", dept.getSort());
            node.put("enabled", dept.getEnabled());
            node.put("leaderId", dept.getLeaderId());
            node.put("leaderName", dept.getLeaderName());
            node.put("employeeCount", employeeCountMap.getOrDefault(dept.getId(), 0));
            node.put("children", new ArrayList<>());

            // Safe parent ID access
            Long parentId = null;
            if (dept.getParent() != null) {
                try {
                    parentId = dept.getParent().getId();
                } catch (Exception e) {
                    // Handle lazy loading or proxy issues
                }
            }
            if (parentId != null) {
                node.put("parentId", parentId);
            }

            nodeMap.put(dept.getId(), node);
        }

        // 2. Assemble tree
        for (Department dept : allDepartments) {
            Map<String, Object> node = nodeMap.get(dept.getId());
            Long parentId = null;
            if (dept.getParent() != null) {
                try {
                    parentId = dept.getParent().getId();
                } catch (Exception e) {
                    // Ignore
                }
            }

            if (parentId != null && nodeMap.containsKey(parentId)) {
                Map<String, Object> parentNode = nodeMap.get(parentId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                children.add(node);
            } else {
                // Root node (or orphan)
                treeNodes.add(node);
            }
        }

        // 3. 递归计算每个部门及其子部门的总人数
        for (Map<String, Object> node : treeNodes) {
            calculateTotalEmployeeCount(node, employeeCountMap, nodeMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", treeNodes);
        return result;
    }

    // 递归计算部门及其子部门的总人数
    private void calculateTotalEmployeeCount(Map<String, Object> node, Map<Long, Integer> employeeCountMap, Map<Long, Map<String, Object>> nodeMap) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        
        // 计算当前部门的基础人数
        Long deptId = (Long) node.get("id");
        int totalCount = employeeCountMap.getOrDefault(deptId, 0);
        
        // 递归计算子部门的人数
        for (Map<String, Object> child : children) {
            calculateTotalEmployeeCount(child, employeeCountMap, nodeMap);
            totalCount += (Integer) child.get("employeeCount");
        }
        
        // 更新当前部门的总人数
        node.put("employeeCount", totalCount);
    }

    // Removed recursive buildDeptNode as it is no longer used

    @Autowired
    private com.artdesign.backend.repository.RouteRepository routeRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateRoutes(Long deptId, List<Long> routeIds) {
        Department dept = findById(deptId);
        if (dept != null) {
            List<com.artdesign.backend.entity.Route> routes = routeRepository.findAllById(routeIds);
            dept.setRoutes(routes);
            save(dept);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Long> getRouteIds(Long deptId) {
        Department dept = findById(deptId);
        if (dept != null && dept.getRoutes() != null) {
            return dept.getRoutes().stream().map(com.artdesign.backend.entity.Route::getId)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
