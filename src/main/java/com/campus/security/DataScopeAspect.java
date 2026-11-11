package com.campus.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 数据权限切面：进入方法时打开开关，退出时关闭。
 */
@Slf4j
@Aspect
@Component
public class DataScopeAspect {

    @Pointcut("@annotation(com.campus.security.DataScope)")
    public void scopePointcut() {
    }

    @Around("scopePointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 超管不限制，直接放行，省掉 SQL 拼装开销
        if (UserContext.isSuperAdmin()) {
            return pjp.proceed();
        }
        try {
            UserContext.setScope(true);
            return pjp.proceed();
        } finally {
            UserContext.setScope(false);
        }
    }
}
