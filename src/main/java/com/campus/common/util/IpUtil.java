package com.campus.common.util;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求相关工具
 */
public final class IpUtil {

    private IpUtil() {
    }

    /**
     * 获取真实 IP（兼容 Nginx 反向代理）
     * 注意：X-Forwarded-For 可被伪造，仅用于日志展示，不要用于鉴权
     */
    public static String getIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                int idx = ip.indexOf(',');
                return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
            }
        }
        String ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    public static String getBrowser(HttpServletRequest request) {
        UserAgent ua = UserAgentUtil.parse(request.getHeader("User-Agent"));
        return ua == null || ua.getBrowser() == null ? "unknown" : ua.getBrowser().getName();
    }

    public static String getOs(HttpServletRequest request) {
        UserAgent ua = UserAgentUtil.parse(request.getHeader("User-Agent"));
        return ua == null || ua.getOs() == null ? "unknown" : ua.getOs().getName();
    }
}
