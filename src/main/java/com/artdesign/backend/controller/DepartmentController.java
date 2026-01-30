package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.entity.Department;
import com.artdesign.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public Result<List<Department>> getAllDepartments() {
        return Result.success(departmentService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        return Result.success(departmentService.findById(id));
    }

    @PostMapping
    public Result<Department> createDepartment(@RequestBody Department department) {
        return Result.success(departmentService.save(department));
    }

    @PutMapping("/{id}")
    public Result<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return Result.success(departmentService.save(department));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/tree")
    public Result<Map<String, Object>> getDepartmentTree() {
        return Result.success(departmentService.getDepartmentTree());
    }
}
