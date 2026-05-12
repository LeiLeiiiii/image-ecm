package com.sunyard.module.auth.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @Type RedisCacheManager.java
 * @Desc
 * @author zhouleibin-bwf
 * @date 2018年9月28日 下午2:16:20
 * @version
 */
public class RedisCacheManager implements CacheManager {

    private RedisTemplate<String, Session> redisTemplate;
    private String keyPrefix = "";
    private Long expire;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return new ShiroCache<K, V>(this.keyPrefix + name, redisTemplate, expire);
    }

    public void setRedisTemplate(RedisTemplate<String, Session> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 *
 * Date Author Note ------------------------------------------------------------------------- 2018年9月28日 zhouleibin-bwf
 * creat
 */
