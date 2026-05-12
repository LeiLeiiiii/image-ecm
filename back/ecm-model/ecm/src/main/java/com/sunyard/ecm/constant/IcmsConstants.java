package com.sunyard.ecm.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/26 13:49
 * @Desc 影像全局常量配置
 */
public class IcmsConstants {

    public static final String SIGNAL_SCAN = "1";
    public static final String BATCH_SCAN = "0";
    // 通用常量
    public final static Integer ZERO = 0;
    public final static Integer ONE = 1;
    public final static String ZERO_STR = "0";
    public final static String ONE_STR = "1";
    public final static Integer TWO = 2;
    public final static Integer THREE = 3;
    public final static Integer FOUR = 4;
    public final static Integer FIVE = 5;
    public final static Integer SIX = 6;
    public final static Integer SEVERN = 7;
    public final static Integer TEN = 10;
    public final static Integer NINE_NINE_NINE = 9999;
    public final static Integer EIGHT_HUNDRED = 800;
    public final static Float ZERO_POINT_FOVE = 0.5F;
    public final static Float MAXQULITY = 100F;
    public final static Integer HUNDRED = 100;
    public final static Long LONG_ZERO = 0L;

    //文件存储地址
    public final static String NEW_FILE_URL = "storage/deal/createInputStreamResources";
    //文件无水印下载地址
    public final static String DOWNLOAD_FILE_URL = "storage/deal/downloadFile";
    //文件存储地址
    public final static String THUMBNAIL_URL = "storage/deal/getThumbnail";

    //业务操作记录字典表
    public final static Integer ADD_BUSI = 1;
    public final static Integer ADD_FILE = 2;
    public final static Integer ADD_COMMENT = 3;
    public final static Integer MULTIPLEX_FILE = 4;
    //新增资料标记
    public final static Integer ADD_DOC_MARK = 5;
    //删除资料标记
    public final static Integer DELETE_DOC_MARK = 6;
    //编辑业务
    public final static Integer EDIT_BUSI = 7;
    //删除业务
    public final static Integer DELETE_BUSI = 8;
    //修改文件-合并文件
    public final static Integer EDIT_FILE_MERG = 9;
    //修改文件-拆分文件
    public final static Integer EDIT_FILE_SPLIT = 10;
    //提交业务
    public final static Integer SUBMIT_BUSI = 11;
    //查看业务
    public final static Integer QUERY_BUSI = 12;

    //资料节点-未归类
    public final static String UNCLASSIFIED_ID = "Sunyard_@#!_2";
    //资料节点-未归类
    public final static String UNCLASSIFIED = "待归类";

    //资料节点-已删除
    public final static String DELETED_CODE = "Sunyard_@#!_1";
    //资料节点-已删除
    public final static String DELETED = "已删除";

    //影像文件操作
    //编辑
    public final static Integer ROTATE_FILE = 0;
    public final static String ROTATE_FILE_STRING = "编辑";
    //压缩
    public final static Integer ZIP_FILE = 1;
    public final static String ZIP_FILE_STRING = "压缩";
    //重命名
    public final static Integer RENAME_FILE = 2;
    public final static String RENAME_FILE_STRING = "重命名";
    //删除
    public final static Integer DELETE_FILE = 3;
    public final static String DELETE_FILE_STRING = "删除";
    public final static String MERGE_DELETE_FILE_STRING = "合并-删除";
    //恢复
    public final static Integer RESTORE_FILE = 13;
    public final static String RESTORE_FILE_STRING = "恢复";
    //合并
    public final static Integer MERG_FILE = 4;
    public final static String MERG_FILE_STRING = "合并";
    //拆分
    public final static Integer MSPILT_FILE = 5;
    public final static String MSPILT_FILE_STRING = "拆分";
    //新增
    public final static Integer ADD_FILE_OPERATION = 6;
    public final static String ADD_FILE_OPERATION_STRING = "新增";
    //移动
    public final static Integer MOVE_FILE = 7;
    public final static String MOVE_FILE_STRING = "移动";
    //还原
    public final static Integer REVERT_FILE = 8;
    public final static String REVERT_FILE_STRING = "还原到";
    //打印
    public final static Integer PRINT_FILE = 9;
    public final static String PRINT_FILE_STRING = "打印";
    //下载
    public final static Integer DOWNLOAD_FILE = 10;
    public final static String DOWNLOAD_FILE_STRING = "下载";
    //归类
    public final static Integer CLASSIFY_FILE = 11;
    public final static String CLASSIFY_FILE_STRING = "归类";
    //复用
    public final static Integer REPEAT_FILE = 12;
    public final static String REPEAT_FILE_STRING = "复用";
    //替换
    public final static Integer REPLACE_FILE = 13;
    public final static String REPLACE_FILE_STRING = "替换";
    //替换
    public final static Integer REGULARIZE_FILE = 13;
    public final static String REGULARIZE_FILE_STRING = "自动转正";

    //分片大小
    public final static Long SPLIT_SIZE = 5242880L;

    //外部接口数据类型（0-静态树，1-动态树）
    //静态树
    public final static Integer STATIC_TREE = 0;
    //动态树
    public final static Integer DYNAMIC_TREE = 1;

    /**
     * 查重配置全局配置
     */
    public final static Integer GLOBAL_TREE = 2;

    //查询资料节点类型：0：当前资料节点，1：选中资料节点，2：全部资料节点
    public final static Integer GLOBAL_PLAG_CHE_QUERY_CURR = 0;
    public final static Integer GLOBAL_PLAG_CHE_QUERY_ADD = 1;
    public final static Integer GLOBAL_PLAG_CHE_QUERY_ALL = 2;
    //新增修改标识（0-修改，1-新增）
    //修改
    public final static Integer OPERATE_FLAG_UPDATE = 0;
    //新增
    public final static Integer OPERATE_FLAG_ADD = 1;

    //文件单位K
    public final static String FILE_UNIT_K = "K";
    //文件单位M
    public final static String FILE_UNIT_M = "M";
    //文件单位G
    public final static String FILE_UNIT_G = "G";
    //自动生成业务编号前缀
    public final static String AUTO_BUSI_PREFIX = "AUTO_";


    /**
     * 可清理缓存的最大阀值
     */
    public static final String CACHE_CLEAR_FILEMAX_THRESHOLD = "CACHE_CLEAR_FILEMAX_THRESHOLD";
    /**
     * 可清理缓存的天数
     */
    public static final String CACHE_CLEAR_DAY_THRESHOLD = "CACHE_CLEAR_DAY_THRESHOLD";


    /**
     * 移动端影像采集页面地址
     */
    public static final String ECMS_MOBILE_CAPTURE_PATH = "ECMS_MOBILE_CAPTURE_PATH";


    //对外接口自定义返回状态码
    /**
     * 业务不能为空
     */
    public final static Integer BUSI_ADD_FAILED = 512;

    /**
     * 角色信息不存在
     */
    public final static Integer BUSI_ROLE_FAILED = 513;

    /**
     * 资料无上传权限
     */
    public final static Integer DOC_NORIGHT = 601;
    /**
     * 文件重复
     */
    public final static Integer BUSI_FILEREPEAT = 602;
    /**
     * 文件类型不允许
     */
    public final static Integer BUSI_NOFILETYPE = 603;
    /**
     * 数据有误
     */
    public final static Integer DATA_FAILED = 405;

    /**
     * 文件new标签标识(1-有new标签 ，0-没有new标签)
     */
    public final static Integer SIGN_FLAG_ONE = 1;

    //标记标志（默认0，标记1）
    //动态树节点
    public final static Integer DOC_MARK_STATIC_TREE = 0;
    //标记
    public final static Integer DOC_MARK_MARK = 1;


    /**
     * 0:查看页面，1：采集页面
     */
    public final static Integer SHOW_PAGE = 0;
    public final static Integer CAPTURE_PAGE = 1;

    public final static Integer OPERATIONFLAG_ADD = 1;
    public final static Integer OPERATIONFLAG_TREE = 0;

    /**
     * 批注或者评论标识（1-批注 2-评论）
     */
    public final static Integer COMMENT = 1;
    public final static Integer REVIEW = 2;

    /**
     * 是否加密，0：不加密，1：加密
     */
    public final static Integer NO_ENCRYPT = 0;
    public final static Integer YES_ENCRYPT = 1;


    /**
     * 文件格式字典表key
     */
    public final static String FILE_TYPE_DIC = "ECMS_COMMON_FILETYPE";
    /**
     *
     */
    public final static String ECMS_COMMON_FILETYPE_OFFICE = "ECMS_COMMON_FILETYPE_OFFICE";
    public final static String ECMS_COMMON_FILETYPE_IMG = "ECMS_COMMON_FILETYPE_IMG";
    public final static String ECMS_COMMON_FILETYPE_YP = "ECMS_COMMON_FILETYPE_YP";
    public final static String ECMS_COMMON_FILETYPE_SP = "ECMS_COMMON_FILETYPE_SP";
    public final static String ECMS_COMMON_FILETYPE_OTHER = "ECMS_COMMON_FILETYPE_OTHER";


    /**
     * 资料权限，维度类型：0角色维度，1业务多维度
     */
    public final static Integer ROLE_TYPE = 0;
    public final static Integer VOL_TYPE = 1;

    /**
     * 资料权限，维度类型：0角色维度，1业务多维度
     */
    public final static String SPLIT_CONFIG = "混贴拆分配置";
    public final static String OCR_CONFIG = "OCR识别配置";


    //节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除
    public final static Integer TREE_TYPE_APPCODE = 1;
    public final static Integer TREE_TYPE_BUSI = 2;
    public final static Integer TREE_TYPE_DOCCODE = 3;
    public final static Integer TREE_TYPE_DOCMARK = 4;
    public final static Integer TREE_TYPE_UNCLASSIFIED = 5;
    public final static Integer TREE_TYPE_DEL = 6;

    public static final String FILE_UPLOAD_HEADER = "fileUploadSun";


    //统计单位 0天 1月 2年
    public static final Integer WORKSTATISTICS_DAY = 0;
    public static final Integer WORKSTATISTICS_MONTH = 1;
    public static final Integer WORKSTATISTICS_YEAR = 2;

    //#节点类型：1:加锁，2:隐藏
    public static final String NODETYPPE_LOCK = "1";
    public static final String NODETYPPE_NOSHOW = "2";


    //文件展示，0:展示锁，1：展示图标，2：展示缩略图
    public static final Integer FILETYPE_LOCK = 0;
    public static final Integer FILETYPE_ICON = 1;
    public static final Integer FILETYPE_TH = 2;


    //删除状态字段
    public static final String IS_DELETED="isDeleted";
    //状态字段
    public static final String STATE="state";

    //状态字段
    public static final String DOC_LEVEL_FIRST = "0";

    //es检索fvh的字段
    public static final String ES_FVH="attachment.content";

    /**
     * 资源请求接口专用，权限过滤方式，默认全量查询，0：全量，1：根据角色权限查询，2：根据多维度权限查询
     */
    public static final Integer TYPEQUERY_QL=0;
    public static final Integer TYPEQUERY_ROLE=1;
    public static final Integer TYPEQUERY_DWD=2;

    /**
     * 自动分类的字典码
     */
    public static final String DICTIONARY_CODE="ECMS_AUTO_CLASS_IDEN";

    /**
     * 异步任务栏检测中的redis标识
     */
    public static final String DETECTING="detecting";

    /**
     * 异步任务栏检测完毕的redis标识
     */
    public static final String DETECTION_COMPLETE="detectionComplete";




    /**
     * 配置的状态开关
     */
    //配置开启
    public static final Integer STATE_OPEN=1;
    //配置关闭
    public static final Integer STATE_CLOSE=0;

    /**
     * 配置类型
     */
    //转正
    public static final Integer REGULARIZE=1;
    //模糊
    public static final Integer OBSCURE=2;
    //翻拍
    public static final Integer REMAKE=3;
    //查重
    public static final Integer PLAGIARISM=4;
    //自动分类
    public static final Integer AUTOMATIC_CLASSIFICATION=5;
    //反光检测
    public static final Integer REFLECTIVE=6;
    //缺角检测
    public static final Integer MISS_CORNER=7;
    //文本查重
    public static final Integer PLAGIARISM_TEXT=8;


    //检测后不需要进行处理
    public static final String NOT_DEAL="0";

    //文档识别类型
    public static final Integer TYPE_ONE=1;
    //转正检测类型
    public static final Integer TYPE_TWO=2;
    //模糊检测类型(与反光缺角并为质量检测)
    public static final Integer TYPE_THREE=3;
    //重试质量检测三个类型
    public static final Integer TYPE_THREE_ALL=31;
    //查重检测类型
    public static final Integer TYPE_FOUR=4;
    //拆分合并
    public static final Integer TYPE_FIVE=5;
    //翻拍检测类型
    public static final Integer TYPE_SIX=6;
    //es图片内容
    public static final Integer TYPE_SEVEN=7;
    //反光检测
    public static final Integer TYPE_EIGHT=8;
    //缺角检测
    public static final Integer TYPE_NINE=9;
    //文本查重
    public static final Integer TYPE_TEN=10;
    //文件es提取状态
    public static final Integer TYPE_ELEVEN=11;


    //智能化处理类型字符串长度
    public static final Integer LENGTH=11;

    //翻拍检测class_ids为0，表示真实
    public static final Integer ID_AUTHENTIC=0;
    //翻拍检测class_ids为1，表示翻拍
    public static final Integer ID_REMAKE=1;

    public static final String ECM = "ECM";
    /**
     * 存特征并查重
     */
    public static final String AFM_TYPE = "110000";

    //是否是父节点
    public static final Integer IS_PARENT=1;

    /**
     * 异步任务初始状态  新增第11位 表示文件es的状态
     */
    public static final String ASYNC_TASK_STATUS_INIT="00000010000";

    /**
     * 自动归类方式，1:OCR识别归类,2走睿征
     */
    public static final String AUTO_CLASS_METHOD_SUNYARD="1";
    public static final String AUTO_CLASS_METHOD_RZ="2";


    /**
     * 图片类型
     */
    public final static List<String> IMGS = Arrays.asList("JPG", "jpg", "JPEG", "jpeg", "png", "PNG", "psd", "PSD", "bmp", "BMP");

    /**
     * 文档类型
     */
    public final static List<String> DOCS = Arrays.asList("doc", "docx", "DOC", "DOCX","pdf","PDF","xls","XLS","xlsx","XLSX","txt","TXT","ppt","PPT","pptx","PPTX","wps","WPS","ofd","OFD","ini","INI","rtf","RTF","Xml","XML","xml","syd","SYD","html","HTML","xmind","XMIND");



    /**
     * 销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁)
     */
    public static final String DESTROY_TYPE_ZERO_STR="历史业务销毁";
    public static final String DESTROY_TYPE_ONE_STR="历史资料销毁";
    public static final String DESTROY_TYPE_TWO_STR="已删除销毁";

    /**
     * 销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁;3:回收站业务删除;4:已删除节点彻底删除)
     */
    public static final Integer DESTROY_TYPE_ZERO = 0;
    public static final Integer DESTROY_TYPE_ONE = 1;
    public static final Integer DESTROY_TYPE_TWO = 2;
    public static final Integer DESTROY_TYPE_THREE = 3;
    public static final Integer DESTROY_TYPE_FOUR = 4;
    /**
     * 销毁任务状态(0:待审核;1:待销毁;2:审核不通过;3:已销毁;)
     */
    public static final String DESTROY_STATUS_ZERO_STR="待审核";
    public static final String DESTROY_STATUS_ONE_STR="待销毁";
    public static final String DESTROY_STATUS_TWO_STR="审核不通过";
    public static final String DESTROY_STATUS_THREE_STR="已销毁";

    /**
     * 审核意见
     */
    public static final String AUDIT_OPINION_YES="同意销毁";
    public static final String AUDIT_OPINION_NO="拒绝销毁";

    /**
     * 销毁任务状态(0:待审核;1:待销毁;2:审核不通过;3:已销毁;)
     */
    public static final Integer DESTROY_STATUS_ZERO = 0;
    public static final Integer DESTROY_STATUS_ONE = 1;
    public static final Integer DESTROY_STATUS_TWO = 2;
    public static final Integer DESTROY_STATUS_THREE = 3;

    // 基础队列名称常量
    public static final String QUEUE_DOC_OCR = "ecm_task_docOcr";
    public static final String QUEUE_AFM = "ecm_task_afm";
    public static final String QUEUE_OBSCURE = "ecm_task_obscure";
    public static final String QUEUE_REGULARIZE = "ecm_task_regularize";
    public static final String QUEUE_REMAKE = "ecm_task_remake";
    public static final String QUEUE_ES_CONTEXT = "ecm_task_esContext";
    public static final String QUEUE_REFLECTIVE = "ecm_task_reflective";
    public static final String QUEUE_MISS_CORNER = "ecm_task_missCorner";
    public static final String EXCHANGE_ECM_INTELLIGENT="exchange_ecm_intelligent";
    //复合处理队列
    public static final String QUEUE_SPECIAL = "ecm_task_special";
    //ext拓展名
    public static final String EXT_PDF = "pdf";

    /**
     * ECM菜单权限标识
     */

    //查重
    public static final String DUPLICATE_CHECK_STATUS = "configurationDuplicateChecking";
    //转正
    public static final String REGULARIZE_STATUS = "formalizeConfiguration";
    //自动分类
    public static final String AUTOMATIC_CLASSIFICATION_STATUS = "autoClassConfiguration";
    //翻拍
    public static final String REMAKE_STATUS = "remakeConfiguration";
    //质量检测
    public static final String QUALITY_CHECK_STATUS = "qualityInspectionConfig";
    //策略
    public static final String STRATEGY_STATUS = "strategyManage";

    //查重失效时间
    public static final Integer AFM_MQ_DISTIME = 1000 * 60 * 60 * 10;

    //智能检测失效时间
    public static final Integer INTELLIGENT_DETECTION_DISTIME = 1000 * 60 * 60;

    //文件来源
    public static final String FILE_SOURCE_PC = "ecm-pc";
    public static final String FILE_SOURCE_MOBILE = "ecm-mobile";
    public static final String FILE_SOURCE_API_PC = "ecm-api-pc";
    public static final String FILE_SOURCE_API_MOBILE = "ecm-api-mobile";

    //关户状态
    public static final String GHZT = "GHZT";
    public static final String CLOSE_STATE_STR = "已关户";

    //压缩包展示常量, 1压缩包根, 2压缩包目录, 3压缩包文件
    public final static Integer TREE_TYPE_ARCHIVE_ROOT = 1;
    public final static Integer TREE_TYPE_ARCHIVE_DIR = 2;
    public final static Integer TREE_TYPE_ARCHIVE_FILE = 3;

    //AI桥接常量
    public static final String BUSINESS_PROCESS = "BusinessProcess";
    public static final String BUSINESS_TYPE = "BusinessType";

    public static final String RIGHT_CHECK = "RightCheck";

}
