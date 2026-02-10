package com.artdesign.backend.controller;

import com.artdesign.backend.common.Result;
import com.artdesign.backend.entity.Position;
import com.artdesign.backend.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam Map<String, Object> params) {
        Page<Position> page = positionService.getPage(params);
        Map<String, Object> map = new HashMap<>();
        map.put("list", page.getContent());
        map.put("total", page.getTotalElements());
        map.put("current", page.getNumber() + 1); // spring page is 0-indexed
        map.put("size", page.getSize());
        return Result.success(map);
    }

    @GetMapping("/all")
    public Result<List<Position>> all() {
        // filter enabled positions only
        List<Position> list = positionService.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
                .sorted((p1, p2) -> {
                    int sort1 = p1.getSort() != null ? p1.getSort() : 999;
                    int sort2 = p2.getSort() != null ? p2.getSort() : 999;
                    return Integer.compare(sort1, sort2);
                })
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Position> add(@RequestBody Position position) {
        position.setId(null); // ensure create
        return Result.success(positionService.save(position));
    }

    @PutMapping("/update")
    public Result<Position> update(@RequestBody Position position) {
        return Result.success(positionService.save(position));
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        positionService.deleteById(id);
        return Result.success("删除成功");
    }
}
