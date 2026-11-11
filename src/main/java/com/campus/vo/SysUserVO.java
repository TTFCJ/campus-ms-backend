package com.campus.vo;

import com.campus.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserVO extends SysUser {

    private String deptName;
    private List<Long> roleIds;
    private List<String> roleNames;
    private String userTypeName;

    /** 学生/教师扩展信息 */
    private String stuNo;
    private String teaNo;
    private String clazzName;
}
