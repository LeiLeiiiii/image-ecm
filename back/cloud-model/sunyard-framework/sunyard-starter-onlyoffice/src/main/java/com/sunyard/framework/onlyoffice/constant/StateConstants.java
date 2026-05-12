package com.sunyard.framework.onlyoffice.constant;

/**
 * @author zhaoyang 2021/12/22 10:06
 */
public class StateConstants {
    public final static String DOC_API_URL = "/web-apps/apps/api/documents/api.js";
    public final static String CONVERTER = "/ConvertService.ashx";
    public final static String SAVE = "/coauthoring/CommandService.ashx";
    /**
     * 在线编辑
     */
    public final static String EDIT = "edit";
    /**
     * 在线浏览
     */
    public final static String VIEW = "view";
    public final static String CACHE = "redis";

    public final static long TIMEOUT = 60;
    /**
     * redis key 值前缀
     */
    public final static String REDISEDIT = "collaborativeEditing_";
    public final static String REDISGETID = "getID_";


    /**
     * 时间单位 秒
     */
    public final static long TIMEUNIT = 1000;
}
