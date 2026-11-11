package com.campus.service;

import com.campus.common.PageResult;
import com.campus.entity.Classroom;

import java.util.List;

public interface ClassroomService {

    PageResult<Classroom> page(String keyword, Integer roomType, Integer status,
                               Long current, Long size);

    List<Classroom> listAvailable();

    void add(Classroom room);

    void update(Classroom room);

    void remove(Long id);
}
