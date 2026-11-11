package com.campus.service;

import com.campus.vo.TimetableItem;

import java.util.List;

public interface ElectService {

    /** 当前可选的课程 */
    List<TimetableItem> available(Long semesterId);

    /** 选课（Redis 锁 + 原子扣减，防超选） */
    void elect(Long scheduleId);

    /** 退课 */
    void drop(Long scheduleId);

    /** 我的已选课程 */
    List<TimetableItem> myElects(Long semesterId);
}
