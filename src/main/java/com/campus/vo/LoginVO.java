package com.campus.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    /** 过期秒数 */
    private Long expiresIn;
}
