package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 奖惩记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("award_punish")
public class AwardPunish extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 1奖励 2处分 */
    private Integer type;

    private String level;
    private String title;
    private String content;
    private LocalDate occurDate;
    private String fileUrl;
    private Long recorderId;
    private Integer status;
}
