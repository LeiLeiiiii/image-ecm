package com.sunyard.ecm.dto.split;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author： zyl
 * @create： 2023/4/24 17:10
 * @desc: 大文件分片DTO
 */
@Data
@ToString
@Accessors(chain = true)
public class SplitUploadBigFileDTO {
    /**
     * 文件唯一标识(MD5)
     */
    @NotBlank(message = "文件标识不能为空")
    private String identifier;

    /**
     * 文件名称
     */
//    @NotBlank(message = "文件名称不能为空")
    private Integer partNumber;
    /**
     * 文件名称
     */
//    @NotBlank(message = "文件名称不能为空")
    private String type;

//    @NotBlank(message = "文件名称不能为空")
    private  Boolean isFlat;

    /**
     * 是否加密0否 1是
     */
    private Integer isEncrypt;
}
