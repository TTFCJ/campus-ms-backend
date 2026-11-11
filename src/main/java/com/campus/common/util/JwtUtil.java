package com.campus.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具
 * <p>
 * 注意：JWT 只负责"身份不可篡改"，真正的登录态以 Redis 中是否存在为准，
 * 这样才能实现强制下线 / 单点登录。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${campus.jwt.secret}")
    private String secret;

    @Value("${campus.jwt.expire-minutes:720}")
    private long expireMinutes;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("campus.jwt.secret 长度必须 >= 32 字节（HS256 要求）");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    public long getExpireSeconds() {
        return expireMinutes * 60;
    }

    /** 签发 token */
    public String createToken(Long userId, String username) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMinutes * 60_000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expire)
                .signWith(key)
                .compact();
    }

    /** 解析并校验签名/过期，失败抛异常 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验是否有效（不抛异常） */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }
}
