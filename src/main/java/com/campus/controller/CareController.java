package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.dto.CareRecordDTO;
import com.campus.dto.CareTargetDTO;
import com.campus.security.RequiresPerm;
import com.campus.service.CareService;
import com.campus.vo.CareRecordVO;
import com.campus.vo.CareTargetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学生关爱")
@RestController
@RequestMapping("/care")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;

    @Operation(summary = "关爱对象分页")
    @RequiresPerm("care:target:list")
    @GetMapping("/target/page")
    public Result<PageResult<CareTargetVO>> page(
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(careService.targetPage(riskLevel, status, keyword, current, size));
    }

    @Operation(summary = "关爱对象详情")
    @RequiresPerm("care:target:list")
    @GetMapping("/target/{id}")
    public Result<CareTargetVO> detail(@PathVariable Long id) {
        return Result.ok(careService.targetDetail(id));
    }

    @Operation(summary = "纳入关爱")
    @RequiresPerm("care:target:add")
    @OperLog(module = "学生关爱", type = "INSERT")
    @PostMapping("/target")
    public Result<Void> add(@RequestBody CareTargetDTO dto) {
        careService.addTarget(dto);
        return Result.ok("已纳入关爱名单", null);
    }

    @Operation(summary = "修改关爱档案")
    @RequiresPerm("care:target:edit")
    @OperLog(module = "学生关爱", type = "UPDATE")
    @PutMapping("/target")
    public Result<Void> update(@RequestBody CareTargetDTO dto) {
        careService.updateTarget(dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "解除关爱")
    @RequiresPerm("care:target:remove")
    @OperLog(module = "学生关爱", type = "UPDATE")
    @PutMapping("/target/{id}/release")
    public Result<Void> release(@PathVariable Long id) {
        careService.releaseTarget(id);
        return Result.ok("已解除关爱", null);
    }

    @Operation(summary = "关爱记录列表")
    @RequiresPerm("care:record:list")
    @GetMapping("/record/list")
    public Result<List<CareRecordVO>> records(
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long studentId) {
        return Result.ok(careService.recordList(targetId, studentId));
    }

    @Operation(summary = "新增关爱记录")
    @RequiresPerm("care:record:add")
    @OperLog(module = "学生关爱", type = "INSERT")
    @PostMapping("/record")
    public Result<Void> addRecord(@RequestBody CareRecordDTO dto) {
        careService.addRecord(dto);
        return Result.ok("记录已保存", null);
    }

    @Operation(summary = "删除关爱记录")
    @RequiresPerm("care:record:remove")
    @OperLog(module = "学生关爱", type = "DELETE")
    @DeleteMapping("/record/{id}")
    public Result<Void> removeRecord(@PathVariable Long id) {
        careService.removeRecord(id);
        return Result.ok("已删除", null);
    }
}
