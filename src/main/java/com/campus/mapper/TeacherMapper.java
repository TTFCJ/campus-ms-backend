package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    @Select("SELECT * FROM teacher WHERE user_id = #{userId} AND is_deleted = 0 LIMIT 1")
    Teacher selectByUserId(@Param("userId") Long userId);
}
