package com.sunyard.module.system.config;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sunyard.module.system.constant.CachePrefixConstants;
import org.apache.ibatis.cache.Cache;

import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;

public class SysDictStyleMybatisCache implements Cache {
    // 缓存唯一标识（Mapper全类名）
    private final String id;
    // 读写锁（保证线程安全）
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private static RedisUtils redisUtils;

    // 关键：提供静态方法，让配置类注入 RedisUtils
    public static void setRedisUtils(RedisUtils redisUtils) {
        SysDictStyleMybatisCache.redisUtils = redisUtils;
    }

    // 构造器（MyBatis自动传入Mapper全类名）
    public SysDictStyleMybatisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("缓存ID不能为空");
        }
        this.id = id;
    }


    /**
     * 生成Hash Field
     */
    private String getCacheField(Object key) {
        //根据完整查询条件参数等信息生成hash值作为redis的key
        String originalKey = key.toString();
        return String.valueOf(originalKey.hashCode());
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 存入缓存
     */
    @Override
    public void putObject(Object key, Object value) {
        String hashKey = CachePrefixConstants.SYS_DICTIONARY;
        String field = getCacheField(key);
        redisUtils.hset(hashKey, field, value, TimeOutConstants.SEVEN_DAY);

    }

    /**
     * 从缓存获取数据
     */
    @Override
    public Object getObject(Object key) {
        String hashKey = CachePrefixConstants.SYS_DICTIONARY;
        String field = getCacheField(key);

        return redisUtils.hget(hashKey, field);
    }

    /**
     * 移除单个缓存项
     */
    @Override
    public Object removeObject(Object key) {

        String hashKey = CachePrefixConstants.SYS_DICTIONARY;
        String field = getCacheField(key);
        //删除缓存
        redisUtils.hdel(hashKey, field);
        return null;
    }

    /**
     * 清空缓存（执行增删改时触发）
     */
    @Override
    public void clear() {
        // 清空整个Hash
        redisUtils.del(CachePrefixConstants.SYS_DICTIONARY);
    }

    /**
     * 获取缓存大小
     */
    @Override
    public int getSize() {
        Long size = redisUtils.getHashSize(CachePrefixConstants.SYS_DICTIONARY);
        return size != null ? size.intValue() : 0;

    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
}
