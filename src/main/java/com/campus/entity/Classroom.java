package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教室 / 场地
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("classroom")
public class Classroom extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String classroomNo;
    private String building;
    private Integer floor;
    private Integer capacity;

    /** 1普通教室 2机房 3实验室 4会议室 */
    private Integer roomType;

    /** 0停用 1可用 */
    private Integer status;
}
