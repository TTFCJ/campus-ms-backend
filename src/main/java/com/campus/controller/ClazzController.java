package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.Clazz;
import com.campus.entity.Student;
import com.campus.mapper.ClazzMapper;
import com.campus.mapper.StudentMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统管理-班级")
@RestController
@RequestMapping("/system/clazz")
@RequiredArgsConstructor
public class ClazzController {

    private final ClazzMapper clazzMapper;
    private final StudentMapper studentMapper;

    @Operation(summary = "班级分页")
    @RequiresPerm("system:clazz:list")
    @GetMapping("/page")
    public Result<PageResult<Clazz>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Clazz> page = new Page<>(current, size);
        var w = Wrappers.<Clazz>lambdaQuery()
                .eq(majorId != null, Clazz::getMajorId, majorId)
                .eq(deptId != null, Clazz::getDeptId, deptId)
                .like(StringUtils.hasText(keyword), Clazz::getClazzName, keyword)
                .orderByAsc(Clazz::getGrade)
                .orderByAsc(Clazz::getClazzName);
        Page<Clazz> result = clazzMapper.selectPage(page, w);
        return Result.ok(PageResult.<Clazz>of(result));
    }

    @Operation(summary = "班级下拉（排课/学生管理用）")
    @GetMapping("/list")
    public Result<List<Clazz>> list() {
        return Result.ok(clazzMapper.selectList(Wrappers.<Clazz>lambdaQuery()
                .eq(Clazz::getStatus, 1)
                .orderByAsc(Clazz::getGrade)
                .orderByAsc(Clazz::getClazzName)));
    }

    @Operation(summary = "新增班级")
    @RequiresPerm("system:clazz:add")
    @OperLog(module = "班级管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody Clazz clazz) {
        clazzMapper.insert(clazz);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改班级")
    @RequiresPerm("system:clazz:edit")
    @OperLog(module = "班级管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody Clazz clazz) {
        clazzMapper.updateById(clazz);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除班级")
    @RequiresPerm("system:clazz:remove")
    @OperLog(module = "班级管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        Long students = studentMapper.selectCount(
                Wrappers.<Student>lambdaQuery().eq(Student::getClazzId, id));
        if (students != null && students > 0) {
            throw new com.campus.common.exception.BizException(
                    "该班级还有 " + students + " 名学生，无法删除");
        }
        clazzMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }
}
