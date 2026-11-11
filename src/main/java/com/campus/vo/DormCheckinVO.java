package com.campus.vo;

import com.campus.entity.DormCheckin;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DormCheckinVO extends DormCheckin {
    private String studentName;
    private String stuNo;
    private String clazzName;
    private String roomNo;
    private String statusName;
    private String sourceName;
}
