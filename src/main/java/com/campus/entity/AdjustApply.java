package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调课申请
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adjust_apply")
public class AdjustApply extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;
    private Long teacherId;
    private Long scheduleId;

    /** 原时间 */
    private Integer oldDayOfWeek;
    private Integer oldSectionStart;
    private Integer oldSectionEnd;
    private Integer oldWeekStart;
    private Integer oldWeekEnd;

    /** 新时间 */
    private Integer newDayOfWeek;
    private Integer newSectionStart;
    private Integer newSectionEnd;
    private Integer newWeekStart;
    private Integer newWeekEnd;
    private Long newClassroomId;

    private String reason;

    /** 0草稿 1审批中 2通过 3驳回 */
    private Integer status;

    private Long currentApprover;
    private String approveOpinion;
}
