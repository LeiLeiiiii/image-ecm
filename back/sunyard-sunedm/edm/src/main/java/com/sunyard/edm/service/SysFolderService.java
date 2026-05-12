package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocSysTeamUser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.dto.PromptDTO;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsDocumentTreeMapper;
import com.sunyard.edm.mapper.DocBsDocumentUserMapper;
import com.sunyard.edm.mapper.DocBsRecycleMapper;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsDocumentTree;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocBsRecycle;
import com.sunyard.edm.vo.DocBsDocumentVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.DeptApi;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @author huronghao
 * @Desc 系统管理-文件夹管理实现类
 * @date 2022-12-14 11:17
 */
@Service
public class SysFolderService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocBsDocumentTreeMapper docBsDocumentTreeMapper;
    @Resource
    private DocBsDocumentUserMapper documentUserMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private CenterCommonService docCommonService;
    /**
     * 文件夹列表
     */
    public List<DocBsDocumentDTO> selectFolder(Long houseId, String folderName, AccountToken token) {
        AssertUtils.isNull(houseId, "参数错误！");
        if (ObjectUtils.isEmpty(folderName)) {
            List<DocBsDocument> docBsDocumentList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getHouseId, houseId)
                    .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                    .eq(DocBsDocument::getDocType, DocConstants.COMPANY)
                    .eq(DocBsDocument::getType, DocConstants.FOLDER)
                    .eq(DocBsDocument::getFolderLevel, DocConstants.ZERO)
                    .orderByAsc(DocBsDocument::getDocSeq)
                    .orderByDesc(DocBsDocument::getUpdateTime));
            //判断是否存在子文件夹
            List<DocBsDocumentDTO> docBsDocumentDTOList = PageCopyListUtils.copyListProperties(docBsDocumentList, DocBsDocumentDTO.class);

            isHaveChild(docBsDocumentDTOList);
            //查询登入人有权限的文件夹
            handleAuthFolder(token, null, houseId, docBsDocumentDTOList);

            return docBsDocumentDTOList;
        } else {
            List<DocBsDocumentDTO> docBsDocumentDTOList = new ArrayList<>();
            //文件夹树结构 最初始的数据
            List<DocBsDocument> docBsDocumentList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .like(DocBsDocument::getDocName, folderName)
                    .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                    .eq(DocBsDocument::getDocType, DocConstants.COMPANY)
                    .eq(DocBsDocument::getType, DocConstants.FOLDER)
                    .eq(DocBsDocument::getHouseId, houseId)
                    .orderByAsc(DocBsDocument::getDocSeq)
                    .orderByDesc(DocBsDocument::getUpdateTime));
            //获取folderId
            if (CollectionUtils.isEmpty(docBsDocumentList)) {
                return docBsDocumentDTOList;
            }
            List<Long> folderIdList = docBsDocumentList.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
            //根据子级查询所有父级id包括本身
            List<DocBsDocumentTree> docBsDocumentTreeList = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>().in(DocBsDocumentTree::getDocId, folderIdList));
            List<Long> fatherIds = docBsDocumentTreeList.stream().map(DocBsDocumentTree::getFatherId).collect(Collectors.toList());

            //文件夹的最大级别
            Integer folderLevel = docBsDocumentList.stream().mapToInt(DocBsDocument::getFolderLevel).max().orElse(DocConstants.ZERO);
            //查询所有 第一层级文件夹 获取第一层级的文件夹id
            List<DocBsDocument> allFirstFolderList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FOLDER).eq(DocBsDocument::getFolderLevel, DocConstants.ZERO).eq(DocBsDocument::getHouseId, houseId));
            List<Long> allFirstFolderIdList = allFirstFolderList.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());

            //获取只需要展示的第一层级 文件夹id
            List<DocBsDocumentTree> docBsDocumentTrees = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                    .in(DocBsDocumentTree::getDocId, folderIdList).in(DocBsDocumentTree::getFatherId, allFirstFolderIdList));
            List<Long> firstFolderIdList = docBsDocumentTrees.stream().map(DocBsDocumentTree::getFatherId).collect(Collectors.toList());

            //第一层级文件夹
            List<DocBsDocument> firstFolderList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FOLDER)
                    .in(DocBsDocument::getBusId, firstFolderIdList)
                    .orderByAsc(DocBsDocument::getDocSeq)
                    .orderByDesc(DocBsDocument::getUpdateTime));
            docBsDocumentDTOList = PageCopyListUtils.copyListProperties(firstFolderList, DocBsDocumentDTO.class);

            //递归查询
            handleChildren(token, docBsDocumentDTOList, folderLevel, fatherIds);
            return docBsDocumentDTOList;
        }
    }

    /**
     * 子级文件夹
     */
    public List<DocBsDocumentDTO> selectChild(Long busId, AccountToken token) {
        AssertUtils.isNull(busId, "参数错误！");
        //查询有所有子文件夹
        List<DocBsDocument> docBsDocumentList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getParentId, busId)
                .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                .eq(DocBsDocument::getDocType, DocConstants.COMPANY)
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .orderByAsc(DocBsDocument::getDocSeq)
                .orderByDesc(DocBsDocument::getUpdateTime));
        //判断是否存在子文件夹
        List<DocBsDocumentDTO> docBsDocumentDTOList = PageCopyListUtils.copyListProperties(docBsDocumentList, DocBsDocumentDTO.class);
        isHaveChild(docBsDocumentDTOList);
        List<Long> busIdList = docBsDocumentDTOList.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
        //处理查询出来文件夹的权限
        handleAuthFolder(token, busIdList, null, docBsDocumentDTOList);

        return docBsDocumentDTOList;
    }

    /**
     * 文件夹详情
     */
    public DocBsDocumentDTO getInfoFolder(Long busId, PageForm pageForm) {
        AssertUtils.isNull(busId, "参数错误！");
        //文件夹信息
        DocBsDocumentDTO docBsDocumentDTO = docBsDocumentMapper.selectFolderInfo(busId);
        //统计机构部门团队用户数据
        handleAuthNum(busId, docBsDocumentDTO);
        //权限相关分页
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocBsDocumentUserDTO> docBsDocumentUserDTOList = documentUserMapper.selectListExtend(busId, DocConstants.ZERO);
        for(DocBsDocumentUserDTO s:docBsDocumentUserDTOList){
            SysUserDTO data = userApi.getUserByUserId(s.getRelId()).getData();
            SysInstDTO data1 = instApi.getInstByInstId(s.getRelId()).getData();
            SysDeptDTO data2 = deptApi.selectById(s.getRelId()).getData();
            if (null != data) {
                s.setUserIdStr(data.getName());
            }
            if (null != data1) {
                s.setInstIdStr(data1.getName());
            }
            if (null != data2) {
                s.setDeptIdStr(data2.getName());
            }
        }
        docCommonService.handleRel(docBsDocumentUserDTOList);
        docBsDocumentDTO.setPageInfo(new PageInfo<>(docBsDocumentUserDTOList));
        return docBsDocumentDTO;
    }

    /**
     * 文件夹权限详情
     */
    public List<DocBsDocumentUserDTO> getInfoAuth(Long busId) {
        AssertUtils.isNull(busId, "参数错误");
        List<DocBsDocumentUserDTO> docBsDocumentUserDTOS = documentUserMapper.selectListExtend( busId, DocConstants.ZERO);
        for(DocBsDocumentUserDTO s:docBsDocumentUserDTOS){
            SysUserDTO data = userApi.getUserByUserId(s.getRelId()).getData();
            SysInstDTO data1 = instApi.getInstByInstId(s.getRelId()).getData();
            SysDeptDTO data2 = deptApi.selectById(s.getRelId()).getData();
            if (null != data) {
                s.setUserIdStr(data.getName());
            }
            if (null != data1) {
                s.setInstIdStr(data1.getName());
            }
            if (null != data2) {
                s.setDeptIdStr(data2.getName());
            }
        }
        docCommonService.handleRel(docBsDocumentUserDTOS);
        return docBsDocumentUserDTOS;
    }

    /**
     * 添加文件夹
     */
    @Transactional(rollbackFor = Exception.class)
    public void addFolder(DocBsDocumentVO document, AccountToken token) {
        checkFolder(document);
        if (document.getParentId() != null) {
            //判断父级是否有修改以上的权限
            docCommonService.isEditPermiss(token, document.getParentId());
        } else {
            //如果没有父级，则判断是否有文档库的权限
            docCommonService.isEditPermissHouse(token, document.getHouseId());
        }
        //校验同级文件夹是否重复
        checkFolderName(document);

        DocBsDocument docBsDocument = new DocBsDocument();
        BeanUtils.copyProperties(document, docBsDocument);
        docBsDocument.setDocType(DocConstants.COMPANY);
        docBsDocument.setType(DocConstants.FOLDER);
        docBsDocument.setDocSize(DocConstants.ZERO.longValue());
        docBsDocument.setParentId(docBsDocument.getParentId());
        docBsDocument.setDocCreator(token.getId());
        docBsDocument.setDocOwner(token.getId());
        docBsDocument.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);
        docBsDocument.setUpdateTime(new Date());
        Long busId = snowflakeUtil.nextId();
        docBsDocument.setBusId(busId);
        //层级处理
        docBsDocument.setFolderLevel(folderLevelHandle(docBsDocument));
        //关联处理
        handleTree(docBsDocument, busId);
        //权限处理
        handleAuth(document, busId);

        docBsDocumentMapper.insert(docBsDocument);
    }

    /**
     * 编辑文件夹
     */
    @Lock4j(keys = "#document.getBusId()")
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(DocBsDocumentVO document, AccountToken token) {
        AssertUtils.isNull(document.getBusId(), "参数错误！");

        checkFolderName(document);

        //校验是否有修改文件夹的权限
        docCommonService.isEditPermiss(token, document.getBusId());

        checkFolder(document);
        //基本信息编辑
        DocBsDocument docBsDocument = new DocBsDocument();
        BeanUtils.copyProperties(document, docBsDocument);
        //关联权限编辑
        handleAuth(document, document.getBusId());
        docBsDocumentMapper.updateById(docBsDocument);
    }

    /**
     * 删除文件夹 单个
     */
    public PromptDTO delPrompt(Long[] busIds, AccountToken token, Integer code) {
        AssertUtils.isNull(busIds, "参数错误！");
        PromptDTO promptDTO = new PromptDTO();
        //判断文件夹是否有管理的权限
        if (code.equals(DocConstants.COMPANY)) {
            for (int i = 0; i < busIds.length; i++) {
                docCommonService.isMangePermiss(token, busIds[i]);
            }
        }
        //1、获取所有子文件夹 数据库闭包结构,本身也会存一条数据
        List<DocBsDocumentTree> parentId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                .in(DocBsDocumentTree::getFatherId, Arrays.asList(busIds)));
        List<Long> folderIds = parentId.stream().map(DocBsDocumentTree::getDocId).collect(Collectors.toList());
        promptDTO.setFolderNum(folderIds.size());
        //判断是否有文档 根据文件夹id去查询文档
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                .in(DocBsDocument::getFolderId, folderIds));
        if (!CollectionUtils.isEmpty(docBsDocuments)) {
            promptDTO.setIsHaveDoc(DocConstants.ONE);
        } else {
            promptDTO.setIsHaveDoc(DocConstants.ZERO);
        }
        return promptDTO;
    }

    /**
     * 批量删除文件夹
     */
    @Lock4j(keys = "#busIds")
    @Transactional(rollbackFor = Exception.class)
    public void delBatchFolder(Long[] busIds, AccountToken token) {
        AssertUtils.isNull(busIds, "参数错误！");
        List<DocBsRecycle> docBsRecycles = new ArrayList<>();
        for (int i = 0; i < busIds.length; i++) {
            AssertUtils.isNull(busIds[i], "参数错误");
            Date recycleDateByParam = docCommonService.getRecycleDateByParam();

            //新建回收站数据
            DocBsRecycle docBsRecycle = new DocBsRecycle();
            docBsRecycle.setDocId(busIds[i]);
            docBsRecycle.setRecycleDate(recycleDateByParam);
            docBsRecycle.setDelDate(new Date());
            docBsRecycles.add(docBsRecycle);

            docCommonService.handleRecycleFolder(busIds[i], recycleDateByParam, token);
        }
        MybatisBatch<DocBsRecycle> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsRecycles);
        MybatisBatch.Method<DocBsRecycle> docMethod = new MybatisBatch.Method<>(DocBsRecycleMapper.class);
        docBatchs.execute(docMethod.insert());
    }

    /**
     * 选择上级文件夹树
     */
    public List<DocBsDocumentDTO> getFolderTree(Long houseId) {
        AssertUtils.isNull(houseId, "参数错误！");
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.FOLDER));

        return PageCopyListUtils.copyListProperties(docBsDocuments, DocBsDocumentDTO.class);
    }

    /**
     * 判断是否存在子文件夹
     */
    private void isHaveChild(List<DocBsDocumentDTO> docBsDocumentDTOList) {
        if (CollectionUtils.isEmpty(docBsDocumentDTOList)) {
            return;
        }
        //告诉前端是否存在子文件夹
        for (DocBsDocumentDTO docBsDocumentDTO : docBsDocumentDTOList) {
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getParentId, docBsDocumentDTO.getBusId())
                    .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                    .eq(DocBsDocument::getDocType, DocConstants.COMPANY)
                    .eq(DocBsDocument::getType, DocConstants.FOLDER));
            if (!CollectionUtils.isEmpty(docBsDocuments)) {
                docBsDocumentDTO.setIsChildFolder(DocConstants.ONE);
            }
        }
    }

    /**
     * 新增 编辑 统一校验
     *
     */
    private void checkFolder(DocBsDocumentVO docBsDocument) {
        AssertUtils.isNull(docBsDocument.getDocName(), "文件夹名称不为空！");
        AssertUtils.isNull(docBsDocument.getHouseId(), "所属文档库不为空！");
        AssertUtils.isNull(docBsDocument.getDocBsDocumentUsers(), "参数错误！");
    }

    /**
     * 文件夹层级处理
     */
    private Integer folderLevelHandle(DocBsDocument document) {
        // 层级处理
        if (document.getParentId() != null) {
            DocBsDocument document1 = docBsDocumentMapper.selectById(document.getParentId());
            if (document1 != null) {
                //层级递增
                document.setFolderLevel(document1.getFolderLevel() + DocConstants.ONE);
            }
        } else {
            //默认0层
            document.setFolderLevel(0);
        }
        return document.getFolderLevel();
    }

    /**
     * 闭包表的处理
     */
    private void handleTree(DocBsDocument document, Long busId) {
        //1、自己到自己的数据
        DocBsDocumentTree docBsDocumentTree = new DocBsDocumentTree();
        docBsDocumentTree.setDocId(busId);
        docBsDocumentTree.setFatherId(busId);
        docBsDocumentTreeMapper.insert(docBsDocumentTree);
        if (document.getParentId() != null) {
            //2、自己和祖先的
            List<DocBsDocumentTree> docId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                    .eq(DocBsDocumentTree::getDocId, document.getParentId()));
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

    /**
     * 关联权限处理
     *
     */
    private void handleAuth(DocBsDocumentVO document, Long busId) {
        //先删后插  删除原先的权限
        documentUserMapper.delete(new LambdaQueryWrapper<DocBsDocumentUser>().eq(DocBsDocumentUser::getDocId, busId));
        //插入新的权限
        List<DocBsDocumentUser> userList = document.getDocBsDocumentUsers();

        //校验文档是否有一个以上的管理权限
        List<Integer> collect = userList.stream().map(DocBsDocumentUser::getPermissType).collect(Collectors.toList());
        AssertUtils.isTrue(!collect.contains(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "至少需要拥有一个管理权限");
        List<DocBsDocumentUser> docBsDocumentUsers = new ArrayList<>();
        for (DocBsDocumentUser docBsDocumentUser : userList) {
            AssertUtils.isNull(docBsDocumentUser.getPermissType(), "存在未选择权限!");
            AssertUtils.isNull(docBsDocumentUser.getType(), "参数错误");
            AssertUtils.isNull(docBsDocumentUser.getRelId(), "参数错误");
            docBsDocumentUser.setDocId(busId);
            docBsDocumentUsers.add(docBsDocumentUser);
        }
        MybatisBatch<DocBsDocumentUser> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocumentUsers);
        MybatisBatch.Method<DocBsDocumentUser> docMethod = new MybatisBatch.Method<>(DocBsDocumentUserMapper.class);
        docBatchs.execute(docMethod.insert());
    }

    /**
     * 给查询出来的文件夹赋权限
     */
    private void handleAuthFolder(AccountToken token, List<Long> busId, Long houseId, List<DocBsDocumentDTO> documentExtendList) {
        List<Long> teams = getTeamListByUser(token);
        List<DocBsDocumentDTO> docBsDocumentDTOS = docBsDocumentMapper.selectAuthFolderByBusIds(
                busId,houseId,token.getInstId(),DocConstants.INST,token.getDeptId(),DocConstants.DEPT,
                token.getId(),DocConstants.USER,teams,DocConstants.TEAM
        );
        List<DocBsDocumentDTO> permissMax = docCommonService.getPermissMax(docBsDocumentDTOS);
        Map<Long, List<DocBsDocumentDTO>> collect = permissMax.stream().collect(Collectors.groupingBy(DocBsDocumentDTO::getBusId));
        documentExtendList.forEach(s -> {
            List<DocBsDocumentDTO> list = collect.get(s.getBusId());
            if (!CollectionUtils.isEmpty(list)) {
                s.setPermissType(list.get(0).getPermissType());
            }
        });
    }

    private List<Long> getTeamListByUser(AccountToken token) {
        List<DocSysTeamUser> users = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, token.getId()));
        return users.stream().map(DocSysTeamUser::getTeamId).collect(Collectors.toList());
    }
    /**
     * 递归 列表文件夹展示
     */
    private List<DocBsDocumentDTO> handleChildren(AccountToken token, List<DocBsDocumentDTO> documentExtendList, Integer folderLevel, List<Long> folderIdList) {
        if (folderLevel < DocConstants.ZERO || CollectionUtils.isEmpty(documentExtendList)) {
            return null;
        }
        //获取所有父级folderIds
        List<Long> folderIds = documentExtendList.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
        //给文件夹赋权限
        handleAuthFolder(token, folderIds, null, documentExtendList);
        List<DocBsDocument> documentList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>().in(DocBsDocument::getParentId, folderIds).in(DocBsDocument::getBusId, folderIdList));
        List<DocBsDocumentDTO> bsDocumentExtendLevel = PageCopyListUtils.copyListProperties(documentList, DocBsDocumentDTO.class);

        Map<Long, List<DocBsDocumentDTO>> collect = bsDocumentExtendLevel.stream()
                .collect(Collectors.groupingBy(DocBsDocumentDTO::getParentId));
        for (DocBsDocumentDTO extend : documentExtendList) {
            extend.setChildren(collect.get(extend.getBusId()) == null ? new ArrayList<>()
                    : collect.get(extend.getBusId()));
        }
        return handleChildren(token, bsDocumentExtendLevel, folderLevel - DocConstants.ONE, folderIdList);
    }

    /**
     * 校验文件夹名称是否重复
     *
     */
    private void checkFolderName(DocBsDocumentVO document) {
        //校验同级文件夹是否重复
        Long integer = docBsDocumentMapper.selectCount(new LambdaQueryWrapper<DocBsDocument>()
                .eq(!ObjectUtils.isEmpty(document.getParentId()),DocBsDocument::getParentId, document.getParentId())
                .isNull(ObjectUtils.isEmpty(document.getParentId()), DocBsDocument::getParentId)
                .eq(DocBsDocument::getDocName, document.getDocName())
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .ne(!ObjectUtils.isEmpty(document.getBusId()), DocBsDocument::getBusId, document.getBusId())
                .eq(DocBsDocument::getDocType, DocConstants.COMPANY)
                .eq(DocBsDocument::getHouseId, document.getHouseId()));

        AssertUtils.isTrue(integer != null && integer.intValue() > 0, "文件夹名称重复");
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     *
     */
    private void handleAuthNum(Long busId, DocBsDocumentDTO docBsDocumentDTO) {
        // 计算机构部门团队用户数据
        List<DocBsDocumentUserDTO> docBsDocumentUserDTOS = documentUserMapper.selectListExtend( busId, DocConstants.ZERO);
        for(DocBsDocumentUserDTO s:docBsDocumentUserDTOS){
            SysUserDTO data = userApi.getUserByUserId(s.getRelId()).getData();
            SysInstDTO data1 = instApi.getInstByInstId(s.getRelId()).getData();
            SysDeptDTO data2 = deptApi.selectById(s.getRelId()).getData();
            if (null != data) {
                s.setUserIdStr(data.getName());
            }
            if (null != data1) {
                s.setInstIdStr(data1.getName());
            }
            if (null != data2) {
                s.setDeptIdStr(data2.getName());
            }
        }
        //根据type统计
        Map<Integer, Long> collect = docBsDocumentUserDTOS.stream().collect(Collectors.groupingBy(DocBsDocumentUserDTO::getType, Collectors.counting()));
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.INST))) {
            docBsDocumentDTO.setInstNum(collect.get(DocConstants.INST).intValue());
        } else {
            docBsDocumentDTO.setInstNum(DocConstants.ZERO);
        }
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.DEPT))) {
            docBsDocumentDTO.setDeptNum(collect.get(DocConstants.DEPT).intValue());
        } else {
            docBsDocumentDTO.setDeptNum(DocConstants.ZERO);
        }
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.TEAM))) {
            docBsDocumentDTO.setTeamNum(collect.get(DocConstants.TEAM).intValue());
        } else {
            docBsDocumentDTO.setTeamNum(DocConstants.ZERO);
        }
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.USER))) {
            docBsDocumentDTO.setUserNum(collect.get(DocConstants.USER).intValue());
        } else {
            docBsDocumentDTO.setUserNum(DocConstants.ZERO);
        }
    }

}
