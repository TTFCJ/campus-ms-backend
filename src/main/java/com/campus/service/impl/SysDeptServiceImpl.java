package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.exception.BizException;
import com.campus.common.util.TreeUtils;
import com.campus.entity.SysDept;
import com.campus.entity.SysUser;
import com.campus.mapper.SysDeptMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<SysDept> tree() {
        return TreeUtils.buildDeptTree(list());
    }

    @Override
    public List<SysDept> list() {
        return deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getSort));
    }

    @Override
    public void add(SysDept dept) {
        checkCodeUnique(dept);
        fillAncestors(dept);
        deptMapper.insert(dept);
    }

    @Override
    public void update(SysDept dept) {
        if (dept.getId() == null) {
            throw new BizException("院系ID不能为空");
        }
        if (dept.getId().equals(dept.getParentId())) {
            throw new BizException("上级不能是自己");
        }
        checkCodeUnique(dept);
        fillAncestors(dept);
        deptMapper.updateById(dept);
    }

    @Override
    public void remove(Long id) {
        Long children = deptMapper.selectCount(
                Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException("存在下级院系/部门，请先删除下级");
        }
        Long users = userMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, id));
        if (users != null && users > 0) {
            throw new BizException("该院系下还有 " + users + " 个用户，无法删除");
        }
        deptMapper.deleteById(id);
    }

    private void fillAncestors(SysDept dept) {
        if (dept.getParentId() == null || dept.getParentId() == 0) {
            dept.setParentId(0L);
            dept.setAncestors("0");
            return;
        }
        SysDept parent = deptMapper.selectById(dept.getParentId());
        if (parent == null) {
            throw new BizException("上级院系不存在");
        }
        dept.setAncestors(parent.getAncestors() + "," + parent.getId());
    }

    private void checkCodeUnique(SysDept dept) {
        var q = Wrappers.<SysDept>lambdaQuery().eq(SysDept::getDeptCode, dept.getDeptCode());
        if (dept.getId() != null) {
            q.ne(SysDept::getId, dept.getId());
        }
        Long c = deptMapper.selectCount(q);
        if (c != null && c > 0) {
            throw new BizException("院系编码已存在");
        }
    }

    /** 导出用：id -> name */
    public java.util.Map<Long, String> nameMap() {
        return list().stream().collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
    }
}
