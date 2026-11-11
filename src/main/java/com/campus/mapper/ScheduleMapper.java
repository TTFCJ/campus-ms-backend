package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /** 按班级查课表 */
    @Select("""
            SELECT s.* FROM schedule s
            WHERE s.semester_id = #{semesterId} AND s.clazz_id = #{clazzId}
              AND s.is_deleted = 0 AND s.status = 1
            ORDER BY s.day_of_week, s.section_start
            """)
    List<Schedule> selectByClazz(@Param("semesterId") Long semesterId,
                                 @Param("clazzId") Long clazzId);

    /** 按教师查课表 */
    @Select("""
            SELECT s.* FROM schedule s
            WHERE s.semester_id = #{semesterId} AND s.teacher_id = #{teacherId}
              AND s.is_deleted = 0 AND s.status = 1
            ORDER BY s.day_of_week, s.section_start
            """)
    List<Schedule> selectByTeacher(@Param("semesterId") Long semesterId,
                                   @Param("teacherId") Long teacherId);

    /** 按学生查课表（通过选课记录关联） */
    @Select("""
            SELECT s.* FROM schedule s
            INNER JOIN elect_course e ON e.schedule_id = s.id AND e.is_deleted = 0 AND e.status = 1
            WHERE s.semester_id = #{semesterId} AND e.student_id = #{studentId}
              AND s.is_deleted = 0 AND s.status = 1
            ORDER BY s.day_of_week, s.section_start
            """)
    List<Schedule> selectByStudent(@Param("semesterId") Long semesterId,
                                   @Param("studentId") Long studentId);

    /** 查询某教师本学期所有教学班（成绩录入页用） */
    @Select("""
            SELECT s.* FROM schedule s
            WHERE s.teacher_id = #{teacherId} AND s.is_deleted = 0 AND s.status = 1
              AND (#{semesterId} IS NULL OR s.semester_id = #{semesterId})
            ORDER BY s.semester_id DESC, s.day_of_week, s.section_start
            """)
    List<Schedule> selectByTeacherAll(@Param("teacherId") Long teacherId,
                                      @Param("semesterId") Long semesterId);
}
