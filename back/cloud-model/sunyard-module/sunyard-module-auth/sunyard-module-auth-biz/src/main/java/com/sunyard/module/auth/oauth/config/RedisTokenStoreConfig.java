
package com.sunyard.module.auth.oauth.config;

/*
 * Project: Sunyard
 *
 * File Created at 2024/6/26
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import javax.annotation.Resource;

import com.sunyard.module.auth.constant.CachePrefixConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * @author Leo
 * @Desc
 * @date 2024/6/26 14:47
 */

@Configuration
public class RedisTokenStoreConfig {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /***/
    @Bean
    public RedisTokenStore redisTokenStore() {
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        redisTokenStore.setPrefix(CachePrefixConstants.AUTH +"OAUTH-TOKEN:"); // 设置KEY的层级前缀，方便查询
        return redisTokenStore;

    }

}

/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/6/26 Leo creat
 */
