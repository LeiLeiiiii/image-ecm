package com.sunyard.module.storage.constant;

/**
 * 状态常量
 * @author zhaoyang
 * @since 2021/12/22 10:06
 */
public class StateConstants {

    /************************** 第三方接口调用错误码 ************************/
    /**
     * 影像配置的常量 SUNAM_CODE系统 SUNICMS_CODE请求根路径
     */
    public static final String SUNAM_CODE = "SunAM";

    /**
     * 本地存储
     */
    public final static Integer COMMON_STORAGE_TYPE_LOCAL = 0;


    /**
     * 对象存储
     */
    public final static Integer COMMON_STORAGE_TYPE_OBJ = 1;

    /**
     * 共用的0
     */
    public static final Integer ZERO = 0;

    /**
     * 是否加密 0否 1是
     */
    public static final Integer IS_ENCRYPT = 1;

    /**
     * 加密方式
     */
    public static final String FILE_ENCRYPT_TYPE= "FILE_ENCRYPT_TYPE";

    /**
     * 加密方式aes
     */
    public static final String FILE_ENCRYPT_TYPE_AES = "AES";


    /**
     * 公用的1 2 3
     */
    public static final Integer COMMON_ONE = 1;
    public static final Integer COMMON_TWO = 2;

    /**
     * 共用的常量
     */
    public static final Integer YES = 1;


    /**
     * 水印配置
     */
    public static final String WATERMARK_PARAM_FILE = "WATERMARK_PARAM_FILE";


    /**
     * 水印配置-字体
     */
    public static final String COMMON_WATERMARK_FAMILYNAME = "COMMON_WATERMARK_FAMILYNAME";


    /**
     * 90
     */
    public static final Integer NINETY = 90;
    /**270 */
    public static final Integer TWO_HUNDRED_SEVENTY = 270;


    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";

}
