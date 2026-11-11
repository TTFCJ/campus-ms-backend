package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Clazz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClazzMapper extends BaseMapper<Clazz> {

    /** 辅导员所带班级ID */
    @Select("SELECT id FROM clazz WHERE counselor_id = #{userId} AND is_deleted = 0 AND status = 1")
    List<Long> selectIdsByCounselorId(@Param("userId") Long userId);
}
