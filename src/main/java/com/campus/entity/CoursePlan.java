package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 培养方案：专业 + 年级 绑定课程
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_plan")
public class CoursePlan extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long majorId;
    private String grade;
    private Long courseId;

    /** 1必修 2选修 */
    private Integer courseType;

    /** 第几学期开课 */
    private Integer semesterIndex;

    private Integer status;
}
