package com.artdesign.backend.repository;

import com.artdesign.backend.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    // 根据启用状态查询
    List<AttendanceRule> findByEnabled(Boolean enabled);

    // 根据规则名称查询
    AttendanceRule findByRuleName(String ruleName);

    // 根据部门ID查询
    List<AttendanceRule> findByDepartmentId(Long departmentId);

    // 根据单双休设置查询
    List<AttendanceRule> findBySingleWeekOff(Boolean singleWeekOff);

    // 根据规则名称和单双休设置查询
    List<AttendanceRule> findByRuleNameContainingAndSingleWeekOff(String ruleName, Boolean singleWeekOff);

    // 根据规则名称查询（模糊查询）
    List<AttendanceRule> findByRuleNameContaining(String ruleName);

}
