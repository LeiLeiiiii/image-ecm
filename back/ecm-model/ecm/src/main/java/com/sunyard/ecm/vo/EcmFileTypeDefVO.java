package com.sunyard.ecm.vo;

import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author XQZ
 * @date 2023/4/20
 * @describe 文件类型定义VO
 */
@Data
public class EcmFileTypeDefVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "文件类型id")
    private Long fileTypeId;

    @ApiModelProperty(value = "文件类型名称")
    private String fileTypeName;

    @ApiModelProperty(value = "文件类型")
    private String fileTypeCode;

    @ApiModelProperty(value = "最大上传文件(0：不限制（单位MB）)")
    private Long uploadSize;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "最近修改人")
    private String updateUser;

}
