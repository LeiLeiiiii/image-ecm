package com.sunyard.ecm.constant;

/**
 * @author： rao
 * @create： 2023/4/26 13:49
 * @Desc redis常量配置
 */
public class RedisConstants {

    public static final String ECM = "ecm-service:";
    /**
     * 用户redis存储的key前缀
     */
    public final static String USER_BUSI_PREFIX = ECM + "USER_BUSI:";
    /**
     *  业务redis存储的key前缀
     */
    public final static String BUSI_BASEINFO_PREFIX = ECM + "BUSI_ALL_PREFIX:";

    public final static String BUSIFILE_PREFIX = ECM + "BUSIFILE:";
    /**
     *  业务redis存储的页面数据
     */
    public final static String PAGE_BUSI_LIST = ECM + "PAGE_BUSI_LIST:";
    public final static String REDIS_KEY_COMMON_ZIPCONFIG = ECM + "ZIPCONFIG:";
    /**
     * 业务总量
     */
    public final static String BUSI_VOLUME_STATISTICS = ECM + "BUSI_VOLUME_STATISTICS:";

    /**
     * redis 消息队列广播形式管道
     * */
    public final static String REDIS_CHANNEL = ECM + "REDIS_CHANNEL_ECM:";

    /**
     *  异步任务栏数据缓存key
     */
    public final static String BUSIASYNC_TASK_PREFIX = ECM + "BUSIASYNC:";

    /**
     * 异步任务栏需要定时任务推送key
     */
    public final static String NEED_PUSH_BUSIASYNC_TASK_PREFIX = ECM + "NEED_PUSH_BUSIASYNC:";


    /**
     * 用户自动归类开关
     */
    public final static String AUTO_CLASS_USER = ECM + "AUTO_CLASS_USER:";

    /**
     * 自动归类待处理列表
     */
    public final static String AUTO_CLASS_PENDING_TASK_LIST = ECM + "AUTO_CLASS_PENDING_TASK_LIST:";

    /**
     * 智能化处理DOC配置缓存
     */
    public final static String INTELLIGENT_PROCESSSING_DOC_DEF = ECM + "INTELLIGENT_PROCESSSING_DOC_DEF";

    /**
     * es锁
     */
    public final static String LOCK_ES_FILE = ECM + "LOCK_ES_FILE:";

    /**
     * 一次有效URL
     */
    public final static String ONCE_URL_NONCE = ECM + "ONCE_URL_NONCE:";

    /**
     * 生成默认流水号key前缀
     */
    public static final String DEFAULT_BUSI_NO_PREFIX_KEY = ECM + "DEFAULT_BUSI_NO:";


}