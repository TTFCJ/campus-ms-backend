package com.campus.dto;

import com.campus.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQuery extends PageQuery {
    private Long deptId;
    /** 1必修 2选修 3公共课 */
    private Integer courseType;
    private Integer status;
}
