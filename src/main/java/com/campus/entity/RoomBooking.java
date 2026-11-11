package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 场地预约
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_booking")
public class RoomBooking extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;
    private Long classroomId;
    private Long applicantId;

    /** 1教师 2学生 3部门 */
    private Integer applicantType;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String purpose;
    private Integer peopleCount;

    /** 0草稿 1审批中 2通过 3驳回 4已取消 */
    private Integer status;

    private Long currentApprover;
    private String approveOpinion;
}
