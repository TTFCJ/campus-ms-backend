package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.dto.SysRoleDTO;
import com.campus.entity.SysRole;
import com.campus.entity.SysRoleMenu;
import com.campus.entity.SysUserRole;
import com.campus.mapper.SysMenuMapper;
import com.campus.mapper.SysRoleMapper;
import com.campus.mapper.SysRoleMenuMapper;
import com.campus.mapper.SysUserRoleMapper;
import com.campus.service.AuthService;
import com.campus.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final AuthService authService;

    @Override
    public PageResult<SysRole> page(String keyword, Long current, Long size) {
        Page<SysRole> page = new Page<>(current == null ? 1 : current, size == null ? 10 : size);
        var wrapper = Wrappers.<SysRole>lambdaQuery()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysRole::getRoleName, keyword)
                        .or().like(SysRole::getRoleCode, keyword))
                .orderByAsc(SysRole::getSort);
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public List<SysRole> listAll() {
        return roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort));
    }

    @Override
    public void add(SysRoleDTO dto) {
        Long exists = roleMapper.selectCount(
                Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (exists != null && exists > 0) {
            throw new BizException("角色标识已存在");
        }
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDataScope(dto.getDataScope() == null ? 4 : dto.getDataScope());
        role.setSort(dto.getSort() == null ? 0 : dto.getSort());
        role.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        role.setRemark(dto.getRemark());
        roleMapper.insert(role);
    }

    @Override
    public void update(SysRoleDTO dto) {
        SysRole old = roleMapper.selectById(dto.getId());
        if (old == null) {
            throw new BizException("角色不存在");
        }
        SysRole role = new SysRole();
        role.setId(dto.getId());
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDataScope(dto.getDataScope());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        role.setRemark(dto.getRemark());
        roleMapper.updateById(role);
        refreshAllUsers();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            return;
        }
        if ("ROLE_SUPER".equals(role.getRoleCode())) {
            throw new BizException("超级管理员角色不允许删除");
        }
        Long used = userRoleMapper.selectCount(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, id));
        if (used != null && used > 0) {
            throw new BizException("该角色下还有 " + used + " 个用户，请先解除绑定");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        if ("ROLE_SUPER".equals(role.getRoleCode())) {
            throw new BizException("超级管理员默认拥有全部权限，无需分配");
        }
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long mid : new java.util.LinkedHashSet<>(menuIds)) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(mid);
                roleMenuMapper.insert(rm);
            }
        }
        refreshAllUsers();
    }

    /** 权限变更后，踢掉所有相关用户，强制重新登录刷新权限 */
    private void refreshAllUsers() {
        // 简化处理：清空所有登录态代价过大，这里依赖 token 里的权限在登录时构建，
        // 生产环境可改为发布"权限变更"事件按 role 精确踢人。
        log.debug("角色权限变更，已登录用户将在下次登录后生效");
    }
}
