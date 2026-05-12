package com.sunyard.framework.redis.config;
/*
 * Project: com.sunyard.am.config
 *
 * File Created at 2021/7/1
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;

import com.sunyard.framework.redis.util.RedisUtils;

import io.lettuce.core.ClientOptions;
import java.util.Arrays;

/**
 * Redis 配置类
 * 支持单机模式和哨兵模式自动切换
 * @author zhouleibin
 * @date 2021/7/1 14:05
 */
@Configuration
public class RedisAutoConfiguration {

    @Value("${spring.redis.host:}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle:2}")
    private int minIdle;

    // 哨兵模式配置
    @Value("${spring.redis.sentinel.master:}")
    private String sentinelMaster;

    @Value("${spring.redis.sentinel.nodes:}")
    private String sentinelNodes;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);

        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .clientOptions(ClientOptions.builder().build())
                .poolConfig(poolConfig)
                .build();

        // 检查是否配置了哨兵模式
        if (StringUtils.hasText(sentinelMaster) && StringUtils.hasText(sentinelNodes)) {
            // 哨兵模式
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
            sentinelConfig.setMaster(sentinelMaster);

            // 解析哨兵节点列表
            Arrays.stream(sentinelNodes.split(","))
                .map(String::trim)
                .forEach(node -> {
                    String[] parts = node.split(":");
                    sentinelConfig.addSentinel(new org.springframework.data.redis.connection.RedisNode(
                        parts[0],
                        Integer.parseInt(parts[1])
                    ));
                });

            // 设置密码
            if (StringUtils.hasText(password)) {
                sentinelConfig.setPassword(RedisPassword.of(password));
            }

            return new LettuceConnectionFactory(sentinelConfig, clientConfig);
        } else if (StringUtils.hasText(host)) {
            // 单机模式
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
            standaloneConfig.setHostName(host);
            standaloneConfig.setPort(port);
            if (StringUtils.hasText(password)) {
                standaloneConfig.setPassword(RedisPassword.of(password));
            }

            return new LettuceConnectionFactory(standaloneConfig, clientConfig);
        } else {
            // 默认配置
            RedisStandaloneConfiguration defaultConfig = new RedisStandaloneConfiguration("localhost", 6379);
            return new LettuceConnectionFactory(defaultConfig, clientConfig);
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }

    @Bean
    public RedisUtils redisUtils(){
        return new RedisUtils();
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */