package com.campus.common.aspect;

import cn.hutool.json.JSONUtil;
import com.campus.common.annotation.OperLog;
import com.campus.common.util.IpUtil;
import com.campus.entity.SysOperLog;
import com.campus.mapper.SysOperLogMapper;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志切面：记录操作人 / IP / 参数 / 耗时 / 结果
 * <p>
 * 注意：日志写入失败不能影响主业务，全部 catch 掉。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper operLogMapper;

    /** 敏感字段，落库前脱敏 */
    private static final String[] SENSITIVE = {"password", "oldPassword", "newPassword"};

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint pjp, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                saveLog(pjp, operLog, result, error, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("[操作日志] 写入失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint pjp, OperLog ann, Object result,
                         Throwable error, long cost) {
        SysOperLog entity = new SysOperLog();
        LoginUser user = UserContext.get();
        entity.setUsername(user == null ? "anonymous" : user.getUsername());
        entity.setOperModule(ann.module());
        entity.setOperType(ann.type());

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        entity.setMethod(sig.getDeclaringTypeName() + "." + sig.getName());
        entity.setOperTime(LocalDateTime.now());
        entity.setCostTime(cost);
        entity.setStatus(error == null ? 1 : 0);
        if (error != null) {
            String msg = error.getMessage();
            entity.setErrorMsg(msg == null ? error.getClass().getSimpleName() : msg.substring(0, Math.min(msg.length(), 1000)));
        }

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            entity.setRequestUri(req.getRequestURI());
            entity.setRequestMethod(req.getMethod());
            entity.setIp(IpUtil.getIp(req));
        }

        if (ann.saveParam()) {
            entity.setRequestParam(truncate(toJson(pjp.getArgs()), 2000));
        }
        if (ann.saveResult() && result != null) {
            entity.setResult(truncate(toJson(result), 2000));
        }
        operLogMapper.insert(entity);
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            List<Object> list = new ArrayList<>();
            if (obj instanceof Object[] arr) {
                for (Object o : arr) {
                    if (o instanceof MultipartFile || o instanceof HttpServletRequest) {
                        continue;
                    }
                    list.add(o);
                }
            } else {
                list.add(obj);
            }
            String json = JSONUtil.toJsonStr(list);
            // 脱敏
            for (String s : SENSITIVE) {
                json = json.replaceAll("(\"" + s + "\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
            }
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
