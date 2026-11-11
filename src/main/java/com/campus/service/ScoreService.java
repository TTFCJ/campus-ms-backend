package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.ScoreItem;
import com.campus.vo.ScoreVO;

import java.util.List;

public interface ScoreService {

    /** 成绩列表（教务按条件查） */
    PageResult<ScoreVO> page(Long semesterId, Long scheduleId, Long clazzId,
                             Long studentId, Integer examType,
                             Long current, Long size);

    /** 某教学班的学生成绩名单（教师录入页） */
    List<ScoreVO> listBySchedule(Long scheduleId);

    /** 批量录入 / 修改 */
    void batchSave(Long scheduleId, List<ScoreItem> items);

    /** 提交该教学班成绩（1已录入 -> 2已提交，之后教师不可改） */
    void submit(Long scheduleId);

    /** 教务复核解锁（2已提交 -> 3复核中 -> 4已锁定） */
    void review(Long scheduleId, boolean lock);

    /** 学生查自己的成绩 */
    List<ScoreVO> myScores(Long semesterId);
}
