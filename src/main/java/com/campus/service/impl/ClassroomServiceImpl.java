package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.entity.Classroom;
import com.campus.entity.Schedule;
import com.campus.mapper.ClassroomMapper;
import com.campus.mapper.ScheduleMapper;
import com.campus.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomMapper classroomMapper;
    private final ScheduleMapper scheduleMapper;

    @Override
    public PageResult<Classroom> page(String keyword, Integer roomType, Integer status,
                                      Long current, Long size) {
        Page<Classroom> page = new Page<>(current == null ? 1 : current,
                size == null ? 10 : size);
        var w = Wrappers.<Classroom>lambdaQuery()
                .eq(roomType != null, Classroom::getRoomType, roomType)
                .eq(status != null, Classroom::getStatus, status)
                .and(StringUtils.hasText(keyword), q -> q
                        .like(Classroom::getClassroomNo, keyword)
                        .or().like(Classroom::getBuilding, keyword))
                .orderByAsc(Classroom::getBuilding)
                .orderByAsc(Classroom::getClassroomNo);
        return PageResult.of(classroomMapper.selectPage(page, w));
    }

    @Override
    public List<Classroom> listAvailable() {
        return classroomMapper.selectList(Wrappers.<Classroom>lambdaQuery()
                .eq(Classroom::getStatus, 1)
                .orderByAsc(Classroom::getBuilding)
                .orderByAsc(Classroom::getClassroomNo));
    }

    @Override
    public void add(Classroom room) {
        checkNoUnique(room);
        classroomMapper.insert(room);
    }

    @Override
    public void update(Classroom room) {
        if (room.getId() == null) {
            throw new BizException("教室ID不能为空");
        }
        checkNoUnique(room);
        classroomMapper.updateById(room);
    }

    @Override
    public void remove(Long id) {
        Long used = scheduleMapper.selectCount(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getClassroomId, id)
                .eq(Schedule::getStatus, 1));
        if (used != null && used > 0) {
            throw new BizException("该教室已被排课使用，无法删除");
        }
        classroomMapper.deleteById(id);
    }

    private void checkNoUnique(Classroom room) {
        var q = Wrappers.<Classroom>lambdaQuery()
                .eq(Classroom::getClassroomNo, room.getClassroomNo());
        if (room.getId() != null) {
            q.ne(Classroom::getId, room.getId());
        }
        Long n = classroomMapper.selectCount(q);
        if (n != null && n > 0) {
            throw new BizException("教室编号已存在：" + room.getClassroomNo());
        }
    }
}
