package com.sunyard.mytool.service.edm.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.mytool.dto.DocBsDocumentDTO;
import com.sunyard.mytool.entity.*;
import com.sunyard.mytool.mapper.db.edm.DocBsDocumentMapper;
import com.sunyard.mytool.mapper.db.edm.DocBsDocumentTreeMapper;
import com.sunyard.mytool.mapper.db.edm.DocBsDocumentUserMapper;
import com.sunyard.mytool.mapper.db.edm.DocBsTagDocumentMapper;
import com.sunyard.mytool.mapper.db.st.StFileMapper;
import com.sunyard.mytool.service.edm.ElasticsearchService;
import com.sunyard.mytool.service.edm.NewEDMService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class NewEDMServiceImpl implements NewEDMService {

    @Value("${instId}")
    private Long instId;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private DocBsDocumentMapper docBsDocumentMapper;
    @Autowired
    private DocBsDocumentTreeMapper docBsDocumentTreeMapper;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private StFileMapper stFileMapper;
    @Autowired
    private DocBsTagDocumentMapper docBsTagDocumentMapper;

    @Override
    @DSTransactional
    public void buildMainData(List<DocBsDocument> folderList, DocBsDocumentDTO fileDoc, StFile stFile, DocBsTagDocument docBsTagDocument) {
       long sTime = System.currentTimeMillis();
        try {
            //1.批量写入文件夹
            MybatisBatch<DocBsDocument> docBsDocumentsBatch = new MybatisBatch<>(sqlSessionFactory, folderList);
            MybatisBatch.Method<DocBsDocument> docBsDocumentsMethod = new MybatisBatch.Method<>(DocBsDocumentMapper.class);
            docBsDocumentsBatch.execute(docBsDocumentsMethod.insert());

            //2.文件夹写入闭包表
            for (DocBsDocument document : folderList) {
                handleTree(document, document.getBusId());
            }
            if (fileDoc.getDoc() != null){
                DocBsDocument doc = fileDoc.getDoc();
                //3.写入文件
                docBsDocumentMapper.insert(doc);
                //4.更新文件夹大小
                handleFolderSize(stFile.getSize(), doc.getFolderId());
                //5.写stfile表
                stFileMapper.insert(stFile);
            }
            //历史文件写入标签
            if (docBsTagDocument != null){
                docBsTagDocumentMapper.insert(docBsTagDocument);
            }
            //6.写doc_bs_document_user表
            handleDocUser(folderList,fileDoc);
            log.info("数据库写入完毕耗时: {}", System.currentTimeMillis() - sTime);
        } catch (Exception e) {
            log.error("写数据库表发生异常", e);
            throw new RuntimeException("写数据库表发生异常");
        }
        if (fileDoc.getDoc() != null){
            //7.添加全文检索
            try {
                elasticsearchService.addFullTextPath(fileDoc);
            } catch (Exception e) {
                log.error("添加全文检索发生异常", e);
                throw new RuntimeException("添加全文检索发生异常: "+e.getMessage());
            }
        }

    }


    /**
     * 闭包表的处理
     * 解释: 先把自己写一遍  ,然后查出自己父级目录在闭包表中的的数据(即父级目录的所有父亲),然后将自己作为子,继承父亲所有的闭包关系写入数据库
     */
    private void handleTree(DocBsDocument document, Long busId) {
        //1、自己到自己的数据
        DocBsDocumentTree docBsDocumentTree = new DocBsDocumentTree();
        docBsDocumentTree.setDocId(busId);
        docBsDocumentTree.setFatherId(busId);
        docBsDocumentTree.setCreateTime(new Date());
        docBsDocumentTree.setIsDeleted(0);
        docBsDocumentTreeMapper.insert(docBsDocumentTree);
        if (document.getParentId() != null) {
            //2、自己和祖先的
            List<DocBsDocumentTree> docId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                    .eq(DocBsDocumentTree::getDocId, document.getParentId()));
            if(!org.apache.commons.collections4.CollectionUtils.isEmpty(docId)){

                List<DocBsDocumentTree> docBsDocumentTrees = new ArrayList<>();
                for (DocBsDocumentTree d : docId) {
                    DocBsDocumentTree documentTree = new DocBsDocumentTree();
                    documentTree.setDocId(busId);
                    documentTree.setFatherId(d.getFatherId());
                    documentTree.setCreateTime(new Date());
                    documentTree.setIsDeleted(0);
                    docBsDocumentTrees.add(documentTree);
                }
                MybatisBatch<DocBsDocumentTree> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocumentTrees);
                MybatisBatch.Method<DocBsDocumentTree> docMethod = new MybatisBatch.Method<>(DocBsDocumentTreeMapper.class);
                docBatchs.execute(docMethod.insert());
            }
        }
    }

    /**
     * 计算文件大小
     */
    private void handleFolderSize(Long size, Long folderId) {
        if (folderId == null) {
            return;
        }
        List<DocBsDocumentTree> docId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                .eq(DocBsDocumentTree::getDocId, folderId));
        if (!CollectionUtils.isEmpty(docId)) {
            List<Long> collect1 = docId.stream().map(DocBsDocumentTree::getFatherId).collect(Collectors.toList());
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectBatchIds(collect1);

            for (DocBsDocument docBsDocument : docBsDocuments) {
                long l = docBsDocument.getDocSize() == null ? 0 + size : docBsDocument.getDocSize() + size;
                if (l < 0) {
                    l = 0;
                }
                docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                        //文件夹原本的大小加上需要新增的大小
                        .set(DocBsDocument::getDocSize, l)
                        .eq(DocBsDocument::getBusId, docBsDocument.getBusId()));
            }
        }
    }

    /**
     * 文档权限关系表处理
     */
    private void handleDocUser(List<DocBsDocument> folderList, DocBsDocumentDTO fileDoc) {
        ArrayList<DocBsDocumentUser> docUsers = new ArrayList<>();
        for (DocBsDocument document : folderList) {
            DocBsDocumentUser docBsDocumentUser = new DocBsDocumentUser();
            docBsDocumentUser.setDocId(document.getBusId());
            docBsDocumentUser.setRelId(instId);
            docBsDocumentUser.setType(1);
            docBsDocumentUser.setPermissType(2);
            docBsDocumentUser.setCreateTime(document.getCreateTime());
            docBsDocumentUser.setIsDeleted(0);
            docUsers.add(docBsDocumentUser);
        }
        if (fileDoc.getDoc()!= null) {
            DocBsDocument doc = fileDoc.getDoc();
            DocBsDocumentUser fileDocumentUser = new DocBsDocumentUser();
            fileDocumentUser.setDocId(doc.getBusId());
            fileDocumentUser.setRelId(instId);
            fileDocumentUser.setType(1);
            fileDocumentUser.setPermissType(2);
            fileDocumentUser.setCreateTime(doc.getCreateTime());
            fileDocumentUser.setIsDeleted(0);
            docUsers.add(fileDocumentUser);
        }
        //批量插入
        MybatisBatch<DocBsDocumentUser> docBsDocumentUsersBatch = new MybatisBatch<>(sqlSessionFactory, docUsers);
        MybatisBatch.Method<DocBsDocumentUser> docBsDocumentUsersMethod = new MybatisBatch.Method<>(DocBsDocumentUserMapper.class);
        docBsDocumentUsersBatch.execute(docBsDocumentUsersMethod.insert());

    }
}
