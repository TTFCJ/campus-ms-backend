package com.campus.dto;

import com.campus.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQuery extends PageQuery {

    /** 用户类型 1超管 2校级 3教务 4教师 5辅导员 6学生 7后勤 8维修 */
    private Integer userType;
    private Integer status;
    private Long deptId;
}
