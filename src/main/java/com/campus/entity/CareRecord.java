package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 关爱记录（谈心 / 家访 / 帮扶 / 预警处置）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("care_record")
public class CareRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long targetId;
    private Long studentId;

    /** 1谈心谈话 2家访 3帮扶 4预警处置 */
    private Integer recordType;

    private String title;
    private String content;
    private String result;

    private Long recorderId;
    private LocalDateTime recordTime;
    private String attachment;
}
