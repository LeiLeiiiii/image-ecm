package com.sunyard.mytool.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 自定义springSessionDefaultRedisSerializer对象，将会替代默认的SESSION序列化对象。
     * 默认是JdkSerializationRedisSerializer，缺点是需要类实现Serializable接口。
     * 并且在反序列化时如果异常会抛出SerializationException异常，
     * 而SessionRepositoryFilter又没有处理异常，故如果序列化异常时就会导致请求异常
     */
    @Bean(name = "springSessionDefaultRedisSerializer")
    public GenericJackson2JsonRedisSerializer getGenericJackson2JsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /**
     * JacksonJsonRedisSerializer和GenericJackson2JsonRedisSerializer的区别：
     * GenericJackson2JsonRedisSerializer在json中加入@class属性，类的全路径包名，方便反系列化。
     * JacksonJsonRedisSerializer如果存放了List则在反系列化的时候，
     * 如果没指定TypeReference则会报错java.util.LinkedHashMap cannot be cast。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer
                = new GenericJackson2JsonRedisSerializer();

        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        redisTemplate.setHashKeySerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setEnableDefaultSerializer(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
