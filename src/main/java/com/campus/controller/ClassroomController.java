package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.Classroom;
import com.campus.security.RequiresPerm;
import com.campus.service.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务-教室")
@RestController
@RequestMapping("/edu/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @Operation(summary = "教室分页")
    @RequiresPerm("edu:classroom:list")
    @GetMapping("/page")
    public Result<PageResult<Classroom>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roomType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(classroomService.page(keyword, roomType, status, current, size));
    }

    @Operation(summary = "可用教室下拉")
    @GetMapping("/list")
    public Result<List<Classroom>> list() {
        return Result.ok(classroomService.listAvailable());
    }

    @Operation(summary = "新增教室")
    @RequiresPerm("edu:classroom:add")
    @OperLog(module = "教室管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody Classroom room) {
        classroomService.add(room);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改教室")
    @RequiresPerm("edu:classroom:edit")
    @OperLog(module = "教室管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody Classroom room) {
        classroomService.update(room);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除教室")
    @RequiresPerm("edu:classroom:remove")
    @OperLog(module = "教室管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        classroomService.remove(id);
        return Result.ok("删除成功", null);
    }
}
