package com.campus.security;

import java.lang.annotation.*;

/**
 * 菜单权限校验：等价于 @PreAuthorize("hasPermission('xxx')")
 * <pre>
 * &#64;RequiresPerm("edu:course:add")
 * public Result&lt;Void&gt; add(...) { ... }
 * </pre>
 * 满足任一权限即可通过；超管直接放行。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPerm {

    /** 权限标识，多个用数组，满足其一即可 */
    String[] value();
}
