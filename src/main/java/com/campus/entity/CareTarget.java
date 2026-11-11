package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关爱对象（学生关爱平台）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("care_target")
public class CareTarget extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 标签，逗号分隔：经济困难,学业预警,心理关注 */
    private String tags;

    /** 1一般 2关注 3重点 */
    private Integer riskLevel;

    /** 1在册 2已解除 */
    private Integer status;

    private String reason;

    private Long counselorId;
}
