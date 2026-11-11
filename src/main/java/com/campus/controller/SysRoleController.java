package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.dto.SysRoleDTO;
import com.campus.entity.SysRole;
import com.campus.security.RequiresPerm;
import com.campus.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "角色分页")
    @RequiresPerm("system:role:list")
    @GetMapping("/page")
    public Result<PageResult<SysRole>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") Long current,
                                            @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(roleService.page(keyword, current, size));
    }

    @Operation(summary = "全部角色（下拉用）")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    @Operation(summary = "新增角色")
    @RequiresPerm("system:role:add")
    @OperLog(module = "角色管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysRoleDTO dto) {
        roleService.add(dto);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改角色")
    @RequiresPerm("system:role:edit")
    @OperLog(module = "角色管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysRoleDTO dto) {
        roleService.update(dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除角色")
    @RequiresPerm("system:role:remove")
    @OperLog(module = "角色管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        roleService.remove(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "角色已授权菜单ID")
    @RequiresPerm("system:role:list")
    @GetMapping("/{id}/menuIds")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.ok(roleService.getMenuIds(id));
    }

    @Operation(summary = "给角色分配菜单")
    @RequiresPerm("system:role:auth")
    @OperLog(module = "角色管理", type = "UPDATE")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.ok("权限分配成功", null);
    }
}
