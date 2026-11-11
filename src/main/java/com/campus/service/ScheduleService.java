package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.ScheduleDTO;
import com.campus.entity.Schedule;
import com.campus.vo.TimetableItem;

import java.util.List;

public interface ScheduleService {

    PageResult<Schedule> page(Long semesterId, Long teacherId, Long clazzId,
                              Long courseId, Long current, Long size);

    void add(ScheduleDTO dto);

    void update(ScheduleDTO dto);

    void remove(Long id);

    /** 班级课表 */
    List<TimetableItem> clazzTimetable(Long semesterId, Long clazzId);

    /** 教师课表 */
    List<TimetableItem> teacherTimetable(Long semesterId, Long teacherId);

    /** 学生课表（本人） */
    List<TimetableItem> studentTimetable(Long semesterId);

    /** 可选课程（当前学期、未选、有余量） */
    List<TimetableItem> electable(Long semesterId);
}
