package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.AwardPunish;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.mapper.AwardPunishMapper;
import com.campus.mapper.StudentMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "学工-奖惩")
@RestController
@RequestMapping("/study/award")
@RequiredArgsConstructor
public class AwardController {

    private final AwardPunishMapper awardMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;

    @Operation(summary = "奖惩分页")
    @RequiresPerm("study:award:list")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<AwardPunish> page = new Page<>(current, size);
        var w = Wrappers.<AwardPunish>lambdaQuery()
                .eq(type != null, AwardPunish::getType, type)
                .eq(studentId != null, AwardPunish::getStudentId, studentId)
                .orderByDesc(AwardPunish::getOccurDate);
        Page<AwardPunish> result = awardMapper.selectPage(page, w);
        return Result.ok(new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toRows(result.getRecords())));
    }

    @Operation(summary = "新增奖惩")
    @RequiresPerm("study:award:add")
    @OperLog(module = "奖惩管理", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody AwardPunish body) {
        awardMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改奖惩")
    @RequiresPerm("study:award:edit")
    @OperLog(module = "奖惩管理", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody AwardPunish body) {
        awardMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除奖惩")
    @RequiresPerm("study:award:remove")
    @OperLog(module = "奖惩管理", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        awardMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }

    private List<Map<String, Object>> toRows(List<AwardPunish> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(AwardPunish::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Set<Long> uids = stuMap.values().stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        for (SysUser u : userMapper.selectBatchIds(uids)) {
            names.put(u.getId(), u.getNickname());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (AwardPunish a : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("studentId", a.getStudentId());
            Student s = stuMap.get(a.getStudentId());
            m.put("studentName", s == null ? "—" : names.getOrDefault(s.getUserId(), "学生"));
            m.put("stuNo", s == null ? "—" : s.getStuNo());
            m.put("type", a.getType());
            m.put("level", a.getLevel());
            m.put("title", a.getTitle());
            m.put("content", a.getContent());
            m.put("occurDate", a.getOccurDate());
            m.put("fileUrl", a.getFileUrl());
            out.add(m);
        }
        return out;
    }
}
