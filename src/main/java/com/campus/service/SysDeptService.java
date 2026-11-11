package com.campus.service;

import com.campus.entity.SysDept;

import java.util.List;

public interface SysDeptService {

    List<SysDept> tree();

    List<SysDept> list();

    void add(SysDept dept);

    void update(SysDept dept);

    void remove(Long id);
}
