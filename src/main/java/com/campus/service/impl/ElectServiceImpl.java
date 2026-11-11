package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.ResultCode;
import com.campus.common.constant.RedisKeys;
import com.campus.common.exception.BizException;
import com.campus.common.util.RedisUtil;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import com.campus.service.ElectService;
import com.campus.service.ScheduleService;
import com.campus.vo.TimetableItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectServiceImpl implements ElectService {

    private final ElectCourseMapper electMapper;
    private final ScheduleMapper scheduleMapper;
    private final SemesterMapper semesterMapper;
    private final ScheduleService scheduleService;
    private final RedisUtil redisUtil;

    @Override
    public List<TimetableItem> available(Long semesterId) {
        return scheduleService.electable(semesterId);
    }

    @Override
    public List<TimetableItem> myElects(Long semesterId) {
        return scheduleService.studentTimetable(semesterId);
    }

    /**
     * 选课流程（防超选两道保险）：
     *
     *   1. Redis 互斥锁 lock:elect:{scheduleId}，挡住瞬时并发
     *   2. UPDATE ... WHERE current_num < max_num，影响行数为 0 即已满
     *      —— 这层即使锁失效也不会超选，由数据库行锁保证
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void elect(Long scheduleId) {
        LoginUser user = UserContext.require();
        Long studentId = user.getStudentId();
        if (studentId == null) {
            throw new BizException("只有学生账号可以选课");
        }

        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || !Integer.valueOf(1).equals(schedule.getStatus())) {
            throw new BizException("该课程不可选");
        }

        // 选课时间窗校验
        checkSelectWindow(schedule.getSemesterId());

        // 是否已选
        ElectCourse exist = electMapper.selectOne(Wrappers.<ElectCourse>lambdaQuery()
                .eq(ElectCourse::getStudentId, studentId)
                .eq(ElectCourse::getScheduleId, scheduleId)
                .last("LIMIT 1"));
        if (exist != null && Integer.valueOf(1).equals(exist.getStatus())) {
            throw new BizException(ResultCode.COURSE_ELECT_DUPLICATE);
        }

        // 已满快速判断（不进锁）
        if (schedule.getCurrentNum() != null && schedule.getMaxNum() != null
                && schedule.getCurrentNum() >= schedule.getMaxNum()) {
            throw new BizException(ResultCode.COURSE_ELECT_FULL);
        }

        String lockKey = RedisKeys.ELECT_LOCK + scheduleId;
        boolean locked = redisUtil.setIfAbsent(lockKey, "1", 5);
        if (!locked) {
            throw new BizException("选课人数较多，请稍后再试");
        }

        try {
            // 原子扣减：影响行数 0 说明被别人抢满
            int rows = scheduleMapper.update(null,
                    Wrappers.<Schedule>lambdaUpdate()
                            .setSql("current_num = current_num + 1")
                            .eq(Schedule::getId, scheduleId)
                            .eq(Schedule::getStatus, 1)
                            .apply("current_num < max_num"));
            if (rows == 0) {
                throw new BizException(ResultCode.COURSE_ELECT_FULL);
            }

            if (exist != null) {
                // 之前退过课，重新置为已选
                electMapper.update(null, Wrappers.<ElectCourse>lambdaUpdate()
                        .set(ElectCourse::getStatus, 1)
                        .set(ElectCourse::getSelectTime, LocalDateTime.now())
                        .eq(ElectCourse::getId, exist.getId()));
            } else {
                ElectCourse e = new ElectCourse();
                e.setStudentId(studentId);
                e.setScheduleId(scheduleId);
                e.setSelectTime(LocalDateTime.now());
                e.setStatus(1);
                electMapper.insert(e);
            }

            log.info("[选课成功] studentId={} scheduleId={}", studentId, scheduleId);
        } finally {
            redisUtil.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void drop(Long scheduleId) {
        LoginUser user = UserContext.require();
        Long studentId = user.getStudentId();
        if (studentId == null) {
            throw new BizException("只有学生账号可以退课");
        }

        ElectCourse exist = electMapper.selectOne(Wrappers.<ElectCourse>lambdaQuery()
                .eq(ElectCourse::getStudentId, studentId)
                .eq(ElectCourse::getScheduleId, scheduleId)
                .eq(ElectCourse::getStatus, 1)
                .last("LIMIT 1"));
        if (exist == null) {
            throw new BizException("没有找到该选课记录");
        }

        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule != null) {
            checkSelectWindow(schedule.getSemesterId());
        }

        electMapper.update(null, Wrappers.<ElectCourse>lambdaUpdate()
                .set(ElectCourse::getStatus, 2)
                .eq(ElectCourse::getId, exist.getId()));

        // 人数 -1，防止减到负数
        scheduleMapper.update(null, Wrappers.<Schedule>lambdaUpdate()
                .setSql("current_num = current_num - 1")
                .eq(Schedule::getId, scheduleId)
                .apply("current_num > 0"));

        log.info("[退课成功] studentId={} scheduleId={}", studentId, scheduleId);
    }

    /** 选课时间窗：semester.select_start ~ select_end */
    private void checkSelectWindow(Long semesterId) {
        if (semesterId == null) {
            return;
        }
        Semester sem = semesterMapper.selectById(semesterId);
        if (sem == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (sem.getSelectStart() != null && now.isBefore(sem.getSelectStart())) {
            throw new BizException(ResultCode.COURSE_ELECT_TIME_INVALID,
                    "选课尚未开始");
        }
        if (sem.getSelectEnd() != null && now.isAfter(sem.getSelectEnd())) {
            throw new BizException(ResultCode.COURSE_ELECT_TIME_INVALID,
                    "选课已结束");
        }
    }
}
