package com.campus.security;

import com.campus.common.constant.RedisKeys;
import com.campus.common.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器
 * 1. 解析 Authorization: Bearer xxx
 * 2. 校验 JWT 签名
 * 3. 校验 Redis 中 token 是否存在（支持强制下线 / 单点登录）
 * 4. 写入 ThreadLocal，并做滑动续期
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${campus.jwt.header:Authorization}")
    private String header;

    @Value("${campus.jwt.prefix:Bearer }")
    private String prefix;

    @Value("${campus.jwt.expire-minutes:720}")
    private long expireMinutes;

    public AuthInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        LoginUser loginUser = redisUtil.get(RedisKeys.LOGIN_TOKEN + token);
        if (loginUser == null) {
            writeUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        UserContext.set(loginUser);

        // 滑动续期：剩余时间不足一半时，延长到完整有效期
        Long ttl = redisUtil.getExpire(RedisKeys.LOGIN_TOKEN + token);
        long half = expireMinutes * 60 / 2;
        if (ttl != null && ttl > 0 && ttl < half) {
            redisUtil.expire(RedisKeys.LOGIN_TOKEN + token, expireMinutes * 60);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String value = request.getHeader(header);
        if (StringUtils.hasText(value)) {
            if (StringUtils.hasText(prefix) && value.startsWith(prefix.trim())) {
                return value.substring(prefix.trim().length()).trim();
            }
            return value.trim();
        }
        // 兼容文件下载等无法带 header 的场景
        String param = request.getParameter("token");
        return StringUtils.hasText(param) ? param : null;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                java.util.Map.of("code", 401, "msg", msg, "timestamp", System.currentTimeMillis())));
    }
}
