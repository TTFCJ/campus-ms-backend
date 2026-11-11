package com.campus.security;

import com.campus.common.ResultCode;
import com.campus.common.exception.BizException;

/**
 * 当前登录用户 ThreadLocal 容器
 * 写完必须在 finally 里 clear，否则线程池复用会串号
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();
    /** 数据权限开关：只有标注 @DataScope 的 Service 方法才会置 true */
    private static final ThreadLocal<Boolean> SCOPE_ON = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return u;
    }

    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    public static boolean isSuperAdmin() {
        LoginUser u = HOLDER.get();
        return u != null && Integer.valueOf(1).equals(u.getUserType());
    }

    public static void setScope(boolean on) {
        SCOPE_ON.set(on);
    }

    public static boolean scopeEnabled() {
        return Boolean.TRUE.equals(SCOPE_ON.get());
    }

    public static void clear() {
        HOLDER.remove();
        SCOPE_ON.remove();
    }
}
