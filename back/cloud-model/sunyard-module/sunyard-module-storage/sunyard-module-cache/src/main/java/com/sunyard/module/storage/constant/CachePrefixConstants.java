package com.sunyard.module.storage.constant;

/**
 * 文件缓存常量
 *
 * @author PJW
 */
public class CachePrefixConstants {

    public static final String STORAGE = "storage-service:";

    /**
     * 文件转码标识
     */
    public static final String TRANSCODING = STORAGE + "Transcoding:";

    /**
     * 文件转码标识
     */
    public static final String CACH_FILE = STORAGE + "Cach_";

    /**
     * 文件转码标识
     */
    public static final String CACH_ING = STORAGE + "ing:";

    /**
     * pdf拆分
     */
    public final static String SPLIT_PDF_FILE = STORAGE + "SPLIT_PDF_FILE:";
    public final static String LOCK_SPLIT_PDF_FILE = STORAGE + "LOCK_SPLIT_PDF_FILE:";
    public final static String SPLIT_PDF_FILE_TOTAL = STORAGE + "SPLIT_PDF_FILE_TOTAL:";

    /**
     * 文件合并压缩包
     */
    public final static String MERGE_ZIP = STORAGE + "MERGE_ZIP:";
    public final static String ZIP_PATH = STORAGE + "ZIP_PATH:";

    // 进度缓存前缀 - 总文件数
    public static final String MERGE_ZIP_TOTAL_COUNT = STORAGE + "merge:zip:total:";
    // 进度缓存前缀 - 已完成文件数
    public static final String MERGE_ZIP_FINISHED_COUNT = STORAGE + "merge:zip:finished:";
}
