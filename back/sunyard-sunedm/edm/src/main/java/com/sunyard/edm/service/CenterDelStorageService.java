package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.sunyard.edm.dto.DocBsDocumentSearchDTO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.mapper.DocBsDocFlowMapper;
import com.sunyard.edm.mapper.DocBsDocRelMapper;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsDocumentTreeMapper;
import com.sunyard.edm.mapper.DocBsDocumentUserMapper;
import com.sunyard.edm.mapper.DocBsRecentlyDocumentMapper;
import com.sunyard.edm.mapper.DocBsRecycleMapper;
import com.sunyard.edm.mapper.DocBsTagDocumentMapper;
import com.sunyard.edm.mapper.DocBsTaskMapper;
import com.sunyard.edm.po.DocBsDocFlow;
import com.sunyard.edm.po.DocBsDocRel;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsDocumentTree;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocBsRecentlyDocument;
import com.sunyard.edm.po.DocBsTagDocument;
import com.sunyard.edm.po.DocBsTask;
import com.sunyard.edm.vo.DocBsRecycleSearchVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @author raochangmei
 * @date 11.15
 * @Desc 文件删除实现类
 */
@Service
public class CenterDelStorageService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsTaskMapper docBsTaskMapper;
    @Resource
    private DocBsRecycleMapper docBsRecycleMapper;
    @Resource
    private DocBsDocumentTreeMapper docBsDocumentTreeMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocBsDocRelMapper docBsDocRelMapper;
    @Resource
    private DocBsDocFlowMapper docBsDocFlowMapper;
    @Resource
    private DocBsTagDocumentMapper docBsTagDocumentMapper;
    @Resource
    private DocBsDocumentUserMapper docBsDocumentUserMapper;
    @Resource
    private CenterQueryService centerQueryService;
    @Resource
    private DocBsRecentlyDocumentMapper docBsRecentlyDocumentMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private FileStorageApi docDelStorageFeign;
    @Resource
    private CenterCommonService docCommonService;
    /**
     * 调用存储服务删除文件
     */
    @Async("GlobalThreadPool")
    public void delTask() {
        List<DocBsTask> bsTaskList = docBsTaskMapper.selectList(new LambdaQueryWrapper<DocBsTask>()
                .eq(DocBsTask::getTaskType, DocConstants.TASK_DEL_FILE)
                .eq(DocBsTask::getTaskStatus, DocConstants.DEL_STORAGE_PENDING));

        if (!CollectionUtils.isEmpty(bsTaskList)) {
            List<Long> collect = bsTaskList.stream().map(DocBsTask::getRelId).collect(Collectors.toList());

            List<Long> ids = bsTaskList.stream().map(DocBsTask::getId).collect(Collectors.toList());
            Result result = docDelStorageFeign.delBatch(collect);
            if (result.isSucc()) {
                docBsTaskMapper.update(null, new LambdaUpdateWrapper<DocBsTask>()
                        .set(DocBsTask::getTaskStatus, DocConstants.DEL_STORAGE_COMPLETE).in(DocBsTask::getId, ids));
            } else {
                docBsTaskMapper.update(null, new LambdaUpdateWrapper<DocBsTask>()
                        .set(DocBsTask::getTaskStatus, DocConstants.DEL_STORAGE_ERROR)
                        .set(DocBsTask::getErrorMsg, result.getMsg())
                        .in(DocBsTask::getId, ids));
            }
        }
    }

    /**
     * 删除过期文件
     */
    @Async("GlobalThreadPool")
    public void delRecyclDocument(AccountToken token, DocBsRecycleSearchVO docBsDocumentExtend, DocBsDocumentSearchDTO searchDTO) {
        List<DocBsDocumentDTO> list2 = docBsDocumentMapper.selectListByRecycle(searchDTO);
        if(CollectionUtils.isEmpty(list2)){
            return;
        }
        List<DocBsDocumentDTO> recycles = list2.stream().filter(s -> s.getRecycleDate().before(new Date())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(recycles)){
            return;
        }
        List<DocBsTask> docBsTasks = new ArrayList<>();
        for (DocBsDocumentDTO documentExtend : recycles) {
            DocBsTask docBsTask = new DocBsTask();
            docBsTask.setRelId(documentExtend.getFileId());
            docBsTask.setTaskType(DocConstants.TASK_DEL_FILE);
            docBsTask.setTaskStatus(DocConstants.DEL_STORAGE_PENDING);
            docBsTasks.add(docBsTask);
        }
        if(!CollectionUtils.isEmpty(docBsTasks)){
            MybatisBatch<DocBsTask> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsTasks);
            MybatisBatch.Method<DocBsTask> method = new MybatisBatch.Method<>(DocBsTaskMapper.class);
            mybatisBatch.execute(method.insert());
        }
        List<Long> collect = recycles.stream().map(DocBsDocumentDTO::getRecycleId).collect(Collectors.toList());
        delDoc(collect.toArray(new Long[0]),token,docBsDocumentExtend.getDocType());
    }

    private void delDoc(Long[] recycles, AccountToken token, Integer code) {
        AssertUtils.isNull(recycles, "参数错误");

        List<Long> recycleIds = new ArrayList<>(Arrays.asList(recycles));
        List<DocBsDocumentDTO> list = docBsDocumentMapper
                .selectListByrecycleIds(recycleIds);
        if (!CollectionUtils.isEmpty(list)) {
            Set<Long> collectIds = list.stream()
                    .map(DocBsDocumentDTO::getDocOwner)
                    .collect(Collectors.toSet());
            Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(collectIds.toArray(new Long[0]));
            List<SysUserDTO> sysUserDTOS = userListByUserIds.getData();
            Map<Long, String> userMap = sysUserDTOS.stream()
                    .collect(Collectors.toMap(SysUserDTO::getUserId, SysUserDTO::getName));
            for (DocBsDocumentDTO docBsDocumentDTO : list) {
                Long docOwner = docBsDocumentDTO.getDocOwner();
                if (userMap.containsKey(docOwner)) {
                    docBsDocumentDTO.setDocOwnerStr(userMap.get(docOwner));
                }
            }
            List<Long> docIdRecyList = new ArrayList<>();
            List<Long> collect1 = list.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
            if (code.equals(DocConstants.COMPANY)) {
                Long[] ans2 = collect1.toArray(new Long[collect1.size()]);
                docCommonService.isMangePermiss(token, ans2);
            }
            Map<Integer, List<DocBsDocumentDTO>> collect = list.stream().collect(Collectors.groupingBy(DocBsDocumentDTO::getType));
            //文件夹清理
            List<DocBsDocumentDTO> folders = collect.get(DocConstants.FOLDER);
            if (!CollectionUtils.isEmpty(folders)) {
                Set<Long> docIds = new HashSet<>();
                for (DocBsDocument document : folders) {
                    //子集文件夹
                    List<DocBsDocumentTree> parentId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                            .in(DocBsDocumentTree::getFatherId, document.getBusId()));
                    List<Long> folderIds = parentId.stream().map(DocBsDocumentTree::getDocId).collect(Collectors.toList());
                    //2、获取所有子文件夹和当前文件夹中的文档。
                    List<DocBsDocument> docIdList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                            .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                            .in(DocBsDocument::getFolderId, folderIds));
                    List<Long> docs = docIdList.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
                    docIds.addAll(docs);
                    //删除文件夹关联关系
                    docBsDocumentTreeMapper.delete(new LambdaQueryWrapper<DocBsDocumentTree>().in(DocBsDocumentTree::getFatherId, folderIds));
                    //删除子文件夹
                    docBsDocumentMapper.deleteBatchIds(folderIds);
                    docIdRecyList.addAll(folderIds);
                }
                docIdRecyList.addAll(docIds);
                //删除关联文档
                handleDelDoc(new ArrayList<>(docIds));
                List<Long> folderIds = folders.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
                docBsDocumentMapper.deleteBatchIds(folderIds);
                //删除es
                for (Long docId : docIds) {
                    centerQueryService.delFullText(docId.toString());
                }

            }
            List<DocBsDocumentDTO> documents = collect.get(DocConstants.DOCUMENT);
            if (!CollectionUtils.isEmpty(documents)) {
                List<Long> docs = documents.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
                handleDelDoc(docs);
                for (Long docId : docs) {
                    centerQueryService.delFullText(docId.toString());
                }
            }

            //有可能是其他的回收站数据
            if (!CollectionUtils.isEmpty(docIdRecyList)) {
                List<DocBsDocumentDTO> list1 = docBsDocumentMapper
                        .selectListByBusIds(docIdRecyList);
                Set<Long> collects = list1.stream()
                        .map(DocBsDocumentDTO::getDocOwner)
                        .collect(Collectors.toSet());
                Result<List<SysUserDTO>> users = userApi.getUserListByUserIds(collects.toArray(new Long[0]));
                List<SysUserDTO> sysUsers= users.getData();
                Map<Long, String> map = sysUsers.stream()
                        .collect(Collectors.toMap(SysUserDTO::getUserId, SysUserDTO::getName));
                for (DocBsDocumentDTO docBsDocumentDTO : list1) {
                    Long docOwner = docBsDocumentDTO.getDocOwner();
                    if (map.containsKey(docOwner)) {
                        docBsDocumentDTO.setDocOwnerStr(map.get(docOwner));
                    }
                }
                List<Long> collect2 = list1.stream().filter(s -> s.getRecycleId() != null).map(DocBsDocumentDTO::getRecycleId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect2)) {
                    recycleIds.addAll(collect2);
                }
            }
        }

        //删除回收站的数据
        docBsRecycleMapper.deleteBatchIds(recycleIds);
    }

    private void handleDelDoc(List<Long> docIds) {
        if (!CollectionUtils.isEmpty(docIds)) {
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectBatchIds(docIds);
            //文件大小处理
            for (DocBsDocument document : docBsDocuments) {
                if (document.getFolderId() != null) {
                    //需要将父级的大小移除掉自己的大小
                    docCommonService.handleFileSize(document);
                }
            }

            //3、删除所有与文档的关联关系
            //3.1 删除文档与附件的关联关系
            handleDelAttch(docIds);

            //3.2 删除文档与文档的关联关系
            docBsDocRelMapper.delete(new LambdaQueryWrapper<DocBsDocRel>().in(DocBsDocRel::getDocId, docIds));
            //3.3 删除文档与动态的关联关系
            docBsDocFlowMapper.delete(new LambdaQueryWrapper<DocBsDocFlow>().in(DocBsDocFlow::getDocId, docIds));
            //3.4 删除文档与权限的关联关系
            docBsDocumentUserMapper.delete(new LambdaQueryWrapper<DocBsDocumentUser>().in(DocBsDocumentUser::getDocId, docIds));
            //3.5 删除文档与标签的关联关系
            docBsTagDocumentMapper.delete(new LambdaQueryWrapper<DocBsTagDocument>().in(DocBsTagDocument::getDocId, docIds));
            //3.6 删除‘最近打开’的关联关系
            docBsRecentlyDocumentMapper.delete(new LambdaQueryWrapper<DocBsRecentlyDocument>().in(DocBsRecentlyDocument::getDocId, docIds));

            //3.6 删除档案
            docBsDocumentMapper.deleteBatchIds(docIds);
        }
    }

    /**
     * 彻底删除附件
     *
     * @param docIds
     */
    private void handleDelAttch(List<Long> docIds) {
        if (!CollectionUtils.isEmpty(docIds)) {
            //3.1.1 获取附件
            List<DocBsDocument> fileList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FILE)
                    .in(DocBsDocument::getRelDoc, docIds));
            //3.1.2 新增记录任务
            if (!CollectionUtils.isEmpty(fileList)) {
                List<DocBsTask> docBsTasks = new ArrayList<>();
                fileList.forEach(s -> {
                    DocBsTask docBsTask = new DocBsTask();
                    docBsTask.setRelId(s.getFileId());
                    docBsTask.setTaskType(DocConstants.TASK_DEL_FILE);
                    docBsTask.setTaskStatus(DocConstants.DEL_STORAGE_PENDING);
                    docBsTasks.add(docBsTask);
                });
                MybatisBatch<DocBsTask> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsTasks);
                MybatisBatch.Method<DocBsTask> method = new MybatisBatch.Method<>(DocBsTaskMapper.class);
                mybatisBatch.execute(method.insert());
                //3.1.2 删除附件
                docBsDocumentMapper.delete(new LambdaQueryWrapper<DocBsDocument>()
                        .eq(DocBsDocument::getType, DocConstants.FILE)
                        .in(DocBsDocument::getRelDoc, docIds));
            }
        }
    }
}
