package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 教学评教
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("evaluation")
public class Evaluation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long semesterId;
    private Long studentId;
    private Long scheduleId;
    private Long teacherId;

    /** 三个维度各自的分数（0-20） */
    private Integer teachScore;
    private Integer contentScore;
    private Integer attitudeScore;

    /** 百分制总分 */
    private BigDecimal totalScore;

    private String comment;

    /** 1有效 0无效 */
    private Integer status;
}
