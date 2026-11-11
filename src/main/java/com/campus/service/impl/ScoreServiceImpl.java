package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.common.exception.BizException;
import com.campus.dto.ScoreItem;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import com.campus.service.ScoreService;
import com.campus.vo.ScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final ScheduleMapper scheduleMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ClazzMapper clazzMapper;
    private final ElectCourseMapper electCourseMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<ScoreVO> page(Long semesterId, Long scheduleId, Long clazzId,
                                    Long studentId, Integer examType,
                                    Long current, Long size) {
        Page<Score> page = new Page<>(current == null ? 1 : current,
                size == null ? 10 : size);
        var w = Wrappers.<Score>lambdaQuery()
                .eq(semesterId != null, Score::getSemesterId, semesterId)
                .eq(scheduleId != null, Score::getScheduleId, scheduleId)
                .eq(studentId != null, Score::getStudentId, studentId)
                .eq(examType != null, Score::getExamType, examType)
                .orderByDesc(Score::getId);
        Page<Score> result = scoreMapper.selectPage(page, w);
        return new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toVOs(result.getRecords()));
    }

    /**
     * 教学班成绩名单：
     * 以「已选课学生」为准，即使成绩还没录入也要出现在名单里
     */
    @Override
    public List<ScoreVO> listBySchedule(Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BizException("教学班不存在");
        }

        // 已选课学生
        List<ElectCourse> elects = electCourseMapper.selectList(
                Wrappers.<ElectCourse>lambdaQuery()
                        .eq(ElectCourse::getScheduleId, scheduleId)
                        .eq(ElectCourse::getStatus, 1));
        List<Long> studentIds = elects.stream().map(ElectCourse::getStudentId).toList();
        if (studentIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Student> stuMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<Long, String> userNames = loadUserNames(stuMap.values());

        // 已有成绩
        Map<Long, Score> scoreMap = scoreMapper.selectList(
                        Wrappers.<Score>lambdaQuery()
                                .eq(Score::getScheduleId, scheduleId)
                                .eq(Score::getExamType, 1))
                .stream()
                .collect(Collectors.toMap(Score::getStudentId, Function.identity(),
                        (a, b) -> a));

        Course course = courseMapper.selectById(schedule.getCourseId());
        Clazz clazz = clazzMapper.selectById(schedule.getClazzId());

        List<ScoreVO> out = new ArrayList<>();
        for (Long sid : studentIds) {
            Student stu = stuMap.get(sid);
            Score sc = scoreMap.get(sid);
            ScoreVO vo = new ScoreVO();
            vo.setId(sc == null ? null : sc.getId());
            vo.setStudentId(sid);
            vo.setScheduleId(scheduleId);
            vo.setCourseId(schedule.getCourseId());
            vo.setSemesterId(schedule.getSemesterId());
            vo.setExamType(1);
            vo.setScore(sc == null ? null : sc.getScore());
            vo.setGradePoint(sc == null ? null : sc.getGradePoint());
            vo.setStatus(sc == null ? 0 : sc.getStatus());
            vo.setRemark(sc == null ? null : sc.getRemark());
            vo.setStudentName(userNames.getOrDefault(sid, "学生" + sid));
            vo.setStuNo(stu == null ? null : stu.getStuNo());
            vo.setClazzName(clazz == null ? null : clazz.getClazzName());
            vo.setCourseName(course == null ? null : course.getCourseName());
            vo.setExamTypeName("期末");
            vo.setStatusName(statusName(vo.getStatus()));
            out.add(vo);
        }
        out.sort(Comparator.comparing(v -> v.getStuNo() == null ? "" : v.getStuNo()));
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long scheduleId, List<ScoreItem> items) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BizException("教学班不存在");
        }
        checkTeacherPermission(schedule);

        for (ScoreItem item : items) {
            if (item.getScore() == null) {
                continue;
            }
            if (item.getScore().compareTo(BigDecimal.ZERO) < 0
                    || item.getScore().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BizException("成绩必须在 0-100 之间");
            }

            Integer examType = item.getExamType() == null ? 1 : item.getExamType();
            Score exist = scoreMapper.selectOne(Wrappers.<Score>lambdaQuery()
                    .eq(Score::getStudentId, item.getStudentId())
                    .eq(Score::getScheduleId, scheduleId)
                    .eq(Score::getExamType, examType)
                    .last("LIMIT 1"));

            if (exist != null && exist.getStatus() != null && exist.getStatus() >= 2) {
                throw new BizException("该教学班成绩已提交，请先联系教务解锁");
            }

            BigDecimal gp = calcGradePoint(item.getScore());

            if (exist == null) {
                Score sc = new Score();
                sc.setStudentId(item.getStudentId());
                sc.setScheduleId(scheduleId);
                sc.setCourseId(schedule.getCourseId());
                sc.setSemesterId(schedule.getSemesterId());
                sc.setExamType(examType);
                sc.setScore(item.getScore());
                sc.setGradePoint(gp);
                sc.setStatus(1);
                sc.setRemark(item.getRemark());
                scoreMapper.insert(sc);
            } else {
                scoreMapper.update(null, Wrappers.<Score>lambdaUpdate()
                        .set(Score::getScore, item.getScore())
                        .set(Score::getGradePoint, gp)
                        .set(Score::getRemark, item.getRemark())
                        .set(Score::getStatus, 1)
                        .eq(Score::getId, exist.getId()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BizException("教学班不存在");
        }
        checkTeacherPermission(schedule);

        Long exists = scoreMapper.selectCount(Wrappers.<Score>lambdaQuery()
                .eq(Score::getScheduleId, scheduleId));
        if (exists == null || exists == 0) {
            throw new BizException("该教学班还没有录入任何成绩");
        }

        scoreMapper.update(null, Wrappers.<Score>lambdaUpdate()
                .set(Score::getStatus, 2)
                .eq(Score::getScheduleId, scheduleId)
                .eq(Score::getStatus, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long scheduleId, boolean lock) {
        if (lock) {
            // 锁定：2 -> 4
            scoreMapper.update(null, Wrappers.<Score>lambdaUpdate()
                    .set(Score::getStatus, 4)
                    .eq(Score::getScheduleId, scheduleId)
                    .eq(Score::getStatus, 2));
        } else {
            // 解锁：2/3/4 -> 1（退回教师可改）
            scoreMapper.update(null, Wrappers.<Score>lambdaUpdate()
                    .set(Score::getStatus, 1)
                    .eq(Score::getScheduleId, scheduleId)
                    .in(Score::getStatus, List.of(2, 3, 4)));
        }
    }

    @Override
    public List<ScoreVO> myScores(Long semesterId) {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("当前账号不是学生");
        }
        List<Score> list = scoreMapper.selectList(Wrappers.<Score>lambdaQuery()
                .eq(Score::getStudentId, u.getStudentId())
                .eq(semesterId != null, Score::getSemesterId, semesterId)
                .in(Score::getStatus, List.of(2, 3, 4))
                .orderByDesc(Score::getId));
        return toVOs(list);
    }

    // ------------------------------------------------------------ 内部

    /** 教师只能操作自己授课的教学班 */
    private void checkTeacherPermission(Schedule schedule) {
        LoginUser u = UserContext.require();
        if (Integer.valueOf(1).equals(u.getUserType())) {
            return; // 超管放行
        }
        if (u.getTeacherId() == null) {
            throw new BizException(ResultCode.FORBIDDEN, "只有教师可以录入成绩");
        }
        if (!u.getTeacherId().equals(schedule.getTeacherId())) {
            throw new BizException(ResultCode.FORBIDDEN, "只能录入自己授课的成绩");
        }
    }

    /** 百分制转绩点：60-100 按 (score-50)/10，60 以下为 0 */
    private BigDecimal calcGradePoint(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(60)) < 0) {
            return BigDecimal.ZERO;
        }
        return score.subtract(BigDecimal.valueOf(50))
                .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
    }

    private List<ScoreVO> toVOs(List<Score> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> studentIds = list.stream().map(Score::getStudentId).collect(Collectors.toSet());
        Set<Long> courseIds = list.stream()
                .map(Score::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, Student> stuMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<Long, String> names = loadUserNames(stuMap.values());
        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of()
                : courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        List<ScoreVO> out = new ArrayList<>(list.size());
        for (Score s : list) {
            ScoreVO vo = new ScoreVO();
            vo.setId(s.getId());
            vo.setStudentId(s.getStudentId());
            vo.setScheduleId(s.getScheduleId());
            vo.setCourseId(s.getCourseId());
            vo.setSemesterId(s.getSemesterId());
            vo.setExamType(s.getExamType());
            vo.setScore(s.getScore());
            vo.setGradePoint(s.getGradePoint());
            vo.setStatus(s.getStatus());
            vo.setRemark(s.getRemark());

            Student stu = stuMap.get(s.getStudentId());
            vo.setStudentName(names.getOrDefault(s.getStudentId(), "学生" + s.getStudentId()));
            vo.setStuNo(stu == null ? null : stu.getStuNo());
            if (stu != null && stu.getClazzId() != null) {
                Clazz c = clazzMapper.selectById(stu.getClazzId());
                vo.setClazzName(c == null ? null : c.getClazzName());
            }
            Course course = s.getCourseId() == null ? null : courseMap.get(s.getCourseId());
            vo.setCourseName(course == null ? null : course.getCourseName());
            vo.setExamTypeName(switch (s.getExamType() == null ? 1 : s.getExamType()) {
                case 2 -> "补考";
                case 3 -> "重修";
                case 4 -> "清考";
                default -> "期末";
            });
            vo.setStatusName(statusName(s.getStatus()));
            out.add(vo);
        }
        return out;
    }

    private Map<Long, String> loadUserNames(Collection<Student> students) {
        if (students.isEmpty()) {
            return Map.of();
        }
        Set<Long> userIds = students.stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNames = new HashMap<>();
        for (SysUser u : sysUserMapper.selectBatchIds(userIds)) {
            userNames.put(u.getId(), u.getNickname());
        }
        Map<Long, String> out = new HashMap<>();
        for (Student s : students) {
            out.put(s.getId(), userNames.getOrDefault(s.getUserId(), "学生" + s.getId()));
        }
        return out;
    }

    private String statusName(Integer status) {
        if (status == null) {
            return "待录入";
        }
        return switch (status) {
            case 1 -> "已录入";
            case 2 -> "已提交";
            case 3 -> "复核中";
            case 4 -> "已锁定";
            default -> "待录入";
        };
    }
}
