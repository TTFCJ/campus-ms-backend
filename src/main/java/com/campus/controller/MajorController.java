package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.Clazz;
import com.campus.entity.Major;
import com.campus.mapper.ClazzMapper;
import com.campus.mapper.MajorMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统管理-专业")
@RestController
@RequestMapping("/system/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorMapper majorMapper;
    private final ClazzMapper clazzMapper;

    @Operation(summary = "专业分页")
    @RequiresPerm("system:major:list")
    @GetMapping("/page")
    public Result<PageResult<Major>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Major> page = new Page<>(current, size);
        var w = Wrappers.<Major>lambdaQuery()
                .eq(deptId != null, Major::getDeptId, deptId)
                .and(StringUtils.hasText(keyword), q -> q
                        .like(Major::getMajorName, keyword)
                        .or().like(Major::getMajorCode, keyword))
                .orderByAsc(Major::getMajorCode);
        Page<Major> result = majorMapper.selectPage(page, w);
        return Result.ok(PageResult.<Major>of(result));
    }

    @Operation(summary = "专业下拉")
    @GetMapping("/list")
    public Result<List<Major>> list() {
        return Result.ok(majorMapper.selectList(Wrappers.<Major>lambdaQuery()
                .eq(Major::getStatus, 1)
                .orderByAsc(Major::getMajorCode)));
    }

    @Operation(summary = "新增专业")
    @RequiresPerm("system:major:add")
    @OperLog(module = "专业管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody Major major) {
        majorMapper.insert(major);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改专业")
    @RequiresPerm("system:major:edit")
    @OperLog(module = "专业管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody Major major) {
        majorMapper.updateById(major);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除专业")
    @RequiresPerm("system:major:remove")
    @OperLog(module = "专业管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        Long clazzCount = clazzMapper.selectCount(
                Wrappers.<Clazz>lambdaQuery().eq(Clazz::getMajorId, id));
        if (clazzCount != null && clazzCount > 0) {
            throw new BizException("该专业下还有 " + clazzCount + " 个班级，无法删除");
        }
        majorMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }
}
