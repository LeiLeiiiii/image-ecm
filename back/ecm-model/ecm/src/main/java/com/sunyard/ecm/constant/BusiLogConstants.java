package com.sunyard.ecm.constant;

/**
 * @author scm
 * @since 2023/8/4 16:35
 * @Desc 业务日志常量配置
 */
public class BusiLogConstants {

    /**
     * 业务日志表名
     */
    public static final String BUSILOG = "业务日志表.xlsx";

    /**
     * 新增业务与编辑业务参数名
     */
    public static final String ADDANDEDIT = "ecmBusiInfoExtend";

    /**
     * 新增业务与编辑业务参数名 - 对外接口
     */
    public static final String ADDANDEDITOPEN = "busExtendDTO";

    /**
     * 编辑业务属性参数名 - 对外接口
     */
    public static final String EDITATTROPEN = "editBusiAttrDTO";


    /**
     * 提交业务与删除业务参数名
     */
    public static final String DELBUSI = "busiId";

    /**
     * 批量删除业务参数名
     */
    public static final String DELBUSIBATCH = "busiIds";

    /**
     * 查看业务参数名，归类文件参数名，删除文件参数名
     */
    public static final String GETSORTDEL = "vo";

    /**
     * 编辑文件参数名（旋转，淡化，锐化,合并）
     */
    public static final String EDITFILE = "vo";

    /**
     * 拆分文件参数名
     */
    public static final String SPLITFILE = "vo";

    /**
     * 还原文件参数名
     */
    public static final String RESTOREFILE = "vo";

    /**
     * 上传文件参数名
     */
    public static final String UPFILE = "ecmFileInfoDTO";

    /**
     * 新增业务方法名
     */
    public static final String ADDBUSIMETHOD = "addBusi";

    /**
     * 编辑业务方法名
     */
    public static final String EDITBUSIMETHOD = "editBusi";

    /**
     * 批量删除业务方法名
     */
    public static final String DELBUSIBATCHMETHOD = "deleteBatch";



    /**
     * 查看业务方法名-移动端
     */
    public static final String QUERYBUSIMETHOD_MOBILE = "scanOrUpdateEcmMobile";
    /**
     * 查看业务方法名-对外接口
     */
    public static final String QUERYBUSIMETHOD_CAPTURE = "scanOrUpdateEcm";
    /**
     * 查看业务方法名-PC端
     */
    public static final String QUERYBUSIMETHOD_PC = "singleCapture";
    /**
     * 查看业务方法名
     */
    public static final String GETMETHOD = "searchEcmsFileList";

    /**
     * 还原文件方法名
     */
    public static final String RESTOREMETHOD = "restoreFileInfo";



    /**
     * 删除文件方法名
     */
    public static final String DELMETHOD = "deleteFileInfo";

    /**
     * 编辑文件方法名
     */
    public static final String EDITMETHOD = "rotateInsertFileInfo";


    /**
     * 拆分文件方法名
     */
    public static final String SPLITMETHOD = "splitFile";

    /**
     * 合并文件方法名
     */
    public static final String MERGEMETHOD = "mergInsertFileInfo";

    /**
     * 业务日志操作类型(0 业务新增 ;1 业务查看 ;2 业务编辑 ;3 业务删除;4 文件新增;5 文件调阅; 6 文件编辑; 7 文件删除;8 影像销毁)
     */
    public final static Integer OPERATION_TYPE_ZERO = 0;
    public final static Integer OPERATION_TYPE_ONE = 1;
    public final static Integer OPERATION_TYPE_TWO = 2;
    public final static Integer OPERATION_TYPE_THREE = 3;
    public final static Integer OPERATION_TYPE_FOUR = 4;
    public final static Integer OPERATION_TYPE_FIVE = 5;
    public final static Integer OPERATION_TYPE_SIX = 6;
    public final static Integer OPERATION_TYPE_SEVEN = 7;
    public final static Integer OPERATION_TYPE_EIGHT = 8;
    public final static String OPERATION_TYPE_ZERO_STR = "业务新增";
    public final static String OPERATION_TYPE_ONE_STR = "业务查看";
    public final static String OPERATION_TYPE_TWO_STR = "业务编辑";
    public final static String OPERATION_TYPE_THREE_STR = "业务删除";
    public final static String OPERATION_TYPE_FOUR_STR = "文件新增";
    public final static String OPERATION_TYPE_FIVE_STR = "文件调阅";
    public final static String OPERATION_TYPE_SIX_STR = "文件编辑";
    public final static String OPERATION_TYPE_SEVEN_STR = "文件删除";
    public final static String OPERATION_TYPE_EIGHT_STR = "影像销毁";
}
