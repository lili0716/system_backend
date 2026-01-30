package com.artdesign.backend.service;

import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.AttendanceFile;
import com.artdesign.backend.entity.AttendanceRule;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AttendanceService {

    // 考勤记录方法
    List<AttendanceRecord> findAllRecords();

    AttendanceRecord findRecordById(Long id);

    AttendanceRecord saveRecord(AttendanceRecord record);

    void deleteRecordById(Long id);

    // 根据用户查询考勤记录
    List<AttendanceRecord> findRecordsByUserId(Long userId);

    // 根据用户和日期范围查询
    List<AttendanceRecord> findRecordsByUserIdAndDateRange(Long userId, Date startDate, Date endDate);

    // 分页查询考勤记录
    Map<String, Object> getRecordList(Map<String, Object> params);

    // 考勤文件方法
    AttendanceFile uploadFile(MultipartFile file, Long uploaderId);

    List<AttendanceFile> findAllFiles();

    AttendanceFile findFileById(Long id);

    // 解析考勤文件
    void parseAttendanceFile(Long fileId);

    // 考勤规则方法
    List<AttendanceRule> findAllRules();

    AttendanceRule findRuleById(Long id);

    AttendanceRule saveRule(AttendanceRule rule);

    void deleteRuleById(Long id);

    // 获取当前启用的规则
    AttendanceRule getCurrentRule();

    // 按条件查询考勤规则
    List<AttendanceRule> findRulesByCondition(String ruleName, Boolean singleWeekOff);

    // 按部门查询考勤规则
    List<AttendanceRule> findRulesByDepartmentId(Long departmentId);

    // 计算工时
    Double calculateWorkHours(Date workInTime, Date workOutTime);

    // 计算考勤状态
    Integer calculateAttendanceStatus(Date workInTime, Date workOutTime, AttendanceRule rule);

}
