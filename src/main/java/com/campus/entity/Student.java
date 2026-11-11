package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String stuNo;
    private Long deptId;
    private Long majorId;
    private Long clazzId;
    /** 1男 2女 */
    private Integer gender;
    private LocalDate birthDate;
    private String idCard;
    private LocalDate enrollmentDate;
    private String politicalStatus;
    private String nationality;
    private String homeAddress;
    private String emergencyContact;
    private String emergencyPhone;
    private String photoUrl;
    /** 1在读 2休学 3退学 4毕业 */
    private Integer isInSchool;
    private String remark;
}
