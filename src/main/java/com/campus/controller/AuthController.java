package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.LoginDTO;
import com.campus.service.AuthService;
import com.campus.vo.LoginVO;
import com.campus.vo.RouteVO;
import com.campus.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "认证中心")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.ok("登录成功", authService.login(dto, request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        String token = auth == null ? null
                : auth.startsWith("Bearer ") ? auth.substring(7).trim() : auth.trim();
        authService.logout(token);
        return Result.ok("已退出登录", null);
    }

    @Operation(summary = "图形验证码")
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        return Result.ok(authService.captcha());
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.ok(authService.getUserInfo());
    }

    @Operation(summary = "当前用户菜单路由")
    @GetMapping("/routers")
    public Result<List<RouteVO>> routers() {
        return Result.ok(authService.getRouters());
    }

    @Operation(summary = "修改自己的密码")
    @PutMapping("/profile/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        authService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return Result.ok("密码修改成功，请重新登录", null);
    }
}
