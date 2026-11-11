package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监考安排
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_invigilator")
public class ExamInvigilator extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;
    private Long teacherId;

    /** 1主监考 2副监考 */
    private Integer invigRole;
}
