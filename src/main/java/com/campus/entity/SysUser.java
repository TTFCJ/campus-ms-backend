package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 只允许写入（接收注册/改密），永不返回前端 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;
    private String realName;
    private String phone;
    private String email;
    private String avatar;

    /** 1超管 2校级 3教务 4教师 5辅导员 6学生 7后勤 8维修 */
    private Integer userType;

    private Long deptId;

    /** 0禁用 1正常 */
    private Integer status;

    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private String remark;
}
