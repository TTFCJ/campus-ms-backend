package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考试安排
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long semesterId;
    private Long courseId;
    private Long scheduleId;

    private String examName;

    /** 1期末 2补考 3重修 */
    private Integer examType;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String room;
    private Integer seatCount;

    /** 0取消 1正常 2已结束 */
    private Integer status;
}
