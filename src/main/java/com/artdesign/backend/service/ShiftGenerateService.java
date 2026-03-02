package com.artdesign.backend.service;

import com.artdesign.backend.entity.ShiftSchedule;
import com.artdesign.backend.entity.ShiftType;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.ShiftScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

@Service
public class ShiftGenerateService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 核心异步排班生成器
     * (包含全局防抖 Redis 锁、SseEmitter 状态流推送以及 JdbcTemplate 大数据量批次极速写盘优化)
     */
    @Async
    @Transactional
    public void generateAsyncSchedules(int year, int month, String operator,
            List<User> employees, ShiftType workType, ShiftType restType,
            Set<String> manualKeys, Set<String> holidayStrs,
            String taskId, String lockKey) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            long t0 = System.currentTimeMillis();
            System.out.println("===[Perf] Async start at " + t0);
            // 推送初始进度
            sendProgress(taskId, mapper, 5, "正在准备批量生成模板...", false);

            int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
            Calendar calendar = Calendar.getInstance();

            List<Object[]> batchArgs = new ArrayList<>();
            int totalTasks = employees.size() * daysInMonth;
            int counter = 0;

            String sql = "INSERT INTO shift_schedules (employee_id, shift_type_id, source, year, month, day, create_by, update_by, create_time, update_time) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (year, month, day, employee_id) DO UPDATE SET " +
                    "shift_type_id = EXCLUDED.shift_type_id, " +
                    "source = EXCLUDED.source, " +
                    "update_by = EXCLUDED.update_by, " +
                    "update_time = EXCLUDED.update_time";

            for (int i = 0; i < employees.size(); i++) {
                User emp = employees.get(i);
                for (int day = 1; day <= daysInMonth; day++) {
                    calendar.set(year, month - 1, day);
                    Date shiftDate = calendar.getTime();

                    String manualKey = emp.getEmployeeId() + "_" + day;
                    if (manualKeys.contains(manualKey)) {
                        counter++;
                        continue;
                    }

                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

                    String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                    boolean isHoliday = holidayStrs.contains(dateStr);

                    ShiftType assignedType = (isWeekend || isHoliday) ? restType : workType;

                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    batchArgs.add(new Object[] {
                            emp.getEmployeeId(),
                            assignedType.getId(),
                            "AUTO",
                            year,
                            month,
                            day,
                            operator,
                            operator,
                            now,
                            now
                    });

                    counter++;
                    // 每组装完成一部分实体，实时播报进度 (占到总体的前 60%)
                    if (counter % 100 == 0) {
                        int p = 5 + (int) ((double) counter / totalTasks * 55);
                        sendProgress(taskId, mapper, p, "进度拼装中，累计 " + counter + " 条记录...", false);
                    }
                }
            }

            if (batchArgs.isEmpty()) {
                sendProgress(taskId, mapper, 100, "操作完成：所有数据皆为人工排班，无自动生成。", true);
                redisTemplate.delete(lockKey);
                return;
            }

            System.out.println("===[Perf] Assembly Done in " + (System.currentTimeMillis() - t0) + "ms. Args size: "
                    + batchArgs.size());

            sendProgress(taskId, mapper, 65, "正在向数据库提交大数据事务，可能需要几秒钟...", false);

            // 拆分极大批次写回数据库以免溢出
            int batchSize = 500;
            int totalBatches = (batchArgs.size() + batchSize - 1) / batchSize;
            for (int i = 0; i < totalBatches; i++) {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, batchArgs.size());
                List<Object[]> subList = batchArgs.subList(start, end);

                long bt = System.currentTimeMillis();
                jdbcTemplate.batchUpdate(sql, subList);
                System.out.println("===[Perf] batchUpdate " + i + " took " + (System.currentTimeMillis() - bt) + "ms");

                // 写库过程中的平滑进度上报（后 35% 进度范围）
                int curP = 65 + (int) (((double) (i + 1) / totalBatches) * 34);
                sendProgress(taskId, mapper, curP, "提交事务: " + (i + 1) + "/" + totalBatches + " 块...", false);
            }
            System.out.println("===[Perf] ALL Done in " + (System.currentTimeMillis() - t0) + "ms");

            // 完全结束指令
            sendProgress(taskId, mapper, 100, "自动生成完成，成功覆盖 " + batchArgs.size() + " 条记录", true);

        } catch (Exception e) {
            System.err.println("异步排班异常结束: " + e.getMessage());
            e.printStackTrace();
            sendError(taskId, mapper, "异步生成意外出错: " + e.getMessage());
        } finally {
            // 最后必定解除内存锁
            redisTemplate.delete(lockKey);
        }
    }

    private void sendProgress(String taskId, com.fasterxml.jackson.databind.ObjectMapper mapper, int percent,
            String text, boolean isComplete) {
        try {
            String json = mapper.writeValueAsString(
                    Map.of("progress", percent, "message", text, "complete", isComplete, "error", false));
            redisTemplate.opsForValue().set("shift_progress:" + taskId, json, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
    }

    private void sendError(String taskId, com.fasterxml.jackson.databind.ObjectMapper mapper, String text) {
        try {
            String json = mapper
                    .writeValueAsString(Map.of("progress", 0, "message", text, "complete", true, "error", true));
            redisTemplate.opsForValue().set("shift_progress:" + taskId, json, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
    }
}
