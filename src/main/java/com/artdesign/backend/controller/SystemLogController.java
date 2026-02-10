package com.artdesign.backend.controller;

import com.artdesign.backend.entity.SystemLog;
import com.artdesign.backend.repository.SystemLogRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/ops/logs")
public class SystemLogController {

    @Autowired
    private SystemLogRepository systemLogRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询系统日志
     */
    @PostMapping("/query")
    public Map<String, Object> queryLogs(@RequestBody Map<String, Object> params) {
        int page = params.get("current") != null ? ((Number) params.get("current")).intValue() - 1 : 0;
        int size = params.get("size") != null ? ((Number) params.get("size")).intValue() : 20;

        Specification<SystemLog> spec = buildSpec(params);
        Page<SystemLog> result = systemLogRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestTime")));

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getContent());
        data.put("current", result.getNumber() + 1);
        data.put("size", result.getSize());
        data.put("total", result.getTotalElements());

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "success");
        res.put("data", data);
        return res;
    }

    /**
     * 按搜索条件全量导出 Excel
     */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportLogs(@RequestBody Map<String, Object> params) {
        Specification<SystemLog> spec = buildSpec(params);
        List<SystemLog> logs = systemLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "requestTime"));

        byte[] excelData = generateExcel(logs);

        String fileName = "系统日志_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    /**
     * 构建动态查询条件
     */
    private Specification<SystemLog> buildSpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 时间范围
            if (params.get("startTime") != null) {
                LocalDateTime start = LocalDateTime.parse((String) params.get("startTime"), FMT);
                predicates.add(cb.greaterThanOrEqualTo(root.get("requestTime"), start));
            }
            if (params.get("endTime") != null) {
                LocalDateTime end = LocalDateTime.parse((String) params.get("endTime"), FMT);
                predicates.add(cb.lessThanOrEqualTo(root.get("requestTime"), end));
            }

            // 用户工号
            if (params.get("employeeId") != null && !((String) params.get("employeeId")).isEmpty()) {
                predicates.add(cb.equal(root.get("employeeId"), params.get("employeeId")));
            }

            // 用户姓名模糊查询
            if (params.get("nickName") != null && !((String) params.get("nickName")).isEmpty()) {
                predicates.add(cb.like(root.get("nickName"), "%" + params.get("nickName") + "%"));
            }

            // 请求方法
            if (params.get("method") != null && !((String) params.get("method")).isEmpty()) {
                predicates.add(cb.equal(root.get("method"), params.get("method")));
            }

            // URI 模糊查询
            if (params.get("uri") != null && !((String) params.get("uri")).isEmpty()) {
                predicates.add(cb.like(root.get("uri"), "%" + params.get("uri") + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 生成 Excel
     */
    private byte[] generateExcel(List<SystemLog> logs) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("系统日志");

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 表头
            String[] headers = { "序号", "请求时间", "用户工号", "用户姓名", "请求方法", "请求URI", "请求参数", "响应状态码", "响应内容", "耗时(ms)",
                    "IP地址" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            for (int i = 0; i < logs.size(); i++) {
                SystemLog log = logs.get(i);
                Row row = sheet.createRow(i + 1);

                createCell(row, 0, String.valueOf(i + 1), dataStyle);
                createCell(row, 1, log.getRequestTime() != null ? log.getRequestTime().format(FMT) : "", dataStyle);
                createCell(row, 2, log.getEmployeeId() != null ? log.getEmployeeId() : "", dataStyle);
                createCell(row, 3, log.getNickName() != null ? log.getNickName() : "", dataStyle);
                createCell(row, 4, log.getMethod() != null ? log.getMethod() : "", dataStyle);
                createCell(row, 5, log.getUri() != null ? log.getUri() : "", dataStyle);
                createCell(row, 6, log.getRequestParams() != null ? log.getRequestParams() : "", dataStyle);
                createCell(row, 7, log.getResponseCode() != null ? String.valueOf(log.getResponseCode()) : "",
                        dataStyle);
                createCell(row, 8, log.getResponseBody() != null ? log.getResponseBody() : "", dataStyle);
                createCell(row, 9, log.getDuration() != null ? String.valueOf(log.getDuration()) : "", dataStyle);
                createCell(row, 10, log.getIp() != null ? log.getIp() : "", dataStyle);
            }

            // 自动列宽
            int[] widths = { 2000, 5000, 3000, 3000, 2500, 8000, 10000, 3000, 10000, 2500, 4000 };
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i]);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成Excel失败", e);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
