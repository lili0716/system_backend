package com.artdesign.backend.repository;

import com.artdesign.backend.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    // 根据启用状态查询
    List<AttendanceRule> findByEnabled(Boolean enabled);

    // 根据规则名称查询
    AttendanceRule findByRuleName(String ruleName);

}
