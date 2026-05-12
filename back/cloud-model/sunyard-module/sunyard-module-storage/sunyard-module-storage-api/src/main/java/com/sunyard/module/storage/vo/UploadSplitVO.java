package com.sunyard.module.storage.vo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.InputStream;

/**
 * @author zyl
 * @Description
 * @since 2024/4/8 17:49
 */
@Data
@ToString
@Accessors(chain = true)
public class UploadSplitVO {
    /**
     * 文件流
     */
    private InputStream inputStream;

    /**
     * 分片文件子节数组
     */
    private byte[] bytes;

    /**
     * 第几分片
     */
    private Integer partNumber;

    /**
     * 存储设备id
     */
    private Long equipmentId;

    /**
     * 是否加密 （0否 1是）
     */
    private Integer isEncrypt;

    /**
     * 加密密钥
     */
    private String encryptKey;

    /**
     * 加密类型
     */
    private Integer encryptType;

    /**
     * 加密标识符
     */
    private String encryptIndex;

    /**
     * 加密标识符
     */
    private String identifier;
    /***************本地所需参数  ********************/

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 业务批次号
     */
    @NotBlank(message = "业务批次号不能为空")
    private String busiBatchNo;

    /***************OSS所需参数  ********************/
    /**
     * 存储桶内路径
     */
    private String key;

    /**
     * 上传id
     */
    private String uploadId;

    /**
     * 分片大小
     */
    private Long partSize;

}
