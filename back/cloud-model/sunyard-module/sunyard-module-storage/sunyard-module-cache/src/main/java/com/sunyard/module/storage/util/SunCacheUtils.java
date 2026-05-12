package com.sunyard.module.storage.util;


import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 判断文件类型工具类
 *
 * @author panjiazhu
 * @date 2022/7/13
 */
@Slf4j
public class SunCacheUtils {


    public final static List<String> VIDEOS = Collections.unmodifiableList(Arrays.asList("wmv", "asf", "rm", "rmvb", "avi", "mov", "mpg", "flv"));
    public final static List<String> IMGS = Collections.unmodifiableList(Arrays.asList("gif", "jpg", "jpeg", "png", "psd", "bmp", "wbmp", "pnm", "jpe", "webp", "heif", "heic", "jfif"));
    public final static List<String> SPECIALIMGS = Collections.unmodifiableList(Arrays.asList("psd", "bmp", "wbmp", "pnm", "jpe", "webp", "heif", "heic", "jfif"));
    public final static List<String> DOCS = Collections.unmodifiableList(Arrays.asList("txt", "doc", "wps", "docx", "xls", "ppt", "pptx", "xlsx", "ini"));
    public final static List<String> HEIF = Collections.unmodifiableList(Arrays.asList( "heif"));
    public final static List<String> HEIC = Collections.unmodifiableList(Arrays.asList( "heic"));

    public final static List<String> OFD = Collections.unmodifiableList(Arrays.asList("ofd"));

    public static final List<String> TIFFLIST = Collections.unmodifiableList(Arrays.asList("tiff", "tif"));
    public final static List<String> AUDIOS = Collections.unmodifiableList(Arrays.asList("amr", "ogg", "m4a","wav"));
    public final static List<String> PDF = Collections.unmodifiableList(Arrays.asList("pdf"));
    public final static List<String> DOCX = Collections.unmodifiableList(Arrays.asList("docx"));
    public final static List<String> XLS = Collections.unmodifiableList(Arrays.asList(new String[]{"xls", "xlsx"}));

}
