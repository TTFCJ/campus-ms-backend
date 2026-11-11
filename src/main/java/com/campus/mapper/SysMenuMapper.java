package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 当前用户的权限标识集合 */
    @Select("""
            SELECT DISTINCT m.perms FROM sys_menu m
            INNER JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.is_deleted = 0
            INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id AND ur.is_deleted = 0
            WHERE ur.user_id = #{userId}
              AND m.is_deleted = 0 AND m.status = 1
              AND m.perms IS NOT NULL AND m.perms <> ''
            """)
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /** 当前用户可见的目录/菜单（不含按钮） */
    @Select("""
            SELECT DISTINCT m.* FROM sys_menu m
            INNER JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.is_deleted = 0
            INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id AND ur.is_deleted = 0
            WHERE ur.user_id = #{userId}
              AND m.is_deleted = 0 AND m.status = 1
              AND m.menu_type IN ('M', 'C')
            ORDER BY m.parent_id, m.sort
            """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /** 超管：全部菜单 */
    @Select("""
            SELECT * FROM sys_menu
            WHERE is_deleted = 0 AND status = 1 AND menu_type IN ('M', 'C')
            ORDER BY parent_id, sort
            """)
    List<SysMenu> selectAllMenus();

    /** 角色已授权的菜单ID */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId} AND is_deleted = 0")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /** 全部权限标识（超管用） */
    @Select("""
            SELECT DISTINCT perms FROM sys_menu
            WHERE is_deleted = 0 AND status = 1 AND perms IS NOT NULL AND perms <> ''
            """)
    List<String> selectAllPerms();
}
