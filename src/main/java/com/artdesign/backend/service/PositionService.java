package com.artdesign.backend.service;

import com.artdesign.backend.entity.Position;
import org.springframework.data.domain.Page;
import java.util.Map;
import java.util.List;

public interface PositionService {

    Page<Position> getPage(Map<String, Object> params);

    List<Position> findAll();

    Position findById(Long id);

    Position save(Position position);

    void deleteById(Long id);

}
