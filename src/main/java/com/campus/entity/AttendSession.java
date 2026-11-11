package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课堂签到场次
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attend_session")
public class AttendSession extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;
    private Long teacherId;

    /** 6 位签到码 */
    private String code;

    private LocalDate sessionDate;
    private Integer sectionStart;
    private Integer sectionEnd;

    private LocalDateTime openTime;
    private LocalDateTime closeTime;

    /** 1进行中 2已结束 */
    private Integer status;
}
