package com.campus.common.util;

import com.campus.entity.SysDept;
import com.campus.entity.SysMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 树形结构工具
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    public static List<SysMenu> buildMenuTree(List<SysMenu> list) {
        Map<Long, List<SysMenu>> group = list.stream()
                .collect(Collectors.groupingBy(m -> Objects.requireNonNullElse(m.getParentId(), 0L)));
        list.forEach(m -> m.setChildren(group.getOrDefault(m.getId(), new ArrayList<>())));
        return group.getOrDefault(0L, new ArrayList<>());
    }

    public static List<SysDept> buildDeptTree(List<SysDept> list) {
        Map<Long, List<SysDept>> group = list.stream()
                .collect(Collectors.groupingBy(d -> Objects.requireNonNullElse(d.getParentId(), 0L)));
        list.forEach(d -> d.setChildren(group.getOrDefault(d.getId(), new ArrayList<>())));
        return group.getOrDefault(0L, new ArrayList<>());
    }
}
