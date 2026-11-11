package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.AdjustApply;
import com.campus.entity.Evaluation;
import com.campus.entity.Schedule;
import com.campus.mapper.AdjustApplyMapper;
import com.campus.mapper.EvaluationMapper;
import com.campus.mapper.ScheduleMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Tag(name = "教务-调课与评教")
@RestController
@RequestMapping("/edu")
@RequiredArgsConstructor
public class AdjustController {

    private final AdjustApplyMapper adjustMapper;
    private final ScheduleMapper scheduleMapper;
    private final EvaluationMapper evaluationMapper;

    // ---------------- 调课 ----------------

    @Operation(summary = "调课分页")
    @RequiresPerm({"edu:adjust:list", "teacher:adjust:list"})
    @GetMapping("/adjust/page")
    public Result<PageResult<AdjustApply>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<AdjustApply> page = new Page<>(current, size);
        var w = Wrappers.<AdjustApply>lambdaQuery()
                .eq(status != null, AdjustApply::getStatus, status)
                .eq(teacherId != null, AdjustApply::getTeacherId, teacherId)
                .orderByDesc(AdjustApply::getCreateTime);
        Page<AdjustApply> result = adjustMapper.selectPage(page, w);
        return Result.ok(PageResult.<AdjustApply>of(result));
    }

    @Operation(summary = "提交调课申请")
    @RequiresPerm("teacher:adjust:add")
    @OperLog(module = "调课", type = "INSERT")
    @PostMapping("/adjust")
    public Result<Void> submit(@RequestBody AdjustApply body) {
        body.setId(null);
        body.setApplyNo("ADJ" + System.currentTimeMillis());
        body.setStatus(1);
        adjustMapper.insert(body);
        return Result.ok("已提交，等待教务审批", null);
    }

    @Operation(summary = "审批调课（通过后自动改课表）")
    @RequiresPerm("edu:adjust:audit")
    @OperLog(module = "调课", type = "APPROVE")
    @PutMapping("/adjust/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        AdjustApply apply = adjustMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        int result = body.get("result") instanceof Number n ? n.intValue() : 1;
        String opinion = (String) body.getOrDefault("opinion", "");

        if (result == 1) {
            Schedule s = scheduleMapper.selectById(apply.getScheduleId());
            if (s == null) {
                throw new BizException("原排课记录不存在");
            }
            s.setDayOfWeek(apply.getNewDayOfWeek());
            s.setSectionStart(apply.getNewSectionStart());
            s.setSectionEnd(apply.getNewSectionEnd());
            if (apply.getNewWeekStart() != null) {
                s.setWeekStart(apply.getNewWeekStart());
            }
            if (apply.getNewWeekEnd() != null) {
                s.setWeekEnd(apply.getNewWeekEnd());
            }
            if (apply.getNewClassroomId() != null) {
                s.setClassroomId(apply.getNewClassroomId());
            }
            scheduleMapper.updateById(s);
            apply.setStatus(2);
        } else {
            apply.setStatus(3);
        }
        apply.setApproveOpinion(opinion);
        adjustMapper.updateById(apply);
        return Result.ok(result == 1 ? "已通过，课表已更新" : "已驳回", null);
    }

    // ---------------- 评教 ----------------

    @Operation(summary = "评教记录分页")
    @RequiresPerm("edu:evaluation:list")
    @GetMapping("/evaluation/page")
    public Result<PageResult<Evaluation>> page(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Evaluation> page = new Page<>(current, size);
        var w = Wrappers.<Evaluation>lambdaQuery()
                .eq(teacherId != null, Evaluation::getTeacherId, teacherId)
                .eq(semesterId != null, Evaluation::getSemesterId, semesterId)
                .orderByDesc(Evaluation::getCreateTime);
        Page<Evaluation> result = evaluationMapper.selectPage(page, w);
        return Result.ok(PageResult.<Evaluation>of(result));
    }

    @Operation(summary = "评教统计（按教师）")
    @RequiresPerm("edu:evaluation:stat")
    @GetMapping("/evaluation/stat")
    public Result<Map<String, Object>> stat(@RequestParam(required = false) Long teacherId) {
        List<Evaluation> list = evaluationMapper.selectList(
                Wrappers.<Evaluation>lambdaQuery()
                        .eq(teacherId != null, Evaluation::getTeacherId, teacherId));

        if (list.isEmpty()) {
            return Result.ok(Map.of("count", 0, "avgScore", 0));
        }
        double avg = list.stream()
                .map(Evaluation::getTotalScore)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", list.size());
        out.put("avgScore", Math.round(avg * 100) / 100.0);
        return Result.ok(out);
    }

    @Operation(summary = "学生提交评教")
    @PostMapping("/evaluation")
    public Result<Void> submitEvaluation(@RequestBody Evaluation body) {
        body.setId(null);
        BigDecimal total = BigDecimal.valueOf(
                (body.getTeachScore() == null ? 0 : body.getTeachScore())
                        + (body.getContentScore() == null ? 0 : body.getContentScore())
                        + (body.getAttitudeScore() == null ? 0 : body.getAttitudeScore()));
        // 三个维度各 20 分满，合计 60，放大到百分制
        body.setTotalScore(total.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP));
        body.setStatus(1);
        evaluationMapper.insert(body);
        return Result.ok("评价已提交", null);
    }
}
