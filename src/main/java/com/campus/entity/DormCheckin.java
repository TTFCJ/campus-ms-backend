package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 归寝记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_checkin")
public class DormCheckin extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long roomId;

    private LocalDate checkDate;
    private LocalDateTime checkTime;

    /** 1正常 2晚归 3未归 4请假 */
    private Integer status;

    /** 1门禁 2手动 3扫码 */
    private Integer source;

    private String remark;
}
