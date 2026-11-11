package com.campus.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CareRecordDTO {
    private Long id;
    private Long targetId;
    private Long studentId;
    private Integer recordType;
    private String title;
    private String content;
    private String result;
    private LocalDateTime recordTime;
    private String attachment;
}
