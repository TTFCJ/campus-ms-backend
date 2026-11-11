package com.campus.vo;

import com.campus.entity.CareTarget;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CareTargetVO extends CareTarget {
    private String studentName;
    private String stuNo;
    private String clazzName;
    private String counselorName;
    private Integer recordCount;
}
