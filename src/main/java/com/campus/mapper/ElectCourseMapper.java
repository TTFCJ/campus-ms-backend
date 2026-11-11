package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ElectCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ElectCourseMapper extends BaseMapper<ElectCourse> {

    /** 某教学班已选人数（Redis 缓存的兜底来源） */
    @Select("""
            SELECT COUNT(*) FROM elect_course
            WHERE schedule_id = #{scheduleId} AND status = 1 AND is_deleted = 0
            """)
    long countSelected(@Param("scheduleId") Long scheduleId);
}
