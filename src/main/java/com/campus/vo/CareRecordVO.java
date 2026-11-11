package com.campus.vo;

import com.campus.entity.CareRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CareRecordVO extends CareRecord {
    private String studentName;
    private String stuNo;
    private String recorderName;
    private String recordTypeName;
}
