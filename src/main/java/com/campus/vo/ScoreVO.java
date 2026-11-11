package com.campus.vo;

import com.campus.entity.Score;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreVO extends Score {

    private String studentName;
    private String stuNo;
    private String clazzName;
    private String courseName;
    private String examTypeName;
    private String statusName;
}
