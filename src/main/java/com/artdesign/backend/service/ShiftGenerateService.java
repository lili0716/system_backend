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
    public void generateAsyncSchedules(int year, int month, String operator,
            List<User> employees, ShiftType workType, ShiftType restType,
            Set<String> manualKeys, Set<String> holidayStrs,
            SseEmitter emitter, String lockKey) {
        try {
            // 推送初始进度
            sendProgress(emitter, 5, "正在准备批量生成模板...");

            int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
            Calendar calendar = Calendar.getInstance();

            List<Object[]> batchArgs = new ArrayList<>();
            int totalTasks = employees.size() * daysInMonth;
            int counter = 0;

            String sql = "INSERT INTO shift_schedules (employee_id, shift_date, shift_type_id, status, source, year, month, day, create_by, update_by, create_time, update_time) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (employee_id, shift_date) DO UPDATE SET " +
                    "shift_type_id = EXCLUDED.shift_type_id, " +
                    "status = EXCLUDED.status, " +
                    "source = EXCLUDED.source, " +
                    "year = EXCLUDED.year, " +
                    "month = EXCLUDED.month, " +
                    "day = EXCLUDED.day, " +
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
                            new java.sql.Date(shiftDate.getTime()),
                            assignedType.getId(),
                            1,
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
                    if (counter % (totalTasks / 10 + 1) == 0) {
                        int p = 5 + (int) ((double) counter / totalTasks * 55);
                        sendProgress(emitter, p, "进度拼装中，累计 " + counter + " 条记录...");
                    }
                }
            }

            if (batchArgs.isEmpty()) {
                sendProgress(emitter, 100, "操作完成：所有数据皆为人工排班，无自动生成。");
                emitter.complete();
                redisTemplate.delete(lockKey);
                return;
            }

            sendProgress(emitter, 65, "正在向数据库提交大数据事务，可能需要几秒钟...");

            // 拆分极大批次写回数据库以免溢出
            int batchSize = 10000;
            int totalBatches = (batchArgs.size() + batchSize - 1) / batchSize;
            for (int i = 0; i < totalBatches; i++) {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, batchArgs.size());
                List<Object[]> subList = batchArgs.subList(start, end);

                jdbcTemplate.batchUpdate(sql, subList);

                // 写库过程中的平滑进度上报（后 35% 进度范围）
                int curP = 65 + (int) (((double) (i + 1) / totalBatches) * 34);
                sendProgress(emitter, curP, "提交事务: " + (i + 1) + "/" + totalBatches + " 块...");
            }

            // 完全结束指令
            emitter.send(SseEmitter.event().name("complete")
                    .data(Map.of("code", 200, "msg", "自动生成完成，成功覆盖 " + batchArgs.size() + " 条记录")));
            emitter.complete();

        } catch (Exception e) {
            System.err.println("异步排班异常结束: " + e.getMessage());
            e.printStackTrace();
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data(Map.of("code", 500, "msg", "异步生成意外出错: " + e.getMessage())));
            } catch (Exception se) {
            }
            emitter.completeWithError(e);
        } finally {
            // 最后必定解除内存锁
            redisTemplate.delete(lockKey);
        }
    }

    private void sendProgress(SseEmitter emitter, int percent, String text) throws IOException {
        emitter.send(SseEmitter.event()
                .name("progress")
                .data(Map.of("progress", percent, "message", text)));
    }
}
