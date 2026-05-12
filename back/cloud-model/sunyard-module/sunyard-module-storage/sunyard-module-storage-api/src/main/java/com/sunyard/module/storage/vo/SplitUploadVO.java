package com.sunyard.module.storage.vo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author： zyl
 * @create： 2023/4/24 17:10
 */
@Data
@ToString
@Accessors(chain = true)
public class SplitUploadVO {
    /**
     * 文件唯一标识(MD5)
     */
    @NotBlank(message = "文件标识不能为空")
    private String identifier;

    /**
     * 源文件文件标识
     */
    @NotBlank(message = "源文件文件标识不能为空")
    private String sourceFileMd5;

    /**
     * 文件大小（byte）
     */
    @NotNull(message = "文件大小不能为空")
    private Long totalSize;

    /**
     * 分片大小（byte）
     */
    @NotNull(message = "分片大小不能为空")
    private Long chunkSize;

    /**
     * 文件名称
     */
    private String fileName;


    /**
     * 分片编号
     */
    private Integer partNumber;
    /**
     * 区分移动端和pc端，移动端传值为0，pc端传值为1
     */
    private String type;
    /**
     * 业务批次号
     */
    @NotBlank(message = "业务批次号不能为空")
    private String busiBatchNo;

    /**
     * 存储设备id
     */
    @NotNull(message = "存储设备id不能为空")
    private Long equipmentId;

    /**
     * 是否加密0否 1是
     */
    private Integer isEncrypt;

    /**
     * 文件id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 是否是对外接口
     */
    private Boolean isOpen;

    /**
     * 文件来源
     */
    @NotNull(message = "文件来源不能为空")
    private String fileSource;

}
