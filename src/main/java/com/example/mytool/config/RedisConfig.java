package com.example.mytool.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis缓存配置类。
 * 技术亮点：
 * 1. 集成Redis作为缓存中间件，极大提升系统性能和响应速度。
 * 2. 采用高效的序列化机制（StringRedisSerializer和Jackson2JsonRedisSerializer），保证数据读写高效且兼容性强。
 * 3. 通过@EnableCaching注解，灵活支持Spring全局缓存注解。
 */
@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash的value也采用GenericJackson2JsonRedisSerializer的序列化方式
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}