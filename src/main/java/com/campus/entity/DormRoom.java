package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_room")
public class DormRoom extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buildingId;
    private String roomNo;
    private Integer floor;
    private Integer bedCount;
    private Integer usedCount;

    /** 1空闲 2满员 3维修中 */
    private Integer roomStatus;
}
