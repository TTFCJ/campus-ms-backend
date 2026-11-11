package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.EcEnroll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EcEnrollMapper extends BaseMapper<EcEnroll> {

    /** 某学生累计获得的第二课堂学分（仅已签到的） */
    @Select("""
            SELECT COALESCE(SUM(a.credit), 0)
            FROM ec_enroll e
            INNER JOIN ec_activity a ON a.id = e.activity_id AND a.is_deleted = 0
            WHERE e.student_id = #{studentId} AND e.status = 2 AND e.is_deleted = 0
            """)
    java.math.BigDecimal sumCredit(@Param("studentId") Long studentId);
}
