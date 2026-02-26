package com.artdesign.backend.repository;

import com.artdesign.backend.entity.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShiftTypeRepository extends JpaRepository<ShiftType, Long> {
    Optional<ShiftType> findByIsDefaultTrue();

    Optional<ShiftType> findByIsRestTrue();

    Optional<ShiftType> findByCode(String code);
}
