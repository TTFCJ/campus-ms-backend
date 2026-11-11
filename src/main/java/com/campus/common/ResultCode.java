package com.campus.common;

import lombok.Getter;

/**
 * 状态码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "请求的资源不存在"),
    ERROR(500, "系统异常，请稍后重试"),

    // 业务码 1xxx
    BUSINESS_ERROR(1000, "业务处理失败"),
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "账号或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USER_LOCKED(1004, "账号已锁定，请稍后再试"),
    CAPTCHA_ERROR(1005, "验证码错误或已失效"),
    OLD_PASSWORD_ERROR(1006, "原密码不正确"),
    USERNAME_EXISTS(1007, "账号已存在"),

    // 教务 2xxx
    COURSE_ELECT_FULL(2001, "选课人数已满"),
    COURSE_ELECT_DUPLICATE(2002, "已经选过这门课"),
    COURSE_ELECT_TIME_INVALID(2003, "不在选课时间范围内"),
    SCHEDULE_CONFLICT(2004, "上课时间冲突"),
    SCORE_LOCKED(2005, "成绩已锁定，无法修改"),

    // 审批 3xxx
    APPROVE_STATUS_ERROR(3001, "当前状态不允许此操作"),
    APPROVE_NOT_YOUR_TASK(3002, "不是您的待办任务"),
    APPLY_NOT_FOUND(3003, "申请单不存在"),

    // 文件 4xxx
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4002, "不支持的文件类型"),
    FILE_TOO_LARGE(4003, "文件超出大小限制");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
