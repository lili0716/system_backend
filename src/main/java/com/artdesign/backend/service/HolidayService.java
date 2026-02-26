package com.artdesign.backend.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 节假日服务：内置2025、2026年中国法定节假日数据
 * 单休规则：周一~周六工作，周日休息；法定节假日休息；调休周六正常上班
 */
@Service
public class HolidayService {

    // 格式 yyyy-M-d
    private static final Set<String> HOLIDAYS = new HashSet<>(Arrays.asList(
            // === 2025年法定节假日 ===
            // 元旦 (1月1日)
            "2025-1-1",
            // 春节 (1月28日-2月4日，实际放假日期)
            "2025-1-28", "2025-1-29", "2025-1-30", "2025-1-31",
            "2025-2-1", "2025-2-2", "2025-2-3", "2025-2-4",
            // 清明节 (4月4日-4月6日)
            "2025-4-4", "2025-4-5", "2025-4-6",
            // 劳动节 (5月1日-5月5日)
            "2025-5-1", "2025-5-2", "2025-5-3", "2025-5-4", "2025-5-5",
            // 端午节 (5月31日-6月2日)
            "2025-5-31", "2025-6-1", "2025-6-2",
            // 国庆+中秋 (10月1日-8日)
            "2025-10-1", "2025-10-2", "2025-10-3", "2025-10-4",
            "2025-10-5", "2025-10-6", "2025-10-7", "2025-10-8",

            // === 2026年法定节假日（预估）===
            // 元旦
            "2026-1-1",
            // 春节（2026年1月17日农历正月初一）
            "2026-1-17", "2026-1-18", "2026-1-19", "2026-1-20",
            "2026-1-21", "2026-1-22", "2026-1-23",
            // 清明节
            "2026-4-5", "2026-4-6", "2026-4-7",
            // 劳动节
            "2026-5-1", "2026-5-2", "2026-5-3", "2026-5-4", "2026-5-5",
            // 端午节
            "2026-6-19", "2026-6-20", "2026-6-21",
            // 中秋节
            "2026-9-25", "2026-9-26", "2026-9-27",
            // 国庆节
            "2026-10-1", "2026-10-2", "2026-10-3", "2026-10-4",
            "2026-10-5", "2026-10-6", "2026-10-7"));

    // 调休工作日（节假日前后调整，原本是周末但需要上班）
    private static final Set<String> WORKDAYS_ON_WEEKEND = new HashSet<>(Arrays.asList(
            // 2025年调休工作日
            "2025-1-26", // 周日，春节前调休上班
            "2025-2-8", // 周六，春节后调休上班
            "2025-4-27", // 周日，劳动节调休上班
            "2025-9-28", // 周日，国庆节调休上班
            "2025-10-11", // 周六，国庆节后调休上班
            // 2026年调休工作日（预估）
            "2026-1-4", // 周日
            "2026-1-25" // 周日
    ));

    /**
     * 判断某天是否为休息日（节假日或周日）
     * 单休规则：周一~周六工作（若为调休工作日则周末也上班），周日休息，法定节假日休息
     */
    public boolean isRestDay(int year, int month, int day) {
        String key = year + "-" + month + "-" + day;

        // 法定节假日 → 休息
        if (HOLIDAYS.contains(key)) {
            return true;
        }

        // 调休工作日（即使是周末也上班）
        if (WORKDAYS_ON_WEEKEND.contains(key)) {
            return false;
        }

        // 计算星期几
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, day);
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);

        // 单休：周日（Calendar.SUNDAY = 1）休息
        return dayOfWeek == java.util.Calendar.SUNDAY;
    }

    /**
     * 获取某年某月所有节假日（用于前端高亮显示）
     */
    public Set<String> getHolidaysOfMonth(int year, int month) {
        Set<String> result = new HashSet<>();
        java.time.YearMonth ym = java.time.YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            String key = year + "-" + month + "-" + d;
            if (HOLIDAYS.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }
}
