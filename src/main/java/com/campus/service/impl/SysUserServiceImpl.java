package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.common.exception.BizException;
import com.campus.dto.SysUserDTO;
import com.campus.dto.SysUserQuery;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.UserContext;
import com.campus.service.AuthService;
import com.campus.service.SysUserService;
import com.campus.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final ClazzMapper clazzMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public PageResult<SysUserVO> page(SysUserQuery query) {
        Page<SysUser> page = new Page<>(query.getCurrent(), query.getSize());
        var wrapper = Wrappers.<SysUser>lambdaQuery()
                .eq(query.getUserType() != null, SysUser::getUserType, query.getUserType())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SysUser::getUsername, query.getKeyword())
                        .or().like(SysUser::getNickname, query.getKeyword())
                        .or().like(SysUser::getPhone, query.getKeyword()))
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(page, wrapper);

        List<SysUserVO> list = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), list);
    }

    @Override
    public SysUserVO detail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserDTO dto) {
        Long exists = userMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD));
        user.setNickname(dto.getNickname());
        user.setRealName(StringUtils.hasText(dto.getRealName()) ? dto.getRealName() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setUserType(dto.getUserType() == null ? 6 : dto.getUserType());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setRemark(dto.getRemark());
        userMapper.insert(user);

        saveRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserDTO dto) {
        SysUser old = userMapper.selectById(dto.getId());
        if (old == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        // 超管账号不允许被普通管理员改角色
        SysUser user = new SysUser();
        user.setId(dto.getId());
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setUserType(dto.getUserType());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        user.setRemark(dto.getRemark());
        userMapper.updateById(user);

        if (dto.getRoleIds() != null) {
            saveRoles(dto.getId(), dto.getRoleIds());
        }
        // 权限变更后强制下线，重新登录才生效
        authService.kickOut(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return;
        }
        if (Integer.valueOf(1).equals(user.getUserType())) {
            throw new BizException("超级管理员账号不允许删除");
        }
        if (Objects.equals(id, UserContext.getUserId())) {
            throw new BizException("不能删除自己");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
        authService.kickOut(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (Integer.valueOf(1).equals(user.getUserType())) {
            throw new BizException("超级管理员账号不允许禁用");
        }
        SysUser upd = new SysUser();
        upd.setId(id);
        upd.setStatus(status);
        userMapper.updateById(upd);
        if (Integer.valueOf(0).equals(status)) {
            authService.kickOut(id);
        }
    }

    @Override
    public String resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        String raw = randomPassword(8);
        SysUser upd = new SysUser();
        upd.setId(id);
        upd.setPassword(passwordEncoder.encode(raw));
        userMapper.updateById(upd);
        authService.kickOut(id);
        log.info("[重置密码] userId={} 操作人={}", id, UserContext.getUserId());
        return raw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        saveRoles(userId, roleIds);
        authService.kickOut(userId);
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }

    // ------------------------------------------------------------ 私有

    private void saveRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long rid : new LinkedHashSet<>(roleIds)) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
    }

    private SysUserVO toVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setUserType(user.getUserType());
        vo.setDeptId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setRemark(user.getRemark());
        vo.setCreateBy(user.getCreateBy());
        vo.setCreateTime(user.getCreateTime());
        vo.setUserTypeName(userTypeName(user.getUserType()));

        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            vo.setDeptName(dept == null ? null : dept.getDeptName());
        }
        List<Long> roleIds = getRoleIds(user.getId());
        vo.setRoleIds(roleIds);
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        } else {
            vo.setRoleNames(Collections.emptyList());
        }

        Student stu = studentMapper.selectByUserId(user.getId());
        if (stu != null) {
            vo.setStuNo(stu.getStuNo());
            if (stu.getClazzId() != null) {
                Clazz clazz = clazzMapper.selectById(stu.getClazzId());
                vo.setClazzName(clazz == null ? null : clazz.getClazzName());
            }
        }
        Teacher tea = teacherMapper.selectByUserId(user.getId());
        if (tea != null) {
            vo.setTeaNo(tea.getTeaNo());
        }
        return vo;
    }

    private String userTypeName(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case 1 -> "超级管理员";
            case 2 -> "校级管理员";
            case 3 -> "教务管理员";
            case 4 -> "教师";
            case 5 -> "辅导员";
            case 6 -> "学生";
            case 7 -> "后勤管理员";
            case 8 -> "维修人员";
            default -> "未知";
        };
    }

    private String randomPassword(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
