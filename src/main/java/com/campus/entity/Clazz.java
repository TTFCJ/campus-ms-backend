package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clazz")
public class Clazz extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deptId;
    private Long majorId;
    private String clazzName;
    private String grade;
    private Long counselorId;
    private Integer studentCount;
    private Integer status;
}
