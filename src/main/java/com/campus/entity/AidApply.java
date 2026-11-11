package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 助学金申请
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("aid_apply")
public class AidApply extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;
    private Long studentId;
    private Long clazzId;

    /** 1国家助学金 2校内助学金 3临时困难补助 4助学贷款 */
    private Integer aidType;

    /** 1特别困难 2困难 3一般困难 */
    private Integer aidLevel;

    private BigDecimal amount;
    private String reason;
    private String fileUrl;

    /** 0草稿 1辅导员初审 2院系复审 3校级终审 4通过 5驳回 */
    private Integer status;

    private Long currentApprover;
    private String approveOpinion;

    /** 0未公示 1公示中 2已公示 */
    private Integer publicStatus;

    private LocalDateTime approveTime;
}
