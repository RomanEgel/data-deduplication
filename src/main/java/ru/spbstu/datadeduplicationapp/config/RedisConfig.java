package ru.spbstu.datadeduplicationapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<byte[], Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setHashValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        template.setEnableTransactionSupport(true);
        return template;
    }
}
