package com.artdesign.backend.service;

import com.artdesign.backend.dto.AttendanceQueryDTO;
import com.artdesign.backend.entity.AttendanceRecord;
import com.artdesign.backend.entity.AttendanceAbnormalRecord;
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

    // 考勤文件方法 - 返回解析结果
    Map<String, Object> uploadAndParseAttendanceFile(MultipartFile file, Long uploaderId);

    List<AttendanceFile> findAllFiles();

    AttendanceFile findFileById(Long id);

    // 解析考勤文件
    Map<String, Object> parseAttendanceFile(Long fileId);

    // 生成失败记录Excel
    byte[] generateFailedRecordsExcel(List<Map<String, Object>> failedRecords);

    // 异常考勤记录方法
    List<AttendanceAbnormalRecord> findAbnormalRecordsByUserId(Long userId);

    List<AttendanceAbnormalRecord> findUncorrectedAbnormalRecords(Long userId);

    AttendanceAbnormalRecord saveAbnormalRecord(AttendanceAbnormalRecord record);

    List<AttendanceAbnormalRecord> findAbnormalRecordsByIds(List<Long> ids);

    void correctAbnormalRecords(List<Long> abnormalRecordIds, Long formId);

    // 考勤规则方法
    List<AttendanceRule> findAllRules();

    AttendanceRule findRuleById(Long id);

    AttendanceRule saveRule(AttendanceRule rule);

    void deleteRuleById(Long id);

    // 获取当前启用的规则
    AttendanceRule getCurrentRule();

    // 获取用户生效的考勤规则（层级查找）
    AttendanceRule getEffectiveRule(com.artdesign.backend.entity.User user);

    // 按条件查询考勤规则
    List<AttendanceRule> findRulesByCondition(String ruleName, Boolean singleWeekOff);

    // 按部门查询考勤规则
    List<AttendanceRule> findRulesByDepartmentId(Long departmentId);

    // 计算工时
    Double calculateWorkHours(Date workInTime, Date workOutTime);

    // 计算考勤状态
    Integer calculateAttendanceStatus(Date workInTime, Date workOutTime, AttendanceRule rule);

    // 考勤查询功能
    Map<String, Object> queryAttendanceRecords(AttendanceQueryDTO dto);

    // 导出考勤记录Excel
    byte[] exportAttendanceRecords(AttendanceQueryDTO dto);

    // 获取考勤记录详情（包含补卡和请假信息）
    Map<String, Object> getAttendanceRecordDetail(Long recordId);

}
