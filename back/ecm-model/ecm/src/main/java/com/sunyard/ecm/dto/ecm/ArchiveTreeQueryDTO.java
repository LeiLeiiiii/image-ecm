package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 压缩包内文件树查询入参
 */
@Data
@ApiModel(value = "压缩包内文件树查询入参", description = "压缩包内文件树查询入参")
public class ArchiveTreeQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件id", required = true)
    private Long fileId;
}
