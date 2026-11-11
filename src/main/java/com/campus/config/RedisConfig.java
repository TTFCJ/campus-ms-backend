package com.campus.config;

import com.campus.security.LoginUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 序列化配置
 * key 用 String，value 用 JSON（保留类型信息，方便直接存 LoginUser）
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 说明：GenericJackson2JsonRedisSerializer 会把类名写入 @class 字段，
     * 所以 LoginUser 必须有默认构造与 getter/setter（Lombok @Data 已满足）。
     * 若后续升级安全要求，可改为自定义 ObjectMapper + 白名单。
     */
    @Bean
    public GenericJackson2JsonRedisSerializer loginUserSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /** 仅用于代码提示，避免 IDE 报未使用 */
    public Class<?> loginUserType() {
        return LoginUser.class;
    }
}
