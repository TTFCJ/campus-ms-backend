package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.exception.BizException;
import com.campus.entity.Semester;
import com.campus.mapper.SemesterMapper;
import com.campus.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    private final SemesterMapper semesterMapper;

    @Override
    public Semester current() {
        Semester s = semesterMapper.selectOne(Wrappers.<Semester>lambdaQuery()
                .eq(Semester::getIsCurrent, 1)
                .last("LIMIT 1"));
        if (s != null) {
            return s;
        }
        List<Semester> list = semesterMapper.selectList(Wrappers.<Semester>lambdaQuery()
                .orderByDesc(Semester::getStartDate)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Semester> list() {
        return semesterMapper.selectList(Wrappers.<Semester>lambdaQuery()
                .orderByDesc(Semester::getStartDate));
    }

    @Override
    public void add(Semester semester) {
        checkUnique(semester);
        if (semester.getIsCurrent() == null) {
            semester.setIsCurrent(0);
        }
        semesterMapper.insert(semester);
        if (Integer.valueOf(1).equals(semester.getIsCurrent())) {
            setCurrent(semester.getId());
        }
    }

    @Override
    public void update(Semester semester) {
        if (semester.getId() == null) {
            throw new BizException("学期ID不能为空");
        }
        checkUnique(semester);
        semesterMapper.updateById(semester);
    }

    @Override
    public void remove(Long id) {
        semesterMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrent(Long id) {
        // 先全部清零
        semesterMapper.update(null, Wrappers.<Semester>lambdaUpdate()
                .set(Semester::getIsCurrent, 0)
                .eq(Semester::getIsCurrent, 1));
        // 再设置目标
        semesterMapper.update(null, Wrappers.<Semester>lambdaUpdate()
                .set(Semester::getIsCurrent, 1)
                .eq(Semester::getId, id));
    }

    private void checkUnique(Semester semester) {
        var q = Wrappers.<Semester>lambdaQuery()
                .eq(Semester::getSemesterName, semester.getSemesterName());
        if (semester.getId() != null) {
            q.ne(Semester::getId, semester.getId());
        }
        Long n = semesterMapper.selectCount(q);
        if (n != null && n > 0) {
            throw new BizException("学期名称已存在：" + semester.getSemesterName());
        }
    }
}
