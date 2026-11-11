package com.campus.service;

import com.campus.common.PageResult;
import com.campus.entity.DormCheckin;
import com.campus.vo.DormCheckinVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DormService {

    PageResult<DormCheckinVO> page(LocalDate date, Integer status, Long clazzId,
                                   String keyword, Long current, Long size);

    /** 登记/覆盖某学生某天的归寝状态 */
    void save(DormCheckin body);

    void remove(Long id);

    /** 今日/指定日 各状态人数 */
    Map<String, Object> stat(LocalDate date);

    /** 近 N 天异常 */
    List<DormCheckinVO> alerts(int days);

    /** 学生查自己的归寝记录 */
    List<DormCheckinVO> myCheckins(int days);
}
