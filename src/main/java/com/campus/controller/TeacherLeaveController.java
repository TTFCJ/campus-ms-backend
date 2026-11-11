package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.Result;
import com.campus.common.exception.BizException;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.RequiresPerm;
import com.campus.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端：查自己课上的请假学生
 *
 * 数据边界：
 *   · 只返回 teacher_id = 当前登录教师 的排课
 *   · 只能查该排课所属班级的学生
 *   · 学生请假状态必须是「已通过」
 */
@Tag(name = "教师-课堂请假")
@RestController
@RequestMapping("/teacher/leave")
@RequiredArgsConstructor
public class TeacherLeaveController {

    private final ScheduleMapper scheduleMapper;
    private final LeaveApplyMapper leaveMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ClazzMapper clazzMapper;
    private final ClassroomMapper classroomMapper;

    private static final String SECRET = "campus-leave-2026";

    @Operation(summary = "我的教学班（含今日请假人数）")
    @RequiresPerm("teacher:leave:list")
    @GetMapping("/schedules")
    public Result<List<Map<String, Object>>> schedules(
            @RequestParam(required = false) Integer semesterId) {

        Long teacherId = currentTeacherId();

        var w = Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getTeacherId, teacherId)
                .orderByAsc(Schedule::getDayOfWeek)
                .orderByAsc(Schedule::getSectionStart);
        if (semesterId != null) {
            w.eq(Schedule::getSemesterId, semesterId);
        }
        List<Schedule> list = scheduleMapper.selectList(w);
        if (list.isEmpty()) {
            return Result.ok(List.of());
        }

        // 关联课程 / 班级 / 教室名称
        Set<Long> cids = list.stream().map(Schedule::getCourseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> lids = list.stream().map(Schedule::getClazzId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> rids = list.stream().map(Schedule::getClassroomId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> courseNames = new HashMap<>();
        if (!cids.isEmpty()) {
            for (Course c : courseMapper.selectBatchIds(cids)) {
                courseNames.put(c.getId(), c.getCourseName());
            }
        }
        Map<Long, String> clazzNames = new HashMap<>();
        if (!lids.isEmpty()) {
            for (Clazz c : clazzMapper.selectBatchIds(lids)) {
                clazzNames.put(c.getId(), c.getClazzName());
            }
        }
        Map<Long, String> roomNames = new HashMap<>();
        if (!rids.isEmpty()) {
            for (Classroom c : classroomMapper.selectBatchIds(rids)) {
                roomNames.put(c.getId(), c.getClassroomNo());
            }
        }

        LocalDate today = LocalDate.now();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Schedule s : list) {
            // 今天该班请假人数
            long leaveCount = countLeave(s.getClazzId(), today);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("courseId", s.getCourseId());
            m.put("courseName", courseNames.getOrDefault(s.getCourseId(), "课程"));
            m.put("clazzId", s.getClazzId());
            m.put("clazzName", clazzNames.getOrDefault(s.getClazzId(), "班级"));
            m.put("classroom", roomNames.getOrDefault(s.getClassroomId(), ""));
            m.put("dayOfWeek", s.getDayOfWeek());
            m.put("sectionStart", s.getSectionStart());
            m.put("sectionEnd", s.getSectionEnd());
            m.put("semesterId", s.getSemesterId());
            m.put("currentNum", s.getCurrentNum());
            m.put("leaveToday", leaveCount);
            out.add(m);
        }
        return Result.ok(out);
    }

    @Operation(summary = "某节课的请假名单（按日期）")
    @RequiresPerm("teacher:leave:list")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam Long scheduleId,
            @RequestParam(required = false) String date) {

        Long teacherId = currentTeacherId();

        Schedule s = scheduleMapper.selectById(scheduleId);
        if (s == null) {
            throw new BizException("排课不存在");
        }
        // 只能看自己的课
        if (!Objects.equals(s.getTeacherId(), teacherId)) {
            throw new BizException("只能查看自己的课程");
        }

        LocalDate d = (date == null || date.isEmpty())
                ? LocalDate.now()
                : LocalDate.parse(date, DateTimeFormatter.ISO_DATE);

        // 查该班级全部学生
        List<Student> students = studentMapper.selectList(
                Wrappers.<Student>lambdaQuery().eq(Student::getClazzId, s.getClazzId()));

        if (students.isEmpty()) {
            return Result.ok(Map.of("total", 0, "leaveCount", 0, "list", List.of()));
        }

        Set<Long> sids = students.stream().map(Student::getId).collect(Collectors.toSet());

        // 该日期内已通过的请假
        List<LeaveApply> leaves = leaveMapper.selectList(
                Wrappers.<LeaveApply>lambdaQuery()
                        .in(LeaveApply::getStudentId, sids)
                        .eq(LeaveApply::getStatus, 3)
                        .le(LeaveApply::getStartTime, d.atTime(23, 59, 59))
                        .ge(LeaveApply::getEndTime, d.atTime(0, 0, 0)));

        Map<Long, LeaveApply> leaveMap = leaves.stream()
                .collect(Collectors.toMap(LeaveApply::getStudentId, l -> l, (a, b) -> a));

        // 学生姓名
        Set<Long> uids = students.stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        if (!uids.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(uids)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Student stu : students) {
            LeaveApply l = leaveMap.get(stu.getId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", stu.getId());
            m.put("stuNo", stu.getStuNo());
            m.put("studentName", names.getOrDefault(stu.getUserId(), "学生"));
            m.put("onLeave", l != null);
            if (l != null) {
                m.put("leaveId", l.getId());
                m.put("applyNo", l.getApplyNo());
                m.put("leaveType", l.getLeaveType());
                m.put("reason", l.getReason());
                m.put("days", l.getDays());
                m.put("startTime", l.getStartTime());
                m.put("endTime", l.getEndTime());
                m.put("qrcode", buildQrPayload(l));
            }
            rows.add(m);
        }

        // 请假的排前面
        rows.sort((a, b) -> Boolean.compare(
                (Boolean) b.get("onLeave"), (Boolean) a.get("onLeave")));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scheduleId", scheduleId);
        out.put("date", d.toString());
        out.put("courseName", courseNameOf(s.getCourseId()));
        out.put("clazzName", clazzNameOf(s.getClazzId()));
        out.put("total", students.size());
        out.put("leaveCount", leaves.size());
        out.put("list", rows);
        return Result.ok(out);
    }

    @Operation(summary = "核验请假二维码")
    @RequiresPerm("teacher:leave:list")
    @PostMapping("/verify")
    public Result<Map<String, Object>> verify(@RequestBody Map<String, String> body) {
        String payload = body.get("payload");
        if (payload == null || payload.isEmpty()) {
            throw new BizException("二维码内容为空");
        }
        // payload 形如 LEAVE|id|applyNo|sign
        String[] parts = payload.split("\\|");
        if (parts.length != 4 || !"LEAVE".equals(parts[0])) {
            throw new BizException("二维码格式不正确");
        }
        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new BizException("二维码内容无效");
        }
        LeaveApply a = leaveMapper.selectById(id);
        if (a == null) {
            throw new BizException("请假单不存在");
        }
        String expect = sign(a.getId(), a.getApplyNo());
        if (!expect.equals(parts[3])) {
            throw new BizException("二维码签名无效，可能是伪造的");
        }
        if (!Integer.valueOf(3).equals(a.getStatus())) {
            throw new BizException("该请假单未通过审批");
        }

        Student stu = studentMapper.selectById(a.getStudentId());
        String name = "";
        String no = "";
        if (stu != null) {
            no = stu.getStuNo();
            SysUser u = userMapper.selectById(stu.getUserId());
            name = u == null ? "" : u.getNickname();
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("valid", true);
        out.put("applyNo", a.getApplyNo());
        out.put("stuNo", no);
        out.put("studentName", name);
        out.put("startTime", a.getStartTime());
        out.put("endTime", a.getEndTime());
        out.put("days", a.getDays());
        out.put("reason", a.getReason());
        return Result.ok(out);
    }

    // ------------------------------------------------------------ 内部

    private Long currentTeacherId() {
        LoginUser u = UserContext.require();
        if (u.getTeacherId() == null) {
            throw new BizException("当前账号不是教师");
        }
        return u.getTeacherId();
    }

    private long countLeave(Long clazzId, LocalDate date) {
        List<Student> students = studentMapper.selectList(
                Wrappers.<Student>lambdaQuery().eq(Student::getClazzId, clazzId));
        if (students.isEmpty()) {
            return 0;
        }
        Set<Long> sids = students.stream().map(Student::getId).collect(Collectors.toSet());
        Long n = leaveMapper.selectCount(Wrappers.<LeaveApply>lambdaQuery()
                .in(LeaveApply::getStudentId, sids)
                .eq(LeaveApply::getStatus, 3)
                .le(LeaveApply::getStartTime, date.atTime(23, 59, 59))
                .ge(LeaveApply::getEndTime, date.atTime(0, 0, 0)));
        return n == null ? 0 : n;
    }

    private String courseNameOf(Long id) {
        Course c = courseMapper.selectById(id);
        return c == null ? "课程" : c.getCourseName();
    }

    private String clazzNameOf(Long id) {
        Clazz c = clazzMapper.selectById(id);
        return c == null ? "班级" : c.getClazzName();
    }

    /** 二维码载荷：LEAVE|id|applyNo|sign */
    public static String buildQrPayload(LeaveApply a) {
        return "LEAVE|" + a.getId() + "|" + a.getApplyNo() + "|" + sign(a.getId(), a.getApplyNo());
    }

    private static String sign(Long id, String applyNo) {
        try {
            String raw = id + "|" + applyNo + "|" + SECRET;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "0000000000000000";
        }
    }
}
