package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
            SELECT r.* FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id AND ur.is_deleted = 0
            WHERE ur.user_id = #{userId}
              AND r.is_deleted = 0 AND r.status = 1
            """)
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
