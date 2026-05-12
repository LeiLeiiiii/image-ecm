package com.sunyard.ecm.constant;

/**
 * @author ty
 * @since 2023-4-12 14:56
 * @Desc 底座系统参数常量配置
 */
public class RoleConstants {

    // 通用常量
    public final static Integer ZERO = 0;
    public final static Integer ONE = 1;

    /**
     * 系统区分 0：档案，1：影像
     */
    // 档案
    public final static Integer ARCHIVES = 0;
    // 影像
    public final static Integer ICMS = 3;

    /**
     * 影像内容管理系统菜单根节点
     */
    public final static String SUNICMS = "sunIcms";

    /**
     * 菜单类型-按钮
     */
    public final static String BUTTON = "B";

    /**
     * 影像采集-菜单权限标识
     */
    public final static String IMAGE_CAPTURE = "imageCapture";

    /**
     * 影像采集-菜单（已删除）权限标识
     */
    public final static String CAPTURE_VIEW_DELETED = "captureViewDeleted";

    /**
     * 影像查看-菜单权限标识
     */
    public final static String IMAGE_VIEW = "imageSearch";

    /**
     * 影像采集-菜单（已删除）权限标识
     */
    public final static String SEARCH_VIEW_DELETED = "searchViewDeleted";
    /**
     * 动态树 树结构权限  分配的按钮权限
     */
    public final static String VTREE_AUTH = "vtreeAuth";
    public final static String VTREE_AUTHSHOWC = "vtreeAuthShowC";
    public final static String VTREE_AUTHSHOW = "vtreeAuthShow";

}
