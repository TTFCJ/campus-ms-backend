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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 课堂签到
 *
 * 流程：
 *   教师在课堂上「发起点名」→ 生成 6 位码（默认 5 分钟有效）
 *   学生输入码签到（超过 2 分钟记为迟到）
 *   教师随时查看「已签 / 未签」名单
 *   未签到的学生 = 缺勤，教务处可按课程/班级统计
 */
@Tag(name = "课堂签到")
@RestController
@RequestMapping("/attend")
@RequiredArgsConstructor
public class AttendController {

    private final AttendSessionMapper sessionMapper;
    private final AttendRecordMapper recordMapper;
    private final ScheduleMapper scheduleMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ClazzMapper clazzMapper;
    private final LeaveApplyMapper leaveMapper;

    // ============================================================ 教师端

    @Operation(summary = "发起点名（返回 6 位签到码）")
    @RequiresPerm("teacher:attend:start")
    @PostMapping("/start")
    public Result<Map<String, Object>> start(@RequestBody Map<String, Object> body) {
        Long teacherId = currentTeacherId();
        Long scheduleId = body.get("scheduleId") instanceof Number n
                ? n.longValue() : null;
        int minutes = body.get("minutes") instanceof Number n
                ? Math.max(1, Math.min(60, n.intValue())) : 5;

        if (scheduleId == null) {
            throw new BizException("缺少教学班");
        }
        Schedule s = scheduleMapper.selectById(scheduleId);
        if (s == null) {
            throw new BizException("排课不存在");
        }
        if (!Objects.equals(s.getTeacherId(), teacherId)) {
            throw new BizException("只能给自己的课发起点名");
        }

        LocalDate today = LocalDate.now();

        // 今天该节课已有进行中的场次 → 直接返回，不重复创建
        AttendSession exist = sessionMapper.selectOne(Wrappers.<AttendSession>lambdaQuery()
                .eq(AttendSession::getScheduleId, scheduleId)
                .eq(AttendSession::getSessionDate, today)
                .eq(AttendSession::getStatus, 1)
                .last("LIMIT 1"));
        if (exist != null && exist.getCloseTime().isAfter(LocalDateTime.now())) {
            return Result.ok(buildStartResult(exist, s));
        }

        LocalDateTime now = LocalDateTime.now();
        AttendSession sess = new AttendSession();
        sess.setScheduleId(scheduleId);
        sess.setTeacherId(teacherId);
        sess.setCode(randomCode());
        sess.setSessionDate(today);
        sess.setSectionStart(s.getSectionStart());
        sess.setSectionEnd(s.getSectionEnd());
        sess.setOpenTime(now);
        sess.setCloseTime(now.plusMinutes(minutes));
        sess.setStatus(1);
        sessionMapper.insert(sess);

        return Result.ok(buildStartResult(sess, s));
    }

    @Operation(summary = "场次详情（已签 / 未签名单）")
    @RequiresPerm({"teacher:attend:list", "edu:attend:list"})
    @GetMapping("/session/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        AttendSession sess = sessionMapper.selectById(id);
        if (sess == null) {
            throw new BizException("签到场次不存在");
        }
        Schedule s = scheduleMapper.selectById(sess.getScheduleId());
        if (s == null) {
            throw new BizException("排课不存在");
        }

        // 教师只能看自己的；教务可看全部
        LoginUser u = UserContext.require();
        boolean isTeacher = u.getTeacherId() != null;
        if (isTeacher && !Objects.equals(s.getTeacherId(), u.getTeacherId())) {
            throw new BizException("只能查看自己课程的点名");
        }

        return Result.ok(buildDetail(sess, s));
    }

    @Operation(summary = "我的教学班点名历史")
    @RequiresPerm("teacher:attend:list")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my(
            @RequestParam(required = false) Long scheduleId) {
        Long teacherId = currentTeacherId();

        var w = Wrappers.<AttendSession>lambdaQuery()
                .eq(AttendSession::getTeacherId, teacherId)
                .eq(scheduleId != null, AttendSession::getScheduleId, scheduleId)
                .orderByDesc(AttendSession::getOpenTime)
                .last("LIMIT 50");
        List<AttendSession> list = sessionMapper.selectList(w);

        List<Map<String, Object>> out = new ArrayList<>();
        for (AttendSession sess : list) {
            Schedule s = scheduleMapper.selectById(sess.getScheduleId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", sess.getId());
            m.put("code", sess.getCode());
            m.put("sessionDate", sess.getSessionDate());
            m.put("openTime", sess.getOpenTime());
            m.put("closeTime", sess.getCloseTime());
            m.put("status", sess.getStatus());
            m.put("courseName", s == null ? "" : courseNameOf(s.getCourseId()));
            m.put("clazzName", s == null ? "" : clazzNameOf(s.getClazzId()));
            m.put("signedCount", recordMapper.selectCount(
                    Wrappers.<AttendRecord>lambdaQuery()
                            .eq(AttendRecord::getSessionId, sess.getId())));
            if (s != null) {
                m.put("total", studentMapper.selectCount(
                        Wrappers.<Student>lambdaQuery()
                                .eq(Student::getClazzId, s.getClazzId())));
            }
            out.add(m);
        }
        return Result.ok(out);
    }

    @Operation(summary = "提前结束点名")
    @RequiresPerm("teacher:attend:start")
    @PutMapping("/session/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        AttendSession sess = sessionMapper.selectById(id);
        if (sess == null) {
            throw new BizException("场次不存在");
        }
        Long teacherId = currentTeacherId();
        if (!Objects.equals(sess.getTeacherId(), teacherId)) {
            throw new BizException("只能结束自己的点名");
        }
        sess.setStatus(2);
        sess.setCloseTime(LocalDateTime.now());
        sessionMapper.updateById(sess);
        return Result.ok("已结束", null);
    }

    @Operation(summary = "我的出勤统计（教师）")
    @RequiresPerm("teacher:attend:list")
    @GetMapping("/stat")
    public Result<Map<String, Object>> stat() {
        Long teacherId = currentTeacherId();

        List<AttendSession> sessions = sessionMapper.selectList(
                Wrappers.<AttendSession>lambdaQuery()
                        .eq(AttendSession::getTeacherId, teacherId));

        long sessionCount = sessions.size();
        long totalPoints = 0;     // 应签总人次
        long signedPoints = 0;    // 实签总人次

        for (AttendSession sess : sessions) {
            Schedule s = scheduleMapper.selectById(sess.getScheduleId());
            if (s == null) continue;
            long total = studentMapper.selectCount(Wrappers.<Student>lambdaQuery()
                    .eq(Student::getClazzId, s.getClazzId()));
            long signed = recordMapper.selectCount(Wrappers.<AttendRecord>lambdaQuery()
                    .eq(AttendRecord::getSessionId, sess.getId()));
            totalPoints += total;
            signedPoints += signed;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionCount", sessionCount);
        out.put("totalPoints", totalPoints);
        out.put("signedPoints", signedPoints);
        out.put("absentPoints", totalPoints - signedPoints);
        out.put("rate", totalPoints == 0 ? 0
                : Math.round(signedPoints * 10000.0 / totalPoints) / 100.0);
        return Result.ok(out);
    }

    // ============================================================ 学生端

    @Operation(summary = "学生签到")
    @RequiresPerm("student:attend:add")
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@RequestBody Map<String, Object> body) {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("只有学生可以签到");
        }
        String code = body.get("code") == null ? "" : String.valueOf(body.get("code")).trim();
        if (code.isEmpty()) {
            throw new BizException("请输入签到码");
        }

        AttendSession sess = sessionMapper.selectOne(Wrappers.<AttendSession>lambdaQuery()
                .eq(AttendSession::getCode, code)
                .eq(AttendSession::getStatus, 1)
                .last("LIMIT 1"));
        if (sess == null) {
            throw new BizException("签到码无效或已结束");
        }
        if (LocalDateTime.now().isAfter(sess.getCloseTime())) {
            throw new BizException("签到已截止");
        }

        Schedule s = scheduleMapper.selectById(sess.getScheduleId());
        Student stu = studentMapper.selectById(u.getStudentId());
        if (s == null || stu == null) {
            throw new BizException("数据异常");
        }
        if (!Objects.equals(stu.getClazzId(), s.getClazzId())) {
            throw new BizException("你不在该教学班，无法签到");
        }

        AttendRecord exist = recordMapper.selectOne(Wrappers.<AttendRecord>lambdaQuery()
                .eq(AttendRecord::getSessionId, sess.getId())
                .eq(AttendRecord::getStudentId, stu.getId())
                .last("LIMIT 1"));
        if (exist != null) {
            throw new BizException("你已经签过到了");
        }

        // 超过 2 分钟记为迟到
        boolean late = LocalDateTime.now()
                .isAfter(sess.getOpenTime().plusMinutes(2));

        AttendRecord rec = new AttendRecord();
        rec.setSessionId(sess.getId());
        rec.setStudentId(stu.getId());
        rec.setCheckinTime(LocalDateTime.now());
        rec.setStatus(late ? 2 : 1);
        recordMapper.insert(rec);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("late", late);
        out.put("courseName", courseNameOf(s.getCourseId()));
        out.put("clazzName", clazzNameOf(s.getClazzId()));
        return Result.ok(late ? "签到成功（迟到）" : "签到成功", out);
    }

    @Operation(summary = "我的签到记录")
    @RequiresPerm("student:attend:list")
    @GetMapping("/my-records")
    public Result<List<Map<String, Object>>> myRecords() {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("只有学生可以查看");
        }

        List<AttendRecord> records = recordMapper.selectList(
                Wrappers.<AttendRecord>lambdaQuery()
                        .eq(AttendRecord::getStudentId, u.getStudentId())
                        .orderByDesc(AttendRecord::getCheckinTime)
                        .last("LIMIT 50"));

        List<Map<String, Object>> out = new ArrayList<>();
        for (AttendRecord r : records) {
            AttendSession sess = sessionMapper.selectById(r.getSessionId());
            if (sess == null) continue;
            Schedule s = scheduleMapper.selectById(sess.getScheduleId());

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("checkinTime", r.getCheckinTime());
            m.put("status", r.getStatus());
            m.put("sessionDate", sess.getSessionDate());
            m.put("courseName", s == null ? "" : courseNameOf(s.getCourseId()));
            m.put("clazzName", s == null ? "" : clazzNameOf(s.getClazzId()));
            out.add(m);
        }
        return Result.ok(out);
    }

    // ============================================================ 教务端

    @Operation(summary = "教务：出勤总览")
    @RequiresPerm("edu:attend:stat")
    @GetMapping("/edu/stat")
    public Result<Map<String, Object>> eduStat(
            @RequestParam(required = false) Long clazzId) {
        List<AttendSession> sessions = sessionMapper.selectList(
                Wrappers.<AttendSession>lambdaQuery());

        Map<String, long[]> byClazz = new LinkedHashMap<>(); // [应到, 实到, 迟到, 早退, 请假]

        long totalPoints = 0;
        long signedPoints = 0;
        long latePoints = 0;
        long earlyPoints = 0;
        long leavePoints = 0;

        for (AttendSession sess : sessions) {
            Schedule s = scheduleMapper.selectById(sess.getScheduleId());
            if (s == null) continue;
            if (clazzId != null && !Objects.equals(s.getClazzId(), clazzId)) continue;

            String name = clazzNameOf(s.getClazzId());

            List<Student> students = studentMapper.selectList(
                    Wrappers.<Student>lambdaQuery().eq(Student::getClazzId, s.getClazzId()));
            Set<Long> sids = students.stream().map(Student::getId).collect(Collectors.toSet());
            if (sids.isEmpty()) continue;

            List<AttendRecord> recs = recordMapper.selectList(
                    Wrappers.<AttendRecord>lambdaQuery()
                            .eq(AttendRecord::getSessionId, sess.getId()));
            long late = recs.stream().filter(r -> Objects.equals(r.getStatus(), 2)).count();
            long early = recs.stream().filter(r -> Objects.equals(r.getStatus(), 3)).count();

            // 当天已批准的请假
            Long leaveN = leaveMapper.selectCount(Wrappers.<LeaveApply>lambdaQuery()
                    .in(LeaveApply::getStudentId, sids)
                    .eq(LeaveApply::getStatus, 3)
                    .le(LeaveApply::getStartTime, sess.getSessionDate().atTime(23, 59, 59))
                    .ge(LeaveApply::getEndTime, sess.getSessionDate().atTime(0, 0, 0)));

            long total = students.size();
            long signed = recs.size();
            long leave = leaveN == null ? 0 : leaveN;

            totalPoints += total;
            signedPoints += signed;
            latePoints += late;
            earlyPoints += early;
            leavePoints += leave;

            long[] arr = byClazz.computeIfAbsent(name, k -> new long[5]);
            arr[0] += total;
            arr[1] += signed;
            arr[2] += late;
            arr[3] += early;
            arr[4] += leave;
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : byClazz.entrySet()) {
            long[] v = e.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("clazzName", e.getKey());
            m.put("total", v[0]);
            m.put("signed", v[1]);
            m.put("late", v[2]);
            m.put("early", v[3]);
            m.put("leave", v[4]);
            m.put("absent", v[0] - v[1] - v[4]);
            m.put("rate", v[0] == 0 ? 0
                    : Math.round(v[1] * 10000.0 / v[0]) / 100.0);
            rows.add(m);
        }
        rows.sort((a, b) -> Double.compare(
                ((Number) a.get("rate")).doubleValue(),
                ((Number) b.get("rate")).doubleValue()));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionCount", sessions.size());
        out.put("totalPoints", totalPoints);
        out.put("signedPoints", signedPoints);
        out.put("latePoints", latePoints);
        out.put("earlyPoints", earlyPoints);
        out.put("leavePoints", leavePoints);
        out.put("absentPoints", totalPoints - signedPoints - leavePoints);
        out.put("rate", totalPoints == 0 ? 0
                : Math.round(signedPoints * 10000.0 / totalPoints) / 100.0);
        out.put("byClazz", rows);
        return Result.ok(out);
    }

    @Operation(summary = "教务：缺勤名单（排除已批准请假）")
    @RequiresPerm("edu:attend:stat")
    @GetMapping("/edu/absent")
    public Result<List<Map<String, Object>>> eduAbsent(
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) String date) {
        LocalDate d = (date == null || date.isEmpty())
                ? LocalDate.now() : LocalDate.parse(date);

        List<AttendSession> sessions = sessionMapper.selectList(
                Wrappers.<AttendSession>lambdaQuery()
                        .eq(AttendSession::getSessionDate, d));

        List<Map<String, Object>> out = new ArrayList<>();

        for (AttendSession sess : sessions) {
            Schedule s = scheduleMapper.selectById(sess.getScheduleId());
            if (s == null) continue;
            if (clazzId != null && !Objects.equals(s.getClazzId(), clazzId)) continue;

            List<Student> students = studentMapper.selectList(
                    Wrappers.<Student>lambdaQuery().eq(Student::getClazzId, s.getClazzId()));
            if (students.isEmpty()) continue;

            Set<Long> sids = students.stream().map(Student::getId).collect(Collectors.toSet());
            Set<Long> signedIds = recordMapper.selectList(
                            Wrappers.<AttendRecord>lambdaQuery()
                                    .eq(AttendRecord::getSessionId, sess.getId()))
                    .stream().map(AttendRecord::getStudentId).collect(Collectors.toSet());

            Set<Long> leaveIds = leaveMapper.selectList(
                            Wrappers.<LeaveApply>lambdaQuery()
                                    .in(LeaveApply::getStudentId, sids)
                                    .eq(LeaveApply::getStatus, 3)
                                    .le(LeaveApply::getStartTime, d.atTime(23, 59, 59))
                                    .ge(LeaveApply::getEndTime, d.atTime(0, 0, 0)))
                    .stream().map(LeaveApply::getStudentId).collect(Collectors.toSet());

            Set<Long> uids = students.stream().map(Student::getUserId).collect(Collectors.toSet());
            Map<Long, String> names = new HashMap<>();
            if (!uids.isEmpty()) {
                for (SysUser u : userMapper.selectBatchIds(uids)) {
                    names.put(u.getId(), u.getNickname());
                }
            }

            for (Student stu : students) {
                if (signedIds.contains(stu.getId())) continue;
                if (leaveIds.contains(stu.getId())) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("sessionId", sess.getId());
                m.put("code", sess.getCode());
                m.put("date", sess.getSessionDate());
                m.put("courseName", courseNameOf(s.getCourseId()));
                m.put("clazzName", clazzNameOf(s.getClazzId()));
                m.put("studentId", stu.getId());
                m.put("stuNo", stu.getStuNo());
                m.put("studentName", names.getOrDefault(stu.getUserId(), "学生"));
                m.put("statusName", "缺勤");
                out.add(m);
            }
        }
        return Result.ok(out);
    }

    // ============================================================ 内部

    private Long currentTeacherId() {
        LoginUser u = UserContext.require();
        if (u.getTeacherId() == null) {
            throw new BizException("当前账号不是教师");
        }
        return u.getTeacherId();
    }

    private String randomCode() {
        // 6 位数字，避免以 0 开头
        return String.valueOf(100000 + ThreadLocalRandom.current().nextInt(900000));
    }

    private Map<String, Object> buildStartResult(AttendSession sess, Schedule s) {
        long total = studentMapper.selectCount(Wrappers.<Student>lambdaQuery()
                .eq(Student::getClazzId, s.getClazzId()));
        List<Student> students = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                .eq(Student::getClazzId, s.getClazzId()));
        Set<Long> uids = students.stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        if (!uids.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(uids)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        List<Map<String, Object>> roster = new ArrayList<>();
        for (Student stu : students) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", stu.getId());
            m.put("stuNo", stu.getStuNo());
            m.put("studentName", names.getOrDefault(stu.getUserId(), "学生"));
            roster.add(m);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", sess.getId());
        out.put("code", sess.getCode());
        out.put("openTime", sess.getOpenTime());
        out.put("closeTime", sess.getCloseTime());
        out.put("courseName", courseNameOf(s.getCourseId()));
        out.put("clazzName", clazzNameOf(s.getClazzId()));
        out.put("total", total);
        out.put("roster", roster);
        return out;
    }

    @Operation(summary = "标记状态（正常 / 迟到 / 早退）")
    @RequiresPerm({"teacher:attend:list", "edu:attend:list"})
    @PutMapping("/record/{id}/mark")
    public Result<Void> mark(@PathVariable Long id,
                             @RequestBody Map<String, Object> body) {
        AttendRecord r = recordMapper.selectById(id);
        if (r == null) {
            throw new BizException("签到记录不存在");
        }
        AttendSession sess = sessionMapper.selectById(r.getSessionId());
        if (sess == null) {
            throw new BizException("场次不存在");
        }
        LoginUser u = UserContext.require();
        if (u.getTeacherId() != null
                && !Objects.equals(sess.getTeacherId(), u.getTeacherId())) {
            throw new BizException("只能操作自己的点名");
        }
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        if (status < 1 || status > 3) {
            throw new BizException("状态只能是 1正常 / 2迟到 / 3早退");
        }
        r.setStatus(status);
        recordMapper.updateById(r);
        return Result.ok("已更新", null);
    }

    @Operation(summary = "教师手动补录（把未签的学生标为正常或请假）")
    @RequiresPerm("teacher:attend:list")
    @PostMapping("/session/{id}/mark-student")
    public Result<Void> markStudent(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        AttendSession sess = sessionMapper.selectById(id);
        if (sess == null) {
            throw new BizException("场次不存在");
        }
        Long teacherId = currentTeacherId();
        if (!Objects.equals(sess.getTeacherId(), teacherId)) {
            throw new BizException("只能操作自己的点名");
        }

        Long studentId = body.get("studentId") instanceof Number n ? n.longValue() : null;
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        if (studentId == null) {
            throw new BizException("缺少学生");
        }
        if (status < 1 || status > 3) {
            throw new BizException("状态只能是 1正常 / 2迟到 / 3早退");
        }

        AttendRecord exist = recordMapper.selectOne(Wrappers.<AttendRecord>lambdaQuery()
                .eq(AttendRecord::getSessionId, id)
                .eq(AttendRecord::getStudentId, studentId)
                .last("LIMIT 1"));

        if (exist != null) {
            exist.setStatus(status);
            recordMapper.updateById(exist);
        } else {
            AttendRecord rec = new AttendRecord();
            rec.setSessionId(id);
            rec.setStudentId(studentId);
            rec.setCheckinTime(LocalDateTime.now());
            rec.setStatus(status);
            recordMapper.insert(rec);
        }
        return Result.ok("已补录", null);
    }

    private Map<String, Object> buildDetail(AttendSession sess, Schedule s) {
        List<Student> students = studentMapper.selectList(
                Wrappers.<Student>lambdaQuery().eq(Student::getClazzId, s.getClazzId()));
        Set<Long> uids = students.stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        if (!uids.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(uids)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        Map<Long, AttendRecord> recMap = recordMapper.selectList(
                        Wrappers.<AttendRecord>lambdaQuery()
                                .eq(AttendRecord::getSessionId, sess.getId()))
                .stream()
                .collect(Collectors.toMap(AttendRecord::getStudentId, r -> r, (a, b) -> a));

        // 当天已批准的请假（不算缺勤）
        Set<Long> sids = students.stream().map(Student::getId).collect(Collectors.toSet());
        Map<Long, LeaveApply> leaveMap = new HashMap<>();
        if (!sids.isEmpty()) {
            List<LeaveApply> leaves = leaveMapper.selectList(
                    Wrappers.<LeaveApply>lambdaQuery()
                            .in(LeaveApply::getStudentId, sids)
                            .eq(LeaveApply::getStatus, 3)
                            .le(LeaveApply::getStartTime, sess.getSessionDate().atTime(23, 59, 59))
                            .ge(LeaveApply::getEndTime, sess.getSessionDate().atTime(0, 0, 0)));
            for (LeaveApply l : leaves) {
                leaveMap.put(l.getStudentId(), l);
            }
        }

        List<Map<String, Object>> signed = new ArrayList<>();
        List<Map<String, Object>> onLeave = new ArrayList<>();
        List<Map<String, Object>> absent = new ArrayList<>();

        for (Student stu : students) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", stu.getId());
            m.put("stuNo", stu.getStuNo());
            m.put("studentName", names.getOrDefault(stu.getUserId(), "学生"));

            AttendRecord r = recMap.get(stu.getId());
            LeaveApply l = leaveMap.get(stu.getId());

            if (r != null) {
                m.put("recordId", r.getId());
                m.put("checkinTime", r.getCheckinTime());
                m.put("status", r.getStatus());
                m.put("statusName", attendStatusName(r.getStatus()));
                signed.add(m);
            } else if (l != null) {
                m.put("leaveId", l.getId());
                m.put("leaveType", l.getLeaveType());
                m.put("reason", l.getReason());
                m.put("statusName", "请假");
                onLeave.add(m);
            } else {
                m.put("statusName", "缺勤");
                absent.add(m);
            }
        }

        long lateCount = signed.stream()
                .filter(x -> Objects.equals(x.get("status"), 2)).count();
        long earlyCount = signed.stream()
                .filter(x -> Objects.equals(x.get("status"), 3)).count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", sess.getId());
        out.put("code", sess.getCode());
        out.put("sessionDate", sess.getSessionDate());
        out.put("openTime", sess.getOpenTime());
        out.put("closeTime", sess.getCloseTime());
        out.put("status", sess.getStatus());
        out.put("courseName", courseNameOf(s.getCourseId()));
        out.put("clazzName", clazzNameOf(s.getClazzId()));
        out.put("total", students.size());
        out.put("signedCount", signed.size());
        out.put("lateCount", lateCount);
        out.put("earlyCount", earlyCount);
        out.put("leaveCount", onLeave.size());
        out.put("absentCount", absent.size());
        out.put("signed", signed);
        out.put("onLeave", onLeave);
        out.put("absent", absent);
        return out;
    }

    private String attendStatusName(Integer status) {
        if (status == null) return "正常";
        return switch (status) {
            case 2 -> "迟到";
            case 3 -> "早退";
            default -> "正常";
        };
    }

    private String courseNameOf(Long id) {
        Course c = courseMapper.selectById(id);
        return c == null ? "课程" : c.getCourseName();
    }

    private String clazzNameOf(Long id) {
        Clazz c = clazzMapper.selectById(id);
        return c == null ? "班级" : c.getClazzName();
    }
}
