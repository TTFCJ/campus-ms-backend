package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程编号 C090101 */
    private String courseCode;

    /** 课程名称 */
    private String courseName;

    /** 开课院系 */
    private Long deptId;

    /** 学分 */
    private BigDecimal credit;

    /** 学时 */
    private Integer classHour;

    /** 1必修 2选修 3公共课 */
    private Integer courseType;

    /** 1考试 2考查 */
    private Integer examType;

    /** 0停开 1正常 */
    private Integer status;

    private String remark;
}
