package com.sunyard.mytool.dto.es;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description ES文件下信息DTO
 */
@Data
@Accessors(chain = true)
public class EcmFileInfoEsDTO extends EcmBusiInfoEsDTO implements Serializable {

    private Long fileId;

    private Long newFileId;

    private String fileName;

    private String docCode;

    private String docTypeName;

    private String dtdTypeName;

    private Map<String , String> dtdAttrMap;

    private String dtdAttrs;

    private String fileFullPath;

    private String fileFullPathCache;

    private String format;

    private HashMap fileExif;

    private List<String> fileLabel;

    private Long newFileSize;

    private String orgCode;

    private Long userId;

    private String filePath;
}
