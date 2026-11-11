package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.exception.BizException;
import com.campus.common.util.TreeUtils;
import com.campus.entity.SysMenu;
import com.campus.entity.SysRoleMenu;
import com.campus.mapper.SysMenuMapper;
import com.campus.mapper.SysRoleMenuMapper;
import com.campus.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysMenu> tree(String keyword) {
        List<SysMenu> all = list();
        if (!StringUtils.hasText(keyword)) {
            return TreeUtils.buildMenuTree(all);
        }
        // 关键字过滤时保留匹配项及其所有祖先，保证树不断头
        List<SysMenu> hit = all.stream()
                .filter(m -> m.getMenuName().contains(keyword)
                        || (m.getPerms() != null && m.getPerms().contains(keyword)))
                .toList();
        if (hit.isEmpty()) {
            return List.of();
        }
        java.util.Set<Long> keep = new java.util.HashSet<>();
        java.util.Map<Long, SysMenu> idMap = all.stream()
                .collect(java.util.stream.Collectors.toMap(SysMenu::getId, m -> m));
        for (SysMenu m : hit) {
            SysMenu cur = m;
            while (cur != null) {
                keep.add(cur.getId());
                cur = idMap.get(cur.getParentId() == null ? 0L : cur.getParentId());
            }
        }
        return TreeUtils.buildMenuTree(all.stream().filter(m -> keep.contains(m.getId())).toList());
    }

    @Override
    public List<SysMenu> list() {
        return menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getSort));
    }

    @Override
    public void add(SysMenu menu) {
        checkPermUnique(menu);
        menuMapper.insert(menu);
    }

    @Override
    public void update(SysMenu menu) {
        if (menu.getId() == null) {
            throw new BizException("菜单ID不能为空");
        }
        if (menu.getId().equals(menu.getParentId())) {
            throw new BizException("上级菜单不能是自己");
        }
        checkPermUnique(menu);
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Long children = menuMapper.selectCount(
                Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException("存在子菜单，请先删除子菜单");
        }
        menuMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
    }

    private void checkPermUnique(SysMenu menu) {
        if (!StringUtils.hasText(menu.getPerms())) {
            return;
        }
        var q = Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getPerms, menu.getPerms());
        if (menu.getId() != null) {
            q.ne(SysMenu::getId, menu.getId());
        }
        Long c = menuMapper.selectCount(q);
        if (c != null && c > 0) {
            throw new BizException("权限标识已存在：" + menu.getPerms());
        }
    }
}
