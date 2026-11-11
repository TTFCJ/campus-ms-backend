package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.CareRecordDTO;
import com.campus.dto.CareTargetDTO;
import com.campus.vo.CareRecordVO;
import com.campus.vo.CareTargetVO;

import java.util.List;

public interface CareService {

    PageResult<CareTargetVO> targetPage(Integer riskLevel, Integer status,
                                        String keyword, Long current, Long size);

    CareTargetVO targetDetail(Long id);

    void addTarget(CareTargetDTO dto);

    void updateTarget(CareTargetDTO dto);

    /** 解除关爱（status -> 2） */
    void releaseTarget(Long id);

    List<CareRecordVO> recordList(Long targetId, Long studentId);

    void addRecord(CareRecordDTO dto);

    void removeRecord(Long id);
}
