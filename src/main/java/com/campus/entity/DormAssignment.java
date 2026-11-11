package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 宿舍分配
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_assignment")
public class DormAssignment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long roomId;
    private String bedNo;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    /** 1在住 2已退宿 */
    private Integer status;
}
