package com.sunyard.ecm.constant;

/**
 * @author scm
 * @since 2023/8/9 11:06
 * @desc sdk常量配置类
 */
public class ApiConstants {
    /**
     * 服务名
     * <p>
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "ecm-service";

    public static final String VERSION = "1.0.0";

    public static final Integer IS_PACK = 1;

    public static final String SIGNAL_SCAN = "1";
    public static final String BATCH_SCAN = "0";
    // 是否是查看页面，0：查看，1：采集
    public static final Integer ONLY_SHOW = 0;
    public static final Integer HAVE_CAPTURE = 1;

    /**
     * 静态树
     */
    public final static String STATIC_TREE = "0";
    public static final String BASEURI = "/web-api";
    public static final String ECMURI = "/ecm";
    public static final String STORAGEURI = "/storage";

    public static final String REQUEST_TOKENSTR = "token";
    public static final String RETURN_RESPONSESTR = "responseStr";

    public final static int AGAIN_NUM = 0;
    public final static String OSS_FAIL_SIZE = "600001";

    /**
     * 是否压缩(1-压缩，0-不压缩 默认0)
     */
    public static final Integer COMPRESS = 1;
    public static final Integer NOCOMPRESS = 0;


//
//    public static final String baseUri = "";
//    public static final String ecmUri = "";
//    public static final String storageUri = "";


    //调阅、扫描
    public static final String ACCESSECM = BASEURI + ECMURI +"/api/capture/scanOrUpdateEcm";

    //移动端调阅、扫描
    public static final String ACCESSECM_MOBILE = BASEURI + ECMURI +"/api/mobile/capture/scanOrUpdateEcmMobile";

    //复制
    public static final String BUSIDOCDUPLICATE = BASEURI + ECMURI +"/api/busiCopy/busiDocDuplicate";

    //资源获取
    public static final String QUERYECM = BASEURI + ECMURI +"/api/query/queryEcm";

    //删除
    public static final String DELETEFILE = BASEURI + ECMURI +"/api/busiDelete/deleteFileByBusiOrDoc";

    //上传-校验文件格式和md5
    public static final String CHECKFILE = BASEURI + ECMURI +"/api/upload/checkFile";

    //上传-获取上传进度
    public static final String SPLITUPLOAD = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/taskInfo";

    //上传-获取上传信息（上传进度、预签名上传url、文件信息）
    public static final String SPLITUPLOADINFO = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/getUploadInfo";

    //上传-创建一个上传任务
    public static final String INITASK = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/initTask";

    //上传-获取每个分片的预签名上传地址
    public static final String PRESIGNUPLOADURL = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/preSignUploadUrl";

    //上传-合并分片
    public static final String MERGE = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/merge";

    //上传-秒传 (OSS中从一个存储桶上传到另一个存储桶)
    public static final String SECONDUPLOAD = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/secondUpload";

    //关联业务-将文件信息存入影像系统（前端页面接口）
    public static final String INSERTFILEINFO = BASEURI + ECMURI +"/api/pc/capture/top/insertFileInfo";

    //关联业务-将文件信息存入影像系统（后端接口）
//    public static final String INSERTFILEINFOBACK = BASEURI + ECMURI +"/api/upload/insertFileInfoBack";
    public static final String INSERTFILEINFOBACK = BASEURI + ECMURI +"/api/upload/insertFileListInfo";

    //根据业务 + 资料节点，查询文件信息(文件下载)
    public static final String FILELIST = BASEURI + ECMURI +"/api/download/getFileInfoByBusiOrDoc";

    //获取业务类型信息（业务类型压缩信息）
    public static final String APPTYPEINFO = BASEURI + ECMURI +"/api/upload/getAppTypeInfo";

    //业务属性回写列表
    public static final String ATTRLIST = BASEURI + ECMURI +"/api/setBusiAttr/setBusiAttr";

    //批扫页面跳转
    public static final String BATCHSCAN = BASEURI + ECMURI +"/api/operate/capture/batchScan";

    //单扫页面跳转
    public static final String SINGLSCAN = BASEURI + ECMURI +"/api/operate/capture/signalScan";

    //文件下载
    public static final String FILEDOWNLOAD = BASEURI + STORAGEURI +"/api/storage/deal/createInputStreamResourcesCacheApi";

    //上传-后台整体上传
    public static final String FILEUPLOAD = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/useS3Upload";

    //上传-分片上传-走鉴权
//    public static final String UPLOADSPLITOPEN = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/uploadSplitOpen";
    public static final String UPLOADSPLITS = BASEURI + STORAGEURI +"/api/storage/oss/splitUpload/uploadSplits";

    //文件分段下载
    public static final String SHARDINGDOWNLOAD = BASEURI + STORAGEURI +"/api/storage/deal/shardingDownFile";

    //文本查重
    public static final String TEXTANTIFRAUDDET= BASEURI + ECMURI +"/api/imageDup/extractFileTextDup";

    //业务属性回写列表
    public static final String EDITBUSISTATUS = BASEURI + ECMURI +"/api/editBusi/busiDeblock";
    //业务属性回写列表
    public static final String QUERYBUSI = BASEURI + ECMURI +"/api/query/queryBusi";

    //影像业务校验接口
    public static final String ECMBUSIINFOCHECK = BASEURI + ECMURI +"/api/check/ecmBusiInfoCheck";

    //影像资料统计接口
    public static final String STATISTICSDOCFILENUM = BASEURI + ECMURI +"/api/statistics/statisticsDocFileNum";

    //复制
    public static final String BUSIARCHIVE = BASEURI + ECMURI +"/api/busiCopy/busiArchive";
}
