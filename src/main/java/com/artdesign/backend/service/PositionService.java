package com.artdesign.backend.service;

import com.artdesign.backend.entity.Position;
import java.util.List;

public interface PositionService {

    List<Position> findAll();

    Position findById(Long id);

    Position save(Position position);

    void deleteById(Long id);

}
