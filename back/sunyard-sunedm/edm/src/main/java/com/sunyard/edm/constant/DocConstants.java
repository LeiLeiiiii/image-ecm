package com.sunyard.edm.constant;

/**
 * 常量
 *
 * @Author 朱山成
 */
public class DocConstants {
    public static final String APPLICATION = "edm-service";

    public static final Long MINIO = 1689192993912786946L;
    /**
     * 文件存储大小
     */
    public static final Integer FILESIZE = 1024;

    /**
     * 常量
     */
    public static final Integer THREE_HUNDDRED = 300;
    public static final Integer ONE = 1;
    public static final String STR_ONE = "1";
    public static final Integer ZERO = 0;
    public static final Integer TWO = 2;
    public static final Integer THREE = 3;

    /**
     * HashMap初始值大小
     */
    public static final Integer SIXTEEN = 16;
    public static final Integer INDEX = -1;
    public static final Integer FAIL = 0;
    public static final Integer SUCCEED = 1;
    /**
     * 层级对比长度
     */
    public static final Integer LEVEL_FOLDER = 2;

    public static final String FOLDERTYPE = "folderType";
    /**
     * 是否删除
     */
    public static final Integer DELETED_NO = 0;
    public static final Integer DELETED_YES = 1;

    /**
     * 0:企业，1:个人
     */
    public static final Integer PERSON = 1;
    public static final Integer COMPANY = 0;


    /**
     * 0:文件夹；1:文档；2:附件
     */
    public static final Integer FOLDER = 0;
    public static final Integer DOCUMENT = 1;
    public static final Integer FILE = 2;

    /**
     * 字符串 0 9
     */
    public static final Character ZERO_CHAR = '0';
    public static final Character NINE_CHAR = '9';

    /**
     * 0:用户；1:机构；2:部门  3：团队
     */
    public static final Integer USER = 0;
    public static final Integer INST = 1;
    public static final Integer DEPT = 2;
    public static final Integer TEAM = 3;


    /**
     * 任务的类型，0：删除存储
     */
    public static final Integer TASK_DEL_FILE = 0;

    /**
     * 0升序 1降序
     */
    public static final String SORT_ASC = "ascending";
    public static final String SORT_DESC = "descending";


    /**
     * 回收状态 0:正常，1:已回收
     */
    public static final Integer RECYCLE_STATUS_NORMAL = 0;
    public static final Integer RECYCLE_STATUS_RECOVERED = 1;

    /**
     * 企业文档状态 0未上架 1待上架 2已上架 3已下架
     */
    public static final Integer DOC_STATUS_WAIT = 0;
    public static final Integer DOC_STATUS_NOPUTAWAY = 1;
    public static final Integer DOC_STATUS_PUTAWAY = 2;
    public static final Integer DOC_STATUS_OUT = 3;

    /**
     * 文档格式，其他所有
     */
    public static final String DOC_COMMON_SUFFIX_OTHER = "6";

    /**
     * 系统配置key 权限，0:可查看，1:可编辑，2：可管理
     */
    public static final Integer DOC_COMMON_PERMISSION_TYPE_LOOK = 0;
    public static final Integer DOC_COMMON_PERMISSION_TYPE_EDIT = 1;
    public static final Integer DOC_COMMON_PERMISSION_TYPE_MANAGE = 2;

    /**
     * 高级配置状态 0:未配置，1:已配置
     */
    public static final Integer DOC_STATUS_NOSET = 0;
    public static final Integer DOC_STATUS_SET = 1;


    /**
     * 系统配置key
     */
    public static final String DOC_RECYCLE_DAY_KEY = "DOC_RECYCLE_DAY_KEY";
    public static final String DOC_FOLDER_TREE_TYPE = "DOC_FOLDER_TREE_TYPE";
    public static final String DOC_MAXIMUM_SIZE = "DOC_MAXIMUM_SIZE";
    /**
     * 0：全量展示，1：部分展示
     */
    public static final String DOC_FOLDER_TREE_TYPE_ALL = "0";


    /**
     * 0:未完成,1:完成，2:异常
     */
    public static final Integer DEL_STORAGE_PENDING = 0;
    public static final Integer DEL_STORAGE_COMPLETE = 1;
    public static final Integer DEL_STORAGE_ERROR = 2;

    /**
     * 0:文档库名称未重名
     */
    public static final Long DOC_HOUSE_EQNAME = 0L;

    /**
     * 0:团队名称未重名
     */
    public static final Long DOC_TEAM_EQNAME = 0L;


    /**
     * 公告状态:   0:未公开  1:公开
     */
    public static final Integer DOC_ANNOUN_CLOSE = 0;
    public static final Integer DOC_ANNOUN_OPEN = 1;

    /**
     * 高级配置:   0:容量上限  1:回收站保留期限 2：企业文件夹视图
     */
    public static final Integer DOC_SYSTEM_CAPACITY = 0;
    public static final Integer DOC_SYSTEM_RECYLESTYLE = 1;
    public static final Integer DOC_SYSTEM_FILVIEW = 2;

    /**
     * 消息通知类型 0待审批提醒 1上架审批结果提醒 2申请审批结果提醒 3分享给我的提醒 4新文档上架提醒
     */
    public static final Integer DOC_MESSAGE_RANGE_UNAPPROVED = 0;
    public static final Integer DOC_MESSAGE_RANGE_PUTONAPPROVED = 1;
    public static final Integer DOC_MESSAGE_RANGE_APPLYAPPROVED = 2;
    public static final Integer DOC_MESSAGE_RANGE_SHAPE = 3;
    public static final Integer DOC_MESSAGE_RANGE_NEWDOCPUTON = 4;

    /**
     * 外链分享 公开，密码
     */
    public static final Integer OUT_PUBLIC = 0;
    public static final Integer OUT_PRIVATE = 1;

    /**
     * 内部分享
     */
    public static final Integer INSIDE = 0;
    public static final Integer OUTSIDE = 1;

    public static final Integer SHAPE_THREE = 0;
    public static final Integer SHAPE_WEEK = 1;
    public static final Integer SHAPE_FOREVER = 2;
    public static final Integer SHAPE_ONE = 3;

    public static final Integer VALID = 0;
    public static final Integer INVALID = 1;

    /**
     * 是否通知
     */
    public static final Integer MSG_YES = 0;
    public static final Integer MSG_NO = 1;

    /**
     * 1:上传，2:上传审核，3:修改，4:下架，5:重新上架 6：删除，7：恢复8:重命名
     */
    public static final Integer FLOW_TYPE_UPLOAD = 1;
    public static final Integer FLOW_TYPE_AUDIT = 2;
    public static final Integer FLOW_TYPE_UPDATE = 3;
    public static final Integer FLOW_TYPE_OFF_SHELF = 4;
    public static final Integer FLOW_TYPE_ON_SHELF = 5;
    public static final Integer FLOW_TYPE_DEL = 6;
    public static final Integer FLOW_TYPE_RESTORE = 7;
    public static final Integer FLOW_TYPE_RENAME = 8;

    public static final String SEARCH_FOLDER_TYPE = "FOLDER";

    public static final String SEARCH_DOCUMENT_TYPE = "DOCUMENT";

}
