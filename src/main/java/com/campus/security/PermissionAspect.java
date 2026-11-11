package com.campus.security;

import com.campus.common.ResultCode;
import com.campus.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限校验切面
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(com.campus.security.RequiresPerm) || @within(com.campus.security.RequiresPerm)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        RequiresPerm perm = AnnotationUtils.findAnnotation(method, RequiresPerm.class);
        if (perm == null) {
            perm = AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), RequiresPerm.class);
        }
        if (perm == null) {
            return pjp.proceed();
        }

        LoginUser user = UserContext.require();
        // 超级管理员放行一切
        if (Integer.valueOf(1).equals(user.getUserType())) {
            return pjp.proceed();
        }

        for (String p : perm.value()) {
            if (user.hasPerm(p)) {
                return pjp.proceed();
            }
        }
        log.warn("[权限拒绝] user={} uri={} need={}",
                user.getUsername(), signature.getDeclaringTypeName(), String.join(",", perm.value()));
        throw new BizException(ResultCode.FORBIDDEN);
    }
}
