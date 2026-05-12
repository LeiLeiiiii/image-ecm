package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmFileOcrInfoEsDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @Description：OCR结果VO
 * @create： 2023/5/29 9:21
 */
@Data
public class OcrResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long newFileId;
    private Long fileId;
    private Long busiId;
    private List<EcmFileOcrInfoEsDTO> ecmFileInfoEsDTO;
    private String number;
}
