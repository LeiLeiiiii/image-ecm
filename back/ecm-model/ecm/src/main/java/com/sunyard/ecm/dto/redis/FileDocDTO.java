package com.sunyard.ecm.dto.redis;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 文件资料DTO类
 */
@Data
public class FileDocDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件id")
    private Long fileId;
    @ApiModelProperty(value = "节点代码")
    private String docCode;

    @ApiModelProperty(value = "节点名称")
    private String docName;

}
