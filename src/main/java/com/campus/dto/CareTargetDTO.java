package com.campus.dto;

import lombok.Data;

@Data
public class CareTargetDTO {
    private Long id;
    private Long studentId;
    private String tags;
    private Integer riskLevel;
    private String reason;
    private Long counselorId;
}
