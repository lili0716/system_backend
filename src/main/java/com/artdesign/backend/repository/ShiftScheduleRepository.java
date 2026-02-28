package com.artdesign.backend.repository;

import com.artdesign.backend.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    List<ShiftSchedule> findByYearAndMonth(int year, int month);

    List<ShiftSchedule> findByYearAndMonthAndEmployeeIdIn(int year, int month, List<String> employeeIds);

    List<ShiftSchedule> findByYearAndMonthAndEmployeeId(int year, int month, String employeeId);

    Optional<ShiftSchedule> findByYearAndMonthAndDayAndEmployeeId(int year, int month, int day, String employeeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShiftSchedule s WHERE s.year = :year AND s.month = :month AND s.source = 'AUTO'")
    void deleteAutoByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
