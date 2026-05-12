package com.sunyard.ecm.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author scm
 * @since 2023/8/21 16:55
 * @desc 文件分片DTO
 */
@Data
@ToString
@Accessors(chain = true)
public class EcmSplitFileDTO {
    /**
     * 文件唯一标识(MD5)
     */
    @NotBlank(message = "文件标识不能为空")
    private String identifier;

    /**
     *
     */
    @NotBlank(message = "处理后的文件标识不能为空")
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
    @NotBlank(message = "文件名称不能为空")
    private String fileName;


    /**
     * 存储设备id
     */
    @NotNull(message = "存储设备id不能为空")
    private Long equipmentId;

    /**
     * 业务批次号
     */
//    @NotBlank(message = "业务批次号不能为空")
    private String busiBatchNo;
}
