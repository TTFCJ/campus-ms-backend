package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 第二课堂报名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ec_enroll")
public class EcEnroll extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;
    private Long studentId;

    private LocalDateTime enrollTime;
    private LocalDateTime checkinTime;

    /** 1已报名 2已签到 3已取消 */
    private Integer status;
}
