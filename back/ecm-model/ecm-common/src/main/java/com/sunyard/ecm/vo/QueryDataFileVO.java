package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zyl
 * @since 2023/8/1 17:36
 * @Description 文件查询信息VO
 */
@Data
public class QueryDataFileVO implements Serializable {
//
//    /***文件EXIF**/
//    private HashMap fileExif;
//
//    /***文件历史版本**/
//    private List<EcmFileHistory> fileHistories;

//    /***文件批注数量**/
//    private Integer fileCommentCount;
    /**
     * value = "文件扩展名
     **/
    private String ext;
    private String docCode;
    //    private String fileSize;
//    private String fileMd5;
    private String fileId;
    private Double fileSort;
//    private String fileName;
    /***文件全路径**/
    private String fileFullPath;

    /***源文件下载路径**/
    private String sourceFileDownloadPath;

    private String FileName;

    /**
     * 查重标识
     * 0:查重中，1:重复，2:不重复,3:未查重
     */
    private Integer dupImgState;

    private Integer dupTextState;

//    /***缓存中的地址**/
//    private String fileFullPathCache;

    /***缓存中的缩略图地址**/
//    private String fileFullPathCacheThumbnail;

//    /***权限**/
//    private EcmDocrightDefDTO docRight;

//    /***0:展示锁，1：展示图标，2：展示缩略图**/
//    private Integer showType;
//
//    /***计算后文件大小**/
//    private String fileSize;
//
//    /***文件单位**/
//    private String fileUnit;
    // 创建者名称
    private String createUserName;
    // 创建时间
    private Date createTime;
    // 修改者名称
    private String updateUserName;
    // 更新时间
    private Date updateTime;
}
