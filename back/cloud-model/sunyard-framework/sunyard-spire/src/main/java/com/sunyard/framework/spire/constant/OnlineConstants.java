package com.sunyard.framework.spire.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zhouleibin
 * 在线预览常量
 */
public class OnlineConstants {
    /**
     * 在线预览是否有水印
     */
    public static final Integer WATER_FLAG_NO = 0;
    public static final Integer WATER_FLAG_HAVE = 1;

    public static final List<String> DOCLIST = Collections.unmodifiableList(Arrays.asList("docx", "doc", "wps"));
    public static final List<String> XLSLIST = Collections.unmodifiableList(Arrays.asList("xls", "xlsx"));
    public static final List<String> PPTLIST = Collections.unmodifiableList(Arrays.asList("ppt", "pptx"));
    public static final List<String> TXTLIST = Collections.unmodifiableList(Arrays.asList("txt","ini"));
    public static final List<String> PDFLIST = Collections.unmodifiableList(Arrays.asList("pdf"));
    public static final List<String> IMGLIST = Collections.unmodifiableList(Arrays.asList("gif", "jpg", "jpeg", "jpe", "png", "psd", "bmp"));
    public static final List<String> TIFFLIST = Collections.unmodifiableList(Arrays.asList("tiff", "tif"));
    public static final List<String> OFDLIST = Collections.unmodifiableList(Arrays.asList("ofd"));
    public static final List<String> HEIFLIST = Collections.unmodifiableList(Arrays.asList("heif"));

}
