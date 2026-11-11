package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.DormCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DormCheckinMapper extends BaseMapper<DormCheckin> {

    /** 某日各状态人数统计 */
    @Select("""
            SELECT status, COUNT(*) AS cnt FROM dorm_checkin
            WHERE check_date = #{date} AND is_deleted = 0
            GROUP BY status
            """)
    List<java.util.Map<String, Object>> countByStatus(@Param("date") LocalDate date);

    /** 近 N 天异常（晚归 + 未归） */
    @Select("""
            SELECT * FROM dorm_checkin
            WHERE is_deleted = 0 AND status IN (2, 3)
              AND check_date >= #{from}
            ORDER BY check_date DESC, status
            """)
    List<DormCheckin> selectAlerts(@Param("from") LocalDate from);
}
