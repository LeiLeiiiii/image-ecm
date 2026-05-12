package com.sunyard.mytool.service.edm.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.DocBsDocumentUser;
import com.sunyard.mytool.mapper.db.edm.DocBsDocumentUserMapper;
import com.sunyard.mytool.service.edm.DocBsDocumentUserService;
import org.springframework.stereotype.Service;

@Service
public class DocBsDocumentUserServiceImpl extends ServiceImpl<DocBsDocumentUserMapper, DocBsDocumentUser> implements DocBsDocumentUserService {
}
