package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍楼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_building")
public class DormBuilding extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String buildingNo;
    private String buildingName;

    /** 1男生 2女生 */
    private Integer gender;

    private Integer floorCount;
    private Long managerId;
    private Integer status;
}
