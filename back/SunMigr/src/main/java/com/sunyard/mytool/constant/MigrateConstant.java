package com.sunyard.mytool.constant;



import com.sunyard.mytool.until.UUIDUtil;

import java.util.HashMap;
import java.util.Map;

public class MigrateConstant {
    //------------------------迁移状态-------------------------
    //待迁移
    public static final int MIGRATE_WAITING = 0;
    //迁移中
    public static final int MIGRATE_MIGRATING = 1;
    //迁移成功
    public static final int MIGRATE_SUCCESS = 2;
    //迁移失败
    public static final int MIGRATE_FAIL = -1;

    public static final String randomId = UUIDUtil.generateUUID();


    //本地存储
    public static final Integer LOCAL_STORAGE = 1;
    //云存储
    public static final Integer CLOUD_STORAGE = 2;
    //公共0
    public static final Integer ZERO = 0;

    //文件是否加密 0否 1是 通用
    public static final Integer IS_ENCRYPT = 1;


    //加密方式aes
    public static final String FILE_ENCRYPT_TYPE_AES = "AES";


    //文件是否迁移 0否 1是
    public static final Integer IS_UPLOADFILE = 1;

    //文件是否覆盖 0否 1是
    public static final Integer IS_COVER = 1;

    //资料节点-未归类
    public static final  String UNCLASSIFIED_ID = "Sunyard_@#!_2";

    //资料节点-已删除
    public static final  String DELETED_CODE = "Sunyard_@#!_1";

    //老影像未归类docCode
    public static final  String OLD_UNCLASSIFIED_ID = "UNTYPE";

    //老影像已删除docCode
    public static final  String OLD_DELETED_CODE = "DELETE";

    public static final Map<String, String> EXIF_MAP = new HashMap<>();
    //初始化值
    static {
        EXIF_MAP.put("Create SoftWare", "make");
        EXIF_MAP.put("CaptureTime", "DateTime");
        EXIF_MAP.put("Camera model", "model");
        EXIF_MAP.put("Latitude", "GPSLatitude");
        EXIF_MAP.put("Longitude", "GPSLongitude");
        EXIF_MAP.put("Altitude", "GPSAltitude");
        EXIF_MAP.put("GPSLatitudeRef", "GPSLatitudeRef");
        EXIF_MAP.put("GPSLongitudeRef", "GPSLongitudeRef");
    }

}
