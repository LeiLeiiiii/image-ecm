package com.sunyard.module.storage.dto;


import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * S3 Base64分片上传请求DTO
 */
@Data
@ToString
@Accessors(chain = true)
public class S3Base64UploadDTO {

    private String file;


    private Long stEquipmentId;

    private String fileSource;

    private String fileName;

    private String md5;

    private Integer isEncrypt;

    private Boolean isFlat;
}