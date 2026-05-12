package com.sunyard.framework.onlyoffice.core.cache;


import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;

import com.sunyard.framework.onlyoffice.core.Cache;

import lombok.extern.slf4j.Slf4j;


/**
 * @author 朱山成
 */
@Slf4j
public class RedisCache implements Cache {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public RedisCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }
}
