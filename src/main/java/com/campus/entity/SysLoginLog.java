package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_login_log")
public class SysLoginLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    /** 0失败 1成功 */
    private Integer status;
    private String msg;
}
