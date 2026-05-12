package com.sunyard.mytool.constant;


import com.sunyard.mytool.until.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RedisKeyConstant {
    public static String environmentId ="default";
    private static final String MIGRATING_LIST_KEY = "migratingList";
    private static final String MIGRATE_QUERY_START_DATE_KEY = "migrateQueryStartDate";
    private static final String APP_ATTR_KEY = "appATTR";
    private static final String DOC_TYPE_KEY = "docType";
    private static final String BRANCH_INFO_KEY = "branchInfo";
    private static final String STORAGE_CONFIG_KEY = "storageConfig";
    private static final String CLOUD_REL_KEY = "cloudRel";
    private static final String APP_DOC_REL_KEY = "appDocRel";
    private static final String APP_TYPE_KEY = "appType";
    private static final String APP_EXTEND_DEF_KEY = "extendDef";
    private static final String QUERY_TIME_LOCK_KEY = "queryTimeLock";
    private static final String REFRESH_QUERY_TIME_LOCK_KEY = "refreshQueryTimeLock";
    private static final String MIGRATE_BATCH_LOCK_KEY = "migrateBatchLock";


    /**
     * redis key
     */
    public final static String REDIS_APP_DEF = "APP_DEF_KEY:";
    public final static String REDIS_DOC_DEF = "DOC_DEF_KEY:";
    public final static String REDIS_APP_ATTR = "APP_ATTR_KEY:";
    public final static String REDIS_SYS_LABEL = "SYS_LABEL_KEY:";

    private static final String GET_BATCH_TEMP_KEY = "getBatchTempLock";

    //文件 缓存key
    private static final String BUSIFILE = "BUSIFILE";
    //业务缓存key
    private static final String BUSI_ALL_PREFIX = "BUSI_ALL_PREFIX";


    //机构key
    private static final String SYS_INST = "SYSINST";

    //用户key
    private static final String SYS_USER = "SYSUSER";




    /**
     *
     * 获取中间表取数锁标识  getBatchTempLock + "." + appCode
     * @param appCode
     * @return
     */
    public static String getBatchTempLock(String appCode) {
        return   GET_BATCH_TEMP_KEY + "." + appCode;
    }

    /**
     *
     * 获取用户锁标识  SYS_USER + ":" + loginName
     */
    public static String getSysUser(String loginName) {
        return   SYS_USER + ":" + loginName;
    }

    /**
     *
     * 获取机构锁标识  SYSINST + ":" + instNo
     */
    public static String getSysInst(String instNo) {
        return   SYS_INST + ":" + instNo;
    }

    /**
     *
     * 获取redis 文件 key  getBatchTempLock + ":" + busiId
     * @param busiId
     * @return
     */
    public static String getBusiFile(Long busiId) {
        return  BUSIFILE + ":" + busiId;
    }

    /**
     *
     * 获取redis 业务 key  BUSI_ALL_PREFIX + ":" + busiId
     * @param busiId
     * @return
     */
    public static String getBusiAllPrefix(Long busiId) {
        return  BUSI_ALL_PREFIX + ":" + busiId;
    }


    public static String getMigratingListKey() {
        return environmentId +"."+MIGRATING_LIST_KEY;
    }

    public static String getMigrateBatchLockKey(String appCode,String busiNo) {

        return environmentId +"."+MIGRATE_BATCH_LOCK_KEY + "." + appCode+"."+busiNo;
    }
    public static String getAppTypeKey(String appCode) {

        return environmentId +"."+APP_TYPE_KEY + "." + appCode;
    }
    public static String getQueryTimeLockKey(String appCode, Date dateAfter, Date dateBefore) {
        SimpleDateFormat sdf4=new SimpleDateFormat(DateUtil.sdf4);
        String dateAfterString= DateUtil.getDateString(sdf4,dateAfter);
        String dateBeforeString= DateUtil.getDateString(sdf4,dateBefore);
        return environmentId +"."+ QUERY_TIME_LOCK_KEY + "." + appCode+"."+dateAfterString+"."+dateBeforeString;
    }

    public static String getRefreshQueryTimeLockKey(String appCode) {
        return environmentId +"."+ REFRESH_QUERY_TIME_LOCK_KEY + "." + appCode;
    }
    public static String getAppAttrKey(String appCode) {
        return environmentId +"."+APP_ATTR_KEY + "." + appCode;
    }
    public static String getExtDefKey(String extId) {
        return environmentId +"."+APP_EXTEND_DEF_KEY + "." + extId;
    }

    public static String getDocTypeKey(String appCode) {
        return environmentId +"."+DOC_TYPE_KEY + "." + appCode;
    }
    public static String getAppDocRelKey(String appCode) {
        return environmentId +"."+APP_DOC_REL_KEY + "." + appCode;
    }

    public static String getMigrateQueryStartDateKey(String appCode) {
        return environmentId +"."+ MIGRATE_QUERY_START_DATE_KEY + "." + appCode;
    }


    public static String getBranchInfoKey(String branchCode) {
        return environmentId +"."+BRANCH_INFO_KEY + "_" + branchCode;
    }

    public static String getStorageConfigKey(String sysFlag) {
        return environmentId +"."+STORAGE_CONFIG_KEY + "." + sysFlag;
    }

    public static String getCloudRelKey(String appCode,String bucket) {
        return environmentId +"."+CLOUD_REL_KEY + "." + appCode+"."+bucket;
    }

}
