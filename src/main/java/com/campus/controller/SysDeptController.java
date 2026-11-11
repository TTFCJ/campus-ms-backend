package com.campus.controller;

import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.SysDept;
import com.campus.security.RequiresPerm;
import com.campus.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统管理-院系")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @Operation(summary = "院系树")
    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        return Result.ok(deptService.tree());
    }

    @Operation(summary = "院系平铺列表")
    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.list());
    }

    @Operation(summary = "新增院系")
    @RequiresPerm("system:dept:add")
    @OperLog(module = "院系管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody SysDept dept) {
        deptService.add(dept);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改院系")
    @RequiresPerm("system:dept:edit")
    @OperLog(module = "院系管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody SysDept dept) {
        deptService.update(dept);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除院系")
    @RequiresPerm("system:dept:remove")
    @OperLog(module = "院系管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        deptService.remove(id);
        return Result.ok("删除成功", null);
    }
}
