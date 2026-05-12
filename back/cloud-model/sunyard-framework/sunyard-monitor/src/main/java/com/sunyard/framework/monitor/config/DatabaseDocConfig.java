package com.sunyard.framework.monitor.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.monitor.constant.DatabaseDocConstants;
import com.sunyard.framework.redis.util.RedisUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 生成数据库文档
 *
 * @author huronghao
 */
@Slf4j
@Configuration
@ConditionalOnExpression("#{environment['spring.datasource.url']!=null}")
public class DatabaseDocConfig {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private Environment environment;

    @PostConstruct
    public void init() {
        log.info("数据库文档:配置初始化！");
        Map map = new HashMap<String, String>();
        map.put("url", environment.getProperty("spring.datasource.url"));
        map.put("username", environment.getProperty("spring.datasource.username"));
        map.put("password", environment.getProperty("spring.datasource.password"));
        redisUtils.set(DatabaseDocConstants.CACHE_NAME + environment.getProperty("spring.application.name"),
            JSONObject.toJSONString(map));
        log.info("数据库文档:配置初始化完成！");
    }
}
