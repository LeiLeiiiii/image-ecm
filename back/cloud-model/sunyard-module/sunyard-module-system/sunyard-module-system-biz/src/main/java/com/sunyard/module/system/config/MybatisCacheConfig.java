package com.sunyard.module.system.config;

import com.sunyard.framework.redis.util.RedisUtils;
import org.springframework.context.annotation.Configuration;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Configuration
public class MybatisCacheConfig {

    @Resource
    private RedisUtils redisUtils;

    // 项目启动时初始化缓存类的redisUtils
    @PostConstruct
    public void init() {
        SysDictStyleMybatisCache.setRedisUtils(redisUtils);
    }
}
