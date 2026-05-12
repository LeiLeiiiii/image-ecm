package com.sunyard.ecm.dto.ecm;

import lombok.Data;

import java.io.Serializable;

/**
 * @author： zyl
 * @create： 2023/6/13 9:00
 * @Description： 文件问属性DTO类
 */
@Data
public class EcmFileAutoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long busiId;
    private String dtdCode;
    private Long docId;
    private String docCode;
    private String docName;
    private String attrCode;
    private String attrName;
    private Long dtdAttrId;
    private Long dtdTypeId;
    private String dtdName;
    private String regex;

}
