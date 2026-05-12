package com.sunyard.module.storage.constant;

/**
 * @author yzy
 * @desc
 * @since 2025/12/18
 */
public class RedisTopicConstant {

    public static final String STORAGE = "storage-service:";
    public static final String FILE_STORAGE_EQUIPMENT_TOPIC = STORAGE + "FILE_STORAGE_EQUIPMENT_TOPIC";
    /**
     * 分布式锁key
     */
    public static final String FILE_STORAGE_LOCK = STORAGE + "file_storage:list:lock";
    /**
     * 锁超时时间（避免死锁）
     */
    public static final long LOCK_TIMEOUT_SECONDS = 5;
    /**
     * 获取锁重试次数
     */
    public static final int LOCK_RETRY_TIMES = 3;
    /**
     * 重试间隔（毫秒）
     */
    public static final long LOCK_RETRY_INTERVAL = 100;

    /**
     * 锁自动失效时间为10秒
     */
    public static final Long EXPIRE = 10 * 1000L;
}
