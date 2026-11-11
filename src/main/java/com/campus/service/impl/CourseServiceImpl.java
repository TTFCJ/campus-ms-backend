package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.dto.CourseDTO;
import com.campus.dto.CourseQuery;
import com.campus.entity.Course;
import com.campus.entity.Schedule;
import com.campus.mapper.CourseMapper;
import com.campus.mapper.ScheduleMapper;
import com.campus.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final ScheduleMapper scheduleMapper;

    @Override
    public PageResult<Course> page(CourseQuery query) {
        Page<Course> page = new Page<>(query.getCurrent(), query.getSize());
        var w = Wrappers.<Course>lambdaQuery()
                .eq(query.getDeptId() != null, Course::getDeptId, query.getDeptId())
                .eq(query.getCourseType() != null, Course::getCourseType, query.getCourseType())
                .eq(query.getStatus() != null, Course::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), q -> q
                        .like(Course::getCourseName, query.getKeyword())
                        .or().like(Course::getCourseCode, query.getKeyword()))
                .orderByDesc(Course::getId);
        return PageResult.of(courseMapper.selectPage(page, w));
    }

    @Override
    public List<Course> listAll() {
        return courseMapper.selectList(Wrappers.<Course>lambdaQuery()
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getCourseCode));
    }

    @Override
    public Course detail(Long id) {
        Course c = courseMapper.selectById(id);
        if (c == null) {
            throw new BizException("课程不存在");
        }
        return c;
    }

    @Override
    public void add(CourseDTO dto) {
        checkCodeUnique(dto);
        Course c = new Course();
        copy(dto, c);
        courseMapper.insert(c);
    }

    @Override
    public void update(CourseDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("课程ID不能为空");
        }
        checkCodeUnique(dto);
        Course c = new Course();
        c.setId(dto.getId());
        copy(dto, c);
        courseMapper.updateById(c);
    }

    @Override
    public void remove(Long id) {
        // 已排课的课程不允许删除
        Long used = scheduleMapper.selectCount(
                Wrappers.<Schedule>lambdaQuery()
                        .eq(Schedule::getCourseId, id)
                        .eq(Schedule::getStatus, 1));
        if (used != null && used > 0) {
            throw new BizException("该课程已排课，无法删除，请先停排");
        }
        courseMapper.deleteById(id);
    }

    private void copy(CourseDTO dto, Course c) {
        c.setCourseCode(dto.getCourseCode());
        c.setCourseName(dto.getCourseName());
        c.setDeptId(dto.getDeptId());
        c.setCredit(dto.getCredit());
        c.setClassHour(dto.getClassHour());
        c.setCourseType(dto.getCourseType() == null ? 1 : dto.getCourseType());
        c.setExamType(dto.getExamType() == null ? 1 : dto.getExamType());
        c.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        c.setRemark(dto.getRemark());
    }

    private void checkCodeUnique(CourseDTO dto) {
        var q = Wrappers.<Course>lambdaQuery().eq(Course::getCourseCode, dto.getCourseCode());
        if (dto.getId() != null) {
            q.ne(Course::getId, dto.getId());
        }
        Long n = courseMapper.selectCount(q);
        if (n != null && n > 0) {
            throw new BizException("课程编号已存在：" + dto.getCourseCode());
        }
    }
}
