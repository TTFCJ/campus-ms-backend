package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("major")
public class Major extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deptId;
    private String majorName;
    private String majorCode;
    private Integer lengthSchool;
    private String degree;
    private Integer status;
}
