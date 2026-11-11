package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.dto.ScheduleDTO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.entity.Schedule;
import com.campus.entity.Semester;
import com.campus.mapper.SemesterMapper;
import com.campus.security.RequiresPerm;
import com.campus.service.ScheduleService;
import com.campus.vo.TimetableItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务-排课与课表")
@RestController
@RequestMapping("/edu")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final SemesterMapper semesterMapper;

    /** 没传学期时用当前学期 */
    private Long currentSemesterId() {
        Semester s = semesterMapper.selectOne(Wrappers.<Semester>lambdaQuery()
                .eq(Semester::getIsCurrent, 1)
                .last("LIMIT 1"));
        return s == null ? null : s.getId();
    }

    // ---------------- 排课管理 ----------------

    @Operation(summary = "排课分页")
    @RequiresPerm("edu:schedule:list")
    @GetMapping("/schedule/page")
    public Result<PageResult<Schedule>> page(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(scheduleService.page(semesterId, teacherId, clazzId,
                courseId, current, size));
    }

    @Operation(summary = "新增排课（自动做班级/教师/教室冲突检测）")
    @RequiresPerm("edu:schedule:add")
    @OperLog(module = "排课管理", type = "INSERT")
    @PostMapping("/schedule")
    public Result<Void> add(@Valid @RequestBody ScheduleDTO dto) {
        scheduleService.add(dto);
        return Result.ok("排课成功", null);
    }

    @Operation(summary = "修改排课")
    @RequiresPerm("edu:schedule:edit")
    @OperLog(module = "排课管理", type = "UPDATE")
    @PutMapping("/schedule")
    public Result<Void> update(@Valid @RequestBody ScheduleDTO dto) {
        scheduleService.update(dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除排课")
    @RequiresPerm("edu:schedule:remove")
    @OperLog(module = "排课管理", type = "DELETE")
    @DeleteMapping("/schedule/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        scheduleService.remove(id);
        return Result.ok("删除成功", null);
    }

    // ---------------- 课表查询 ----------------

    @Operation(summary = "班级课表")
    @RequiresPerm("edu:timetable:view")
    @GetMapping("/timetable/clazz")
    public Result<List<TimetableItem>> clazzTimetable(
            @RequestParam(required = false) Long semesterId,
            @RequestParam Long clazzId) {
        Long sid = semesterId != null ? semesterId : currentSemesterId();
        return Result.ok(scheduleService.clazzTimetable(sid, clazzId));
    }

    @Operation(summary = "教师课表")
    @GetMapping("/timetable/teacher")
    public Result<List<TimetableItem>> teacherTimetable(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long teacherId) {
        Long sid = semesterId != null ? semesterId : currentSemesterId();
        return Result.ok(scheduleService.teacherTimetable(sid, teacherId));
    }

    @Operation(summary = "我的课表（学生）")
    @GetMapping("/timetable/my")
    public Result<List<TimetableItem>> myTimetable(
            @RequestParam(required = false) Long semesterId) {
        Long sid = semesterId != null ? semesterId : currentSemesterId();
        return Result.ok(scheduleService.studentTimetable(sid));
    }
}
