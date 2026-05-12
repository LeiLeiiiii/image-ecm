package com.sunyard.ecm.dto.ecm;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 * @author： zyl
 * @create： 2023/5/29 14:04
 * @Description：文件ORC识别DTO类
 */
@Document(indexName = "EcmFileOcrDetailEsDTO")
@Data
public class EcmFileOcrDetailEsDTO implements Serializable {

    private String label;
    private String value;
    private Long dtdTypeId;
    private Long id;
    private String regex;
}
