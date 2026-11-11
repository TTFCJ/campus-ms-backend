package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假申请
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("leave_apply")
public class LeaveApply extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;
    private Long studentId;
    private Long clazzId;

    /** 1事假 2病假 3其他 */
    private Integer leaveType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal days;

    private String reason;
    private String fileUrl;

    /** 0草稿 1辅导员审批中 2学工审批中 3通过 4驳回 5已销假 */
    private Integer status;

    private Long currentApprover;
    private String approveOpinion;
    private LocalDateTime approveTime;
}
