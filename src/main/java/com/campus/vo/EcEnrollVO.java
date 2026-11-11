package com.campus.vo;

import com.campus.entity.EcEnroll;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class EcEnrollVO extends EcEnroll {

    private String activityTitle;
    private Integer category;
    private String categoryName;
    private BigDecimal credit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;

    private String studentName;
    private String stuNo;
    private String statusName;
}
