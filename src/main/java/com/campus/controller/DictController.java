package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.SysDictData;
import com.campus.entity.SysDictType;
import com.campus.mapper.SysDictDataMapper;
import com.campus.mapper.SysDictTypeMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统-数据字典")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;

    // ---------------- 字典类型 ----------------

    @Operation(summary = "字典类型列表")
    @RequiresPerm("system:dict:list")
    @GetMapping("/type/list")
    public Result<List<SysDictType>> typeList() {
        return Result.ok(typeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .orderByAsc(SysDictType::getDictType)));
    }

    @Operation(summary = "新增字典类型")
    @RequiresPerm("system:dict:add")
    @OperLog(module = "数据字典", type = "INSERT")
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody SysDictType body) {
        typeMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改字典类型")
    @RequiresPerm("system:dict:edit")
    @OperLog(module = "数据字典", type = "UPDATE")
    @PutMapping("/type")
    public Result<Void> updateType(@RequestBody SysDictType body) {
        typeMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除字典类型")
    @RequiresPerm("system:dict:remove")
    @OperLog(module = "数据字典", type = "DELETE")
    @DeleteMapping("/type/{id}")
    public Result<Void> removeType(@PathVariable Long id) {
        typeMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }

    // ---------------- 字典数据 ----------------

    @Operation(summary = "按类型查字典数据（前端下拉用）")
    @GetMapping("/data/{dictType}")
    public Result<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return Result.ok(dataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort)));
    }

    @Operation(summary = "字典数据列表")
    @RequiresPerm("system:dict:list")
    @GetMapping("/data/list")
    public Result<List<SysDictData>> dataList(@RequestParam(required = false) String dictType) {
        return Result.ok(dataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .eq(dictType != null, SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictType)
                .orderByAsc(SysDictData::getSort)));
    }

    @Operation(summary = "新增字典数据")
    @RequiresPerm("system:dict:add")
    @OperLog(module = "数据字典", type = "INSERT")
    @PostMapping("/data")
    public Result<Void> addData(@RequestBody SysDictData body) {
        dataMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改字典数据")
    @RequiresPerm("system:dict:edit")
    @OperLog(module = "数据字典", type = "UPDATE")
    @PutMapping("/data")
    public Result<Void> updateData(@RequestBody SysDictData body) {
        dataMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除字典数据")
    @RequiresPerm("system:dict:remove")
    @OperLog(module = "数据字典", type = "DELETE")
    @DeleteMapping("/data/{id}")
    public Result<Void> removeData(@PathVariable Long id) {
        dataMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }
}
