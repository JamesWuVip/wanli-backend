package com.wanli.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类 只有在Redis可用时才创建相关Bean
 *
 * @author wanli-team
 * @version 1.0.0
 */
@Configuration
@ConditionalOnProperty(
    name = "spring.data.redis.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class RedisConfig {

  /**
   * 配置RedisTemplate
   *
   * @param connectionFactory Redis连接工厂
   * @return RedisTemplate
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // 设置key序列化方式
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // 设置value序列化方式
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    template.afterPropertiesSet();
    return template;
  }
}
