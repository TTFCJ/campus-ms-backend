package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.SysRoleDTO;
import com.campus.entity.SysRole;

import java.util.List;

public interface SysRoleService {

    PageResult<SysRole> page(String keyword, Long current, Long size);

    List<SysRole> listAll();

    void add(SysRoleDTO dto);

    void update(SysRoleDTO dto);

    void remove(Long id);

    List<Long> getMenuIds(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);
}
