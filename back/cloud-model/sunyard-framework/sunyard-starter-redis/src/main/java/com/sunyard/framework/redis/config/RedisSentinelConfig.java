// ============================================================================
// 该文件已废弃，内容已合并到 RedisAutoConfiguration.java
// 请勿删除此文件，仅作备份参考
// ============================================================================
/*
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

/*
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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

import java.util.Arrays;

@Configuration
public class RedisSentinelConfig {

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
                .poolConfig(poolConfig)
                .build();

        if (StringUtils.hasText(sentinelMaster) && StringUtils.hasText(sentinelNodes)) {
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
            sentinelConfig.setMaster(sentinelMaster);

            Arrays.stream(sentinelNodes.split(","))
                .map(String::trim)
                .forEach(node -> {
                    String[] parts = node.split(":");
                    sentinelConfig.addSentinel(new org.springframework.data.redis.connection.RedisNode(
                        parts[0],
                        Integer.parseInt(parts[1])
                    ));
                });

            if (StringUtils.hasText(password)) {
                sentinelConfig.setPassword(RedisPassword.of(password));
            }

            return new LettuceConnectionFactory(sentinelConfig, clientConfig);
        } else if (StringUtils.hasText(host)) {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
            standaloneConfig.setHostName(host);
            standaloneConfig.setPort(port);
            if (StringUtils.hasText(password)) {
                standaloneConfig.setPassword(RedisPassword.of(password));
            }

            return new LettuceConnectionFactory(standaloneConfig, clientConfig);
        } else {
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
*/
// ============================================================================
// 废弃结束
// ============================================================================
