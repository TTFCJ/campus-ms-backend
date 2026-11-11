package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 第二课堂活动
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ec_activity")
public class EcActivity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 1讲座报告 2志愿服务 3社团活动 4学科竞赛 5社会实践 6文体活动 7技能特长 */
    private Integer category;

    private BigDecimal credit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer capacity;
    private Integer enrollCount;
    private String coverUrl;
    private String content;
    private Long publisherId;

    /** 0草稿 1报名中 2进行中 3已结束 4已取消 */
    private Integer status;

    /** 当前签到码 */
    private String checkinCode;
    /** 签到开始 */
    private LocalDateTime checkinStart;
    /** 签到截止 */
    private LocalDateTime checkinEnd;
}
