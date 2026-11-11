package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.dto.SysUserDTO;
import com.campus.dto.SysUserQuery;
import com.campus.security.RequiresPerm;
import com.campus.service.SysUserService;
import com.campus.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户分页列表")
    @RequiresPerm("system:user:list")
    @GetMapping("/page")
    public Result<PageResult<SysUserVO>> page(SysUserQuery query) {
        return Result.ok(userService.page(query));
    }

    @Operation(summary = "用户详情")
    @RequiresPerm("system:user:list")
    @GetMapping("/{id}")
    public Result<SysUserVO> detail(@PathVariable Long id) {
        return Result.ok(userService.detail(id));
    }

    @Operation(summary = "新增用户")
    @RequiresPerm("system:user:add")
    @OperLog(module = "用户管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysUserDTO dto) {
        userService.add(dto);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改用户")
    @RequiresPerm("system:user:edit")
    @OperLog(module = "用户管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysUserDTO dto) {
        userService.update(dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除用户")
    @RequiresPerm("system:user:remove")
    @OperLog(module = "用户管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        userService.remove(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "启用/禁用")
    @RequiresPerm("system:user:edit")
    @OperLog(module = "用户管理", type = "UPDATE")
    @PutMapping("/{id}/status/{status}")
    public Result<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.changeStatus(id, status);
        return Result.ok(status == 1 ? "已启用" : "已禁用", null);
    }

    @Operation(summary = "重置密码")
    @RequiresPerm("system:user:resetPwd")
    @OperLog(module = "用户管理", type = "UPDATE")
    @PutMapping("/{id}/resetPwd")
    public Result<Map<String, String>> resetPwd(@PathVariable Long id) {
        String pwd = userService.resetPassword(id);
        return Result.ok("密码已重置", Map.of("password", pwd));
    }

    @Operation(summary = "查询用户角色")
    @RequiresPerm("system:user:list")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> roles(@PathVariable Long id) {
        return Result.ok(userService.getRoleIds(id));
    }

    @Operation(summary = "分配角色")
    @RequiresPerm("system:user:edit")
    @OperLog(module = "用户管理", type = "UPDATE")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.ok("分配成功", null);
    }
}
