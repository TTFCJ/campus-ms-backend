package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.dto.CourseDTO;
import com.campus.dto.CourseQuery;
import com.campus.entity.Course;
import com.campus.security.RequiresPerm;
import com.campus.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务-课程库")
@RestController
@RequestMapping("/edu/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "课程分页")
    @RequiresPerm("edu:course:list")
    @GetMapping("/page")
    public Result<PageResult<Course>> page(CourseQuery query) {
        return Result.ok(courseService.page(query));
    }

    @Operation(summary = "课程下拉列表")
    @GetMapping("/list")
    public Result<List<Course>> list() {
        return Result.ok(courseService.listAll());
    }

    @Operation(summary = "课程详情")
    @RequiresPerm("edu:course:list")
    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        return Result.ok(courseService.detail(id));
    }

    @Operation(summary = "新增课程")
    @RequiresPerm("edu:course:add")
    @OperLog(module = "课程库", type = "INSERT")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CourseDTO dto) {
        courseService.add(dto);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改课程")
    @RequiresPerm("edu:course:edit")
    @OperLog(module = "课程库", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CourseDTO dto) {
        courseService.update(dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除课程")
    @RequiresPerm("edu:course:remove")
    @OperLog(module = "课程库", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        courseService.remove(id);
        return Result.ok("删除成功", null);
    }
}
