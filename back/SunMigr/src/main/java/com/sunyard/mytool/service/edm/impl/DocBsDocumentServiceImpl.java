package com.sunyard.mytool.service.edm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.DocBsDocument;
import com.sunyard.mytool.mapper.db.edm.DocBsDocumentMapper;
import com.sunyard.mytool.service.edm.DocBsDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@DS("EDMDataSource")
public class DocBsDocumentServiceImpl extends ServiceImpl<DocBsDocumentMapper, DocBsDocument> implements DocBsDocumentService {

    @Autowired
    private DocBsDocumentMapper docBsDocumentMapper;
    @Override
    public DocBsDocument getDocByConditions(Long folderId, String folderName) {

        LambdaQueryWrapper<DocBsDocument> q = new LambdaQueryWrapper<>();
        //父id空值处理
        if (folderId != null) {
            q.eq(DocBsDocument::getFolderId, folderId);
        } else {
            q.isNull(DocBsDocument::getFolderId);
        }
        q.eq(DocBsDocument::getDocName, folderName)
                .eq(DocBsDocument::getType, 0);
        return docBsDocumentMapper.selectOne(q);
    }
}
