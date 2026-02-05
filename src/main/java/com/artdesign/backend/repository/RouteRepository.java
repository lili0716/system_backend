package com.artdesign.backend.repository;

import com.artdesign.backend.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
    java.util.List<Route> findByParentIsNull();
}
