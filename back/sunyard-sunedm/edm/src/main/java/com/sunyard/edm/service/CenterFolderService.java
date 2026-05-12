package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocSysTeamUser;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsDocumentTreeMapper;
import com.sunyard.edm.mapper.DocBsDocumentUserMapper;
import com.sunyard.edm.mapper.DocBsRecycleMapper;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsDocumentTree;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocBsRecycle;
import com.sunyard.edm.vo.DocBsDocumentVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;

/**
 * @author raochangmei
 * @date 11.15
 * @Desc 文档中心-文件夹相关实现类
 */
@Service
public class CenterFolderService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsRecycleMapper docBsRecycleMapper;
    @Resource
    private DocBsDocumentTreeMapper docBsDocumentTreeMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private SysFolderService docSysFolderService;



    /**
     * 文件夹树
     */
    public List<DocBsDocumentDTO> getFolderTree(AccountToken token, Long houseId, Integer code) {
        List<DocBsDocumentDTO> ret = null;
        if (code.equals(DocConstants.PERSON)) {
            //个人
            ret = getPersonTree(token);
        } else {
            AssertUtils.isNull(houseId, "参数错误");
            //企业
            //获取配置，判断返回是全量还是不是全量
            Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(DocConstants.DOC_FOLDER_TREE_TYPE);
            SysParamDTO sysParam = sysParamDTOResult.getData();
            if (sysParam != null && DocConstants.DOC_FOLDER_TREE_TYPE_ALL.equals(sysParam.getValue())) {
                ret = docBsDocumentMapper.selectAuthFolderAll(houseId);
                if (CollectionUtils.isEmpty(ret)) {
                    return ret;
                }
                //过滤出未删除的数据和空的数据
                ret = ret.stream().filter(s -> !DocConstants.ONE.equals(s.getIsPermissDeleted())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(ret)) {
                    return ret;
                }

                ret = docCommonService.getPermissMax(ret);
                //重新排序
                ret = ret.stream()
                        .sorted(Comparator.comparing(DocBsDocumentDTO::getDocSeq)).collect(Collectors.toList());
                handlePermissAll(token, houseId, ret);
                for (DocBsDocumentDTO document : ret) {
                    document.setName(document.getDocName());
                    document.setLabel(document.getDocName());
                    document.setValue(document.getBusId() + "");
                }

            } else {
                List<Long> teams = getTeamListByUser(token);
                ret = docBsDocumentMapper.selectDocBsDocumentListSort(houseId,
                        token.getInstId(),DocConstants.INST,token.getDeptId(),DocConstants.DEPT,
                        token.getId(),DocConstants.USER,teams,DocConstants.TEAM);
                if (CollectionUtils.isEmpty(ret)) {
                    return ret;
                }

                for (DocBsDocumentDTO document : ret) {
                    document.setIsQuantity(false);
                    document.setName(document.getDocName());
                    document.setLabel(document.getDocName());
                    document.setValue(document.getBusId() + "");
                }
                ret = docCommonService.getPermissMax(ret);
                handleUserPermiss(ret);
                //重新排序
                ret = ret.stream()
                        .sorted(Comparator.comparing(DocBsDocumentDTO::getDocSeq)).collect(Collectors.toList());
            }
            ret = docCommonService.getChildren(ret);
        }
        docCommonService.handleCollectionFolder(ret, token.getId());
        return ret;
    }

    /**
     * 个人树的处理
     *
     * @param token
     * @return
     */
    private List<DocBsDocumentDTO> getPersonTree(AccountToken token) {
        List<DocBsDocumentDTO> ret;
        LambdaQueryWrapper<DocBsDocument> lambdaQueryWrapper = new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL);
        lambdaQueryWrapper.eq(DocBsDocument::getDocType, DocConstants.PERSON);
        lambdaQueryWrapper.eq(DocBsDocument::getDocOwner, token.getId());
        lambdaQueryWrapper.orderByAsc(DocBsDocument::getDocSeq);
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(lambdaQueryWrapper);
        ret = PageCopyListUtils.copyListProperties(docBsDocuments, DocBsDocumentDTO.class);
        Map<Long, List<DocBsDocument>> collect = docBsDocuments.stream().collect(Collectors.groupingBy(DocBsDocument::getBusId));

        for (DocBsDocumentDTO document : ret) {
            document.setName(document.getDocName());
            document.setLabel(document.getDocName());
            document.setValue(document.getBusId() + "");
            if (document.getParentId() != null && !CollectionUtils.isEmpty(collect.get(document.getParentId()))) {
                document.setFolderNameParent(collect.get(document.getParentId()).get(0).getDocName());
            }
        }

        ret = docCommonService.getChildren(ret);
        return ret;
    }

    /**
     * 全量树的权限处理
     *
     * @param token
     * @param houseId
     * @param ret
     */
    private void handlePermissAll(AccountToken token, Long houseId, List<DocBsDocumentDTO> ret) {
        ret.forEach(s -> {
            s.setPermissType(null);
        });
        List<Long> teams = getTeamListByUser(token);
        List<DocBsDocumentDTO> list = docBsDocumentMapper.selectDocBsDocumentList(houseId,
                token.getInstId(),DocConstants.INST,token.getDeptId(),DocConstants.DEPT,
                token.getId(),DocConstants.USER,teams,DocConstants.TEAM);
        if (!CollectionUtils.isEmpty(list)) {
            List<DocBsDocumentDTO> permissMax = docCommonService.getPermissMax(list);
            Map<Long, List<DocBsDocumentDTO>> collect = permissMax.stream().collect(Collectors.groupingBy(DocBsDocumentDTO::getBusId));
            ret.forEach(s -> {
                List<DocBsDocumentDTO> list1 = collect.get(s.getBusId());
                if (!CollectionUtils.isEmpty(list1)) {
                    s.setPermissType(list1.get(0).getPermissType());
                } else {
                    s.setPermissType(null);
                }
            });
        }
    }


    private void handleUserPermiss(List<DocBsDocumentDTO> ret) {
        if (!CollectionUtils.isEmpty(ret)) {
            List<Long> parentIds = ret.stream()
                    .sorted(Comparator.comparing(DocBsDocumentDTO::getFolderLevel).reversed())
                    .map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
            List<DocBsDocumentTree> docId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                    .in(DocBsDocumentTree::getDocId, parentIds));
            Map<Long, List<DocBsDocumentTree>> collect = docId.stream().filter(s -> !s.getDocId().equals(s.getFatherId())).collect(Collectors.groupingBy(DocBsDocumentTree::getDocId));
            Map<Integer, List<DocBsDocumentDTO>> collect1 = ret.stream().sorted(Comparator.comparing(DocBsDocumentDTO::getFolderLevel).reversed()).collect(Collectors.groupingBy(DocBsDocumentDTO::getFolderLevel));
            for (DocBsDocumentDTO docBsDocumentDTO : ret) {
                Long parentId1 = docBsDocumentDTO.getParentId();
                Long parentId = getParentId(docBsDocumentDTO, collect1, collect);
                docBsDocumentDTO.setIsBrevicary(false);
                if (parentId1 != null && !parentId1.equals(parentId)) {
                    docBsDocumentDTO.setIsBrevicary(true);
                } else if (parentId1 != null && !parentIds.contains(parentId1)) {
                    docBsDocumentDTO.setIsBrevicary(true);
                    docBsDocumentDTO.setFolderLevel(DocConstants.ZERO);
                    parentId = null;
                }
                docBsDocumentDTO.setParentId(parentId);
            }
        }
    }

    Long getParentId(DocBsDocumentDTO documentExtend, Map<Integer, List<DocBsDocumentDTO>> map, Map<Long, List<DocBsDocumentTree>> collect) {
        Long ret = documentExtend.getParentId();

        if (documentExtend.getFolderLevel() < DocConstants.LEVEL_FOLDER) {
            return ret;
        }
        int i = 1;
        return handleGetLong(documentExtend, map, ret, i, collect);
    }

    private Long handleGetLong(DocBsDocumentDTO documentExtend, Map<Integer, List<DocBsDocumentDTO>> map, Long ret, int i, Map<Long, List<DocBsDocumentTree>> tree) {
        if (documentExtend.getFolderLevel() - i < 0) {
            return ret;
        }
        List<DocBsDocumentDTO> list1 = map.get(documentExtend.getFolderLevel() - i);
        if (!CollectionUtils.isEmpty(list1)) {
            List<DocBsDocumentTree> docBsDocumentTrees = tree.get(documentExtend.getBusId());
            List<Long> collect = docBsDocumentTrees.stream().map(DocBsDocumentTree::getFatherId).collect(Collectors.toList());
            for (DocBsDocumentDTO docBsDocumentDTO : list1) {
                if (collect.contains(docBsDocumentDTO.getBusId())) {
                    return docBsDocumentDTO.getBusId();
                }
            }
        }
        //需要往上获取更高层级的id，当作父级id
        i = i + 1;
        return handleGetLong(documentExtend, map, ret, i, tree);
    }
    /**
     * 新增
     */
    @Lock4j
    @Transactional(rollbackFor = Exception.class)
    public void addFolder(AccountToken token, DocBsDocumentDTO docBsDocument) {
        DocBsDocument document = new DocBsDocument();
        BeanUtils.copyProperties(docBsDocument, document);
        //层级处理
        document.setFolderLevel(folderLevelHandle(docBsDocument));
        checkFolder(document);
        if (docBsDocument.getDocType().equals(DocConstants.COMPANY)) {
            if (docBsDocument.getParentId() != null) {
                //判断父级是否有修改以上的权限
                docCommonService.isEditPermiss(token, docBsDocument.getParentId());
            } else {
                //如果没有父级，则判断是否有文档库的权限
                docCommonService.isEditPermissHouse(token, docBsDocument.getHouseId());
            }
        }
        //初始化文件夹大小为0；
        document.setDocSize(0L);
        document.setFolderId(docBsDocument.getParentId());
        document.setType(DocConstants.FOLDER);
        Long busId = snowflakeUtil.nextId();
        document.setBusId(busId);

        //关联处理
        handleTree(docBsDocument, busId);
        if (docBsDocument.getDocType().equals(DocConstants.COMPANY)) {
            //企业权限处理
            handleFolderUser(docBsDocument, busId);
        }
        document.setUpdateTime(new Date());
        document.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);
        docBsDocumentMapper.insert(document);
    }

    private void handleFolderUser(DocBsDocumentDTO docBsDocument, Long busId) {
        if (!CollectionUtils.isEmpty(docBsDocument.getDocBsDocumentUsers())) {
            //校验文档是否有一个以上的管理权限
            List<Integer> collect = docBsDocument.getDocBsDocumentUsers().stream().map(DocBsDocumentUser::getPermissType).collect(Collectors.toList());
            AssertUtils.isTrue(!collect.contains(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "至少需要拥有一个管理权限");
            List<DocBsDocumentUser> docBsDocumentUsers = new ArrayList<>();
            docBsDocument.getDocBsDocumentUsers().forEach(s -> {
                DocBsDocumentUser docBsDocumentUser = new DocBsDocumentUser();
                docBsDocumentUser.setRelId(s.getRelId());
                docBsDocumentUser.setDocId(busId);
                docBsDocumentUser.setPermissType(s.getPermissType());
                docBsDocumentUser.setType(s.getType());
                docBsDocumentUsers.add(docBsDocumentUser);
            });
            MybatisBatch<DocBsDocumentUser> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocumentUsers);
            MybatisBatch.Method<DocBsDocumentUser> docMethod = new MybatisBatch.Method<>(DocBsDocumentUserMapper.class);
            docBatchs.execute(docMethod.insert());
        }
    }

    /**
     * 修改
     */
    @Lock4j
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(AccountToken token, DocBsDocumentVO docBsDocument, Integer code) {
        AssertUtils.isNull(docBsDocument.getBusId(), "参数错误");

        if (code.equals(DocConstants.PERSON)) {
            docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                    .set(DocBsDocument::getDocSeq, docBsDocument.getDocSeq())
                    .set(DocBsDocument::getDocName, docBsDocument.getDocName())
                    .eq(DocBsDocument::getBusId, docBsDocument.getBusId()));
        } else {
            docSysFolderService.updateFolder(docBsDocument, token);
        }

    }

    /**
     * 删除
     *
     */
    @Lock4j
    @Transactional(rollbackFor = Exception.class)
    public void delFolder(AccountToken token, Long folderId, Integer code) {
        AssertUtils.isNull(folderId, "参数错误");
        if (code.equals(DocConstants.COMPANY)) {
            docCommonService.isMangePermiss(token, folderId);
        }

        Date recycleDateByParam = docCommonService.getRecycleDateByParam();

        //新建回收站数据
        DocBsRecycle docBsRecycle = new DocBsRecycle();
        docBsRecycle.setDocId(folderId);
        docBsRecycle.setRecycleDate(recycleDateByParam);
        docBsRecycle.setDelDate(new Date());
        docBsRecycleMapper.insert(docBsRecycle);
        docCommonService.handleRecycleFolder(folderId, recycleDateByParam, token);
    }


    /**
     * 查询文件夹树
     */
    public List<DocBsDocumentDTO> queryFolderTree(AccountToken token, Long houseId) {
        AssertUtils.isNull(houseId, "参数错误");
        List<Long> teams = getTeamListByUser(token);
        ArrayList<Integer> permissType = new ArrayList<>();
        permissType.add(DocConstants.DOC_COMMON_PERMISSION_TYPE_EDIT);
        permissType.add(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE);
        List<DocBsDocumentDTO> list = docBsDocumentMapper.selectAuthFolder(houseId,null,token.getInstId(),
                DocConstants.INST,token.getDeptId(),DocConstants.DEPT,token.getId(),DocConstants.USER,
                teams,DocConstants.TEAM,permissType);
        list = docCommonService.getPermissMax(list);
        return list;
    }

    private List<Long> getTeamListByUser(AccountToken token) {
        List<DocSysTeamUser> users = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, token.getId()));
        return users.stream().map(DocSysTeamUser::getTeamId).collect(Collectors.toList());
    }

    /**
     * 树
     */
    public List<DocBsDocumentDTO> moveFolderTree(AccountToken token, Long houseId, Integer code) {
        List<DocBsDocumentDTO> ret = null;
        if (DocConstants.PERSON.equals(code)) {
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FOLDER)
                    .eq(DocBsDocument::getDocType, DocConstants.PERSON)
                    .eq(DocBsDocument::getDocOwner, token.getId()));
            ret = PageCopyListUtils.copyListProperties(docBsDocuments, DocBsDocumentDTO.class);
        } else {
            AssertUtils.isNull(houseId, "参数错误");
            List<Long> teams = getTeamListByUser(token);
            ArrayList<Integer> permissType = new ArrayList<>();
            permissType.add(DocConstants.DOC_COMMON_PERMISSION_TYPE_EDIT);
            permissType.add(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE);
            ret = docBsDocumentMapper.selectAuthFolder(houseId,DocConstants.RECYCLE_STATUS_NORMAL,token.getInstId(),
                    DocConstants.INST,token.getDeptId(),DocConstants.DEPT,token.getId(),DocConstants.USER,
                    teams,DocConstants.TEAM,permissType);
            ret = docCommonService.getPermissMax(ret);
        }
        return ret;
    }

    private void handleTree(DocBsDocumentDTO docBsDocument, Long busId) {
        //1、自己到自己的数据
        DocBsDocumentTree docBsDocumentTree = new DocBsDocumentTree();
        docBsDocumentTree.setDocId(busId);
        docBsDocumentTree.setFatherId(busId);
        docBsDocumentTreeMapper.insert(docBsDocumentTree);
        if (docBsDocument.getParentId() != null) {
            //2、自己和祖先的
            List<DocBsDocumentTree> docId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                    .eq(DocBsDocumentTree::getDocId, docBsDocument.getParentId()));
            if(!CollectionUtils.isEmpty(docId)){
                List<DocBsDocumentTree> docBsDocumentTrees = new ArrayList<>();
                for (DocBsDocumentTree d : docId) {
                    DocBsDocumentTree documentTree = new DocBsDocumentTree();
                    documentTree.setDocId(busId);
                    documentTree.setFatherId(d.getFatherId());
                    docBsDocumentTrees.add(documentTree);
                }
                MybatisBatch<DocBsDocumentTree> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocumentTrees);
                MybatisBatch.Method<DocBsDocumentTree> docMethod = new MybatisBatch.Method<>(DocBsDocumentTreeMapper.class);
                docBatchs.execute(docMethod.insert());
            }

        }
    }

    private Integer folderLevelHandle(DocBsDocumentDTO docBsDocument) {
        // 层级处理
        if (docBsDocument.getParentId() != null) {
            DocBsDocument document1 = docBsDocumentMapper.selectById(docBsDocument.getParentId());
            if (document1 != null) {
                //层级递增
                docBsDocument.setFolderLevel(document1.getFolderLevel() + 1);
            }
        } else {
            //默认0层
            docBsDocument.setFolderLevel(0);
        }
        return docBsDocument.getFolderLevel();
    }

    /**
     * 统一校验
     *
     * @param docBsDocument
     */
    private void checkFolder(DocBsDocument docBsDocument) {
        AssertUtils.isNull(docBsDocument.getDocName(), "参数错误");
        if (docBsDocument.getBusId() != null) {
            //修改
            Long integer = docBsDocumentMapper.selectCount(new LambdaQueryWrapper<DocBsDocument>()
                    .ne(DocBsDocument::getBusId, docBsDocument.getBusId())
                    .eq(DocBsDocument::getDocType, docBsDocument.getDocType())
                    .eq(DocBsDocument::getDocName, docBsDocument.getDocName())
                    .eq(DocBsDocument::getType, DocConstants.FOLDER)
                    .eq(docBsDocument.getDocType().equals(DocConstants.PERSON),DocBsDocument::getDocOwner, docBsDocument.getDocOwner())
                    .eq(docBsDocument.getParentId() != null, DocBsDocument::getParentId, docBsDocument.getParentId())
                    .eq(docBsDocument.getHouseId() != null, DocBsDocument::getHouseId, docBsDocument.getHouseId())
                    .eq(DocBsDocument::getFolderLevel, docBsDocument.getFolderLevel())
            );
            AssertUtils.isTrue(integer != null && integer.intValue() > 0, "名称重复");
        } else {
            //新增
            Long integer = docBsDocumentMapper.selectCount(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getDocName, docBsDocument.getDocName())
                    .eq(DocBsDocument::getType, DocConstants.FOLDER)
                    .eq(DocBsDocument::getDocType, docBsDocument.getDocType())
                    .eq(docBsDocument.getDocType().equals(DocConstants.PERSON), DocBsDocument::getDocOwner, docBsDocument.getDocOwner())
                    .eq(docBsDocument.getParentId() != null, DocBsDocument::getParentId, docBsDocument.getParentId())
                    .eq(docBsDocument.getHouseId() != null, DocBsDocument::getHouseId, docBsDocument.getHouseId())
                    .eq(DocBsDocument::getFolderLevel, docBsDocument.getFolderLevel())
            );
            AssertUtils.isTrue(integer != null && integer.intValue() > 0, "名称重复");
        }
    }
}
