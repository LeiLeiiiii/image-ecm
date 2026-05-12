package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.po.EcmDtdDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/29 14:04
 * @Description：文件ORC识别DTO类
 */
@Document(indexName = "ecmfileinfo")
@Data
public class EcmFileOcrInfoEsDTO implements Serializable {

    @ApiModelProperty(value = "OCR识别出的信息")
    private List<EcmOcrIndentifyDTO> ocrIdentifyInfo;
    @ApiModelProperty(value = "OCR识别出的信息")
    private List<EcmDtdDef> dtdTypeName;
    @ApiModelProperty(value = "是否OCR识别过(0:没有识别过；1：识别过)")
    private String identifyType;
    @ApiModelProperty(value = "EXIF信息")
    private String exif;
}
