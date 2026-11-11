package com.campus.service;

import com.campus.entity.SysDept;
import com.campus.entity.SysMenu;

import java.util.List;

public interface SysMenuService {

    /** 完整菜单树（含按钮），可带关键字过滤 */
    List<SysMenu> tree(String keyword);

    List<SysMenu> list();

    void add(SysMenu menu);

    void update(SysMenu menu);

    void remove(Long id);
}
