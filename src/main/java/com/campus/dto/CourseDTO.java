package com.campus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDTO {

    private Long id;

    @NotBlank(message = "课程编号不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private Long deptId;

    @DecimalMin(value = "0.5", message = "学分至少 0.5")
    private BigDecimal credit;

    @Min(value = 1, message = "学时至少为 1")
    private Integer classHour;

    private Integer courseType;
    private Integer examType;
    private Integer status;
    private String remark;
}
