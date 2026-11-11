package com.campus.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解，标注在 Controller 方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 所属模块 */
    String module() default "";

    /** 操作类型：INSERT/UPDATE/DELETE/EXPORT/APPROVE/OTHER */
    String type() default "OTHER";

    /** 是否保存请求参数 */
    boolean saveParam() default true;

    /** 是否保存返回结果 */
    boolean saveResult() default false;
}
