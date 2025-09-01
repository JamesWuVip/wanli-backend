package com.wanli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类.
 * 配置RedisTemplate用于Redis操作.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate.
     *
     * @param connectionFactory Redis连接工厂.
     * @return RedisTemplate实例.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            final RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置key序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置value序列化方式
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(
                new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}