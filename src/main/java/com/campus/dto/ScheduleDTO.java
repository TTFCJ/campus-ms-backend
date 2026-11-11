package com.campus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleDTO {

    private Long id;

    @NotNull(message = "学期不能为空")
    private Long semesterId;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "教师不能为空")
    private Long teacherId;

    @NotNull(message = "班级不能为空")
    private Long clazzId;

    private Long classroomId;

    @NotNull
    @Min(value = 1, message = "起始周 1-30")
    @Max(value = 30, message = "起始周 1-30")
    private Integer weekStart;

    @NotNull
    @Min(value = 1, message = "结束周 1-30")
    @Max(value = 30, message = "结束周 1-30")
    private Integer weekEnd;

    @NotNull
    @Min(value = 1, message = "星期 1-7")
    @Max(value = 7, message = "星期 1-7")
    private Integer dayOfWeek;

    @NotNull
    @Min(value = 1, message = "节次 1-12")
    @Max(value = 12, message = "节次 1-12")
    private Integer sectionStart;

    @NotNull
    @Min(value = 1, message = "节次 1-12")
    @Max(value = 12, message = "节次 1-12")
    private Integer sectionEnd;

    /** 选课人数上限，默认 60 */
    private Integer maxNum;

    private Integer status;
}
