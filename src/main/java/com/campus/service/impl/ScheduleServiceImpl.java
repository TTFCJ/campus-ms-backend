package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.dto.ScheduleDTO;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import com.campus.service.ScheduleService;
import com.campus.vo.TimetableItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final ClazzMapper clazzMapper;
    private final ClassroomMapper classroomMapper;
    private final ElectCourseMapper electCourseMapper;

    @Override
    public PageResult<Schedule> page(Long semesterId, Long teacherId, Long clazzId,
                                     Long courseId, Long current, Long size) {
        Page<Schedule> page = new Page<>(current == null ? 1 : current,
                size == null ? 10 : size);
        var w = Wrappers.<Schedule>lambdaQuery()
                .eq(semesterId != null, Schedule::getSemesterId, semesterId)
                .eq(teacherId != null, Schedule::getTeacherId, teacherId)
                .eq(clazzId != null, Schedule::getClazzId, clazzId)
                .eq(courseId != null, Schedule::getCourseId, courseId)
                .orderByAsc(Schedule::getDayOfWeek)
                .orderByAsc(Schedule::getSectionStart);
        return PageResult.of(scheduleMapper.selectPage(page, w));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ScheduleDTO dto) {
        checkTimeRange(dto);
        Schedule s = new Schedule();
        copy(dto, s);
        checkConflict(s, null);
        s.setCurrentNum(0);
        scheduleMapper.insert(s);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ScheduleDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("排课ID不能为空");
        }
        Schedule old = scheduleMapper.selectById(dto.getId());
        if (old == null) {
            throw new BizException("排课记录不存在");
        }
        checkTimeRange(dto);
        Schedule s = new Schedule();
        s.setId(dto.getId());
        copy(dto, s);
        s.setCurrentNum(old.getCurrentNum());
        checkConflict(s, dto.getId());
        scheduleMapper.updateById(s);
    }

    @Override
    public void remove(Long id) {
        Schedule s = scheduleMapper.selectById(id);
        if (s == null) {
            return;
        }
        Long selected = electCourseMapper.selectCount(
                Wrappers.<ElectCourse>lambdaQuery()
                        .eq(ElectCourse::getScheduleId, id)
                        .eq(ElectCourse::getStatus, 1));
        if (selected != null && selected > 0) {
            throw new BizException("已有 " + selected + " 名学生选课，无法删除排课");
        }
        scheduleMapper.deleteById(id);
    }

    // ------------------------------------------------------------ 课表

    @Override
    public List<TimetableItem> clazzTimetable(Long semesterId, Long clazzId) {
        return toItems(scheduleMapper.selectByClazz(semesterId, clazzId));
    }

    @Override
    public List<TimetableItem> teacherTimetable(Long semesterId, Long teacherId) {
        // 不传 teacherId 时，默认查当前登录教师自己的课表
        Long tid = teacherId;
        if (tid == null) {
            LoginUser u = UserContext.require();
            tid = u.getTeacherId();
            if (tid == null) {
                throw new BizException("当前账号不是教师，请指定 teacherId");
            }
        }
        return toItems(scheduleMapper.selectByTeacher(semesterId, tid));
    }

    @Override
    public List<TimetableItem> studentTimetable(Long semesterId) {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("当前账号不是学生");
        }
        return toItems(scheduleMapper.selectByStudent(semesterId, u.getStudentId()));
    }

    @Override
    public List<TimetableItem> electable(Long semesterId) {
        List<Schedule> list = scheduleMapper.selectList(
                Wrappers.<Schedule>lambdaQuery()
                        .eq(Schedule::getSemesterId, semesterId)
                        .eq(Schedule::getStatus, 1)
                        .apply("current_num < max_num")
                        .orderByAsc(Schedule::getDayOfWeek)
                        .orderByAsc(Schedule::getSectionStart));
        return toItems(list);
    }

    // ------------------------------------------------------------ 内部

    /** 时间段合法性 */
    private void checkTimeRange(ScheduleDTO dto) {
        if (dto.getSectionStart() > dto.getSectionEnd()) {
            throw new BizException("开始节次不能大于结束节次");
        }
        if (dto.getWeekStart() > dto.getWeekEnd()) {
            throw new BizException("起始周不能大于结束周");
        }
    }

    /**
     * 冲突检测：班级 / 教师 / 教室 三者任一重叠即拒绝
     *
     * 重叠判定：existing.start <= new.end AND existing.end >= new.start
     */
    private void checkConflict(Schedule s, Long excludeId) {
        Long byClazz = scheduleMapper.selectCount(baseConflictQuery(s, excludeId)
                .eq(Schedule::getClazzId, s.getClazzId()));
        if (byClazz != null && byClazz > 0) {
            throw new BizException("该班级在此时间段已有排课");
        }

        Long byTeacher = scheduleMapper.selectCount(baseConflictQuery(s, excludeId)
                .eq(Schedule::getTeacherId, s.getTeacherId()));
        if (byTeacher != null && byTeacher > 0) {
            throw new BizException("该教师在此时间段已有排课");
        }

        if (s.getClassroomId() != null) {
            Long byRoom = scheduleMapper.selectCount(baseConflictQuery(s, excludeId)
                    .eq(Schedule::getClassroomId, s.getClassroomId()));
            if (byRoom != null && byRoom > 0) {
                throw new BizException("该教室在此时间段已被占用");
            }
        }
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Schedule>
    baseConflictQuery(Schedule s, Long excludeId) {
        return Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getSemesterId, s.getSemesterId())
                .eq(Schedule::getDayOfWeek, s.getDayOfWeek())
                .eq(Schedule::getStatus, 1)
                .ne(excludeId != null, Schedule::getId, excludeId)
                .and(w -> w.le(Schedule::getSectionStart, s.getSectionEnd())
                        .ge(Schedule::getSectionEnd, s.getSectionStart()));
    }

    private void copy(ScheduleDTO dto, Schedule s) {
        s.setSemesterId(dto.getSemesterId());
        s.setCourseId(dto.getCourseId());
        s.setTeacherId(dto.getTeacherId());
        s.setClazzId(dto.getClazzId());
        s.setClassroomId(dto.getClassroomId());
        s.setWeekStart(dto.getWeekStart());
        s.setWeekEnd(dto.getWeekEnd());
        s.setDayOfWeek(dto.getDayOfWeek());
        s.setSectionStart(dto.getSectionStart());
        s.setSectionEnd(dto.getSectionEnd());
        s.setMaxNum(dto.getMaxNum() == null ? 60 : dto.getMaxNum());
        s.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        if (dto.getClassroomId() != null) {
            Classroom room = classroomMapper.selectById(dto.getClassroomId());
            if (room != null) {
                s.setClassroom(room.getClassroomNo());
            }
        }
    }

    /** Schedule -> TimetableItem（批量查名字，避免 N+1） */
    private List<TimetableItem> toItems(List<Schedule> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> courseIds = list.stream().map(Schedule::getCourseId).collect(Collectors.toSet());
        Set<Long> teacherIds = list.stream().map(Schedule::getTeacherId).collect(Collectors.toSet());
        Set<Long> clazzIds = list.stream().map(Schedule::getClazzId).collect(Collectors.toSet());

        Map<Long, Course> courses = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, Teacher> teachers = teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, Function.identity()));
        Map<Long, Clazz> clazzes = clazzMapper.selectBatchIds(clazzIds).stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        // 教师姓名要从 sys_user 取，这里只填 id，需要再查一次
        Map<Long, String> teacherNames = loadTeacherNames(teachers.values());

        List<TimetableItem> out = new ArrayList<>(list.size());
        for (Schedule s : list) {
            TimetableItem it = new TimetableItem();
            it.setScheduleId(s.getId());
            it.setCourseId(s.getCourseId());
            Course c = courses.get(s.getCourseId());
            if (c != null) {
                it.setCourseName(c.getCourseName());
                it.setCourseCode(c.getCourseCode());
            }
            it.setTeacherId(s.getTeacherId());
            it.setTeacherName(teacherNames.getOrDefault(s.getTeacherId(), "未知教师"));
            it.setClazzId(s.getClazzId());
            Clazz cl = clazzes.get(s.getClazzId());
            if (cl != null) {
                it.setClazzName(cl.getClazzName());
            }
            it.setClassroom(s.getClassroom());
            it.setDayOfWeek(s.getDayOfWeek());
            it.setSectionStart(s.getSectionStart());
            it.setSectionEnd(s.getSectionEnd());
            it.setWeekStart(s.getWeekStart());
            it.setWeekEnd(s.getWeekEnd());
            it.setCurrentNum(s.getCurrentNum());
            it.setMaxNum(s.getMaxNum());
            out.add(it);
        }
        return out;
    }

    private Map<Long, String> loadTeacherNames(Collection<Teacher> teachers) {
        if (teachers.isEmpty()) {
            return Map.of();
        }
        Set<Long> userIds = teachers.stream().map(Teacher::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNames = new HashMap<>();
        // 用 SysUserMapper 查姓名
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        for (SysUser u : users) {
            userNames.put(u.getId(), u.getNickname());
        }
        Map<Long, String> out = new HashMap<>();
        for (Teacher t : teachers) {
            out.put(t.getId(), userNames.getOrDefault(t.getUserId(), "教师" + t.getId()));
        }
        return out;
    }

    private final SysUserMapper sysUserMapper;
}
