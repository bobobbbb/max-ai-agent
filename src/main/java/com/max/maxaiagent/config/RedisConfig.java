package com.max.maxaiagent.config;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.max.maxaiagent.dto.ChatContentDTO;
import com.max.maxaiagent.serializer.ChatContentDTORedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author xtwang
 */
@Configuration
public class RedisConfig {
 
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * 配置通用的Redis模板，用于处理各种数据类型
     */
    @Bean
    public RedisTemplate<String, Object> streamRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用String序列化器作为key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 使用通用的Jackson序列化器作为value的序列化方式
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));

        // 设置hash类型的key和value序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置ChatContentDTO的Redis模板
     */
    @Bean
    public RedisTemplate<String, ChatContentDTO> jsonRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, ChatContentDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用String序列化器作为key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 使用自定义的ChatContentDTO序列化器作为value的序列化方式
        template.setValueSerializer(new ChatContentDTORedisSerializer(objectMapper()));

        // 设置hash类型的key和value序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new ChatContentDTORedisSerializer(objectMapper()));

        template.afterPropertiesSet();
        return template;
    }
}