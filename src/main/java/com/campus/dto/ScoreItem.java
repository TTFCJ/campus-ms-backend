package com.campus.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条成绩录入
 */
@Data
public class ScoreItem {

    private Long id;

    private Long studentId;

    private Integer examType;

    private BigDecimal score;

    private String remark;
}
