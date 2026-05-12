package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/29 14:04
 * @Description：文件ORC识别DTO类
 */
@Data
public class EcmOcrIndentifyDTO implements Serializable {

    @ApiModelProperty(value = "OCR识别出的信息")
    private Long dtdTypeId;


    @ApiModelProperty(value = "OCR识别出的信息")
    private String dtdTypeName;


    @ApiModelProperty(value = "OCR识别出的信息")
    private List<EcmFileOcrDetailEsDTO> attr;

}
