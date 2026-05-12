package com.sunyard.module.storage.constant;

/**
 * 文件常量
 * @author PJW
 */
public class FileConstants {
    /**
     * 是否上传完成（0:未完成，1：完成）
     */
    public static final Integer FILE_NOT_FINISH = 0;
    public static final Integer FILE_FINISH = 1;

    /**
     * 区分移动端和pc端，移动端传值为0，pc端不传
     */
    public static final String MOBILE_FILE_UPLOADURL = "0";
    public static final String PC_FILE_UPLOADURL = "1";

    /**
     * 存储方式
     */
    public static final Integer OSS = 1;
    public static final Integer NAS = 0;
    public static final Integer SFTP = 2;
    public static final Integer ERROR = 404;
    public static final String NOSUCHBUCKET_ERROR = "NoSuchBucket";

    public final static String SPLIT = "-";


    /**
     * 水印
     */
    public static final String TEL = "tel";
    public static final String COMPANY = "company";
    public static final String SYS_DATE = "sysDate";
    public static final String CUSTOM = "custom";
    public static final String USERNAME = "username";

    /**
     * 存储路径类型
     */
    public static final String DATE = "date";
    public static final String HASH = "hash";


    // 锁获取时间超时为30秒
    public final static Long MERRGE_ACQUIRETIMEOUT = 30 * 1000L;
    // 锁自动失效时间为10秒
    public final static Long MERRGE_EXPIRE = 10 * 1000L;

}
