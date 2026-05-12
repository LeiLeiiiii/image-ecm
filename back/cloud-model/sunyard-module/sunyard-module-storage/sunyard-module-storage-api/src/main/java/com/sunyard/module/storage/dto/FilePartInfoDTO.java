package com.sunyard.module.storage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 文件分片信息
 * @author zyl
 * @Description
 * @since 2024/4/8 18:08
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FilePartInfoDTO {

    /**
     * 分片 ETag（分片数据的MD5值）
     */
    private String eTag;

    /**
     * 分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000
     */
    private Integer partNumber;

    /**
     * 分片大小，单位字节
     */
    private Long partSize;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 加密长度
     */
    private Integer encryptLength;
}
