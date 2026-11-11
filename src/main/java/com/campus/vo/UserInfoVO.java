package com.campus.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserInfoVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer userType;
    private Long deptId;
    private String deptName;

    private Long studentId;
    private Long teacherId;
    private Long clazzId;
    private String clazzName;

    private Set<String> roles;
    private Set<String> perms;
    /** 数据范围 1全部 2本院系 3本班 4仅本人 */
    private Integer dataScope;

    private List<Long> counselorClazzIds;
}
