package com.sunyard.framework.onlyoffice.core;

/**
 * @author 朱山成
 */
public interface Cache {


    /**
     * 获取指定缓存对象
     * @param key key
     * @return Result obj
     */
    Object get(String key);

    /**
     * 添加缓存
     * @param key 唯一key
     * @param value 缓存值
     * @param time 过期时间
     */
    void set(String key,Object value,long time);

    /**
     * 添加缓存
     * @param key 唯一key
     * @param value 缓存值
     */
    void set(String key,Object value);

    /**
     * 判断key是否存在
     * @param key 唯一key
     * @return Result 是否存在
     */
    boolean hasKey(String key);

    /**
     * 删除缓存
     * @param key 唯一key
     */
    void remove(String key);
}
