package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报修工单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair_order")
public class RepairOrder extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long studentId;
    private Long roomId;
    private String address;

    /** 1水电 2门窗 3家具 4网络 5其他 */
    private Integer repairType;

    private String content;
    private String fileUrl;

    /** 0待派单 1维修中 2已完成 3取消 */
    private Integer status;

    private Long handlerId;
    private LocalDateTime dispatchTime;
    private LocalDateTime finishTime;
    private String finishPic;
    private Integer studentConfirm;
    private Integer score;
    private String feedback;
}
