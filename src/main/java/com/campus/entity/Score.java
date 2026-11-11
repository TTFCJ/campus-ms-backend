package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 成绩
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("score")
public class Score extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long scheduleId;

    /** 冗余字段，报表免 join */
    private Long courseId;
    private Long semesterId;

    /** 1期末 2补考 3重修 4清考 */
    private Integer examType;

    /** 百分制 */
    private BigDecimal score;

    /** 绩点 */
    private BigDecimal gradePoint;

    /**
     * 0待录入 1已录入 2已提交 3复核中 4已锁定
     * 只有 0/1 可改，2 之后要教务解锁
     */
    private Integer status;

    private String remark;
}
