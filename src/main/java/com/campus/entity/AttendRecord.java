package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 签到记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attend_record")
public class AttendRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Long studentId;
    private LocalDateTime checkinTime;

    /** 1正常 2迟到 */
    private Integer status;
}
