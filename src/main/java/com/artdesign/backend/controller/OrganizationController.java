package com.artdesign.backend.controller;

import com.artdesign.backend.entity.Department;
import com.artdesign.backend.entity.Position;
import com.artdesign.backend.service.DepartmentService;
import com.artdesign.backend.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PositionService positionService;

    // 部门管理API
    @GetMapping("/departments")
    public Map<String, Object> getDepartments() {
        List<Department> departments = departmentService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", departments);
        return result;
    }

    @GetMapping("/departments/tree")
    public Map<String, Object> getDepartmentTree() {
        Map<String, Object> tree = departmentService.getDepartmentTree();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", tree);
        return result;
    }

    @PostMapping("/departments")
    public Map<String, Object> createDepartment(@RequestBody Department department) {
        Department savedDept = departmentService.save(department);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", savedDept);
        return result;
    }

    @PutMapping("/departments")
    public Map<String, Object> updateDepartment(@RequestBody Department department) {
        Department updatedDept = departmentService.save(department);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", updatedDept);
        return result;
    }

    @DeleteMapping("/departments/{id}")
    public Map<String, Object> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }

    // 职位管理API
    @GetMapping("/positions")
    public Map<String, Object> getPositions() {
        List<Position> positions = positionService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", positions);
        return result;
    }

    @PostMapping("/positions")
    public Map<String, Object> createPosition(@RequestBody Position position) {
        Position savedPosition = positionService.save(position);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", savedPosition);
        return result;
    }

    @PutMapping("/positions")
    public Map<String, Object> updatePosition(@RequestBody Position position) {
        Position updatedPosition = positionService.save(position);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", updatedPosition);
        return result;
    }

    @DeleteMapping("/positions/{id}")
    public Map<String, Object> deletePosition(@PathVariable Long id) {
        positionService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }

}
