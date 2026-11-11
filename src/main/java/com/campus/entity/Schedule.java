package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排课（一条记录 = 某班某课某时段）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule")
public class Schedule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long semesterId;
    private Long courseId;
    private Long teacherId;
    private Long clazzId;
    private Long classroomId;

    /** 教室名称（冗余，避免每次 join） */
    private String classroom;

    /** 起始周 / 结束周 */
    private Integer weekStart;
    private Integer weekEnd;

    /** 星期几 1-7 */
    private Integer dayOfWeek;

    /** 节次区间 */
    private Integer sectionStart;
    private Integer sectionEnd;

    /** 选课人数上限 / 当前已选 */
    private Integer maxNum;
    private Integer currentNum;

    /** 0停排 1正常 */
    private Integer status;
}
