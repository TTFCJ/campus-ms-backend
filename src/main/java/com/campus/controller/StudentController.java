package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.Clazz;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.mapper.ClazzMapper;
import com.campus.mapper.StudentMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "学工-学生档案")
@RestController
@RequestMapping("/study/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final ClazzMapper clazzMapper;

    @Operation(summary = "学生分页")
    @RequiresPerm("study:student:list")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer isInSchool,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {

        Page<Student> page = new Page<>(current, size);
        var w = Wrappers.<Student>lambdaQuery()
                .eq(clazzId != null, Student::getClazzId, clazzId)
                .eq(deptId != null, Student::getDeptId, deptId)
                .eq(isInSchool != null, Student::getIsInSchool, isInSchool)
                .like(StringUtils.hasText(keyword), Student::getStuNo, keyword)
                .orderByAsc(Student::getStuNo);

        Page<Student> result = studentMapper.selectPage(page, w);
        List<Map<String, Object>> rows = toRows(result.getRecords());
        return Result.ok(new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), rows));
    }

    @Operation(summary = "学生详情")
    @RequiresPerm("study:student:list")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Student s = studentMapper.selectById(id);
        if (s == null) {
            throw new BizException("学生不存在");
        }
        return Result.ok(toRows(List.of(s)).get(0));
    }

    @Operation(summary = "新增学生")
    @RequiresPerm("study:student:add")
    @OperLog(module = "学生档案", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody Student student) {
        studentMapper.insert(student);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改学生")
    @RequiresPerm("study:student:edit")
    @OperLog(module = "学生档案", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody Student student) {
        studentMapper.updateById(student);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除学生")
    @RequiresPerm("study:student:remove")
    @OperLog(module = "学生档案", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        studentMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }

    /** 组装姓名 + 班级名 */
    private List<Map<String, Object>> toRows(List<Student> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> uids = list.stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        for (SysUser u : userMapper.selectBatchIds(uids)) {
            names.put(u.getId(), u.getNickname());
        }
        Set<Long> cids = list.stream().map(Student::getClazzId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> clazzNames = new HashMap<>();
        if (!cids.isEmpty()) {
            for (Clazz c : clazzMapper.selectBatchIds(cids)) {
                clazzNames.put(c.getId(), c.getClazzName());
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Student s : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("userId", s.getUserId());
            m.put("stuNo", s.getStuNo());
            m.put("name", names.getOrDefault(s.getUserId(), "学生" + s.getId()));
            m.put("clazzId", s.getClazzId());
            m.put("clazzName", clazzNames.get(s.getClazzId()));
            m.put("deptId", s.getDeptId());
            m.put("majorId", s.getMajorId());
            m.put("gender", s.getGender());
            m.put("birthDate", s.getBirthDate());
            m.put("idCard", s.getIdCard());
            m.put("enrollmentDate", s.getEnrollmentDate());
            m.put("politicalStatus", s.getPoliticalStatus());
            m.put("nationality", s.getNationality());
            m.put("homeAddress", s.getHomeAddress());
            m.put("emergencyContact", s.getEmergencyContact());
            m.put("emergencyPhone", s.getEmergencyPhone());
            m.put("photoUrl", s.getPhotoUrl());
            m.put("isInSchool", s.getIsInSchool());
            out.add(m);
        }
        return out;
    }
}
