package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知公告
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;

    /** 1通知 2公告 3制度 */
    private Integer noticeType;

    /** 0全校 1院系 2班级 3指定人 */
    private Integer receiveScope;

    private Long receiveDeptId;
    private Long receiveClazzId;
    private String attachment;
    private Integer isTop;
    private Long publishBy;
    private LocalDateTime publishTime;

    /** 0草稿 1已发布 2已撤回 */
    private Integer status;

    private Integer readCount;
}
