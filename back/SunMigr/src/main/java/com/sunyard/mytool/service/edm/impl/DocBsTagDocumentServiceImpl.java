package com.sunyard.mytool.service.edm.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.DocBsTagDocument;
import com.sunyard.mytool.mapper.db.edm.DocBsTagDocumentMapper;
import com.sunyard.mytool.service.edm.DocBsTagDocumentService;
import org.springframework.stereotype.Service;

@Service
public class DocBsTagDocumentServiceImpl extends ServiceImpl<DocBsTagDocumentMapper, DocBsTagDocument> implements DocBsTagDocumentService {
}
