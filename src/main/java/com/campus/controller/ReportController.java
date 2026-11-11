package com.campus.controller;

import com.campus.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Tag(name = "统计报表")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final JdbcTemplate jdbc;

    @Operation(summary = "学生分布（按院系）")
    @GetMapping("/student")
    public Result<Map<String, Object>> student() {
        List<Map<String, Object>> byDept = jdbc.queryForList("""
                SELECT d.dept_name AS name, COUNT(s.id) AS value
                FROM sys_dept d
                LEFT JOIN student s ON s.dept_id = d.id AND s.is_deleted = 0
                WHERE d.is_deleted = 0 AND d.parent_id > 0
                GROUP BY d.id, d.dept_name
                ORDER BY value DESC
                """);

        List<Map<String, Object>> byClazz = jdbc.queryForList("""
                SELECT c.clazz_name AS name, COUNT(s.id) AS value
                FROM clazz c
                LEFT JOIN student s ON s.clazz_id = c.id AND s.is_deleted = 0
                WHERE c.is_deleted = 0
                GROUP BY c.id, c.clazz_name
                ORDER BY value DESC
                """);

        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student WHERE is_deleted = 0", Integer.class);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total == null ? 0 : total);
        out.put("byDept", byDept);
        out.put("byClazz", byClazz);
        return Result.ok(out);
    }

    @Operation(summary = "成绩统计（分数段 + 挂科率）")
    @GetMapping("/score")
    public Result<Map<String, Object>> score() {
        List<Map<String, Object>> dist = jdbc.queryForList("""
                SELECT CASE
                         WHEN score < 60 THEN '不及格'
                         WHEN score < 70 THEN '60-69'
                         WHEN score < 80 THEN '70-79'
                         WHEN score < 90 THEN '80-89'
                         ELSE '90-100'
                       END AS name,
                       COUNT(*) AS value
                FROM score
                WHERE is_deleted = 0 AND score IS NOT NULL
                GROUP BY name
                """);

        // 保证 5 个分段都在
        Map<String, Long> bucket = new LinkedHashMap<>();
        bucket.put("不及格", 0L);
        bucket.put("60-69", 0L);
        bucket.put("70-79", 0L);
        bucket.put("80-89", 0L);
        bucket.put("90-100", 0L);
        for (Map<String, Object> row : dist) {
            bucket.put((String) row.get("name"), ((Number) row.get("value")).longValue());
        }
        List<Map<String, Object>> ordered = new ArrayList<>();
        bucket.forEach((k, v) -> ordered.add(Map.of("name", k, "value", v)));

        Map<String, Object> agg = jdbc.queryForMap("""
                SELECT COUNT(*) AS total,
                       ROUND(AVG(score), 2) AS avgScore,
                       SUM(CASE WHEN score < 60 THEN 1 ELSE 0 END) AS failCount
                FROM score WHERE is_deleted = 0 AND score IS NOT NULL
                """);

        long total = ((Number) agg.getOrDefault("total", 0)).longValue();
        long fail = ((Number) agg.getOrDefault("failCount", 0)).longValue();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("avgScore", agg.get("avgScore"));
        out.put("failRate", total == 0 ? 0 : Math.round(fail * 10000.0 / total) / 100.0);
        out.put("distribution", ordered);
        return Result.ok(out);
    }

    @Operation(summary = "资助统计")
    @GetMapping("/aid")
    public Result<Map<String, Object>> aid() {
        List<Map<String, Object>> byType = jdbc.queryForList("""
                SELECT CASE aid_type
                         WHEN 1 THEN '国家助学金'
                         WHEN 2 THEN '校内助学金'
                         WHEN 3 THEN '临时困难补助'
                         WHEN 4 THEN '助学贷款'
                         ELSE '其他'
                       END AS name,
                       COUNT(*) AS value,
                       COALESCE(SUM(amount), 0) AS amount
                FROM aid_apply
                WHERE is_deleted = 0
                GROUP BY aid_type
                """);

        List<Map<String, Object>> byStatus = jdbc.queryForList("""
                SELECT CASE status
                         WHEN 0 THEN '草稿'
                         WHEN 1 THEN '辅导员初审'
                         WHEN 2 THEN '院系复审'
                         WHEN 3 THEN '校级终审'
                         WHEN 4 THEN '通过'
                         WHEN 5 THEN '驳回'
                         ELSE '未知'
                       END AS name,
                       COUNT(*) AS value
                FROM aid_apply WHERE is_deleted = 0
                GROUP BY status
                """);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("byType", byType);
        out.put("byStatus", byStatus);
        return Result.ok(out);
    }

    @Operation(summary = "报修统计")
    @GetMapping("/repair")
    public Result<Map<String, Object>> repair() {
        List<Map<String, Object>> byStatus = jdbc.queryForList("""
                SELECT CASE status
                         WHEN 0 THEN '待派单'
                         WHEN 1 THEN '维修中'
                         WHEN 2 THEN '已完成'
                         WHEN 3 THEN '已取消'
                         ELSE '未知'
                       END AS name,
                       COUNT(*) AS value
                FROM repair_order WHERE is_deleted = 0
                GROUP BY status
                """);

        List<Map<String, Object>> byType = jdbc.queryForList("""
                SELECT CASE repair_type
                         WHEN 1 THEN '水电'
                         WHEN 2 THEN '门窗'
                         WHEN 3 THEN '家具'
                         WHEN 4 THEN '网络'
                         ELSE '其他'
                       END AS name,
                       COUNT(*) AS value
                FROM repair_order WHERE is_deleted = 0
                GROUP BY repair_type
                """);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("byStatus", byStatus);
        out.put("byType", byType);
        return Result.ok(out);
    }

    @Operation(summary = "总览（大屏用）")
    @GetMapping("/screen")
    public Result<Map<String, Object>> screen() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("studentCount", count("SELECT COUNT(*) FROM student WHERE is_deleted = 0"));
        out.put("teacherCount", count("SELECT COUNT(*) FROM teacher WHERE is_deleted = 0"));
        out.put("courseCount", count("SELECT COUNT(*) FROM course WHERE is_deleted = 0"));
        out.put("clazzCount", count("SELECT COUNT(*) FROM clazz WHERE is_deleted = 0"));
        out.put("noticeCount", count("SELECT COUNT(*) FROM notice WHERE is_deleted = 0 AND status = 1"));
        out.put("activityCount", count("SELECT COUNT(*) FROM ec_activity WHERE is_deleted = 0"));
        out.put("careCount", count("SELECT COUNT(*) FROM care_target WHERE is_deleted = 0 AND status = 1"));
        out.put("repairPending", count("SELECT COUNT(*) FROM repair_order WHERE is_deleted = 0 AND status = 0"));
        return Result.ok(out);
    }

    private long count(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }
}
