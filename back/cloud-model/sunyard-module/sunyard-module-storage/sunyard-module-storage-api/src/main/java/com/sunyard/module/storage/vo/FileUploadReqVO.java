package com.sunyard.module.storage.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 *
 * @author PJW
 */
@Data
public class FileUploadReqVO {
    /**
     * 文件附件
     */
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    /**
     * 文件附件
     */
    private String path;

}
