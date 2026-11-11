package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学期学年
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("semester")
public class Semester extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 2025-2026 */
    private String academicYear;

    /** 1第一学期 2第二学期 3小学期 */
    private Integer term;

    /** 2025-2026-1 */
    private String semesterName;

    private LocalDate startDate;
    private LocalDate endDate;

    /** 选课时间窗 */
    private LocalDateTime selectStart;
    private LocalDateTime selectEnd;

    /** 是否当前学期 0否 1是 */
    private Integer isCurrent;

    /** 0未开始 1进行中 2已结束 */
    private Integer status;
}
