package com.campus.vo;

import lombok.Data;

/**
 * 课表条目：前端拿到后按 dayOfWeek + sectionStart 排格子
 */
@Data
public class TimetableItem {

    private Long scheduleId;
    private Long courseId;
    private String courseName;
    private String courseCode;

    private Long teacherId;
    private String teacherName;

    private Long clazzId;
    private String clazzName;

    private String classroom;

    private Integer dayOfWeek;
    private Integer sectionStart;
    private Integer sectionEnd;
    private Integer weekStart;
    private Integer weekEnd;

    private Integer currentNum;
    private Integer maxNum;
}
