package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 选课记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("elect_course")
public class ElectCourse extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long scheduleId;

    private LocalDateTime selectTime;

    /** 1已选 2已退 */
    private Integer status;
}
