package com.campus.common.constant;

/**
 * Redis Key 统一管理
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 登录 token 缓存：login:token:{token} -> LoginUser */
    public static final String LOGIN_TOKEN = "login:token:";

    /** 用户维度 token 集合，用于踢人：login:user:{userId} -> Set<token> */
    public static final String USER_TOKENS = "login:user:";

    /** 图形验证码：captcha:{uuid} */
    public static final String CAPTCHA = "captcha:";

    /** 登录失败次数：login:retry:{username} */
    public static final String LOGIN_RETRY = "login:retry:";

    /** 选课分布式锁：lock:elect:{scheduleId} */
    public static final String ELECT_LOCK = "lock:elect:";

    /** 选课已选人数缓存：elect:count:{scheduleId} */
    public static final String ELECT_COUNT = "elect:count:";

    /** 字典缓存：dict:{type} */
    public static final String DICT = "dict:";

    /** 接口幂等：idem:{key} */
    public static final String IDEMPOTENT = "idem:";
}
