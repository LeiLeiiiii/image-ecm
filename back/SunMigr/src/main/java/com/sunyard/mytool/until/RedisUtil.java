package com.sunyard.mytool.until;


import com.sunyard.mytool.constant.MigrateConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Order(15)
@Component
public class RedisUtil {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${spring.redis.max-lock-time:180}")
    private long maxLockTime;


    // =============================Common 基础============================




    /**
     * 加分布式锁
     *
     * @param key
     * @throws Exception
     */
    public boolean safeLock(String key) {
        long start = System.currentTimeMillis();
        try {
            boolean isLockedSeccessfully = setIfAbsent(key, MigrateConstant.randomId, maxLockTime);
            return isLockedSeccessfully;
        } catch (Exception e) {
            logger.error("加锁时发生异常，视为加锁失败", e);
            return false;
        } finally {
            logger.debug("*redis加锁*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 加分布式锁
     *
     * @param key
     * @throws Exception
     */
    public boolean lock(String key) {
        long start = System.currentTimeMillis();
        try {
            boolean isLockedSeccessfully = setIfAbsent(key, MigrateConstant.randomId, maxLockTime);
            if (!isLockedSeccessfully) {
                String lockedServiceId = null;
                try {
                    lockedServiceId = (String) get(key);
                } catch (Exception e) {
                    logger.error("获取加锁信息发生异常，删除该锁");
                    del(key);
                    return false;
                }
                logger.debug("本机serviceId为:{},加锁serviceId为:{}", MigrateConstant.randomId, lockedServiceId);
                if (MigrateConstant.randomId.equals(lockedServiceId)) {
                    //加锁服务为当前服务,刷新加锁时间即可
                    set(key, MigrateConstant.randomId, maxLockTime);
                    return true;
                } else {
                    return false;
                }
            } else {
                logger.debug("加锁成功:{}:{}", MigrateConstant.randomId, key);
                return true;
            }
        } catch (Exception e) {
            logger.error("加锁时发生异常，视为加锁失败", e);
            return false;
        } finally {
            logger.debug("*redis加锁*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 加分布式锁
     *
     * @param key
     * @param expireSeconds
     * @throws Exception
     */
    public boolean lock(String key,long expireSeconds) {
        long start = System.currentTimeMillis();
        try {
            boolean isLockedSeccessfully = setIfAbsent(key, MigrateConstant.randomId, expireSeconds);
            if (!isLockedSeccessfully) {
                String lockedServiceId = null;
                try {
                    lockedServiceId = (String) get(key);
                } catch (Exception e) {
                    logger.error("获取加锁信息发生异常，删除该锁");
                    del(key);
                    return false;
                }
                logger.debug("本机serviceId为:{},加锁serviceId为:{}", MigrateConstant.randomId, lockedServiceId);
                if (MigrateConstant.randomId.equals(lockedServiceId)) {
                    //加锁服务为当前服务,刷新加锁时间即可
                    set(key, MigrateConstant.randomId, expireSeconds);
                    return true;
                } else {
                    return false;
                }
            } else {
                logger.debug("加锁成功:{}:{}", MigrateConstant.randomId, key);
                return true;
            }
        } catch (Exception e) {
            logger.error("加锁时发生异常，视为加锁失败", e);
            return false;
        } finally {
            logger.debug("*redis加锁*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 安全释放锁
     * 只有锁的持有者才能释放锁
     */
    public boolean releaseLockSafely(String key) {
        try {
            String currentValue = (String) get(key);
            if (currentValue != null && currentValue.equals(MigrateConstant.randomId)) {
                del(key);
                logger.info("解锁成功:{}:{}", MigrateConstant.randomId, key);
                return true;
            } else {
                logger.error("尝试释放不属于自己的锁，key: {}, 当前值: {}, 尝试释放值: {}",
                        key, currentValue, MigrateConstant.randomId);
                return false;
            }
        } catch (Exception e) {
            logger.error("解锁时发生异常", e);
            return false;
        }
    }

    /**
     * 解分布式锁
     *
     * @param key
     * @throws Exception
     */

    public void releaseLock(String key) {
        try {
            del(key);
        } catch (Exception e) {
            logger.error("解锁发生异常", e);
        }
    }


    public boolean setIfAbsent(String key, Object value, long time) {
        long start = System.currentTimeMillis();
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
        } finally {
            logger.debug("*setIfAbsent*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public void expire(String key, long time) {
        long start = System.currentTimeMillis();
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } finally {
            logger.debug("*expire*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        // 如果返回值为 null，则返回 0L
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public Boolean del(String key) {
        long start = System.currentTimeMillis();
        try {
            return redisTemplate.delete(key);
        } finally {
            logger.debug("*del*耗时:{}", System.currentTimeMillis() - start);
        }
    }
    // ============================String 字符串=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        long start = System.currentTimeMillis();
        try {
            Object obj = key == null ? null : redisTemplate.opsForValue().get(key);
            return obj;
        } finally {
            logger.debug("*redisGet{}*耗时:{}", key, System.currentTimeMillis() - start);
        }
    }


    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public void set(String key, Object value, long time) {
        long start = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } finally {
            logger.debug("*redisSet{}*耗时:{}", key, System.currentTimeMillis() - start);
        }
    }

    /**
     * 普通缓存放入  无过期时间
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        long start = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue().set(key, value);
        } finally {
            logger.debug("*redisSet{}*耗时:{}", key, System.currentTimeMillis() - start);
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }
    // ===============================List 列表=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     */
    public List<Object> getList(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     */
    public long getListSize(String key) {
        long start = System.currentTimeMillis();
        try {
            long result = redisTemplate.opsForList().size(key);
            return result;
        } finally {
            logger.debug("*redisGetListSize{}*耗时:{}", key, System.currentTimeMillis() - start);
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0
     *              时，-1，表尾，-2倒数第二个元素，依次类推
     */
    public Object getListIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 把value存到list(链表)最右边,同时刷新list过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public boolean rightPush(String key, Object value, long time) {
        long start = System.currentTimeMillis();
        try {
            redisTemplate.opsForList().rightPush(key, value);
            try {
                expire(key, time);
            } catch (Exception e) {
                logger.error("刷新超时时间时发生异常");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            logger.debug("*redisRightPush{}*耗时:{}", key, System.currentTimeMillis() - start);
        }
    }


    /**
     * 把list存到list(链表)最右边,同时刷新list过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public void rightPushAll(String key, List value, long time) {
        redisTemplate.opsForList().rightPushAll(key, value.toArray());
        if (time > 0) {
            try {
                expire(key, time);
            } catch (Exception e) {
                logger.error("刷新超时时间时发生异常");
            }
        }
    }

    /**
     * 从list(链表)最左边取值
     *
     * @param key 键
     * @return 赋值结果
     */
    public Object leftPop(String key) {
        long start = System.currentTimeMillis();
        try {
            Object obj = redisTemplate.opsForList().leftPop(key);
            return obj;
        } finally {
            logger.debug("*redisLeftPop{}*耗时:{}", key, System.currentTimeMillis() - start);
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void updateIndex(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long listRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }
    // ============================Set 集合=============================
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
            logger.error(e.getMessage(), e);
            return false;
        }
    }

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

}
