package com.sunyard.framework.onlyoffice.core.cache;

import com.sunyard.framework.onlyoffice.constant.StateConstants;
import com.sunyard.framework.onlyoffice.core.Cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import lombok.extern.slf4j.Slf4j;

/**
 * @author PJW
 * @Description: 内部缓存
 */
@Slf4j
public class InsideCache implements Cache {


    /**
     * 创建缓存，默认 1小时过期
     */
    private TimedCache<String, Object> cache = CacheUtil.newTimedCache(StateConstants.TIMEOUT * StateConstants.TIMEUNIT * 60);


    public InsideCache() {
        cache.schedulePrune(StateConstants.TIMEOUT * StateConstants.TIMEUNIT);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public boolean hasKey(String key) {
        return cache.get(key) != null;
    }

    /**
     * @param key
     * @param value
     * @param time  时间 单位是秒
     */
    @Override
    public void set(String key, Object value, long time) {
        log.info("key:{},value:{},size:{}", key, value, cache.size());
        cache.put(key, value, time * StateConstants.TIMEUNIT);
    }

    @Override
    public void set(String key, Object value) {
        cache.put(key, value);
    }
    @Override
    public void remove(String key) {
        cache.remove(key);
    }


}
