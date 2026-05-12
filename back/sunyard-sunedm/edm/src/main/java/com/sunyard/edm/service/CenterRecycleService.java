package com.sunyard.edm.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import com.sunyard.edm.po.DocBsRecycle;
import com.sunyard.edm.po.DocBsTagDocument;
import com.sunyard.edm.po.DocBsTask;
import com.sunyard.edm.vo.DocBsRecycleSearchVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @Author PJW 2022/12/14 10:05
 * @Desc 回收站相关实现类
 */
@Service
public class CenterRecycleService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
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
    private DocBsRecentlyDocumentMapper docBsRecentlyDocumentMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private CenterQueryService centerQueryService;
    @Resource
    private CenterDelStorageService centerDelStorageService;
    @Resource
    private CenterCommonService docCommonService;

    /**
     * 批量回复
     */
    @Lock4j(keys = "#recycles")
    @Transactional(rollbackFor = Exception.class)
    public void recycleResume(Long[] recycles, AccountToken token, Integer code) {
        AssertUtils.isNull(recycles, "参数错误");

        List<Long> recycleIds = Arrays.asList(recycles);
        List<DocBsRecycle> recycleList = docBsRecycleMapper.selectBatchIds(recycleIds);
        AssertUtils.isNull(recycleList, "参数错误");
        List<Long> docIds = recycleList.stream().map(DocBsRecycle::getDocId).collect(Collectors.toList());
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectBatchIds(docIds);

        List<Long> collect1 = docBsDocuments.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
        if (code.equals(DocConstants.COMPANY)) {
            Long[] ans2 = collect1.toArray(new Long[collect1.size()]);
            docCommonService.isMangePermiss(token, ans2);
        }
        Map<Integer, List<DocBsDocument>> collect = docBsDocuments.stream().collect(Collectors.groupingBy(DocBsDocument::getType));
        //文档恢复
        List<DocBsDocument> documentList = collect.get(DocConstants.DOCUMENT);
        recycleNormal(documentList, token);

        //文件夹恢复
        List<DocBsDocument> folderList = collect.get(DocConstants.FOLDER);
        handleFolderNormal(folderList, token);
        //删除回收站的数据
        docBsRecycleMapper.deleteBatchIds(recycleIds);


    }

    private void handleFolderNormal(List<DocBsDocument> folderList, AccountToken token) {
        if (!CollectionUtils.isEmpty(folderList)) {
            for (DocBsDocument document : folderList) {
                //子集文件夹
                List<DocBsDocumentTree> parentId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                        .in(DocBsDocumentTree::getFatherId, document.getBusId()));
                List<Long> folderIds = parentId.stream().map(DocBsDocumentTree::getDocId).collect(Collectors.toList());

                //2、获取所有子文件夹和当前文件夹中的文档。
                List<DocBsDocument> docIdList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                        .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                        .in(DocBsDocument::getFolderId, folderIds));
                recycleNormal(docIdList, token);

                //5、文件夹恢复
                docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                        .set(DocBsDocument::getRecycleDate, null)
                        .set(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                        .in(DocBsDocument::getBusId, folderIds));
            }
        }
    }

    /**
     * 分页
     */
    public PageInfo<DocBsDocumentDTO> recycleList(AccountToken token, DocBsRecycleSearchVO docBsDocumentExtend, PageForm pageForm) {
        if (StringUtils.isEmpty(docBsDocumentExtend.getDictionSuffix())) {
            return new PageInfo<>();
        }
        if (docBsDocumentExtend.getDelEndDate() != null) {
            Date createEndDate = docBsDocumentExtend.getDelEndDate();
            Calendar c = Calendar.getInstance();
            c.setTime(createEndDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            //这是明天
            Date tomorrow = c.getTime();
            docBsDocumentExtend.setDelEndDate(tomorrow);
        }


        //todo LambdaQueryWrapper
        /*QueryWrapper<DocBsRecycle> queryWrapper = new QueryWrapper<DocBsRecycle>()
                .eq("d.is_deleted", DocConstants.ZERO)
                .like(!StringUtils.isEmpty(docBsDocumentExtend.getDocOwner()), "e.name", docBsDocumentExtend.getDocOwner())
                .like(!StringUtils.isEmpty(docBsDocumentExtend.getDocName()), "a.doc_name", docBsDocumentExtend.getDocName())
                .between(docBsDocumentExtend.getDelStartDate() != null, "d.del_date", docBsDocumentExtend.getDelStartDate(), docBsDocumentExtend.getDelEndDate())
                .in(!CollectionUtils.isEmpty(docBsDocumentExtend.getTagId()), "c.tag_id", docBsDocumentExtend.getTagId());
        if (docBsDocumentExtend.getDelTimeSort() != null) {
            if (docBsDocumentExtend.getDelTimeSort().equals(DocConstants.SORT_ASC)) {
                queryWrapper.orderByAsc("d.del_date");
            } else {
                queryWrapper.orderByDesc("d.del_date");
            }
        } else if (docBsDocumentExtend.getRecycleDateSort() != null) {
            if (docBsDocumentExtend.getRecycleDateSort().equals(DocConstants.SORT_ASC)) {
                queryWrapper.orderByAsc("a.recycle_date");
            } else {
                queryWrapper.orderByDesc("a.recycle_date");
            }
        }

        if (docBsDocumentExtend.getDocType().equals(DocConstants.PERSON)) {
            queryWrapper.eq("a.doc_owner", token.getId());
        } else {
            queryWrapper.eq("a.doc_type", DocConstants.COMPANY);
            queryWrapper.eq(docBsDocumentExtend.getHouseId() != null, "a.house_id", docBsDocumentExtend.getHouseId());
        }*/
        if (org.apache.commons.lang.StringUtils.isEmpty(docBsDocumentExtend.getDictionSuffix())) {
            return new PageInfo<>();
        }
        DocBsDocumentSearchDTO searchDTO = new DocBsDocumentSearchDTO();
        searchDTO.setIsDeleted(DocConstants.ZERO);
        searchDTO.setDocOwner(token.getId());
        searchDTO.setDocName(docBsDocumentExtend.getDocName());
        searchDTO.setDelStartDate(docBsDocumentExtend.getDelStartDate());
        searchDTO.setDelEndDate(docBsDocumentExtend.getDelEndDate());
        searchDTO.setTagId(docBsDocumentExtend.getTagId());
        searchDTO.setDelTimeSort(docBsDocumentExtend.getDelTimeSort());
        searchDTO.setRecycleDateSort(docBsDocumentExtend.getRecycleDateSort());
        searchDTO.setDocType(docBsDocumentExtend.getDocType());
        searchDTO.setHouseId(docBsDocumentExtend.getHouseId());
        if (!ObjectUtils.isEmpty(docBsDocumentExtend.getDocOwner())) {
                Result<List<SysUserDTO>> result = userApi
                        .getUserDetailByName(docBsDocumentExtend.getDocOwner());
                List<Long> userIds = result.getData().stream().map(SysUserDTO::getUserId)
                        .collect(Collectors.toList());
                userIds.add(-Long.MAX_VALUE);
                searchDTO.setUserIds(userIds);
            }
        docCommonService.handleSuffixSearch(docBsDocumentExtend.getDictionSuffix(), searchDTO);


        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocBsDocumentDTO> list = docBsDocumentMapper.selectListByRecycle(searchDTO);
        if(CollectionUtils.isEmpty(list)){
            return new PageInfo<>();
        }
        PageInfo<DocBsDocumentDTO> pageInfo = new PageInfo<>(list);
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

        docCommonService.handleDocSize(list);
        List<DocBsDocumentDTO> collect = list.stream().filter(s -> s.getRecycleDate().after(new Date())).collect(Collectors.toList());
        //剩余保留时间
        for (DocBsDocumentDTO documentExtend : collect) {
            if (documentExtend.getRecycleDate() != null) {
                Date recycleDate = documentExtend.getRecycleDate();
                Instant instant = recycleDate.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate localDate = instant.atZone(zoneId).toLocalDate();
                long result = ChronoUnit.DAYS.between(LocalDate.now(), localDate);
                documentExtend.setRetainDay(result + "天");
            }
        }
        //彻底删除过期文件
        centerDelStorageService.delRecyclDocument(token, docBsDocumentExtend, searchDTO);
        pageInfo.setList(collect);
        return pageInfo;
    }


    private void recycleNormal(List<DocBsDocument> docIdList, AccountToken token) {
        if (!CollectionUtils.isEmpty(docIdList)) {
            List<Long> docs = docIdList.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
            //文档恢复
            docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                    .set(DocBsDocument::getRecycleDate, null)
                    .set(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                    .in(DocBsDocument::getBusId, docs));
            //附件恢复
            docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                    .set(DocBsDocument::getRecycleDate, null)
                    .set(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                    .in(DocBsDocument::getRelDoc, docs));
            List<DocBsDocFlow>  docBsDocFlows = new ArrayList<>();
            for (Long id : docs) {
                DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
                docBsDocFlow.setDocId(id);
                docBsDocFlow.setUserId(token.getId());
                docBsDocFlow.setFlowDate(new Date());
                docBsDocFlow.setFlowDescribe("重新上架");
                docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_RESTORE);
                docBsDocFlows.add(docBsDocFlow);
            }
            MybatisBatch<DocBsDocFlow> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocFlows);
            MybatisBatch.Method<DocBsDocFlow> docMethod = new MybatisBatch.Method<>(DocBsDocFlowMapper.class);
            docBatchs.execute(docMethod.insert());
        }
    }


    /**
     * 彻底删除
     */
    @Lock4j(keys = "#recycles")
    @Transactional(rollbackFor = Exception.class)
    public void delDoc(Long[] recycles, AccountToken token, Integer code) {
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

                MybatisBatch<DocBsTask> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsTasks);
                MybatisBatch.Method<DocBsTask> docMethod = new MybatisBatch.Method<>(DocBsTaskMapper.class);
                docBatchs.execute(docMethod.insert());
                //3.1.2 删除附件
                docBsDocumentMapper.delete(new LambdaQueryWrapper<DocBsDocument>()
                        .eq(DocBsDocument::getType, DocConstants.FILE)
                        .in(DocBsDocument::getRelDoc, docIds));
            }
        }
    }
}
