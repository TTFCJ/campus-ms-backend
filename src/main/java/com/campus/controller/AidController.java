package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.AidApply;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.mapper.AidApplyMapper;
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
import java.util.stream.Collectors;

@Tag(name = "学工-助学金")
@RestController
@RequestMapping("/study/aid")
@RequiredArgsConstructor
public class AidController {

    private final AidApplyMapper aidMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;

    @Operation(summary = "助学金分页")
    @RequiresPerm({"study:aid:list", "student:aid:list"})
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer aidType,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<AidApply> page = new Page<>(current, size);
        var w = Wrappers.<AidApply>lambdaQuery()
                .eq(status != null, AidApply::getStatus, status)
                .eq(aidType != null, AidApply::getAidType, aidType)
                .eq(clazzId != null, AidApply::getClazzId, clazzId)
                .orderByDesc(AidApply::getCreateTime);
        Page<AidApply> result = aidMapper.selectPage(page, w);
        return Result.ok(new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toRows(result.getRecords())));
    }

    @Operation(summary = "我的助学金申请")
    @RequiresPerm("student:aid:list")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my() {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("当前账号不是学生");
        }
        return Result.ok(toRows(aidMapper.selectList(Wrappers.<AidApply>lambdaQuery()
                .eq(AidApply::getStudentId, u.getStudentId())
                .orderByDesc(AidApply::getCreateTime))));
    }

    @Operation(summary = "提交申请")
    @RequiresPerm("student:aid:add")
    @OperLog(module = "助学金", type = "INSERT")
    @PostMapping
    public Result<Void> submit(@RequestBody AidApply body) {
        LoginUser u = UserContext.require();
        Long sid = body.getStudentId() != null ? body.getStudentId() : u.getStudentId();
        Student stu = studentMapper.selectById(sid);
        if (stu == null) {
            throw new BizException("学生不存在");
        }
        body.setId(null);
        body.setStudentId(sid);
        body.setClazzId(stu.getClazzId());
        body.setApplyNo("AID" + System.currentTimeMillis());
        body.setStatus(1);
        body.setPublicStatus(0);
        aidMapper.insert(body);
        return Result.ok("已提交，等待辅导员初审", null);
    }

    @Operation(summary = "审批助学金")
    @RequiresPerm("study:aid:audit")
    @OperLog(module = "助学金", type = "APPROVE")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        AidApply apply = aidMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请单不存在");
        }
        int result = body.get("result") instanceof Number n ? n.intValue() : 1;
        String opinion = (String) body.getOrDefault("opinion", "");

        if (result == 1) {
            int next = switch (apply.getStatus() == null ? 1 : apply.getStatus()) {
                case 1 -> 2; // 辅导员初审 -> 院系复审
                case 2 -> 3; // 院系复审 -> 校级终审
                default -> 4; // 终审 -> 通过
            };
            apply.setStatus(next);
        } else {
            apply.setStatus(5);
        }
        apply.setApproveOpinion(opinion);
        apply.setApproveTime(LocalDateTime.now());
        aidMapper.updateById(apply);
        return Result.ok(result == 1 ? "已通过" : "已驳回", null);
    }

    @Operation(summary = "公示")
    @RequiresPerm("study:aid:public")
    @OperLog(module = "助学金", type = "UPDATE")
    @PutMapping("/{id}/public")
    public Result<Void> makePublic(@PathVariable Long id) {
        AidApply a = new AidApply();
        a.setId(id);
        a.setPublicStatus(2);
        aidMapper.updateById(a);
        return Result.ok("已公示", null);
    }

    private List<Map<String, Object>> toRows(List<AidApply> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(AidApply::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Set<Long> uids = stuMap.values().stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        for (SysUser u : userMapper.selectBatchIds(uids)) {
            names.put(u.getId(), u.getNickname());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (AidApply a : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("applyNo", a.getApplyNo());
            m.put("studentId", a.getStudentId());
            m.put("clazzId", a.getClazzId());
            Student s = stuMap.get(a.getStudentId());
            m.put("studentName", s == null ? "—" : names.getOrDefault(s.getUserId(), "学生"));
            m.put("stuNo", s == null ? "—" : s.getStuNo());
            m.put("aidType", a.getAidType());
            m.put("aidLevel", a.getAidLevel());
            m.put("amount", a.getAmount());
            m.put("reason", a.getReason());
            m.put("fileUrl", a.getFileUrl());
            m.put("status", a.getStatus());
            m.put("publicStatus", a.getPublicStatus());
            m.put("approveOpinion", a.getApproveOpinion());
            m.put("createTime", a.getCreateTime());
            out.add(m);
        }
        return out;
    }
}
