package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.EcActivity;
import com.campus.security.RequiresPerm;
import com.campus.service.EcService;
import com.campus.vo.EcEnrollVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "第二课堂")
@RestController
@RequestMapping("/ec")
@RequiredArgsConstructor
public class EcController {

    private final EcService ecService;

    // ---------------- 活动管理 ----------------

    @Operation(summary = "活动分页")
    @RequiresPerm("ec:activity:list")
    @GetMapping("/activity/page")
    public Result<PageResult<EcActivity>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(ecService.activityPage(status, category, keyword, current, size));
    }

    @Operation(summary = "报名中的活动（学生端）")
    @GetMapping("/activity/open")
    public Result<List<EcActivity>> open() {
        return Result.ok(ecService.openActivities());
    }

    @Operation(summary = "活动详情")
    @GetMapping("/activity/{id}")
    public Result<EcActivity> detail(@PathVariable Long id) {
        return Result.ok(ecService.activityDetail(id));
    }

    @Operation(summary = "新增活动")
    @RequiresPerm("ec:activity:add")
    @OperLog(module = "第二课堂", type = "INSERT")
    @PostMapping("/activity")
    public Result<Void> add(@RequestBody EcActivity body) {
        ecService.addActivity(body);
        return Result.ok("已创建", null);
    }

    @Operation(summary = "修改活动")
    @RequiresPerm("ec:activity:edit")
    @OperLog(module = "第二课堂", type = "UPDATE")
    @PutMapping("/activity")
    public Result<Void> update(@RequestBody EcActivity body) {
        ecService.updateActivity(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "发布活动（开放报名）")
    @RequiresPerm("ec:activity:edit")
    @OperLog(module = "第二课堂", type = "UPDATE")
    @PostMapping("/activity/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        ecService.publish(id);
        return Result.ok("已发布", null);
    }

    @Operation(summary = "删除活动")
    @RequiresPerm("ec:activity:remove")
    @OperLog(module = "第二课堂", type = "DELETE")
    @DeleteMapping("/activity/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        ecService.removeActivity(id);
        return Result.ok("已删除", null);
    }

    // ---------------- 报名 ----------------

    @Operation(summary = "报名")
    @RequiresPerm("ec:enroll:add")
    @OperLog(module = "第二课堂", type = "INSERT")
    @PostMapping("/activity/{id}/enroll")
    public Result<Void> enroll(@PathVariable Long id) {
        ecService.enroll(id);
        return Result.ok("报名成功", null);
    }

    @Operation(summary = "取消报名")
    @RequiresPerm("ec:enroll:remove")
    @OperLog(module = "第二课堂", type = "DELETE")
    @DeleteMapping("/activity/{id}/enroll")
    public Result<Void> cancel(@PathVariable Long id) {
        ecService.cancel(id);
        return Result.ok("已取消报名", null);
    }

    @Operation(summary = "签到（管理端）")
    @RequiresPerm("ec:activity:checkin")
    @OperLog(module = "第二课堂", type = "UPDATE")
    @PostMapping("/activity/{id}/checkin")
    public Result<Void> checkin(@PathVariable Long id,
                                @RequestParam Long studentId) {
        ecService.checkin(id, studentId);
        return Result.ok("已签到", null);
    }

    @Operation(summary = "我的报名")
    @RequiresPerm("ec:enroll:list")
    @GetMapping("/enroll/my")
    public Result<List<EcEnrollVO>> myEnrolls() {
        return Result.ok(ecService.myEnrolls());
    }

    @Operation(summary = "活动报名名单（管理端）")
    @RequiresPerm("ec:activity:list")
    @GetMapping("/activity/{id}/enrolls")
    public Result<List<EcEnrollVO>> enrolls(@PathVariable Long id) {
        return Result.ok(ecService.activityEnrolls(id));
    }

    @Operation(summary = "学分统计")
    @RequiresPerm({"ec:credit:stat", "ec:enroll:list"})
    @GetMapping("/credit/my")
    public Result<Map<String, Object>> credit(
            @RequestParam(required = false) Long studentId) {
        return Result.ok(ecService.creditStat(studentId));
    }
}
