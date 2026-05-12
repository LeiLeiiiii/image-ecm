package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zyl
 * @since 2023/9/27 15:58
 * @Description 文件OCR信息存入ES的DTO类
 */
@Data
public class EcmFileOcrInfoEsExtendDTO {

    private List<EcmFileOcrInfoEsDTO> ecmFileOcrInfoEsDTOList;

    @ApiModelProperty(value = "是否OCR识别过(0:没有识别过；1：识别过成功 ；2：识别过但是没识别出内容)")
    private String identifyType;
}
