package com.campus.controller;

import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.SysMenu;
import com.campus.security.RequiresPerm;
import com.campus.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统管理-菜单")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "菜单树")
    @RequiresPerm("system:menu:list")
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree(@RequestParam(required = false) String keyword) {
        return Result.ok(menuService.tree(keyword));
    }

    @Operation(summary = "菜单列表（平铺）")
    @RequiresPerm("system:menu:list")
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.ok(menuService.list());
    }

    @Operation(summary = "新增菜单")
    @RequiresPerm("system:menu:add")
    @OperLog(module = "菜单管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        menuService.add(menu);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改菜单")
    @RequiresPerm("system:menu:edit")
    @OperLog(module = "菜单管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.update(menu);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除菜单")
    @RequiresPerm("system:menu:remove")
    @OperLog(module = "菜单管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        menuService.remove(id);
        return Result.ok("删除成功", null);
    }
}
