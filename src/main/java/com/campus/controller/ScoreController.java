package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.dto.ScoreItem;
import com.campus.security.RequiresPerm;
import com.campus.service.ScoreService;
import com.campus.vo.ScoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "教务-成绩")
@RestController
@RequestMapping("/edu/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @Operation(summary = "成绩分页（教务）")
    @RequiresPerm("edu:score:list")
    @GetMapping("/page")
    public Result<PageResult<ScoreVO>> page(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer examType,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(scoreService.page(semesterId, scheduleId, clazzId,
                studentId, examType, current, size));
    }

    @Operation(summary = "教学班成绩名单（含未录入的学生）")
    @RequiresPerm({"teacher:score:list", "edu:score:list"})
    @GetMapping("/schedule/{scheduleId}")
    public Result<List<ScoreVO>> bySchedule(@PathVariable Long scheduleId) {
        return Result.ok(scoreService.listBySchedule(scheduleId));
    }

    @Operation(summary = "批量录入/修改成绩")
    @RequiresPerm({"teacher:score:add", "edu:score:add"})
    @OperLog(module = "成绩管理", type = "UPDATE")
    @PutMapping("/schedule/{scheduleId}")
    public Result<Void> batchSave(@PathVariable Long scheduleId,
                                  @RequestBody List<ScoreItem> items) {
        scoreService.batchSave(scheduleId, items);
        return Result.ok("保存成功", null);
    }

    @Operation(summary = "提交成绩（提交后教师不可改）")
    @RequiresPerm("teacher:score:submit")
    @OperLog(module = "成绩管理", type = "UPDATE")
    @PostMapping("/schedule/{scheduleId}/submit")
    public Result<Void> submit(@PathVariable Long scheduleId) {
        scoreService.submit(scheduleId);
        return Result.ok("成绩已提交", null);
    }

    @Operation(summary = "教务复核：锁定 / 解锁")
    @RequiresPerm("edu:score:review")
    @OperLog(module = "成绩管理", type = "UPDATE")
    @PostMapping("/schedule/{scheduleId}/review")
    public Result<Void> review(@PathVariable Long scheduleId,
                               @RequestBody Map<String, Boolean> body) {
        boolean lock = Boolean.TRUE.equals(body.get("lock"));
        scoreService.review(scheduleId, lock);
        return Result.ok(lock ? "已锁定" : "已解锁，教师可修改", null);
    }

    @Operation(summary = "我的成绩（学生）")
    @RequiresPerm("student:score:list")
    @GetMapping("/my")
    public Result<List<ScoreVO>> my(@RequestParam(required = false) Long semesterId) {
        return Result.ok(scoreService.myScores(semesterId));
    }
}
