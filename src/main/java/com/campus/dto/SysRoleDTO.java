package com.campus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysRoleDTO {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    private String roleCode;

    /** 1全部 2本院系 3本班 4仅本人 5本部门 */
    @Min(value = 1, message = "数据范围取值 1-5")
    @Max(value = 5, message = "数据范围取值 1-5")
    private Integer dataScope;

    private Integer sort;
    private Integer status;
    private String remark;
}
