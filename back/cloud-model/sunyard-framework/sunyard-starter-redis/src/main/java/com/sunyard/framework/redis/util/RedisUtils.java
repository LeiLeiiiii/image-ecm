package com.sunyard.framework.redis.util;
/*
 * Project: com.sunyard.am.utils
 *
 * File Created at 2021/7/1
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 13:52
 */
public final class RedisUtils {
    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // =============================common============================

    public Set<String>  getAllKeysByHeader(String keyheader) {
        String pattern = keyheader+"*";
        Set<String> keys = redisTemplate.keys(pattern);
        return keys;
    }

    /**
     * 通过scan获取所有Key
     */
    public Set<String>  executeWithStickyConnection(String prefix){
        Set<String> keys=new HashSet<>();

        // 设置扫描选项，匹配指定前缀
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").build();

        // 执行 SCAN 命令
        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(redisConnection -> redisConnection.scan(options))) {
            // 遍历所有符合条件的 keys
            if (cursor != null) {
                while (cursor.hasNext()) {
                    byte[] keyBytes = cursor.next();
                    // 将 byte[] 转为 String
                    String key = new String(keyBytes);
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return Result 时间(秒) 返回0代表为永久有效
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return Result true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
     * 删除缓存 批量删除（支持模糊 比如使用通配符*来匹配更多的key）
     *
     * @param pattern 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void delByPattern(String pattern) {
        // 获取匹配的key列表
        Set<String> keys = redisTemplate.keys(pattern);

        // 删除匹配的key
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return Result 值
     */
    public String get(String key) {
        try {
            Object object = redisTemplate.opsForValue().get(key);
            return null != object ? object.toString() : null;
        } catch (Exception e) {
            log.error("redis反序列化异常", e);
            return null;
        }
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return Result true成功 false失败
     */

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return Result true成功 false 失败
     */

    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 时间单位
     * @return Result true成功 false 失败
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * 添加过期时间
     *
     * @param key        键
     * @param expireTime 时间
     * @return long
     */
    public Long incr(String key, Long expireTime) {
        Long incr = this.incr(key);
        redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        return incr;
    }

    /**
     * key值 自增 不带过期时间
     *
     * @param key 键
     * @return Long
     */
    public Long incr(String key) {
        RedisAtomicLong entityIdCounter =
                new RedisAtomicLong(key, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        return entityIdCounter.incrementAndGet();
    }

    /**
     * 增加(自增长), 负数则为自减
     *
     * @param key       键
     * @param increment increment
     * @return Long
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 增加(自增长), 负数则为自减
     *
     * @param key       键
     * @param increment increment
     * @return Double
     */
    public Double incrByDouble(String key, double increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    // ================================Map=================================

    /**
     * 获取map值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return Result redisTemplate.opsForHash().get(key, item);
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    public Object hget1(String key, List item) {
        return redisTemplate.opsForHash().multiGet(key, item);
    }

    /**
     * 获取map所有值
     *
     * @param key  键 不能为null
     * @return Result redisTemplate.opsForHash().get(key, item);
     */
    public Map<Object,Object> hgetall(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return Result 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return boolean
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return Result true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("系统异常", e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return Result true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return Result true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return Result true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return double
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return double
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    /**
     * 获取hash缓存的长度
     *
     * @param key 键
     * @return Long
     */
    public Long getHashSize(String key) {
        try {
            return redisTemplate.opsForHash().size(key);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return Set
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return Result true 存在 false不存在
     */
    public Boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return Result 成功个数
     */
    public Long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return Result 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            expire(key, time);
            return count;
        } catch (Exception e) {
            log.error("系统异常",e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return Long
     */
    public Long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return Result 移除的个数
     */

    public Long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return List
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return Long
     */
    public Long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return Object
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return boolean
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return boolean
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }

    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return Result
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }

    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return Result
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return Result
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("系统异常",e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return Result 移除的个数
     */
    public Long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("系统异常",e);
            return null;
        }

    }

    /**
     * 操作Redis中的ZSet类型数据
     * @return org.springframework.data.redis.core.ZSetOperations<java.lang.String,java.lang.Object>
     * @author haod.liu
     * @date 2024/6/17 下午5:12
     */
    public ZSetOperations<String, Object> getZSetOperations() {
        try {
            return redisTemplate.opsForZSet();
        } catch (Exception e) {
            log.error("RedisUtils-getZSetOperations错误：:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 操作Redis中的String类型数据
     * @return org.springframework.data.redis.core.ValueOperations<java.lang.String,java.lang.Object>
     * @author haod.liu
     * @date 2024/6/17 下午5:12
     */
    public ValueOperations<String, Object> getValueOperations() {
        try {
            return redisTemplate.opsForValue();
        } catch (Exception e) {
            log.error("RedisUtils-getValueOperations错误：:{}", e.getMessage());
            return null;
        }
    }


    /**
     * 返回的Cursor对象是一个迭代器，scan可以在不阻塞Redis的情况下逐步遍历数据，主要用于遍历大型哈希表，因为不需要一次性将所有数据加载到内存中，一般用于处理大Key。
     * @return org.springframework.data.redis.core.Cursor<java.util.Map.Entry<java.lang.Object,java.lang.Object>>
     * @author haod.liu
     * @param scanOptions spring data redis中的一个类，应该是处理每次的数量之类的
     * @date 2024/6/17 下午5:13
     */
    public Cursor<Map.Entry<Object, Object>> getScan(String key, ScanOptions scanOptions) {
        try {
            return redisTemplate.opsForHash().scan(key, scanOptions);
        } catch (Exception e) {
            log.error("RedisUtils-getScan错误：:{}", e.getMessage());
            return null;
        }
    }

    /**
     *  发布消息到Redis频道
     * @param channel
     * @param message
     */
    public void convertAndSend(String channel, Object message) {
        redisTemplate.convertAndSend(channel,message);
    }

    /**
     *设置值
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    public void opsForValueAndSet(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     *查询key
     */
    public Set<String> stringKeys(String key){
        return redisTemplate.keys(key);
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
