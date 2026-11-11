package com.campus.security;

import java.lang.annotation.*;

/**
 * 数据权限开关：标注在 Service 方法上，该方法内的查询会自动拼接数据权限条件。
 * 注意：只对 Mapper 查询生效，且以当前登录用户的数据范围为基准。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** 关联用户字段名，默认按表名自动推断 */
    String userAlias() default "";

    /** 关联部门字段名，默认 dept_id */
    String deptAlias() default "dept_id";

    /** 忽略的表名（逗号分隔），这些表不加权限条件 */
    String ignoreTables() default "";
}
