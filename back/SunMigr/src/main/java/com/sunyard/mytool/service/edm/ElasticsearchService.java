package com.sunyard.mytool.service.edm;

import com.sunyard.mytool.dto.DocBsDocumentDTO;
import com.sunyard.mytool.dto.es.EcmBusiInfoEsDTO;
import com.sunyard.mytool.dto.es.EcmFileInfoEsDTO;

import java.util.concurrent.Future;

public interface ElasticsearchService {

    /**
     * ES添加数据
     */
    void addFullTextPath(DocBsDocumentDTO docBsDocumentDTO);

    /**
     * 添加es业务信息
     */
    void addEsBusiInfo(EcmBusiInfoEsDTO ecmBusiInfoEsDTO, Long userId);

    /**
     * 添加es文件信息
     */
    Future<String> addEsFileInfo(EcmFileInfoEsDTO ecmFileInfoEsDTO, Long userId);
}
