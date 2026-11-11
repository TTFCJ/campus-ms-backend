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
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;
    private String ancestors;
    private String deptName;
    private String deptCode;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;

    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
