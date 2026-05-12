package com.sunyard.mytool.until;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import io.lettuce.core.RedisConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
@Order(16)
public class IDUtils implements DisposableBean {
    @Resource
    RedisUtil redisUtil;
    private Snowflake snowflake;
    private static final String ID_LOCK_PREFIX = "snowflake:alloc:";
    private String lockKey;

    @PostConstruct
    public void init() {
        // 顺序遍历（1-31有效范围）
        for (int dataCenterId = 1; dataCenterId <= 31; dataCenterId++) {
            for (int workId = 1; workId <= 31; workId++) {
                lockKey = String.format("%s%d:%d", ID_LOCK_PREFIX, dataCenterId, workId);
                try {
                    // 判断key是否存在
                    if (!redisUtil.hasKey(lockKey)) {
                        try {
                            //非原子性，多节点启动时建议先后启动   原子性set必须设置过期时间
                            redisUtil.set(lockKey, "1");
                            snowflake = IdUtil.getSnowflake(workId, dataCenterId);
                            log.info("Successfully allocated - DataCenterID:{}, WorkerID:{}, nextId:{}", dataCenterId, workId, snowflake.nextId());
                            return;
                        } catch (Exception e) {
                            redisUtil.releaseLock(lockKey); // 初始化失败释放锁
                            throw e;
                        }
                    }
                } catch (RedisConnectionException e) {
                    log.warn("Redis operation failed on {}/{}, retrying...", dataCenterId, workId);
                }
            }
        }
        throw new IllegalStateException("No available WorkerID/DataCenterID combination");
    }

    public String getUUIDBits(int size) {
        Random random = new Random();
        String uuidStr = UUID.randomUUID().toString().replace("-", "");;

        // 随机选取20个字符
        int startIndex = random.nextInt(uuidStr.length() - size);
        String randomUuid = uuidStr.substring(startIndex, startIndex + size);

        return randomUuid;
    }

    /**
     * 获取雪花算法ID
     * @return
     */
    public long  nextId() {
        return snowflake.nextId();
    }

    /**
     * 程序停止时，删除锁
     */
    public void destroy() {
        if (redisUtil.hasKey(lockKey)) {
            redisUtil.releaseLock(lockKey);
        }
    }
}
