package com.campus.controller;

import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.Semester;
import com.campus.security.RequiresPerm;
import com.campus.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务-学期")
@RestController
@RequestMapping("/edu/semester")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @Operation(summary = "当前学期")
    @GetMapping("/current")
    public Result<Semester> current() {
        return Result.ok(semesterService.current());
    }

    @Operation(summary = "学期列表")
    @GetMapping("/list")
    public Result<List<Semester>> list() {
        return Result.ok(semesterService.list());
    }

    @Operation(summary = "新增学期")
    @RequiresPerm("system:semester:add")
    @OperLog(module = "学期管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody Semester semester) {
        semesterService.add(semester);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改学期")
    @RequiresPerm("system:semester:edit")
    @OperLog(module = "学期管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody Semester semester) {
        semesterService.update(semester);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除学期")
    @RequiresPerm("system:semester:remove")
    @OperLog(module = "学期管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        semesterService.remove(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "设为当前学期")
    @RequiresPerm("system:semester:edit")
    @OperLog(module = "学期管理", type = "UPDATE")
    @PutMapping("/{id}/current")
    public Result<Void> setCurrent(@PathVariable Long id) {
        semesterService.setCurrent(id);
        return Result.ok("已设为当前学期", null);
    }
}
