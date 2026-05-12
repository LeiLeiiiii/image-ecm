package com.sunyard.module.storage.vo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author： zyl
 * @create： 2023/4/24 17:10
 */
@Data
@ToString
@Accessors(chain = true)
public class SplitUploadBigFileVo {
    /**
     * 文件唯一标识(MD5)
     */
    private String identifier;

    /**
     * 分片号
     */
    private Integer partNumber;
    /**
     * 区分移动端和pc端，移动端传值为0，pc端不传
     */
    private String type;

    private  Boolean isFlat;

    /**
     *是否加密0否 1是
     */
    private  Integer isEncrypt;

    /**
     * 文件id
     */
    private  Long id;

    /**
     * 设备id
     */
    private Long equipmentId;
}
