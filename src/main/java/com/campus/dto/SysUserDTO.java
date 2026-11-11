package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SysUserDTO {

    private Long id;

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,30}$", message = "账号只能包含字母、数字、下划线，3-30位")
    private String username;

    /** 新增时不填则用默认密码 123456；修改时忽略 */
    @Size(min = 6, max = 30, message = "密码长度 6-30 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String nickname;

    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;
    private String avatar;

    /** 1超管 2校级 3教务 4教师 5辅导员 6学生 7后勤 8维修 */
    private Integer userType;

    private Long deptId;
    private Integer status;
    private String remark;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
