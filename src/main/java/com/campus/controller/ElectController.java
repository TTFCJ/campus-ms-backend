package com.campus.controller;

import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.security.RequiresPerm;
import com.campus.service.ElectService;
import com.campus.vo.TimetableItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务-选课")
@RestController
@RequestMapping("/edu/elect")
@RequiredArgsConstructor
public class ElectController {

    private final ElectService electService;

    @Operation(summary = "可选课程列表")
    @RequiresPerm({"student:elect:list", "edu:elect:list"})
    @GetMapping("/available")
    public Result<List<TimetableItem>> available(@RequestParam Long semesterId) {
        return Result.ok(electService.available(semesterId));
    }

    @Operation(summary = "选课")
    @RequiresPerm("student:elect:add")
    @OperLog(module = "选课", type = "INSERT")
    @PostMapping("/{scheduleId}")
    public Result<Void> elect(@PathVariable Long scheduleId) {
        electService.elect(scheduleId);
        return Result.ok("选课成功", null);
    }

    @Operation(summary = "退课")
    @RequiresPerm("student:elect:remove")
    @OperLog(module = "选课", type = "DELETE")
    @DeleteMapping("/{scheduleId}")
    public Result<Void> drop(@PathVariable Long scheduleId) {
        electService.drop(scheduleId);
        return Result.ok("退课成功", null);
    }

    @Operation(summary = "我的已选课程")
    @RequiresPerm("student:elect:list")
    @GetMapping("/my")
    public Result<List<TimetableItem>> my(@RequestParam Long semesterId) {
        return Result.ok(electService.myElects(semesterId));
    }
}
