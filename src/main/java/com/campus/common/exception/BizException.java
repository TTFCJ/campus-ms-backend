package com.campus.common.exception;

import com.campus.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;
    private final Object data;

    public BizException(String msg) {
        super(msg);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
        this.data = null;
    }

    public BizException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.data = null;
    }

    public BizException(ResultCode rc) {
        super(rc.getMsg());
        this.code = rc.getCode();
        this.data = null;
    }

    public BizException(ResultCode rc, String msg) {
        super(msg);
        this.code = rc.getCode();
        this.data = null;
    }

    /** 断言为真，否则抛业务异常 */
    public static void isTrue(boolean condition, String msg) {
        if (!condition) {
            throw new BizException(msg);
        }
    }

    public static void isTrue(boolean condition, ResultCode rc) {
        if (!condition) {
            throw new BizException(rc);
        }
    }

    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new BizException(msg);
        }
    }
}
