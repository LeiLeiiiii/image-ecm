package com.sunyard.mytool.service.edm.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.DocBsDocumentTree;
import com.sunyard.mytool.mapper.db.edm.DocBsDocumentTreeMapper;
import com.sunyard.mytool.service.edm.DocBsDocumentTreeService;
import org.springframework.stereotype.Service;

@Service
public class DocBsDocumentTreeServiceImpl extends ServiceImpl<DocBsDocumentTreeMapper, DocBsDocumentTree> implements DocBsDocumentTreeService {
}
