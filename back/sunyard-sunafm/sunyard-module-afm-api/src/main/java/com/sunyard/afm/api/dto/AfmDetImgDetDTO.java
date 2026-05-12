package com.sunyard.afm.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * 在线检测-图像查重结果返参
 *
 * @author P-JWei
 * @date 2024/3/11 14:48:33
 * @title
 * @description
 */
@Data
public class AfmDetImgDetDTO implements Serializable {
    /**
     * 文件id
     */
    private Long exifId;
    /**
     * 文件id
     */
    private Long noteId;
    /**
     * 排除的list
     */
    private List<String> exList;
    /**
     * 文件md5
     */
    private String fileIndex;
    /**
     * 文件名
     */
    private String fileName;

    /**
     * 向量集合所在位置名称
     */
    private String collectionName;

    /**
     * md5
     */
    private String fileMd5;

    /**
     * 文件url
     */
    private String fileUrl;

    /**
     * 文件url
     */
    private String base64;


    /**
     * 迁移程序-批量
     */
    private MultipartFile file;
    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 业务类型
     */
    private String businessType;
    /**
     * 业务类型
     */
    private String businessTypeCode;
    /**
     * 业务类型中文名
     */
    private String businessTypeName;

    /**
     * 业务索引（主索引）
     */
    private String businessIndex;
    /**
     * 资料类型
     */
    private String materialType;
    /**
     * 资料类型
     */
    private String materialTypeCode;

    /**
     * 资料类型中文名
     */
    private String materialTypeName;

    /**
     * 上传人登录名
     */
    private String uploadUserCode;

    /**
     * 上传人（姓名）
     */
    private String uploadUserName;

    /**
     * 上传机构
     */
    private String uploadOrg;

    /**
     * 文件元数据（json格式）
     */
    private String fileExif;

    /**
     * 文件查询条件
     */
    private String queryExpr;

    /**
     * 查重结果回调地址
     */
    private String backUrl;

    /**
     * 检测方式
     * 第一位～第二位：是否查询查重
     * 01：仅对传入文件做查重
     * 10：仅对传入文件做特征保存
     * 11：对传入文件做查重并且保存特征
     * 第三位：是否查询篡改结果，0否，1是
     * 第四位～第七位：
     * 000:都不做检测
     * 100:做验真
     * 010做查重
     * 001:做连续性
     */
    private String invoiceType;

    /**
     * token
     */
    private String fileToken;

    /**
     * 文件数量限制
     */
    private Integer fileLimit;

    /**
     * 相似度阈值
     */
    private Double fileSimilarity;

    /**
     * 是否进行二次算法opencv
     */
    private Integer isOpencvCheck;

    /**
     * 几个线程并发处理
     */
    private Integer threadNum;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 几个线程并发处理
     */
    private Long serverId;

    /**
     * 文本查重内容
     */
    private String fileText;
}
