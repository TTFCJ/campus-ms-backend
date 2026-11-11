package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 UUID（开启验证码时必填） */
    private String captchaKey;

    /** 验证码 */
    private String captcha;
}
