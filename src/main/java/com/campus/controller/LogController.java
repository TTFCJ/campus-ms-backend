package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.SysLoginLog;
import com.campus.entity.SysOperLog;
import com.campus.mapper.SysLoginLogMapper;
import com.campus.mapper.SysOperLogMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统-日志")
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class LogController {

    private final SysOperLogMapper operLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    @Operation(summary = "操作日志分页")
    @RequiresPerm("system:log:oper")
    @GetMapping("/oper/page")
    public Result<PageResult<SysOperLog>> operPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<SysOperLog> page = new Page<>(current, size);
        var w = Wrappers.<SysOperLog>lambdaQuery()
                .like(StringUtils.hasText(username), SysOperLog::getUsername, username)
                .like(StringUtils.hasText(module), SysOperLog::getOperModule, module)
                .eq(status != null, SysOperLog::getStatus, status)
                .orderByDesc(SysOperLog::getOperTime);
        Page<SysOperLog> result = operLogMapper.selectPage(page, w);
        return Result.ok(PageResult.<SysOperLog>of(result));
    }

    @Operation(summary = "登录日志分页")
    @RequiresPerm("system:log:login")
    @GetMapping("/login/page")
    public Result<PageResult<SysLoginLog>> loginPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<SysLoginLog> page = new Page<>(current, size);
        var w = Wrappers.<SysLoginLog>lambdaQuery()
                .like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                .eq(status != null, SysLoginLog::getStatus, status)
                .orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> result = loginLogMapper.selectPage(page, w);
        return Result.ok(PageResult.<SysLoginLog>of(result));
    }
}
