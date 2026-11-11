package com.campus.common;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageQuery {

    /** 当前页，从 1 开始 */
    private Long current = 1L;
    /** 每页条数，最大 200 */
    private Long size = 10L;
    /** 模糊搜索关键字 */
    private String keyword;
    /** 开始时间 yyyy-MM-dd HH:mm:ss */
    private String startTime;
    /** 结束时间 yyyy-MM-dd HH:mm:ss */
    private String endTime;

    public Long getCurrent() {
        return current == null || current < 1 ? 1L : current;
    }

    public Long getSize() {
        if (size == null || size < 1) {
            return 10L;
        }
        return Math.min(size, 200L);
    }
}
