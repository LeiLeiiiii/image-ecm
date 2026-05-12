package com.sunyard.ecm.constant;

/**
 * @author ty
 * @since 2023-4-12 14:56
 * @Desc 全局公用常量配置
 */
public class StateConstants {

    /**
     * 共用的0
     */
    public static final Integer ZERO = 0;
    /**
     * 公用的1 2 3
     */
    public static final Integer COMMON_ONE = 1;
    public static final String COMMON_ONE_STR = "1";
    public static final Integer COMMON_TWO = 2;
    public static final Integer COMMON_THREE = 3;
    /**
     * 共用的0
     */
    public static final Integer SUCC = 200;
    public static final Integer FAIL = 500;

    /**
     * 共用的常量
     */
    public static final Integer YES = 1;
    public static final Integer NO = 0;

    /**
     * 未删除状态
     */
    public final static int NOT_DELETE = 0;

    /**
     * 已删除状态
     */
    public final static int DELETED = 1;

    /**
     * 字符串的长度限定
     */
    public static final Integer LENGTH_MAX_10 = 10;
    public static final Integer LENGTH_MAX_11 = 11;
    public static final Integer LENGTH_MAX_20 = 20;
    public static final Integer LENGTH_MAX_30 = 30;
    public static final Integer LENGTH_MAX_32 = 32;
    public static final Integer LENGTH_MAX_50 = 50;
    public static final Integer LENGTH_MAX_64 = 64;
    public static final Integer LENGTH_MAX_70 = 70;
    public static final Integer LENGTH_MAX_100 = 100;
    public static final Integer LENGTH_MAX_128 = 128;
    public static final Integer LENGTH_MAX_254 = 254;
    public static final Integer LENGTH_MAX_255 = 255;
    public static final Integer LENGTH_MAX_500 = 500;
    public static final Integer LENGTH_MAX_512 = 512;
    public static final Integer LENGTH_MAX_10000 = 10000;


    /**
     * 超级管理员
     */
    public static final Integer LOGIN_TYPE_999 = 999;

    /**
     * 档案顺序号生成位数
     */
    public static final Integer ARCNO_ZERO_NUM = 5;

    /**
     * 登录配置 类型 本地
     */
    public static final String LOGIN_CONFIG_TYPE_BD_STR = "LOGIN_CONFIG_TYPE_BD_STR";
    /**
     * 登录配置 类型 LDAP
     */
    public static final String LOGIN_CONFIG_TYPE_LDAP_STR = "LOGIN_CONFIG_TYPE_LDAP_STR";
    /**
     * 登录配置  激活状态
     */
    public static final Integer LOGIN_TYPE_ACTIVATE = 1;
    public static final Integer LOGIN_TYPE_UNACTIVATE = 0;

    /**
     * 水印配置
     */
    public static final String WATERMARK_PARAM = "WATERMARK_PARAM";

    /**
     * 管理员账户
     */
    public static final Integer ADMINISTRATORACCOUNT = 1;

    /**
     * 普通账户
     */
    public static final Integer GENERALACCOUNT = 0;

    /**
     * 本地存储
     */
    public final static Integer COMMON_STORAGE_TYPE_LOCAL = 0;

    /**
     * 对象存储
     */
    public final static Integer COMMON_STORAGE_TYPE_OBJ = 1;

    /**
     * 业务数量缓存标识key
     */
    public static final String BUSI_INFO_COUNT = "BUSI_INFO_COUNT";

    /**
     * 采集页面有数据变动
     */
    public final static Integer CHANG_FLAG_TRUE = 1;

    /**
     * 采集页面有数据未变动
     */
    public final static Integer CHANG_FLAG_FLASE = 0;

    /**
     * 影像业务类型父节点代码默认值为s0
     */
    public final static String PARENT_APP_CODE_DEFAULT = "s0";

}
