package com.campus.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 统一分页返回体
 * { "total":100, "current":1, "size":10, "records":[] }
 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private Long current;
    private Long size;
    private List<T> records;

    public PageResult() {
        this.records = new ArrayList<>();
    }

    public PageResult(Long total, Long current, Long size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 实体分页 -> VO 分页 */
    public static <E, V> PageResult<V> of(IPage<E> page, Function<E, V> mapper) {
        List<V> list = page.getRecords().stream().map(mapper).toList();
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    public static <T> PageResult<T> empty(Long current, Long size) {
        return new PageResult<>(0L, current, size, new ArrayList<>());
    }
}
