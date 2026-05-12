package com.sunyard.framework.onlyoffice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import com.sunyard.framework.onlyoffice.constant.StateConstants;
import com.sunyard.framework.onlyoffice.core.Cache;
import com.sunyard.framework.onlyoffice.core.cache.InsideCache;
import com.sunyard.framework.onlyoffice.core.cache.RedisCache;


/**
 * @author 朱山成
 */
@Configuration
public class FwCacheConfig {
    @Value("${onlyoffice.cache:}")
    private String cache;


    @Bean
    public Cache getCache(RedisTemplate<String, Object> redisTemplate) {
        if ( StringUtils.hasText(cache) && StateConstants.CACHE.equals(cache)) {
            return new RedisCache(redisTemplate);
        }
        return new InsideCache();
    }

}
