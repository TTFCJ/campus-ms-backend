package com.campus.security;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 登录用户上下文对象（缓存于 Redis，token 为 key）
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** sys_user.id */
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    /** 1超管 2校级 3教务 4教师 5辅导员 6学生 7后勤 8维修 */
    private Integer userType;
    /** 所属院系 */
    private Long deptId;

    /** 学生/教师业务主键（非对应身份为 null） */
    private Long studentId;
    private Long teacherId;
    /** 学生所在班级 */
    private Long clazzId;
    /** 辅导员所带班级 */
    private List<Long> counselorClazzIds;

    /** 数据范围：多角色取最宽 1全部 2本院系 3本班 4仅本人 */
    private Integer dataScope;

    private Set<String> roles = new HashSet<>();
    private Set<String> perms = new HashSet<>();

    /** JWT 签发时间，用于强制下线校验 */
    private Long loginTime;
    private String loginIp;

    public boolean hasPerm(String perm) {
        return perms != null && perms.contains(perm);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
