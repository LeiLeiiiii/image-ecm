package com.sunyard.ecm.dto.es;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zyl
 * @since 2023/8/9 17:34
 * @Description ES文件下信息DTO
 */
@Data
@Accessors(chain = true)
public class EcmFileInfoEsDTO extends EcmBusiInfoEsDTO implements Serializable {

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "新文件id")
    private Long newFileId;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "资料类型id")
    private String docCode;

    @ApiModelProperty(value = "资料类型名称")
    private String docTypeName;

    @ApiModelProperty(value = "单证类型名称")
    private String dtdTypeName;

    @ApiModelProperty(value = "单证类型的属性Map")
    private Map<String , String> dtdAttrMap;

    @ApiModelProperty(value = "单证类型的属性Map")
    private String dtdAttrs;

    @ApiModelProperty(value = "文件路径水印")
    private String fileFullPath;

    @ApiModelProperty(value = "文件路径")
    private String fileFullPathCache;

    @ApiModelProperty(value = "文件后缀")
    private String format;

    @ApiModelProperty(value = "文件EXIF")
    private HashMap fileExif;

    @ApiModelProperty(value = "文件标签")
    private List<String> fileLabel;

    @ApiModelProperty(value = "文件大小")
    private Long newFileSize;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

}
