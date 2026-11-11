package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.Result;
import com.campus.common.exception.BizException;
import com.campus.entity.EcActivity;
import com.campus.entity.EcEnroll;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.mapper.EcActivityMapper;
import com.campus.mapper.EcEnrollMapper;
import com.campus.mapper.StudentMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.security.LoginUser;
import com.campus.security.RequiresPerm;
import com.campus.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 第二课堂活动签到
 *
 * 确认学生是否参加了活动的完整链路：
 *   1. 组织者在现场点「开启签到」→ 生成 6 位码
 *   2. 学生活动当天输入码签到
 *   3. 签到成功 → ec_enroll.status = 2 → 学分自动计入
 *   4. 组织者随时看「已签 / 未签」名单，可批量补确认
 */
@Tag(name = "第二课堂-签到")
@RestController
@RequestMapping("/ec/checkin")
@RequiredArgsConstructor
public class EcCheckinController {

    private final EcActivityMapper activityMapper;
    private final EcEnrollMapper enrollMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;

    // ============================================================ 组织者

    @Operation(summary = "开启活动签到（生成 6 位码）")
    @RequiresPerm("ec:activity:checkin")
    @PostMapping("/open")
    public Result<Map<String, Object>> open(@RequestBody Map<String, Object> body) {
        Long activityId = body.get("activityId") instanceof Number n
                ? n.longValue() : null;
        int minutes = body.get("minutes") instanceof Number n
                ? Math.max(5, Math.min(600, n.intValue())) : 120;

        if (activityId == null) {
            throw new BizException("缺少活动");
        }
        EcActivity a = activityMapper.selectById(activityId);
        if (a == null) {
            throw new BizException("活动不存在");
        }

        // 只有发布人 / 超管能开
        LoginUser u = UserContext.require();
        boolean isSuper = u.getUserType() != null && u.getUserType() == 1;
        if (!isSuper && !Objects.equals(a.getPublisherId(), u.getUserId())) {
            throw new BizException("只能给自己发布的活动开启签到");
        }
        if (a.getStatus() == 0) {
            throw new BizException("活动还没发布");
        }
        if (a.getStatus() == 4) {
            throw new BizException("活动已取消");
        }

        LocalDateTime now = LocalDateTime.now();
        a.setCheckinCode(randomCode());
        a.setCheckinStart(now);
        // 默认持续到活动结束，取两者较大值
        LocalDateTime byActivity = a.getEndTime() == null
                ? now.plusMinutes(minutes) : a.getEndTime();
        LocalDateTime byMinutes = now.plusMinutes(minutes);
        a.setCheckinEnd(byActivity.isAfter(byMinutes) ? byActivity : byMinutes);
        activityMapper.updateById(a);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("activityId", a.getId());
        out.put("code", a.getCheckinCode());
        out.put("checkinStart", a.getCheckinStart());
        out.put("checkinEnd", a.getCheckinEnd());
        out.put("enrollCount", a.getEnrollCount());
        out.put("title", a.getTitle());
        return Result.ok(out);
    }

    @Operation(summary = "关闭签到")
    @RequiresPerm("ec:activity:checkin")
    @PutMapping("/close/{activityId}")
    public Result<Void> close(@PathVariable Long activityId) {
        EcActivity a = activityMapper.selectById(activityId);
        if (a == null) {
            throw new BizException("活动不存在");
        }
        LoginUser u = UserContext.require();
        boolean isSuper = u.getUserType() != null && u.getUserType() == 1;
        if (!isSuper && !Objects.equals(a.getPublisherId(), u.getUserId())) {
            throw new BizException("只能操作自己发布的活动");
        }
        a.setCheckinCode(null);
        a.setCheckinEnd(LocalDateTime.now());
        activityMapper.updateById(a);
        return Result.ok("签到已关闭", null);
    }

    @Operation(summary = "签到名单（已签 / 未签）")
    @RequiresPerm({"ec:activity:checkin", "ec:activity:list"})
    @GetMapping("/list/{activityId}")
    public Result<Map<String, Object>> list(@PathVariable Long activityId) {
        EcActivity a = activityMapper.selectById(activityId);
        if (a == null) {
            throw new BizException("活动不存在");
        }

        List<EcEnroll> enrolls = enrollMapper.selectList(
                Wrappers.<EcEnroll>lambdaQuery()
                        .eq(EcEnroll::getActivityId, activityId)
                        .ne(EcEnroll::getStatus, 3));   // 排除已取消
        if (enrolls.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("title", a.getTitle());
            empty.put("credit", a.getCredit());
            empty.put("code", a.getCheckinCode());
            empty.put("checkinEnd", a.getCheckinEnd());
            empty.put("signed", List.of());
            empty.put("unsigned", List.of());
            empty.put("signedCount", 0);
            empty.put("unsignedCount", 0);
            return Result.ok(empty);
        }

        Set<Long> sids = enrolls.stream().map(EcEnroll::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Set<Long> uids = stuMap.values().stream().map(Student::getUserId)
                .collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        if (!uids.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(uids)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        List<Map<String, Object>> signed = new ArrayList<>();
        List<Map<String, Object>> unsigned = new ArrayList<>();

        for (EcEnroll e : enrolls) {
            Student stu = stuMap.get(e.getStudentId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("enrollId", e.getId());
            m.put("studentId", e.getStudentId());
            m.put("stuNo", stu == null ? "" : stu.getStuNo());
            m.put("studentName", stu == null ? "学生"
                    : names.getOrDefault(stu.getUserId(), "学生"));
            m.put("enrollTime", e.getEnrollTime());

            if (Objects.equals(e.getStatus(), 2)) {
                m.put("checkinTime", e.getCheckinTime());
                signed.add(m);
            } else {
                unsigned.add(m);
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("activityId", a.getId());
        out.put("title", a.getTitle());
        out.put("credit", a.getCredit());
        out.put("code", a.getCheckinCode());
        out.put("checkinStart", a.getCheckinStart());
        out.put("checkinEnd", a.getCheckinEnd());
        out.put("signedCount", signed.size());
        out.put("unsignedCount", unsigned.size());
        out.put("signed", signed);
        out.put("unsigned", unsigned);
        return Result.ok(out);
    }

    @Operation(summary = "批量确认参加（补录）")
    @RequiresPerm("ec:activity:checkin")
    @PostMapping("/confirm")
    public Result<Map<String, Object>> confirm(@RequestBody Map<String, Object> body) {
        Long activityId = body.get("activityId") instanceof Number n
                ? n.longValue() : null;
        if (activityId == null) {
            throw new BizException("缺少活动");
        }
        EcActivity a = activityMapper.selectById(activityId);
        if (a == null) {
            throw new BizException("活动不存在");
        }
        LoginUser u = UserContext.require();
        boolean isSuper = u.getUserType() != null && u.getUserType() == 1;
        if (!isSuper && !Objects.equals(a.getPublisherId(), u.getUserId())) {
            throw new BizException("只能操作自己发布的活动");
        }

        List<?> raw = body.get("studentIds") instanceof List
                ? (List<?>) body.get("studentIds") : List.of();
        List<Long> sids = raw.stream()
                .filter(x -> x instanceof Number)
                .map(x -> ((Number) x).longValue())
                .toList();
        if (sids.isEmpty()) {
            throw new BizException("请选择学生");
        }

        int updated = 0;
        for (Long sid : sids) {
            EcEnroll e = enrollMapper.selectOne(Wrappers.<EcEnroll>lambdaQuery()
                    .eq(EcEnroll::getActivityId, activityId)
                    .eq(EcEnroll::getStudentId, sid)
                    .last("LIMIT 1"));
            if (e == null) {
                // 没报名也直接补一条已签到记录
                EcEnroll ne = new EcEnroll();
                ne.setActivityId(activityId);
                ne.setStudentId(sid);
                ne.setEnrollTime(LocalDateTime.now());
                ne.setCheckinTime(LocalDateTime.now());
                ne.setStatus(2);
                enrollMapper.insert(ne);
                updated++;
            } else if (!Objects.equals(e.getStatus(), 2)) {
                e.setStatus(2);
                e.setCheckinTime(LocalDateTime.now());
                enrollMapper.updateById(e);
                updated++;
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("updated", updated);
        out.put("msg", "已确认 " + updated + " 人参加，学分已计入");
        return Result.ok(out);
    }

    // ============================================================ 学生

    @Operation(summary = "按签到码查活动（签到前预览）")
    @GetMapping("/preview/{code}")
    public Result<Map<String, Object>> preview(@PathVariable String code) {
        EcActivity a = activityMapper.selectOne(Wrappers.<EcActivity>lambdaQuery()
                .eq(EcActivity::getCheckinCode, code)
                .last("LIMIT 1"));
        if (a == null) {
            throw new BizException("签到码无效");
        }
        if (a.getCheckinEnd() != null && LocalDateTime.now().isAfter(a.getCheckinEnd())) {
            throw new BizException("签到已截止");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("activityId", a.getId());
        out.put("title", a.getTitle());
        out.put("credit", a.getCredit());
        out.put("location", a.getLocation());
        out.put("endTime", a.getEndTime());
        return Result.ok(out);
    }

    @Operation(summary = "学生扫码签到")
    @RequiresPerm("ec:enroll:add")
    @PostMapping("/do")
    public Result<Map<String, Object>> checkin(@RequestBody Map<String, Object> body) {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("只有学生可以签到");
        }
        String code = body.get("code") == null
                ? "" : String.valueOf(body.get("code")).trim();
        if (code.isEmpty()) {
            throw new BizException("请输入签到码");
        }

        EcActivity a = activityMapper.selectOne(Wrappers.<EcActivity>lambdaQuery()
                .eq(EcActivity::getCheckinCode, code)
                .last("LIMIT 1"));
        if (a == null) {
            throw new BizException("签到码无效");
        }
        if (a.getStatus() == 4) {
            throw new BizException("活动已取消");
        }

        LocalDateTime now = LocalDateTime.now();
        if (a.getCheckinStart() != null && now.isBefore(a.getCheckinStart())) {
            throw new BizException("签到还没开始");
        }
        if (a.getCheckinEnd() != null && now.isAfter(a.getCheckinEnd())) {
            throw new BizException("签到已截止");
        }

        EcEnroll e = enrollMapper.selectOne(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getActivityId, a.getId())
                .eq(EcEnroll::getStudentId, u.getStudentId())
                .last("LIMIT 1"));

        if (e == null) {
            throw new BizException("你还没有报名这个活动");
        }
        if (Objects.equals(e.getStatus(), 2)) {
            throw new BizException("你已经签到过了");
        }
        if (Objects.equals(e.getStatus(), 3)) {
            throw new BizException("你的报名已取消");
        }

        e.setStatus(2);
        e.setCheckinTime(now);
        enrollMapper.updateById(e);

        // 是否迟到：活动开始 15 分钟后签到
        boolean late = a.getStartTime() != null
                && now.isAfter(a.getStartTime().plusMinutes(15));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("late", late);
        out.put("activityId", a.getId());
        out.put("title", a.getTitle());
        out.put("credit", a.getCredit());
        return Result.ok(late
                ? "签到成功（迟到），" + a.getCredit() + " 学分已计入"
                : "签到成功，" + a.getCredit() + " 学分已计入", out);
    }

    @Operation(summary = "我负责的活动（组织者）")
    @RequiresPerm("ec:activity:checkin")
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine() {
        LoginUser u = UserContext.require();
        boolean isSuper = u.getUserType() != null && u.getUserType() == 1;

        var w = Wrappers.<EcActivity>lambdaQuery()
                .ne(EcActivity::getStatus, 4)
                .orderByDesc(EcActivity::getStartTime);
        if (!isSuper) {
            w.eq(EcActivity::getPublisherId, u.getUserId());
        }
        List<EcActivity> list = activityMapper.selectList(w);

        List<Map<String, Object>> out = new ArrayList<>();
        for (EcActivity a : list) {
            long signed = enrollMapper.selectCount(Wrappers.<EcEnroll>lambdaQuery()
                    .eq(EcEnroll::getActivityId, a.getId())
                    .eq(EcEnroll::getStatus, 2));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("credit", a.getCredit());
            m.put("startTime", a.getStartTime());
            m.put("endTime", a.getEndTime());
            m.put("location", a.getLocation());
            m.put("status", a.getStatus());
            m.put("enrollCount", a.getEnrollCount());
            m.put("signedCount", signed);
            m.put("checkinCode", a.getCheckinCode());
            m.put("checkinEnd", a.getCheckinEnd());
            out.add(m);
        }
        return Result.ok(out);
    }

    // ============================================================ 内部

    private String randomCode() {
        return String.valueOf(100000 + ThreadLocalRandom.current().nextInt(900000));
    }
}
