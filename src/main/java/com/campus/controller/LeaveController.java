package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.LeaveApply;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.entity.Clazz;
import com.campus.mapper.LeaveApplyMapper;
import com.campus.mapper.StudentMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.mapper.ClazzMapper;
import com.campus.security.LoginUser;
import com.campus.security.RequiresPerm;
import com.campus.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "学工-请假")
@RestController
@RequestMapping("/study/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveApplyMapper leaveMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final ClazzMapper clazzMapper;

    @Operation(summary = "请假分页")
    @RequiresPerm({"study:leave:list", "student:leave:list"})
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {

        Page<LeaveApply> page = new Page<>(current, size);
        var w = Wrappers.<LeaveApply>lambdaQuery()
                .eq(status != null, LeaveApply::getStatus, status)
                .eq(clazzId != null, LeaveApply::getClazzId, clazzId)
                .eq(studentId != null, LeaveApply::getStudentId, studentId)
                .orderByDesc(LeaveApply::getCreateTime);

        Page<LeaveApply> result = leaveMapper.selectPage(page, w);
        return Result.ok(new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toRows(result.getRecords())));
    }

    @Operation(summary = "我的请假（学生）")
    @RequiresPerm("student:leave:list")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my() {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("当前账号不是学生");
        }
        List<LeaveApply> list = leaveMapper.selectList(Wrappers.<LeaveApply>lambdaQuery()
                .eq(LeaveApply::getStudentId, u.getStudentId())
                .orderByDesc(LeaveApply::getCreateTime));
        return Result.ok(toRows(list));
    }

    @Operation(summary = "提交请假")
    @RequiresPerm("student:leave:add")
    @OperLog(module = "请假管理", type = "INSERT")
    @PostMapping
    public Result<Void> submit(@RequestBody LeaveApply body) {
        LoginUser u = UserContext.require();
        Long sid = body.getStudentId() != null ? body.getStudentId() : u.getStudentId();
        if (sid == null) {
            throw new BizException("缺少学生信息");
        }
        Student stu = studentMapper.selectById(sid);
        if (stu == null) {
            throw new BizException("学生不存在");
        }
        body.setId(null);
        body.setStudentId(sid);
        body.setClazzId(stu.getClazzId());
        body.setApplyNo("LV" + System.currentTimeMillis());
        body.setStatus(1);
        // 指派给该班辅导员
        Clazz clazz = clazzMapper.selectById(stu.getClazzId());
        body.setCurrentApprover(clazz == null ? null : clazz.getCounselorId());
        leaveMapper.insert(body);
        return Result.ok("已提交，等待辅导员审批", null);
    }

    @Operation(summary = "审批请假")
    @RequiresPerm({"study:leave:audit", "counselor:leave:audit"})
    @OperLog(module = "请假管理", type = "APPROVE")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestBody Map<String, Object> body) {
        LeaveApply apply = leaveMapper.selectById(id);
        if (apply == null) {
            throw new BizException("请假单不存在");
        }
        int result = body.get("result") instanceof Number n ? n.intValue() : 1;
        String opinion = (String) body.getOrDefault("opinion", "");

        if (result == 1) {
            // 同意：≤3 天直接通过，>3 天转学工
            boolean shortLeave = apply.getDays() != null
                    && apply.getDays().doubleValue() <= 3;
            if (apply.getStatus() == 1 && shortLeave) {
                apply.setStatus(3);
            } else if (apply.getStatus() == 1) {
                apply.setStatus(2);
                apply.setCurrentApprover(null);
            } else {
                apply.setStatus(3);
            }
        } else {
            apply.setStatus(4);
        }
        apply.setApproveOpinion(opinion);
        apply.setApproveTime(LocalDateTime.now());
        leaveMapper.updateById(apply);
        return Result.ok(result == 1 ? "已通过" : "已驳回", null);
    }

    @Operation(summary = "请假二维码载荷（仅已通过的请假）")
    @GetMapping("/{id}/qrcode")
    public Result<Map<String, Object>> qrcode(@PathVariable Long id) {
        LeaveApply a = leaveMapper.selectById(id);
        if (a == null) {
            throw new BizException("请假单不存在");
        }
        if (!Integer.valueOf(3).equals(a.getStatus())) {
            throw new BizException("只有审批通过的请假才能生成二维码");
        }

        LoginUser u = UserContext.require();
        if (u.getStudentId() != null && !u.getStudentId().equals(a.getStudentId())) {
            throw new BizException("只能查看自己的请假单");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("payload", TeacherLeaveController.buildQrPayload(a));
        out.put("applyNo", a.getApplyNo());
        out.put("startTime", a.getStartTime());
        out.put("endTime", a.getEndTime());
        out.put("days", a.getDays());
        out.put("reason", a.getReason());
        return Result.ok(out);
    }

    private List<Map<String, Object>> toRows(List<LeaveApply> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(LeaveApply::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Set<Long> uids = stuMap.values().stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        for (SysUser u : userMapper.selectBatchIds(uids)) {
            names.put(u.getId(), u.getNickname());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (LeaveApply a : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("applyNo", a.getApplyNo());
            m.put("studentId", a.getStudentId());
            m.put("clazzId", a.getClazzId());
            Student s = stuMap.get(a.getStudentId());
            m.put("studentName", s == null ? "—" : names.getOrDefault(s.getUserId(), "学生"));
            m.put("stuNo", s == null ? "—" : s.getStuNo());
            m.put("leaveType", a.getLeaveType());
            m.put("startTime", a.getStartTime());
            m.put("endTime", a.getEndTime());
            m.put("days", a.getDays());
            m.put("reason", a.getReason());
            m.put("fileUrl", a.getFileUrl());
            m.put("status", a.getStatus());
            m.put("approveOpinion", a.getApproveOpinion());
            m.put("createTime", a.getCreateTime());
            out.add(m);
        }
        return out;
    }
}
