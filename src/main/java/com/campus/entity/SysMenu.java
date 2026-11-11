package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;
    private String menuName;

    /** M目录 C菜单 F按钮 */
    private String menuType;

    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer sort;

    /** 0隐藏 1显示 */
    private Integer visible;
    private Integer keepAlive;
    private Integer status;

    /** 非数据库字段：树形结构子节点 */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
