package com.sunyard.mytool.dto;

import com.sunyard.mytool.entity.ecm.EcmDtdDef;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @Description：文件ORC识别DTO类
 */
@Document(indexName = "ecmfileinfo")
@Data
public class EcmFileOcrInfoEsDTO implements Serializable {


    private List<EcmOcrIndentifyDTO> ocrIdentifyInfo;

    private List<EcmDtdDef> dtdTypeName;

    private String identifyType;

    private String exif;
}
