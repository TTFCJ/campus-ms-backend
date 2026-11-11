package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.CourseDTO;
import com.campus.dto.CourseQuery;
import com.campus.entity.Course;

import java.util.List;

public interface CourseService {

    PageResult<Course> page(CourseQuery query);

    List<Course> listAll();

    Course detail(Long id);

    void add(CourseDTO dto);

    void update(CourseDTO dto);

    void remove(Long id);
}
