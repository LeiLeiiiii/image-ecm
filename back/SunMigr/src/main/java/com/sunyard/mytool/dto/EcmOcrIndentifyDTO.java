package com.sunyard.mytool.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description：文件ORC识别DTO类
 */
@Data
public class EcmOcrIndentifyDTO implements Serializable {


    private Long dtdTypeId;



    private String dtdTypeName;



    private List<EcmFileOcrDetailEsDTO> attr;

}
