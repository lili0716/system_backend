package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Position;
import com.artdesign.backend.repository.PositionRepository;
import com.artdesign.backend.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Override
    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    @Override
    public Position findById(Long id) {
        return positionRepository.findById(id).orElse(null);
    }

    @Override
    public Position save(Position position) {
        return positionRepository.save(position);
    }

    @Override
    public void deleteById(Long id) {
        positionRepository.deleteById(id);
    }

    @Override
    public org.springframework.data.domain.Page<Position> getPage(java.util.Map<String, Object> params) {
        int page = 1;
        if (params.get("page") != null) {
            page = Integer.parseInt(params.get("page").toString());
        }

        int size = 10;
        if (params.get("size") != null) {
            size = Integer.parseInt(params.get("size").toString());
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1,
                size, org.springframework.data.domain.Sort.by("sort").ascending());

        return positionRepository.findAll((root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (params.get("name") != null && !params.get("name").toString().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + params.get("name") + "%"));
            }

            if (params.get("code") != null && !params.get("code").toString().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("code"), "%" + params.get("code") + "%"));
            }

            if (params.get("enabled") != null && !params.get("enabled").toString().isEmpty()) {
                if (!"all".equals(params.get("enabled").toString())) {
                    predicates.add(criteriaBuilder.equal(root.get("enabled"),
                            Boolean.valueOf(params.get("enabled").toString())));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

}
