package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
public class SysOperLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String operModule;
    private String operType;
    private String method;
    private String requestUri;
    private String requestMethod;
    private String ip;
    private LocalDateTime operTime;
    private String requestParam;
    private String result;
    private Long costTime;
    /** 0失败 1成功 */
    private Integer status;
    private String errorMsg;
}
