package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.CoursePlan;
import com.campus.entity.Exam;
import com.campus.entity.ExamInvigilator;
import com.campus.mapper.CoursePlanMapper;
import com.campus.mapper.ExamInvigilatorMapper;
import com.campus.mapper.ExamMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务-考试与培养方案")
@RestController
@RequestMapping("/edu")
@RequiredArgsConstructor
public class ExamController {

    private final ExamMapper examMapper;
    private final ExamInvigilatorMapper invigilatorMapper;
    private final CoursePlanMapper planMapper;

    // ---------------- 考试 ----------------

    @Operation(summary = "考试分页")
    @RequiresPerm("edu:exam:list")
    @GetMapping("/exam/page")
    public Result<PageResult<Exam>> page(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Integer examType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Exam> page = new Page<>(current, size);
        var w = Wrappers.<Exam>lambdaQuery()
                .eq(semesterId != null, Exam::getSemesterId, semesterId)
                .eq(examType != null, Exam::getExamType, examType)
                .eq(status != null, Exam::getStatus, status)
                .orderByAsc(Exam::getExamDate)
                .orderByAsc(Exam::getStartTime);
        Page<Exam> result = examMapper.selectPage(page, w);
        return Result.ok(PageResult.<Exam>of(result));
    }

    @Operation(summary = "新增考试")
    @RequiresPerm("edu:exam:add")
    @OperLog(module = "考试安排", type = "INSERT")
    @PostMapping("/exam")
    public Result<Void> addExam(@RequestBody Exam body) {
        body.setId(null);
        if (body.getStatus() == null) {
            body.setStatus(1);
        }
        examMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改考试")
    @RequiresPerm("edu:exam:edit")
    @OperLog(module = "考试安排", type = "UPDATE")
    @PutMapping("/exam")
    public Result<Void> updateExam(@RequestBody Exam body) {
        examMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除考试")
    @RequiresPerm("edu:exam:edit")
    @OperLog(module = "考试安排", type = "DELETE")
    @DeleteMapping("/exam/{id}")
    public Result<Void> removeExam(@PathVariable Long id) {
        examMapper.deleteById(id);
        invigilatorMapper.delete(Wrappers.<ExamInvigilator>lambdaQuery()
                .eq(ExamInvigilator::getExamId, id));
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "监考安排")
    @RequiresPerm("edu:exam:invigilate")
    @PostMapping("/exam/{id}/invigilators")
    public Result<Void> setInvigilators(@PathVariable Long id,
                                        @RequestBody List<Long> teacherIds) {
        invigilatorMapper.delete(Wrappers.<ExamInvigilator>lambdaQuery()
                .eq(ExamInvigilator::getExamId, id));
        for (Long tid : teacherIds) {
            ExamInvigilator e = new ExamInvigilator();
            e.setExamId(id);
            e.setTeacherId(tid);
            e.setInvigRole(1);
            invigilatorMapper.insert(e);
        }
        return Result.ok("监考已安排", null);
    }

    @Operation(summary = "监考名单")
    @GetMapping("/exam/{id}/invigilators")
    public Result<List<ExamInvigilator>> getInvigilators(@PathVariable Long id) {
        return Result.ok(invigilatorMapper.selectList(
                Wrappers.<ExamInvigilator>lambdaQuery()
                        .eq(ExamInvigilator::getExamId, id)));
    }

    // ---------------- 培养方案 ----------------

    @Operation(summary = "培养方案分页")
    @RequiresPerm("edu:plan:list")
    @GetMapping("/plan/page")
    public Result<PageResult<CoursePlan>> planPage(
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<CoursePlan> page = new Page<>(current, size);
        var w = Wrappers.<CoursePlan>lambdaQuery()
                .eq(majorId != null, CoursePlan::getMajorId, majorId)
                .eq(grade != null, CoursePlan::getGrade, grade)
                .orderByAsc(CoursePlan::getSemesterIndex);
        Page<CoursePlan> result = planMapper.selectPage(page, w);
        return Result.ok(PageResult.<CoursePlan>of(result));
    }

    @Operation(summary = "新增培养方案")
    @RequiresPerm("edu:plan:add")
    @OperLog(module = "培养方案", type = "INSERT")
    @PostMapping("/plan")
    public Result<Void> addPlan(@RequestBody CoursePlan body) {
        body.setId(null);
        planMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "删除培养方案")
    @RequiresPerm("edu:plan:remove")
    @OperLog(module = "培养方案", type = "DELETE")
    @DeleteMapping("/plan/{id}")
    public Result<Void> removePlan(@PathVariable Long id) {
        planMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }
}
