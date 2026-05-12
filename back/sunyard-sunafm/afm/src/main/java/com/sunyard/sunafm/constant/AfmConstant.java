package com.sunyard.sunafm.constant;

/**
 * 常量
 */
public class AfmConstant {
    //定义Exchange的常量-----fanout：扇形，就是广播类型
    public static final String EXCHANGE_NAME = "exchange_afm_dev";
    //消息队列失效时间10分中
    public static final Integer FAILURE_TIME_MESSAGE = 10;
    //消息队列数组
    public static final String SUFF = "#_sunyard#";

    //消息队列数组
    public static final String QUEUE_NAMES = "queue_afm_dev";

    //消息队列数组
    public static final String COLLECTION_HEAD = "COLLECT_";

    //消息队列数组
    public static final String AFM_SOURCESYS = "AFM";

    //共用
    public static final Integer NO = 0;
    //共用
    public static final Integer YES = 1;

    //默认查询文件数量大小
    public static final Integer FILE_NUM_MILVUS_DEFULT = 5;
    //默认查询相似度
    public static final Double FILE_SIMILARITY_DEFULT = 0.85;

    //默认查询文件数量大小
    //默认查询相似度
    public static final String AFM_PARAM_SYSTEM = "AFM_PARAM_SYSTEM";
    public static final String FILE_NUM_SYSTEM = "FILE_NUM_SYSTEM";
    //默认查询相似度
    public static final String FILE_SIMILARITY_SYSTEM = "FILE_SIMILARITY_SYSTEM";

    //同一张图片的相似度
    public static final Double FILE_SAM = 1.0;

    /**
     * 检测方式
     * 第一位～第二位：是否查询查重
     * 10：仅对传入文件做特征保存
     * 11：对传入文件做查重并且保存特征
     * 第三位：是否查询篡改结果，0否，1是
     * 第四位～第七位：
     * 000:都不做检测
     * 100:做验真
     * 010做查重
     * 001:做连续性
     */
//    public static final String DO_DET_NOTE = "01";
    public static final String DO_DET_SAVE = "10";
    public static final String DO_DET_SAVE_AND_NOTE = "11";
    public static final String DO_INVOICE_NOT = "000";
    public static final String DO_INVOICE_YZ = "100";
    public static final String DO_INVOICE_CC = "010";
    public static final String DO_INVOICE_LX = "001";


    //来源
    public static final String AFM_SOURCE = "AFM_SOURCE";

    public static final String SUCC_CODE = "200";


    /**
     * 服务器切换
     */
    public static final Integer AUTO_SWITCH_YES = 0;
    public static final Integer AUTO_SWITCH_ERROR = 1;


    /**
     * 对外请求三种状态
     */
    public static final Integer OPEN_API_STATUS_INIT = 0;
    public static final Integer OPEN_API_STATUS_SUCC = 1;
    public static final Integer OPEN_API_STATUS_ERROR = 2;

    /**
     *  查重类型
     */
    public static final Integer IMAGE_ANTI_FRAUD_DET_TYPE = 0;
    public static final Integer TEXT_ANTI_FRAUD_DET_TYPE = 1;

    /**
     * ECM AFM 数据同步类型
     */
    public static final Integer ECM_AFM_SYNC_UPDATE = 1;
    public static final Integer ECM_AFM_SYNC_DELETE = 2;

}
