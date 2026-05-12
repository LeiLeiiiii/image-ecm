package com.sunyard.mytool.service.edm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.DocBsDocument;

public interface DocBsDocumentService extends IService<DocBsDocument> {

    /**
     * 查询文件夹
     * @param folderId 父级id
     * @param folderName 文件夹名称
     */
    DocBsDocument getDocByConditions(Long folderId, String folderName);
}
