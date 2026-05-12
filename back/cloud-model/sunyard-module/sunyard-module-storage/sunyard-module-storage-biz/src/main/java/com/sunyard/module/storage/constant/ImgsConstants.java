package com.sunyard.module.storage.constant;

/**
 * 图片常量
 * @author PJW
 */
public class ImgsConstants {

    /**
     * 压缩图片的常量
     */
    public static final Integer THUMBNAILS_WIDTH = 120;
    public static final Integer THUMBNAILS_HIGHT = 120;
    public static final String THUMBNAILS_OUTPUTFORMAT = "png";


    /**
     * 旋转--90度.270度，清晰度
     */
    public static final Integer ROTATIONANGLE_90 = 90;
    public static final Integer ROTATIONANGLE_270 = 270;
    public static final Double ROTATIONANGLE_SCALE_1 = 1.0;

    /**
     * 水印 0-查看  1-打印 2：下载
     */
    public static final Integer WATERMARK_TYPE_SHOW = 0;
    public static final Integer WATERMARK_TYPE_PRINTING = 1;
    public static final Integer WATERMARK_TYPE_DOWNLOAD = 2;

}
