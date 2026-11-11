package com.campus.service;

import com.campus.entity.Semester;

import java.util.List;

public interface SemesterService {

    /** 当前学期（is_current=1），没有则取最近一个 */
    Semester current();

    List<Semester> list();

    void add(Semester semester);

    void update(Semester semester);

    void remove(Long id);

    /** 设为当前学期（互斥） */
    void setCurrent(Long id);
}
