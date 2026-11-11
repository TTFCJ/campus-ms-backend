package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.DormCheckin;
import com.campus.security.RequiresPerm;
import com.campus.service.DormService;
import com.campus.vo.DormCheckinVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "宿舍归寝")
@RestController
@RequestMapping("/dorm/checkin")
@RequiredArgsConstructor
public class DormController {

    private final DormService dormService;

    @Operation(summary = "归寝记录分页")
    @RequiresPerm("dorm:checkin:list")
    @GetMapping("/page")
    public Result<PageResult<DormCheckinVO>> page(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(dormService.page(date, status, clazzId, keyword, current, size));
    }

    @Operation(summary = "登记归寝（已有记录则覆盖）")
    @RequiresPerm("dorm:checkin:add")
    @OperLog(module = "宿舍归寝", type = "INSERT")
    @PostMapping
    public Result<Void> save(@RequestBody DormCheckin body) {
        dormService.save(body);
        return Result.ok("已登记", null);
    }

    @Operation(summary = "删除归寝记录")
    @RequiresPerm("dorm:checkin:edit")
    @OperLog(module = "宿舍归寝", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        dormService.remove(id);
        return Result.ok("已删除", null);
    }

    @Operation(summary = "归寝统计（正常/晚归/未归/请假）")
    @RequiresPerm("dorm:checkin:list")
    @GetMapping("/stat")
    public Result<Map<String, Object>> stat(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(dormService.stat(date));
    }

    @Operation(summary = "异常预警（近 N 天晚归/未归）")
    @RequiresPerm("dorm:checkin:alert")
    @GetMapping("/alerts")
    public Result<List<DormCheckinVO>> alerts(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(dormService.alerts(days));
    }

    @Operation(summary = "我的归寝记录（学生）")
    @GetMapping("/my")
    public Result<List<DormCheckinVO>> my(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(dormService.myCheckins(days));
    }
}
