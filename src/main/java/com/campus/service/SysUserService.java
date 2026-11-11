package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.SysUserDTO;
import com.campus.dto.SysUserQuery;
import com.campus.vo.SysUserVO;

import java.util.List;

public interface SysUserService {

    PageResult<SysUserVO> page(SysUserQuery query);

    SysUserVO detail(Long id);

    void add(SysUserDTO dto);

    void update(SysUserDTO dto);

    void remove(Long id);

    void changeStatus(Long id, Integer status);

    /** 重置为随机密码，返回明文 */
    String resetPassword(Long id);

    void assignRoles(Long userId, List<Long> roleIds);

    List<Long> getRoleIds(Long userId);
}
