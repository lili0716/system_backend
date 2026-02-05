package com.artdesign.backend.service;

import com.artdesign.backend.entity.Department;
import java.util.List;
import java.util.Map;

public interface DepartmentService {

    List<Department> findAll();

    Department findById(Long id);

    Department save(Department department);

    void deleteById(Long id);

    List<Department> findByParentId(Long parentId);

    Map<String, Object> getDepartmentTree();

    void updateRoutes(Long deptId, List<Long> routeIds);

    List<Long> getRouteIds(Long deptId);
}
