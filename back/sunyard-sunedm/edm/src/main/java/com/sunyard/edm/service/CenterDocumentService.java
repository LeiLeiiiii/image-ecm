package com.sunyard.edm.service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.sunyard.edm.dto.DocBsDocumentSearchDTO;
import com.sunyard.edm.dto.ExtendPageDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocBsDocFlowDTO;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.dto.DocBsLevelFolderDTO;
import com.sunyard.edm.dto.DocCapacityDTO;
import com.sunyard.edm.mapper.DocBsDocFlowMapper;
import com.sunyard.edm.mapper.DocBsDocRelMapper;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsDocumentUserMapper;
import com.sunyard.edm.mapper.DocBsRecycleMapper;
import com.sunyard.edm.mapper.DocBsTagDocumentMapper;
import com.sunyard.edm.mapper.DocBsTaskMapper;
import com.sunyard.edm.mapper.DocSysHouseMapper;
import com.sunyard.edm.mapper.DocSysTagMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsDocFlow;
import com.sunyard.edm.po.DocBsDocRel;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocBsRecycle;
import com.sunyard.edm.po.DocBsTagDocument;
import com.sunyard.edm.po.DocBsTask;
import com.sunyard.edm.po.DocSysHouse;
import com.sunyard.edm.po.DocSysTag;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.edm.util.DocUtils;
import com.sunyard.edm.vo.AddOrUpdateDocumentListVO;
import com.sunyard.edm.vo.AddOrUpdateDocumentVO;
import com.sunyard.edm.vo.DocBsDocumentSearchVO;
import com.sunyard.edm.vo.DocBsMessageVO;
import com.sunyard.edm.vo.UpgradeCompanyVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.module.system.api.DeptApi;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * @author raochangmei
 * @date 11.15
 * @Desc 个人、企业文档库文件处理实现类
 */
@Service
@Slf4j
public class CenterDocumentService {

    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsDocRelMapper docBsDocRelMapper;
    @Resource
    private DocBsTagDocumentMapper docBsTagDocumentMapper;
    @Resource
    private DocSysTagMapper docSysTagMapper;
    @Resource
    private DocBsDocumentUserMapper docBsDocumentUserMapper;
    @Resource
    private DocBsTaskMapper docBsTaskMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocBsRecycleMapper docBsRecycleMapper;
    @Resource
    private DocBsDocFlowMapper docBsDocFlowMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DocSysHouseMapper docSysHouseMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private FileStorageApi docDelStorageFeign;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private FileHandleApi fileHandleApi;
    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private WorkbenchService workbenchService;
    @Resource
    private CenterQueryService queryGlobalService;
    @Resource
    private CenterDelStorageService centerDelStorageService;

    /**
     * 列表查询
     */
    public PageInfo<DocBsDocumentDTO> search(AccountToken token, DocBsDocumentSearchVO docBsDocumentExtend, PageForm pageForm) {
        handleSearch(docBsDocumentExtend, token);
        if (docBsDocumentExtend.getFolderIds() != null && docBsDocumentExtend.getFolderIds().size() == 0) {
            return new PageInfo<>(new ArrayList<>());
        }
        //todo LambdaQueryWrapper
        /*QueryWrapper<DocBsDocument> queryWrapper = new QueryWrapper<DocBsDocument>()
                .like(!StringUtils.isEmpty(docBsDocumentExtend.getAttchName()), "c.doc_name", docBsDocumentExtend.getAttchName())
                .eq(!StringUtils.isEmpty(docBsDocumentExtend.getAttchName()), "c.type", DocConstants.FILE)
                .between(docBsDocumentExtend.getCreateStartDate() != null, "a.create_time", docBsDocumentExtend.getCreateStartDate(), docBsDocumentExtend.getCreateEndDate())
                .between(docBsDocumentExtend.getUpdateStartDate() != null, "a.update_time", docBsDocumentExtend.getUpdateStartDate(), docBsDocumentExtend.getUpdateEndDate())
                .like(!StringUtils.isEmpty(docBsDocumentExtend.getDocName()), "a.doc_name", docBsDocumentExtend.getDocName())
                .eq(docBsDocumentExtend.getFolderId() != null, "a.folder_id", docBsDocumentExtend.getFolderId())
                .in(!CollectionUtils.isEmpty(docBsDocumentExtend.getFolderIds()), "a.folder_id", docBsDocumentExtend.getFolderIds())
                .eq("a.type", DocConstants.DOCUMENT)
                .eq("a.recycle_status", DocConstants.RECYCLE_STATUS_NORMAL);*/
        ExtendPageDTO extendPageDTO = new ExtendPageDTO();
        extendPageDTO.setAttchName(docBsDocumentExtend.getAttchName());
        extendPageDTO.setFile(DocConstants.FILE);
        extendPageDTO.setCreateStartDate(docBsDocumentExtend.getCreateStartDate());
        extendPageDTO.setCreateEndDate(docBsDocumentExtend.getCreateEndDate());
        extendPageDTO.setUpdateStartDate(docBsDocumentExtend.getUpdateStartDate());
        extendPageDTO.setUpdateEndDate(docBsDocumentExtend.getUpdateEndDate());
        extendPageDTO.setDocName(docBsDocumentExtend.getDocName());
        extendPageDTO.setFolderId(docBsDocumentExtend.getFolderId());
        extendPageDTO.setFolderIds(docBsDocumentExtend.getFolderIds());
        extendPageDTO.setDocument(DocConstants.DOCUMENT);
        extendPageDTO.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);

        //查寻处理
        return getDocBsDocumentExtendPageInfo(token, docBsDocumentExtend, pageForm, extendPageDTO, DocConstants.SEARCH_DOCUMENT_TYPE);
    }

    /**
     * 列表查询初始处理
     */
    private void handleSearch(DocBsDocumentSearchVO docBsDocumentExtend, AccountToken token) {
        if (DocConstants.COMPANY.equals(docBsDocumentExtend.getDocType())) {
            AssertUtils.isNull(docBsDocumentExtend.getHouseId(), "参数错误");
        }
        if (docBsDocumentExtend.getFolderId() == null || DocConstants.ZERO.equals(docBsDocumentExtend.getFolderId().intValue())) {
            docBsDocumentExtend.setFolderId(null);
            if (DocConstants.COMPANY.equals(docBsDocumentExtend.getDocType())) {
                Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(DocConstants.DOC_FOLDER_TREE_TYPE);
                SysParamDTO sysParam = sysParamDTOResult.getData();
                if (sysParam == null || DocConstants.DOC_STATUS_SET.equals(sysParam.getStatus()) || !DocConstants.DOC_FOLDER_TREE_TYPE_ALL.equals(sysParam.getValue())) {
                    docBsDocumentExtend.setFolderIds(new ArrayList<>());
                    List<Long> teams = getTeamListByUser(token);
                    List<DocBsDocumentDTO> list = docBsDocumentMapper.selectDocBsDocumentList(docBsDocumentExtend.getHouseId(),
                            token.getInstId(),DocConstants.INST,token.getDeptId(),DocConstants.DEPT,
                            token.getId(),DocConstants.USER,teams,DocConstants.TEAM);
                    if (!CollectionUtils.isEmpty(list)) {
                        //过滤出未删除的数据和空的数据
                        list = list.stream().filter(s -> !DocConstants.ONE.equals(s.getIsPermissDeleted())).collect(Collectors.toList());
                        if (!CollectionUtils.isEmpty(list)) {
                            List<Long> collect = list.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
                            docBsDocumentExtend.setFolderIds(collect);
                        }
                    }

                }
            }

        }
        if (docBsDocumentExtend.getCreateEndDate() != null) {
            Date createEndDate = docBsDocumentExtend.getCreateEndDate();
            Calendar c = Calendar.getInstance();
            c.setTime(createEndDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            //这是明天
            Date tomorrow = c.getTime();
            docBsDocumentExtend.setCreateEndDate(tomorrow);
        }
        if (docBsDocumentExtend.getUpdateEndDate() != null) {
            Date createEndDate = docBsDocumentExtend.getUpdateEndDate();
            Calendar c = Calendar.getInstance();
            c.setTime(createEndDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            //这是明天
            Date tomorrow = c.getTime();
            docBsDocumentExtend.setUpdateEndDate(tomorrow);
        }
    }

    private List<Long> getTeamListByUser(AccountToken token) {
        List<DocSysTeamUser> users = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, token.getId()));
        return users.stream().map(DocSysTeamUser::getTeamId).collect(Collectors.toList());
    }
    /**
     * 新增
     */
    private List<AddOrUpdateDocumentVO> add(AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend, Integer code) {
        List<AddOrUpdateDocumentVO> list = new ArrayList<>();
        list.add(docBsDocumentExtend);
        return batchAdd(token, list, code);
    }

    /**
     * 批量上传
     */
    @Lock4j
    @Transactional(rollbackFor = Exception.class)
    public List<AddOrUpdateDocumentVO> batchAdd(AccountToken token, List<AddOrUpdateDocumentVO> docBsDocumentExtends, Integer code) {
        AssertUtils.isNull(docBsDocumentExtends, "参数错误");

        Map<Long, List<AddOrUpdateDocumentVO>> collect = docBsDocumentExtends.stream().collect(Collectors.groupingBy(AddOrUpdateDocumentVO::getFolderId));
        if (collect.keySet() == null || collect.keySet().size() != 1) {
            AssertUtils.isTrue(true, "参数有误");
        }

        List<AddOrUpdateDocumentVO> ret = new ArrayList<>();
        Long folderSize = 0L;
        folderSize = batchAddDeatil(token, docBsDocumentExtends, code, ret, folderSize);

        //计算文件夹大小
        //1、获取文件夹的大小
        Long folderId = collect.keySet().stream().collect(Collectors.toList()).get(0);

        docCommonService.handleFolderSize(folderSize, folderId);
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //设置子线程共享
        RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
        centerDelStorageService.delTask();
        return ret;
    }

    /**
     * 批量新增的详细处理
     */
    private Long batchAddDeatil(AccountToken token, List<AddOrUpdateDocumentVO> docBsDocumentExtends, Integer code, List<AddOrUpdateDocumentVO> ret, Long folderSize) {
        List<DocBsDocument> docBsDocuments = new ArrayList<>();
        List<DocBsDocument> attchs = new ArrayList<>();
        List<DocBsDocFlow> docBsDocFlows = new ArrayList<>();
        for (AddOrUpdateDocumentVO docBsDocumentExtend : docBsDocumentExtends) {
            docBsDocumentExtend.setDocType(code);
            folderSize += docBsDocumentExtend.getDocSize();
            Long busId = snowflakeUtil.nextId();
            //后缀统一转为小写
            docBsDocumentExtend.setDocSuffix(docBsDocumentExtend.getDocSuffix().toLowerCase());
            docBsDocumentExtend.setType(DocConstants.DOCUMENT);
            docBsDocumentExtend.setBusId(busId);
            docBsDocumentExtend.setUpdateTime(new Date());
            docBsDocumentExtend.setUploadTime(new Date());
            docBsDocumentExtend.setDocCreator(token.getId());
            docBsDocumentExtend.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);

            if (code.equals(DocConstants.COMPANY)) {
                //企业
                docBsDocumentExtend.setDocStatus(DocConstants.DOC_STATUS_PUTAWAY);
                docBsDocumentExtend.setDocOwner(docBsDocumentExtend.getDocOwner());

                //标签关联创建
                saveTagRel(docBsDocumentExtend, busId);
                //权限创建
                saveUserRel(docBsDocumentExtend, busId);
                //所有者
                docBsDocumentExtend.setDocOwner(token.getId());
                //只添加企业文档数据到es
                queryGlobalService.addFullTextPath(docBsDocumentExtend);
            } else {
                //个人
                docBsDocumentExtend.setDocOwner(token.getId());
            }
            docBsDocumentExtend.setDocName(getSuffixFile(docBsDocumentExtend.getDocName()));
            docBsDocuments.add(docBsDocumentExtend);

            if (!CollectionUtils.isEmpty(docBsDocumentExtend.getAttchList())) {
                for (DocBsDocument attch : docBsDocumentExtend.getAttchList()) {
                    attch.setType(DocConstants.FILE);
                    attch.setDocType(docBsDocumentExtend.getDocType());
                    attch.setHouseId(docBsDocumentExtend.getHouseId());
                    attch.setFolderId(docBsDocumentExtend.getFolderId());
                    attch.setDocOwner(token.getId());
                    attch.setDocCreator(token.getId());
                    attch.setRelDoc(docBsDocumentExtend.getBusId());
                    attch.setUpdateTime(new Date());
                    attch.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);
                    folderSize += attch.getDocSize();
                    attchs.add(attch);
                }
//                addAttchList(token, docBsDocumentExtend, folderSize);
            }

            //添加文档动态
            DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
            docBsDocFlow.setDocId(docBsDocumentExtend.getBusId());
            docBsDocFlow.setUserId(docBsDocumentExtend.getDocCreator());
            docBsDocFlow.setFlowDate(new Date());
            docBsDocFlow.setFlowDescribe("上传");
            docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_UPLOAD);
            docBsDocFlows.add(docBsDocFlow);
        }
        MybatisBatch<DocBsDocument> docBsDocumentsBatch = new MybatisBatch<>(sqlSessionFactory, docBsDocuments);
        MybatisBatch.Method<DocBsDocument> docBsDocumentsMethod = new MybatisBatch.Method<>(DocBsDocumentMapper.class);
        docBsDocumentsBatch.execute(docBsDocumentsMethod.insert());
        if(!CollectionUtils.isEmpty(attchs)){
            MybatisBatch<DocBsDocument> attchsBatch = new MybatisBatch<>(sqlSessionFactory, attchs);
            MybatisBatch.Method<DocBsDocument> attchsMethod = new MybatisBatch.Method<>(DocBsDocumentMapper.class);
            attchsBatch.execute(attchsMethod.insert());
        }
        MybatisBatch<DocBsDocFlow> docBsDocFlowsBatch = new MybatisBatch<>(sqlSessionFactory, docBsDocFlows);
        MybatisBatch.Method<DocBsDocFlow> docBsDocFlowsMethod = new MybatisBatch.Method<>(DocBsDocFlowMapper.class);
        docBsDocFlowsBatch.execute(docBsDocFlowsMethod.insert());
        return folderSize;
    }


    private void saveUserRel(AddOrUpdateDocumentVO docBsDocumentExtend, Long busId) {
        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getUserTeamDeptListExtends())) {
            List<Integer> collect = docBsDocumentExtend.getUserTeamDeptListExtends().stream().map(DocBsDocumentUser::getPermissType).collect(Collectors.toList());
            AssertUtils.isTrue(!collect.contains(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "至少需要拥有一个管理权限");
            List<DocBsDocumentUser> docBsDocumentUsers = new ArrayList<>();
            docBsDocumentExtend.getUserTeamDeptListExtends().forEach(s -> {
                DocBsDocumentUser docBsDocumentUser = new DocBsDocumentUser();
                docBsDocumentUser.setDocId(busId);
                docBsDocumentUser.setType(s.getType() != null ? s.getType() : s.getRelType());
                docBsDocumentUser.setPermissType(s.getPermissType());
                docBsDocumentUser.setRelId(s.getRelId());
                docBsDocumentUsers.add(docBsDocumentUser);

            });
            MybatisBatch<DocBsDocumentUser> docBsDocumentUsersBatch = new MybatisBatch<>(sqlSessionFactory, docBsDocumentUsers);
            MybatisBatch.Method<DocBsDocumentUser> docBsDocumentUsersMethod = new MybatisBatch.Method<>(DocBsDocumentUserMapper.class);
            docBsDocumentUsersBatch.execute(docBsDocumentUsersMethod.insert());
        }
    }

    private void saveTagRel(AddOrUpdateDocumentVO docBsDocumentExtend, Long busId) {
        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getTagIds())) {
            List<DocBsTagDocument> tagDocuments = new ArrayList<>();
            docBsDocumentExtend.getTagIds().forEach(s -> {
                DocBsTagDocument document = new DocBsTagDocument();
                document.setDocId(busId);
                document.setTagId(s);
                tagDocuments.add(document);
            });
            MybatisBatch<DocBsTagDocument> tagDocumentsBatch = new MybatisBatch<>(sqlSessionFactory, tagDocuments);
            MybatisBatch.Method<DocBsTagDocument> tagDocumentsMethod = new MybatisBatch.Method<>(DocBsTagDocumentMapper.class);
            tagDocumentsBatch.execute(tagDocumentsMethod.insert());
        }
    }

    /**
     * 编辑
     */
    private AddOrUpdateDocumentVO update(AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend) {
        LambdaUpdateWrapper<DocBsDocument> eq = new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getDocName, docBsDocumentExtend.getDocName())
                .set(DocBsDocument::getDocDescribe, docBsDocumentExtend.getDocDescribe())
                .set(DocBsDocument::getUpdateTime, new Date())
                .eq(DocBsDocument::getBusId, docBsDocumentExtend.getBusId());
        DocBsDocument document1 = docBsDocumentMapper.selectById(docBsDocumentExtend.getBusId());


        //更新主文档
        handleUpdate(docBsDocumentExtend, eq, document1);
        docBsDocumentMapper.update(null, eq);
        //企业处理
        handleCompany(docBsDocumentExtend);
        if (docBsDocumentExtend.getDocSize() != null) {
            docBsDocumentExtend.setDocSize(docBsDocumentExtend.getDocSize() - document1.getDocSize());
        }
        //修改附件
        handleAttchUpadte(token, docBsDocumentExtend);
        //企业修改时连带修改ES数据
        if (docBsDocumentExtend.getDocType().equals(DocConstants.COMPANY)) {
            //es编辑 先删在添加
            queryGlobalService.delFullText(docBsDocumentExtend.getBusId().toString());
            //文档检索 文档所有者
            docBsDocumentExtend.setDocOwner(document1.getDocOwner());
            queryGlobalService.addFullTextPath(docBsDocumentExtend);
        }
        //添加文档动态
        DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
        docBsDocFlow.setDocId(docBsDocumentExtend.getBusId());
        docBsDocFlow.setUserId(token.getId());
        docBsDocFlow.setFlowDate(new Date());
        docBsDocFlow.setFlowDescribe("修改");
        docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_UPDATE);
        docBsDocFlowMapper.insert(docBsDocFlow);
        return null;
    }

    private void handleCompany(AddOrUpdateDocumentVO docBsDocumentExtend) {
        if (docBsDocumentExtend.getDocType().equals(DocConstants.COMPANY)) {
            //修改标签,先删后插
            handleSetTag(docBsDocumentExtend);
            //修改文档权限,先删后插
            handleSetUser(docBsDocumentExtend);

        }
    }

    private void handleSetUserList(AddOrUpdateDocumentVO docBsDocumentExtend) {
        docBsDocumentUserMapper.delete(new LambdaQueryWrapper<DocBsDocumentUser>().in(DocBsDocumentUser::getDocId, docBsDocumentExtend.getBusIds()));
        for (Long id : docBsDocumentExtend.getBusIds()) {
            saveUserRel(docBsDocumentExtend, id);
        }

    }

    private void handleSetUser(AddOrUpdateDocumentVO docBsDocumentExtend) {
        docBsDocumentUserMapper.delete(new LambdaQueryWrapper<DocBsDocumentUser>().eq(DocBsDocumentUser::getDocId, docBsDocumentExtend.getBusId()));
        saveUserRel(docBsDocumentExtend, docBsDocumentExtend.getBusId());
    }

    private void handleUpdate(AddOrUpdateDocumentVO docBsDocumentExtend, LambdaUpdateWrapper<DocBsDocument> eq, DocBsDocument document1) {
        //需要处理原始数据的情况
        if (!document1.getFileId().equals(docBsDocumentExtend.getFileId())) {
            //如果文件不相同，则需要去存储中将历史上传的数据删掉
            DocBsTask docBsTask = new DocBsTask();
            docBsTask.setRelId(document1.getFileId());
            docBsTask.setTaskType(DocConstants.TASK_DEL_FILE);
            docBsTask.setTaskStatus(DocConstants.DEL_STORAGE_PENDING);
            docBsTaskMapper.insert(docBsTask);
            eq.set(DocBsDocument::getDocSuffix, docBsDocumentExtend.getDocSuffix())
                    .set(DocBsDocument::getDocSize, docBsDocumentExtend.getDocSize())
                    .set(DocBsDocument::getFileId, docBsDocumentExtend.getFileId());
        }
    }

    /**
     * 重命名
     */
    public DocBsDocument updateDocumentName(Long busId, Long folderId, String docName, Integer code, AccountToken token) {
        AssertUtils.isNull(busId, "参数错误！");
        AssertUtils.isNull(folderId, "参数错误！");
        AssertUtils.isNull(docName, "参数错误！");

        if (code.equals(DocConstants.COMPANY)) {
            docCommonService.isEditPermiss(token, busId);
        }
        docName = getSuffixFile(docName);

        //重复名称校验
        DocBsDocument document = docBsDocumentMapper.selectOne(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getFolderId, folderId)
                .eq(DocBsDocument::getDocName, docName)
                .ne(DocBsDocument::getBusId, busId)
        );

        if (document == null) {
            docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                    .set(DocBsDocument::getDocName, docName).eq(DocBsDocument::getBusId, busId));
            DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
            docBsDocFlow.setDocId(busId);
            docBsDocFlow.setUserId(token.getId());
            docBsDocFlow.setFlowDate(new Date());
            docBsDocFlow.setFlowDescribe("重命名");
            docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_RENAME);
            docBsDocFlowMapper.insert(docBsDocFlow);
            return null;
        } else {
            return document;
        }
    }

    /**
     * 获取详情
     */
    public DocBsDocumentDTO getInfo(Long busId, Long u) {
        DocBsDocumentDTO documentExtend = getDocBsDocumentDTO(busId, u);
        return documentExtend;
    }

    private DocBsDocumentDTO getDocBsDocumentDTO(Long busId, Long u) {
        AssertUtils.isNull(busId, "参数错误！");
        DocBsDocument document = docBsDocumentMapper.selectById(busId);
        AssertUtils.isNull(document, "当前文档不存在！");
        DocBsDocumentDTO documentExtend = new DocBsDocumentDTO();
        DocBsDocumentDTO documentExtend1 = new DocBsDocumentDTO();
        BeanUtils.copyProperties(document, documentExtend1);

        //创建人处理
        if (document.getDocOwner() != null) {
            Result<SysUserDTO> result = userApi.getUserByUserId(document.getDocOwner());
            SysUserDTO sysUserDTO = result.getData();
            documentExtend1.setDocOwnerStr(sysUserDTO.getName());
        }

        if (document.getDocCreator() != null) {
            Result<SysUserDTO> result = userApi.getUserByUserId(document.getDocCreator());
            SysUserDTO sysUserDTO = result.getData();
            documentExtend1.setDocCreatorStr(sysUserDTO.getName());
        }

        //文件目录处理
        documentExtend1.setFolderIdAllStr(docCommonService.handleFolderAll(document.getFolderId()));

        documentExtend.setDocBsDocument(documentExtend1);

        //关联附件列表
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getRelDoc, busId)
                .eq(DocBsDocument::getType, DocConstants.FILE));
        List<DocBsDocumentDTO> list = PageCopyListUtils.copyListProperties(docBsDocuments, DocBsDocumentDTO.class);
        docCommonService.handleDocSize(list);
        documentExtend.setAttchList(list);

        //动态
        handleFlow(busId, documentExtend);

        handleInfo(busId, documentExtend, documentExtend1);
        //添加’最近打开‘记录
        if (!ObjectUtils.isEmpty(u)) {
            workbenchService.addRecently(busId, u);
        }
        return documentExtend;
    }

    /**
     * 获取关联文档详情
     */
    public DocBsDocumentDTO getAssociationInfo(Long busId, AccountToken token) {
        //判断关联文档权限
        AssertUtils.isNull(busId, "参数错误！");
        DocBsDocument document = docBsDocumentMapper.selectById(busId);
        AssertUtils.isNull(document, "当前文档不存在！");
        //查找团队
        List<DocSysTeamUser> users = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, token.getId()));
        List<Long> teams  = users.stream().map(DocSysTeamUser::getTeamId).collect(Collectors.toList());
        List<DocBsDocumentUser> docBsDocumentUsers = docBsDocumentUserMapper.selectList(new LambdaQueryWrapper<DocBsDocumentUser>()
                .eq(DocBsDocumentUser::getDocId, busId)
                .eq(DocBsDocumentUser::getIsDeleted, DocConstants.DELETED_NO)
                .and(m -> m.or(s -> s.eq(DocBsDocumentUser::getRelId, token.getInstId()).eq(DocBsDocumentUser::getType, DocConstants.INST))
                        .or(s -> s.eq(DocBsDocumentUser::getRelId, token.getDeptId()).eq(DocBsDocumentUser::getType, DocConstants.DEPT))
                        .or(s -> s.eq(DocBsDocumentUser::getRelId, token.getId()).eq(DocBsDocumentUser::getType, DocConstants.USER))
                        .or(!CollectionUtils.isEmpty(teams), s -> s.in(DocBsDocumentUser::getRelId, teams).eq(DocBsDocumentUser::getType, DocConstants.TEAM)))
        );
        if(CollectionUtils.isEmpty(docBsDocumentUsers)){
            AssertUtils.isTrue(true, "没有查看该文档权限！");
        }
        DocBsDocumentDTO documentExtend = getDocBsDocumentDTO(busId, token.getId());
        return documentExtend;
    }

    /**
     * 动态处理
     */
    private void handleFlow(Long busId, DocBsDocumentDTO documentExtend) {
        List<DocBsDocFlow> flows = docBsDocFlowMapper.selectList(new LambdaQueryWrapper<DocBsDocFlow>()
                .eq(DocBsDocFlow::getDocId, busId));
        if (!CollectionUtils.isEmpty(flows)) {
            List<DocBsDocFlowDTO> docBsDocFlowDTOS = PageCopyListUtils.copyListProperties(flows, DocBsDocFlowDTO.class);
            List<Long> userIds = docBsDocFlowDTOS.stream().map(DocBsDocFlowDTO::getUserId).collect(Collectors.toList());
            Result<List<SysUserDTO>> userId = userApi.getUserListByUserIds(userIds.toArray(new Long[userIds.size()]));
            Map<Long, List<SysUserDTO>> collect = userId.getData().stream().collect(Collectors.groupingBy(SysUserDTO::getUserId));
            docBsDocFlowDTOS.forEach(s -> {
                List<SysUserDTO> sysUsers = collect.get(s.getUserId());
                if (!CollectionUtils.isEmpty(sysUsers)) {
                    s.setUserIdStr(sysUsers.get(0).getName());
                }
            });
            documentExtend.setDocBsDocFlowList(docBsDocFlowDTOS);
        }
    }

    /**
     * 企业详情处理
     */
    private void handleInfo(Long busId, DocBsDocumentDTO documentExtend, DocBsDocumentDTO documentExtend1) {
        if (documentExtend1.getDocType().equals(DocConstants.COMPANY)) {
            //文档关联的标签
            List<DocBsTagDocument> tagDocuments = docBsTagDocumentMapper.selectList(new LambdaQueryWrapper<DocBsTagDocument>()
                    .eq(DocBsTagDocument::getDocId, busId));
            if (!CollectionUtils.isEmpty(tagDocuments)) {
                List<Long> tagIds = tagDocuments.stream().map(DocBsTagDocument::getTagId).collect(Collectors.toList());
                List<DocSysTag> tags = docSysTagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                        .in(DocSysTag::getTagId, tagIds));
                if (!CollectionUtils.isEmpty(tags)) {
                    Map<Long, List<DocSysTag>> collect2 = tags.stream().collect(Collectors.groupingBy(DocSysTag::getTagId));

                    List<Long> collect1 = tags.stream().map(DocSysTag::getParentId).collect(Collectors.toList());

                    List<DocSysTag> tagList1 = docSysTagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                            .in(DocSysTag::getTagId, collect1));
                    tagList1.addAll(tags);
                    Map<Long, List<DocSysTag>> collect3 = tagList1.stream().collect(Collectors.groupingBy(DocSysTag::getTagId));

                    List<String> list = new ArrayList<>();
                    for (Long id : collect2.keySet()) {
                        List<DocSysTag> tagList = collect2.get(id);
                        List<DocSysTag> all = new ArrayList();
                        for (DocSysTag tag : tagList) {
                            if (tag.getParentId() != DocConstants.ZERO.intValue()) {
                                DocSysTag tag1 = collect3.get(tag.getParentId()).get(0);
                                all.add(tag1);
                            } else {
                                all.add(tag);
                            }
                        }
                        //重新排序
                        all = all.stream()
                                .sorted(Comparator.comparing(DocSysTag::getTagLevel)).collect(Collectors.toList());

                        List<String> collect = all.stream().map(DocSysTag::getTagName).collect(Collectors.toList());
                        String join = Strings.join(collect, "/");
                        list.add(join);
                    }

                    documentExtend.setTagIdStr(Strings.join(list, "，"));
                    documentExtend.setDocSysTags(tags);
                }
            }

            //文档关联的文档
            List<DocBsDocRel> docBsDocRels = docBsDocRelMapper.selectList(new LambdaQueryWrapper<DocBsDocRel>().eq(DocBsDocRel::getDocId, busId).or().eq(DocBsDocRel::getRelId, busId));
            if (!CollectionUtils.isEmpty(docBsDocRels)) {
                List<Long> collect = docBsDocRels.stream().map(DocBsDocRel::getRelId).collect(Collectors.toList());
                collect.addAll(docBsDocRels.stream().map(DocBsDocRel::getDocId).collect(Collectors.toList()));

                List<DocBsDocument> relDoc = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                        .in(DocBsDocument::getBusId, collect).ne(DocBsDocument::getBusId, busId));
                //计算文件大小
                List<DocBsDocumentDTO> docBsDocuments1 = PageCopyListUtils.copyListProperties(relDoc, DocBsDocumentDTO.class);
                docCommonService.handleDocSize(docBsDocuments1);
                documentExtend.setDocumentList(docBsDocuments1);
            }

        }
    }


    private void handleAttchUpadte(AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend) {
        Long folderSize = docBsDocumentExtend.getDocSize();
        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getAttchList())) {
            //附件不为空，则需要获取对应的新增，和删除的附件
            List<Long> fileNew = docBsDocumentExtend.getAttchList().stream().map(DocBsDocument::getFileId).collect(Collectors.toList());
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FILE)
                    .eq(DocBsDocument::getRelDoc, docBsDocumentExtend.getBusId()));
            List<Long> fileOld = docBsDocuments.stream().map(DocBsDocument::getFileId).collect(Collectors.toList());
            List<DocBsDocument> add = new ArrayList<>();
            List<DocBsDocument> del = new ArrayList<>();
            docBsDocumentExtend.getAttchList().forEach(s -> {
                if (!fileOld.contains(s.getFileId())) {
                    //新的有老得没有，需要新增
                    add.add(s);
                }
            });

            docBsDocuments.forEach(s -> {
                if (!fileNew.contains(s.getFileId())) {
                    //老得有新的没有，需要删除
                    del.add(s);
                }
            });

            for (DocBsDocument d : del) {
                folderSize -= d.getDocSize();
            }
            List<DocBsDocument> batchs = new ArrayList<>();
            for (DocBsDocument attch : add) {
                attch.setType(DocConstants.FILE);
                attch.setDocType(docBsDocumentExtend.getDocType());
                attch.setHouseId(docBsDocumentExtend.getHouseId());
                attch.setFolderId(docBsDocumentExtend.getFolderId());
                attch.setDocOwner(token.getId());
                attch.setDocCreator(token.getId());
                attch.setRelDoc(docBsDocumentExtend.getBusId());
                attch.setUpdateTime(new Date());
                attch.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);
                folderSize += attch.getDocSize();
                batchs.add(attch);
            }
            if(CollectionUtil.isNotEmpty(batchs)){
                MybatisBatch<DocBsDocument> docBatchs = new MybatisBatch<>(sqlSessionFactory, batchs);
                MybatisBatch.Method<DocBsDocument> docMethod = new MybatisBatch.Method<>(DocBsDocumentMapper.class);
                docBatchs.execute(docMethod.insert());
            }
            handleDelAttchList(del);
        } else {
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FILE)
                    .eq(DocBsDocument::getRelDoc, docBsDocumentExtend.getBusId()));
            for (DocBsDocument d : docBsDocuments) {
                folderSize -= d.getDocSize();
            }
            handleDelAttchList(docBsDocuments);

        }

        //更新大小
        docCommonService.handleFolderSize(folderSize, docBsDocumentExtend.getFolderId());
    }

    private void handleDelAttchList(List<DocBsDocument> docBsDocuments) {
        List<DocBsTask> docBsTasks = new ArrayList<>();
        for (DocBsDocument d : docBsDocuments) {
            //1、新增删除存储任务
            DocBsTask docBsTask = new DocBsTask();
            docBsTask.setRelId(d.getFileId());
            docBsTask.setTaskType(DocConstants.TASK_DEL_FILE);
            docBsTask.setTaskStatus(DocConstants.DEL_STORAGE_PENDING);
            docBsTasks.add(docBsTask);
            //2、未关联的附件数据，则将对应的附件全部删掉
            docBsDocumentMapper.deleteById(d.getBusId());
        }
        if(CollectionUtil.isNotEmpty(docBsTasks)){
            MybatisBatch<DocBsTask> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsTasks);
            MybatisBatch.Method<DocBsTask> docMethod = new MybatisBatch.Method<>(DocBsTaskMapper.class);
            docBatchs.execute(docMethod.insert());
        }
    }

    private DocBsDocument checkDoc(AddOrUpdateDocumentVO docBsDocumentExtend) {
        AssertUtils.isNull(docBsDocumentExtend.getDocName(), "参数错误！");
        AssertUtils.isNull(docBsDocumentExtend.getFileId(), "参数错误！");
        AssertUtils.isNull(docBsDocumentExtend.getDocSize(), "参数错误！");
        AssertUtils.isNull(docBsDocumentExtend.getFolderId(), "参数错误！");

        //附件校验
        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getAttchList())) {
            List<DocBsDocument> attchList = docBsDocumentExtend.getAttchList();
            for (DocBsDocument attch : attchList) {
                AssertUtils.isNull(attch.getDocSize(), "参数错误！");
                AssertUtils.isNull(attch.getFileId(), "参数错误！");
                AssertUtils.isNull(attch.getDocName(), "参数错误！");
            }
        }

        return handleCheckName(docBsDocumentExtend);
    }

    /**
     * 重命名校验
     */
    private DocBsDocument handleCheckName(AddOrUpdateDocumentVO docBsDocumentExtend) {
        if (!StringUtils.isEmpty(docBsDocumentExtend.getDocName())) {
            docBsDocumentExtend.setDocName(getSuffixFile(docBsDocumentExtend.getDocName()));
            if (docBsDocumentExtend.getBusId() == null) {
                //重复名称校验
                DocBsDocument document = docBsDocumentMapper.selectOne(new LambdaQueryWrapper<DocBsDocument>()
                        .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                        .eq(DocBsDocument::getDocType, docBsDocumentExtend.getDocType())
                        .eq(DocBsDocument::getFolderId, docBsDocumentExtend.getFolderId())
                        .eq(DocBsDocument::getDocName, docBsDocumentExtend.getDocName())
                );
                return document;
            } else {
                //修改
                //重复名称校验
                DocBsDocument document = docBsDocumentMapper.selectOne(new LambdaQueryWrapper<DocBsDocument>()
                        .eq(DocBsDocument::getFolderId, docBsDocumentExtend.getFolderId())
                        .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                        .eq(DocBsDocument::getDocName, docBsDocumentExtend.getDocName())
                        .ne(DocBsDocument::getBusId, docBsDocumentExtend.getBusId())
                );
                return document;
            }
        }
        return null;
    }

    /**
     * 加入回收站
     */
    @Lock4j(keys = "#busIds")
    @Transactional(rollbackFor = Exception.class)
    public void addRecycle(AccountToken token, Long[] busIds, Integer code) {
        if (code.equals(DocConstants.COMPANY)) {
            docCommonService.isMangePermiss(token, busIds);
        }

        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>().in(DocBsDocument::getBusId, busIds));

        Date date = docCommonService.getRecycleDateByParam();
        List<DocBsRecycle> docBsRecycles = new ArrayList<>();
        for (DocBsDocument document : docBsDocuments) {
            //加入回收站
            DocBsRecycle docBsRecycle = new DocBsRecycle();
            docBsRecycle.setDocId(document.getBusId());
            docBsRecycle.setDelDate(new Date());
            docBsRecycle.setRecycleDate(date);
            docBsRecycles.add(docBsRecycle);
        }
        MybatisBatch<DocBsRecycle> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsRecycles);
        MybatisBatch.Method<DocBsRecycle> docMethod = new MybatisBatch.Method<>(DocBsRecycleMapper.class);
        docBatchs.execute(docMethod.insert());

        Map<Integer, List<DocBsDocument>> collect = docBsDocuments.stream().collect(Collectors.groupingBy(DocBsDocument::getType));

        //文档加入回收站
        List<DocBsDocument> documentList = collect.get(DocConstants.DOCUMENT);
        if (!CollectionUtils.isEmpty(documentList)) {
            List<Long> dos = documentList.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
            docCommonService.handleRecycleDoc(dos, date, token);
        }

        //文件夹加入回收站
        List<DocBsDocument> folderList = collect.get(DocConstants.FOLDER);
        if (CollectionUtils.isEmpty(folderList)) {
            return;
        }

        for (DocBsDocument document : folderList) {
            docCommonService.handleRecycleFolder(document.getBusId(), date, token);
        }
    }

    /**
     * 上架到企业
     */
    @Lock4j(keys = "#upgradeCompanyVo.docId")
    @Transactional(rollbackFor = Exception.class)
    public void upgradeCompany(UpgradeCompanyVO upgradeCompanyVo) {
        AssertUtils.isNull(upgradeCompanyVo.getDocId(), "参数错误");
        AssertUtils.isNull(upgradeCompanyVo.getHouseId(), "参数错误");
        AssertUtils.isNull(upgradeCompanyVo.getFolderId(), "参数错误");
        AssertUtils.isNull(upgradeCompanyVo.getUserList(), "参数错误");

        DocBsDocument document = docBsDocumentMapper.selectById(upgradeCompanyVo.getDocId());

        docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getHouseId, upgradeCompanyVo.getHouseId())
                .set(DocBsDocument::getFolderId, upgradeCompanyVo.getFolderId())
                .set(DocBsDocument::getDocType, DocConstants.COMPANY)
                .set(DocBsDocument::getDocStatus, DocConstants.DOC_STATUS_PUTAWAY)
                .eq(DocBsDocument::getBusId, upgradeCompanyVo.getDocId())
        );

        docCommonService.handleFileSize(document);

        AddOrUpdateDocumentVO vo = new AddOrUpdateDocumentVO();
        BeanUtils.copyProperties(document, vo);
        vo.setTagIds(upgradeCompanyVo.getDocSysTags());
        vo.setUserTeamDeptListExtends(upgradeCompanyVo.getUserList());
        vo.setBusId(upgradeCompanyVo.getDocId());
        //修改标签,先删后插
        handleSetTag(vo);
        //修改文档权限,先删后插
        handleSetUser(vo);
        //发起通知
        if (DocConstants.MSG_YES.equals(upgradeCompanyVo.getIsMsg())) {
            List<Long> acceptUserId = getAcceptUserId(upgradeCompanyVo.getUserList());
            DocBsLevelFolderDTO docBsLevelFolderDTO = docBsDocumentMapper.searchLevelFolder(upgradeCompanyVo.getFolderId());
            DocSysHouse docSysHouse = docSysHouseMapper.selectById(upgradeCompanyVo.getHouseId());
            List<DocBsMessageVO> newDocumentMsgBean = createNewDocumentMsgBean(acceptUserId, upgradeCompanyVo.getHouseId(), upgradeCompanyVo.getFolderId(), docSysHouse.getHouseName() + "/" + docBsLevelFolderDTO.getFolderName());
            workbenchService.addMessageBatch(newDocumentMsgBean);
        }
        queryGlobalService.addFullTextPath(vo);
    }


    /**
     * 移动文档至不同的文件夹
     */
    public void moveDoc(AccountToken token, Long busId, Long folderId, Integer code) {
        Long[] longs = {busId};
        moveDocBatch(token, longs, folderId, code);
    }

    /**
     * 校验名称是否重复
     */
    public List<Map> checkDocumentName(Long folderId, String[] docNames) {
        AssertUtils.isNull(folderId, "参数错误");
        AssertUtils.isNull(docNames, "参数错误");

        List<String> is = new ArrayList<>();
        //将文件后缀全部转为小写再去匹配
        for (String s : docNames) {
            is.add(getSuffixFile(s));
        }

        List<String> names = new ArrayList<>();
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper
                .selectList(new LambdaQueryWrapper<DocBsDocument>()
                        .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                        .eq(DocBsDocument::getFolderId, folderId));
        List<String> collect = docBsDocuments.stream().map(DocBsDocument::getDocName).collect(Collectors.toList());
        is.forEach(s -> {
            if (collect.contains(s)) {
                names.add(s);
            }
        });

        List<Map> ret = new ArrayList<>();
        //重复的名称，获取（1）不重复的名称
        for (String s : names) {
            Map map = new HashMap(DocConstants.SIXTEEN);
            map.put("old", s);
            handleReName(collect, map, s);

            ret.add(map);
        }
        return ret;
    }

    private void handleReName(List<String> collect, Map map, String s) {
        StringBuffer s11 = new StringBuffer();
        StringBuffer frist = new StringBuffer();
        String two = "";
        two = getSuffix(s, s11, frist, two);
        if (!collect.contains(s11.toString())) {
            map.put("new", s11.toString());
            map.put("newFrist", frist.toString());
            map.put("newTwo", two);
        } else {
            handleReName(collect, map, s11.toString());
        }
    }

    private String getSuffix(String s, StringBuffer s11, StringBuffer frist, String two) {
        String[] split = s.split("\\.");
        for (int i = 0; i < split.length; i++) {
            if (i < split.length - 1) {
                s11.append(split[i] + "(1).");
                frist.append(split[i] + "(1)");
            } else {
                s11.append(split[i]);
                two = "." + split[i];
            }
        }
        return two;
    }

    /**
     * 删除文档
     */
    public void delDoc(Long busId, Integer code, AccountToken token) {
        Long[] objects1 = new Long[]{busId};
        delDocBatch(objects1, code, token);
    }

    /**
     * 所有者模糊搜索
     */
    public List<SysUserDTO> queryUserList(AccountToken token, String name) {
        Result<List<SysUserDTO>> userDetailByName = userApi.getUserDetailByName(name);
        List<SysUserDTO> filteredUsers = userDetailByName.getData().stream()
                .filter(user -> token.getInstId().equals(user.getInstId()))
                .collect(Collectors.toList());
        return filteredUsers;
    }

    /**
     * 批量保存
     */
    public List<AddOrUpdateDocumentVO> saveList(AccountToken token, AddOrUpdateDocumentListVO docBsDocumentExtend, Integer company) {
        List<AddOrUpdateDocumentVO> addOrUpdateDocumentVOS = docBsDocumentExtend.getAddOrUpdateDocumentVOS();
        for (AddOrUpdateDocumentVO vo : addOrUpdateDocumentVOS) {
            vo.setUserTeamDeptListExtends(docBsDocumentExtend.getUserTeamDeptListExtends());
            vo.setDocOwner(docBsDocumentExtend.getDocOwner());
            vo.setTagIds(docBsDocumentExtend.getTagIds());
        }
        return batchAdd(token, addOrUpdateDocumentVOS, company);
    }

    /**
     * 设置标签
     */
    public void setTag(AddOrUpdateDocumentVO vo, AccountToken token) {
        AssertUtils.isNull(vo.getBusIds(), "参数错误");
        docCommonService.isEditPermiss(token, vo.getBusIds());
        handleSetTagList(vo);
        //更新es里面的标签集合
        queryGlobalService.updateFullText(vo);
    }

    private void handleSetTagList(AddOrUpdateDocumentVO vo) {
        if (!CollectionUtils.isEmpty(vo.getBusIds())) {
            for (Long id : vo.getBusIds()) {
                //修改标签,先删后插
                docBsTagDocumentMapper.delete(new LambdaQueryWrapper<DocBsTagDocument>().eq(DocBsTagDocument::getDocId, id));
                saveTagRel(vo, id);
            }
        }
    }

    private void handleSetTag(AddOrUpdateDocumentVO vo) {
        docBsTagDocumentMapper.delete(new LambdaQueryWrapper<DocBsTagDocument>().eq(DocBsTagDocument::getDocId, vo.getBusId()));
        saveTagRel(vo, vo.getBusId());
    }

    /**
     * 设置权限
     */
    public void setUserDept(AddOrUpdateDocumentVO vo, AccountToken token) {
        AssertUtils.isNull(vo.getBusIds(), "参数错误");
        //至少要有一个管理者
        List<DocBsDocumentUserDTO> userTeamDeptListExtends = vo.getUserTeamDeptListExtends();
        Boolean b = true;
        if(!CollectionUtils.isEmpty(userTeamDeptListExtends)){
            List<DocBsDocumentUserDTO> collect = userTeamDeptListExtends.stream().filter(s -> s.getPermissType().equals(2)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(collect)){
                b=false;
            }
        }
        AssertUtils.isTrue(b, "至少有一个管理者");
        docCommonService.isEditPermiss(token, vo.getBusIds());
        //修改文档权限,先删后插
        handleSetUserList(vo);
    }

    /**
     * 关联文档
     */
    @Lock4j(keys = "#vo.busIds")
    @Transactional(rollbackFor = Exception.class)
    public void relDoc(AccountToken token, AddOrUpdateDocumentVO vo) {
        AssertUtils.isNull(vo.getBusIds(), "参数错误");
        docCommonService.isEditPermiss(token, vo.getBusIds());

        for (Long id : vo.getBusIds()) {
            docBsDocRelMapper.delete(new LambdaQueryWrapper<DocBsDocRel>().eq(DocBsDocRel::getDocId, id));
            if (!CollectionUtils.isEmpty(vo.getDocIds())) {
                List<DocBsDocRel> docBsDocRels = new ArrayList<>();
                vo.getDocIds().forEach(s -> {
                    DocBsDocRel docBsDocRel = new DocBsDocRel();
                    docBsDocRel.setDocId(id);
                    docBsDocRel.setRelId(s);
                    docBsDocRels.add(docBsDocRel);
                });
                MybatisBatch<DocBsDocRel> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocRels);
                MybatisBatch.Method<DocBsDocRel> docMethod = new MybatisBatch.Method<>(DocBsDocRelMapper.class);
                docBatchs.execute(docMethod.insert());
            }
        }

    }


    /**
     * 关联文档-文档列表
     */
    public PageInfo<DocBsDocumentDTO> relDocList(AccountToken token, DocBsDocumentSearchVO docBsDocumentExtend, PageForm pageForm) {
        docBsDocumentExtend.setShowFlag(true);
        return search(token, docBsDocumentExtend, pageForm);

    }

    /**
     * 下架
     */
    public void soldOut(Long[] busId, AccountToken token) {
        AssertUtils.isNull(busId, "参数错误");
        for (Long id : busId) {
            docCommonService.isMangePermiss(token, id);
        }
        docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getDocStatus, DocConstants.DOC_STATUS_OUT)
                .set(DocBsDocument::getLowerTime, new Date())
                .in(DocBsDocument::getBusId, busId));
        List<DocBsDocFlow> docBsDocFlows = new ArrayList<>();
        for (Long id : busId) {
            DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
            docBsDocFlow.setDocId(id);
            docBsDocFlow.setUserId(token.getId());
            docBsDocFlow.setFlowDate(new Date());
            docBsDocFlow.setFlowDescribe("下架");
            docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_OFF_SHELF);
            docBsDocFlows.add(docBsDocFlow);
        }
        MybatisBatch<DocBsDocFlow> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocFlows);
        MybatisBatch.Method<DocBsDocFlow> docMethod = new MybatisBatch.Method<>(DocBsDocFlowMapper.class);
        docBatchs.execute(docMethod.insert());

    }

    /**
     * 获取详情
     *
     */
    public Map getInfoUser(Long busId, AccountToken token, PageForm pageForm) {
        AssertUtils.isNull(busId, "参数错误");
        String describes = "";
        LambdaQueryWrapper<DocBsDocumentUser> docId = new LambdaQueryWrapper<DocBsDocumentUser>().eq(DocBsDocumentUser::getDocId, busId);
        LambdaQueryWrapper<DocBsDocumentUser> user1 = docId.clone().eq(DocBsDocumentUser::getType, DocConstants.USER);
        Long aLong = docBsDocumentUserMapper.selectCount(user1);
        if (aLong.intValue() > 0) {
            describes = aLong.intValue() + "用户,";
        }

        LambdaQueryWrapper<DocBsDocumentUser> inst = docId.clone().eq(DocBsDocumentUser::getType, DocConstants.INST);
        Long instNum = docBsDocumentUserMapper.selectCount(inst);
        if (instNum.intValue() > 0) {
            describes = describes + instNum.intValue() + "机构,";
        }

        LambdaQueryWrapper<DocBsDocumentUser> dept = docId.clone().eq(DocBsDocumentUser::getType, DocConstants.DEPT);
        Long deptNum = docBsDocumentUserMapper.selectCount(dept);
        if (deptNum.intValue() > 0) {
            describes = describes + deptNum.intValue() + "部门,";
        }


        LambdaQueryWrapper<DocBsDocumentUser> team = docId.clone().eq(DocBsDocumentUser::getType, DocConstants.TEAM);
        Long teamNum = docBsDocumentUserMapper.selectCount(team);
        if (teamNum.intValue() > 0) {
            describes = describes + teamNum.intValue() + "团队,";
        }
        if (describes.length() > 0) {
            describes = describes.substring(0, describes.length() - 1);
        }

        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocBsDocumentUserDTO> docBsDocumentUserDTOS = docBsDocumentUserMapper.selectListExtend( busId,DocConstants.DELETED_NO);
        for (DocBsDocumentUserDTO s : docBsDocumentUserDTOS) {
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
        Map map = new HashMap(DocConstants.SIXTEEN);
        map.put("page", new PageInfo<>(docBsDocumentUserDTOS));
        map.put("describes", describes);
        return map;
    }

    /**
     * 查询文件夹
     */
    public PageInfo<DocBsDocumentDTO> searchNoFolder(AccountToken token, DocBsDocumentSearchVO docBsDocumentExtend, PageForm pageForm) {
        handleSearch(docBsDocumentExtend, token);
        if (docBsDocumentExtend.getFolderId() != null && DocConstants.ZERO.equals(docBsDocumentExtend.getFolderId().intValue())) {
            docBsDocumentExtend.setFolderId(null);
        }
        //todo LambdaQueryWrapper
        /*QueryWrapper<DocBsDocument> queryWrapper = new QueryWrapper<DocBsDocument>()
                .eq(!StringUtils.isEmpty(docBsDocumentExtend.getAttchName()), "c.doc_name", docBsDocumentExtend.getAttchName())
                .eq(!StringUtils.isEmpty(docBsDocumentExtend.getAttchName()), "c.type", DocConstants.FILE)
                .between(docBsDocumentExtend.getCreateStartDate() != null, "a.create_time", docBsDocumentExtend.getCreateStartDate(), docBsDocumentExtend.getCreateEndDate())
                .between(docBsDocumentExtend.getUpdateStartDate() != null, "a.update_time", docBsDocumentExtend.getUpdateStartDate(), docBsDocumentExtend.getUpdateEndDate())
                .like(!StringUtils.isEmpty(docBsDocumentExtend.getDocName()), "a.doc_name", docBsDocumentExtend.getDocName())
                .eq(docBsDocumentExtend.getFolderId() != null, "a.folder_id", docBsDocumentExtend.getFolderId())
                .in("a.type", DocConstants.DOCUMENT, DocConstants.FOLDER)
                .eq("a.recycle_status", DocConstants.RECYCLE_STATUS_NORMAL);*/

        ExtendPageDTO extendPageDTO = new ExtendPageDTO();
        extendPageDTO.setAttchName(docBsDocumentExtend.getAttchName());
        extendPageDTO.setFile(DocConstants.FILE);
        extendPageDTO.setCreateStartDate(docBsDocumentExtend.getCreateStartDate());
        extendPageDTO.setCreateEndDate(docBsDocumentExtend.getCreateEndDate());
        extendPageDTO.setUpdateStartDate(docBsDocumentExtend.getUpdateStartDate());
        extendPageDTO.setUpdateEndDate(docBsDocumentExtend.getUpdateEndDate());
        extendPageDTO.setDocName(docBsDocumentExtend.getDocName());
        extendPageDTO.setFolderId(docBsDocumentExtend.getFolderId());
        List<Integer> types = new ArrayList<>();
        types.add(DocConstants.DOCUMENT);
        types.add(DocConstants.FOLDER);
        extendPageDTO.setTypes(types);
        extendPageDTO.setRecycleStatus(DocConstants.RECYCLE_STATUS_NORMAL);
        return getDocBsDocumentExtendPageInfo(token, docBsDocumentExtend, pageForm, extendPageDTO, DocConstants.SEARCH_FOLDER_TYPE);
    }

    /**
     * 个人已使用容量
     */
    public DocCapacityDTO usedCapacity(AccountToken token) {
        String remark = "";
        DocCapacityDTO docCapacityDTO = new DocCapacityDTO();
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(DocConstants.DOC_MAXIMUM_SIZE);
        SysParamDTO sysParam = sysParamDTOResult.getData();
        String value = "100";
        if (sysParam != null) {
            value = sysParam.getValue();
        }
        //总量
        long l1 = Long.parseLong(value) * 1024 * 1024 * 1024;
        remark = "/" + sysParam.getValue() + "GB";

        List<DocBsDocument> recl = getRecl(token);

        //回收站数量
        Long aLong = docCommonService.handleCapacity(recl);

        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .eq(DocBsDocument::getDocType, DocConstants.PERSON)
                .isNull(DocBsDocument::getFolderId)
                .eq(DocBsDocument::getDocOwner, token.getId())
        );
        //所有的容量-回收站的容量，就是正常使用的容量
        Long aLong1 = docCommonService.handleCapacity(docBsDocuments);
        //正常使用量
        long l = docCommonService.handleCapacity(docBsDocuments) - aLong;

        docCapacityDTO.setRemark(DocUtils.getFilseSize(aLong1) + remark);

        //当总数超过总量，当修改过总量的情况下会存在这种情况
        if (aLong1 > l1) {
            //总量
            BigDecimal decimal = new BigDecimal(aLong1);
            //回收站的数量
            BigDecimal decimal1 = new BigDecimal(aLong);
            //正常使用的数量
            BigDecimal decimal2 = new BigDecimal(l);
            BigDecimal divide = decimal1.divide(decimal, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal divide1 = decimal2.divide(decimal, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal bigDecimal = new BigDecimal(1);
            docCapacityDTO.setUsedCapacity(divide1);
            docCapacityDTO.setAllCapacity(bigDecimal.subtract(divide).subtract(divide1));
            docCapacityDTO.setRecentlyCapacity(divide);
            docCapacityDTO.setAllUsedCapacity(divide1.add(divide));
        } else {
            //总量
            BigDecimal decimal = new BigDecimal(l1);
            //回收站的数量
            BigDecimal decimal1 = new BigDecimal(aLong);
            //正常使用的数量
            BigDecimal decimal2 = new BigDecimal(l);
            BigDecimal divide = decimal1.divide(decimal, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal divide1 = decimal2.divide(decimal, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal bigDecimal = new BigDecimal(1);
            docCapacityDTO.setUsedCapacity(divide1);
            docCapacityDTO.setAllCapacity(bigDecimal.subtract(divide).subtract(divide1));
            docCapacityDTO.setRecentlyCapacity(divide);
            docCapacityDTO.setAllUsedCapacity(divide1.add(divide));
        }

        //获取
        return docCapacityDTO;
    }

    /**
     * 获取回收站数据
     */
    private List<DocBsDocument> getRecl(AccountToken token) {
        List<DocBsDocument> recl = new ArrayList<>();
        List<DocBsDocument> docBsDocuments1 = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .eq(DocBsDocument::getDocOwner, token.getId())
                .eq(DocBsDocument::getDocType, DocConstants.PERSON)
                .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_RECOVERED)
        );
        recl.addAll(docBsDocuments1);
        List<Long> collect = docBsDocuments1.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());

        //回收站中除了文件夹以外的文档数据
        List<DocBsDocument> docBsDocuments2 = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                .notIn(!CollectionUtils.isEmpty(collect), DocBsDocument::getFolderId, collect)
                .eq(DocBsDocument::getDocOwner, token.getId())
                .eq(DocBsDocument::getDocType, DocConstants.PERSON)
                .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_RECOVERED)
        );

        if (!CollectionUtils.isEmpty(docBsDocuments2)) {
            recl.addAll(docBsDocuments2);
            //回收站中文档数据对应的附件数量
            List<Long> collect1 = docBsDocuments2.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
            List<DocBsDocument> docBsDocuments3 = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.FILE)
                    .in(DocBsDocument::getRelDoc, collect1)
                    .eq(DocBsDocument::getDocOwner, token.getId())
                    .eq(DocBsDocument::getDocType, DocConstants.PERSON)
                    .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_RECOVERED)
            );
            recl.addAll(docBsDocuments3);
        }
        return recl;
    }

    /**
     * 带文件单个上传
     */
    public List<AddOrUpdateDocumentVO> addUpload(MultipartFile file1, List<MultipartFile> file2, AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend, Integer code) {
        AssertUtils.isNull(docBsDocumentExtend.getFolderId(), "参数错误");
        File file=null;
        try {
            file = multipartFileToFile(file1);
        } catch (Exception e) {
            AssertUtils.isTrue(true, "上传失败，请稍后重试");
            e.printStackTrace();
        }
        docBsDocumentExtend.setDocType(code.intValue());
        if (code.equals(DocConstants.COMPANY)) {
            handleCheckPermiss(docBsDocumentExtend);
            AssertUtils.isNull(docBsDocumentExtend.getHouseId(), "参数错误");
            //权限校验
            docCommonService.isEditPermiss(token, docBsDocumentExtend.getFolderId());
        } else {
            docCommonService.checkCapacity(token);
        }

        //重名校验
        List<AddOrUpdateDocumentVO> list1 = new ArrayList<>();
        AddOrUpdateDocumentVO vo = new AddOrUpdateDocumentVO();
        BeanUtils.copyProperties(docBsDocumentExtend, vo);
        vo.setDocName(getSuffixFile(file1.getOriginalFilename()));
        if (!StringUtils.isEmpty(docBsDocumentExtend.getDocName())) {
            vo.setDocName(getSuffixFile(docBsDocumentExtend.getDocName()));
        }
        DocBsDocument document = handleCheckName(vo);
        if (document != null) {
            list1.add(vo);
        }
        if (!CollectionUtils.isEmpty(list1)) {
            return list1;
        }
        List<UploadListVO> uploadListVOList = new ArrayList<>();
        try {
            UploadListVO uploadListVO = new UploadListVO();
            uploadListVO.setFileByte(FileUtils.getContent(file));
            uploadListVO.setFileName(file1.getOriginalFilename());
            uploadListVO.setStEquipmentId(DocConstants.MINIO);
            uploadListVO.setUserId(token.getId());
            uploadListVO.setFileSource(DocConstants.APPLICATION);
            uploadListVOList.add(uploadListVO);
        } catch (Exception e) {
        }
        Result<List<SysFileDTO>> upload = docDelStorageFeign.uploadBatch(uploadListVOList);
        if (!upload.isSucc()) {
            AssertUtils.isTrue(true, "上传失败，请稍后重试");
        }

        handleFileNoEmpt(file2, token, docBsDocumentExtend, upload.getData().get(0));
        docBsDocumentExtend.setFileId(upload.getData().get(0).getId());
        docBsDocumentExtend.setDocSize(upload.getData().get(0).getSize());
        docBsDocumentExtend.setDocSuffix("." + upload.getData().get(0).getExt());

        List<AddOrUpdateDocumentVO> add = add(token, docBsDocumentExtend, code);
        //发起通知
        sendMessage(docBsDocumentExtend);
        return add;
    }

    /**
     * MultipartFile转为file
     * @return
     */
    private   File multipartFileToFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        // 找到文件名中最后一个点的位置
        int lastIndex = originalFilename.lastIndexOf('.');

        // 提取文件的原始名称（不包含扩展名）
        String filenameWithoutExtension = originalFilename.substring(0, lastIndex);
        String extension = originalFilename.substring(lastIndex);
        File tempFile = Files.createTempFile(filenameWithoutExtension, extension).toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        return tempFile;

    }

    /**
     * 发起通知
     */
    private void sendMessage(AddOrUpdateDocumentVO docBsDocumentExtend) {
        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getRelIds()) && DocConstants.MSG_YES.equals(docBsDocumentExtend.getIsMsg())) {
            List<DocBsDocumentUserDTO> list = new ArrayList<>();
            for (int i = 0; i < docBsDocumentExtend.getRelIds().size(); i++) {
                DocBsDocumentUserDTO docBsDocumentUserDTO = new DocBsDocumentUserDTO();
                docBsDocumentUserDTO.setRelId(docBsDocumentExtend.getRelIds().get(i));
                docBsDocumentUserDTO.setType(docBsDocumentExtend.getTypes().get(i));
                list.add(docBsDocumentUserDTO);

            }
            List<Long> acceptUserId = getAcceptUserId(list);
            DocBsLevelFolderDTO docBsLevelFolderDTO = docBsDocumentMapper.searchLevelFolder(docBsDocumentExtend.getFolderId());
            DocSysHouse docSysHouse = docSysHouseMapper.selectById(docBsDocumentExtend.getHouseId());
            List<DocBsMessageVO> newDocumentMsgBean = createNewDocumentMsgBean(acceptUserId, docBsDocumentExtend.getHouseId(), docBsDocumentExtend.getFolderId(), docSysHouse.getHouseName() + "/" + docBsLevelFolderDTO.getFolderName());
            workbenchService.addMessageBatch(newDocumentMsgBean);
        }
    }

    /**
     * 附件不为空处理
     *
     */
    private void handleFileNoEmpt(List<MultipartFile> file2, AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend, SysFileDTO upload) {
        if (!CollectionUtils.isEmpty(file2)) {
            List<UploadListVO> uploadListVOList = dealMultipartFile(file2, token);
            Result<List<SysFileDTO>> stSysFileResult = docDelStorageFeign.uploadBatch(uploadListVOList);
            if (!stSysFileResult.isSucc()) {
                //删除主文件
                DocBsTask docBsTask = new DocBsTask();
                docBsTask.setRelId(upload.getId());
                docBsTask.setTaskType(DocConstants.TASK_DEL_FILE);
                docBsTask.setTaskStatus(DocConstants.DEL_STORAGE_PENDING);
                docBsTaskMapper.insert(docBsTask);
                AssertUtils.isTrue(true, "上传失败，请稍后重试");
            }
            List<DocBsDocument> attchList = new ArrayList<>();
            for (SysFileDTO file : stSysFileResult.getData()) {
                DocBsDocument document1 = new DocBsDocument();
                document1.setDocName(getSuffixFile(file.getOriginalFilename()));
                document1.setFileId(file.getId());
                document1.setDocSize(file.getSize());
                document1.setDocSuffix("." + file.getExt());
                attchList.add(document1);
            }
            docBsDocumentExtend.setAttchList(attchList);
        }
    }


    /**
     * 将后缀为大写的转为小写
     */
    private String getSuffixFile(String originalFilename) {
        String suffix = "";
        if (!StringUtils.isEmpty(originalFilename)) {
            String[] split = originalFilename.split("\\.");
            if (split.length <= 1) {
                return originalFilename;
            }
            String filename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String suff = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            suffix = filename + suff;
        }
        return suffix;
    }

    /**
     * 带文件批量上传
     */
    public List<AddOrUpdateDocumentVO> batchAddUpload(List<MultipartFile> file1, AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend, Integer code) {
        log.info("1、进入批量上传接口,{}", System.currentTimeMillis());
        AssertUtils.isNull(docBsDocumentExtend.getFolderId(), "参数错误");
        docBsDocumentExtend.setDocType(code);
        if (docBsDocumentExtend.getFileOldName() != null
                && docBsDocumentExtend.getFileNewName() != null
                && docBsDocumentExtend.getFileOldName().size() != docBsDocumentExtend.getFileNewName().size()) {
            AssertUtils.isTrue(true, "参数错误");
        }
        if (code.equals(DocConstants.COMPANY)) {
            handleCheckPermiss(docBsDocumentExtend);
            AssertUtils.isNull(docBsDocumentExtend.getHouseId(), "参数错误");
            //权限校验
            docCommonService.isEditPermiss(token, docBsDocumentExtend.getFolderId());
        } else {
            docCommonService.checkCapacity(token);
        }
        //重名校验
        List<AddOrUpdateDocumentVO> list1 = new ArrayList<>();
        Map<String, String> map = new HashMap(DocConstants.SIXTEEN);
        checkNameAgain(file1, docBsDocumentExtend, list1, map);
        if (!CollectionUtils.isEmpty(list1)) {
            return list1;
        }

        return addUploadLast(file1, token, docBsDocumentExtend, code, map);
    }

    /**
     * 重命名具体校验逻辑
     */
    private void checkNameAgain(List<MultipartFile> file1, AddOrUpdateDocumentVO docBsDocumentExtend, List<AddOrUpdateDocumentVO> list1, Map<String, String> map) {
        for (MultipartFile file : file1) {
            AddOrUpdateDocumentVO vo = new AddOrUpdateDocumentVO();
            BeanUtils.copyProperties(docBsDocumentExtend, vo);
            vo.setDocName(getSuffixFile(file.getOriginalFilename()));
            if (!CollectionUtils.isEmpty(docBsDocumentExtend.getFileNewName())) {
                for (int i = 0; i < docBsDocumentExtend.getFileOldName().size(); i++) {
                    if (vo.getDocName().equals(docBsDocumentExtend.getFileOldName().get(i))) {
                        vo.setDocName(getSuffixFile(docBsDocumentExtend.getFileNewName().get(i)));
                        map.put(docBsDocumentExtend.getFileOldName().get(i), docBsDocumentExtend.getFileNewName().get(i));
                        break;
                    }
                }
            }
            DocBsDocument document = handleCheckName(vo);
            if (document != null) {
                list1.add(vo);
            }
        }
    }

    /**
     * 上传文档批量最后一步
     *
     */
    private List<AddOrUpdateDocumentVO> addUploadLast(List<MultipartFile> file1, AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend, Integer code, Map<String, String> map) {
        log.info("2、进行feign上传,{}", System.currentTimeMillis());
        List<UploadListVO> uploadListVOList = dealMultipartFile(file1, token);
        Result<List<SysFileDTO>> stSysFileResult = docDelStorageFeign.uploadBatch(uploadListVOList);
        log.info("2、feign返回,{}", System.currentTimeMillis());
        if (stSysFileResult.isSucc()) {
            List<AddOrUpdateDocumentVO> list = new ArrayList<>();
            for (SysFileDTO stSysFile : stSysFileResult.getData()) {
                AddOrUpdateDocumentVO vo = new AddOrUpdateDocumentVO();
                BeanUtils.copyProperties(docBsDocumentExtend, vo);
                if (map.get(stSysFile.getOriginalFilename()) != null) {
                    vo.setDocName(getSuffixFile(map.get(stSysFile.getOriginalFilename())));
                } else {
                    vo.setDocName(getSuffixFile(stSysFile.getOriginalFilename()));
                }

                vo.setFileId(stSysFile.getId());
                vo.setDocSize(stSysFile.getSize());
                vo.setDocSuffix("." + stSysFile.getExt());
                list.add(vo);
            }

            //发起通知
            sendMessage(docBsDocumentExtend);
            return batchAdd(token, list, code);
        } else {
            throw new SunyardException("上传失败");
        }
    }

    private List<UploadListVO> dealMultipartFile(List<MultipartFile> file1, AccountToken token) {
        List<UploadListVO> uploadListVOList = new ArrayList<>();
        file1.forEach(item -> {
            try {
                UploadListVO uploadListVO = new UploadListVO();
                uploadListVO.setUserId(token.getId());
                uploadListVO.setFileName(item.getOriginalFilename());
                uploadListVO.setFileByte(item.getBytes());
                uploadListVO.setStEquipmentId(DocConstants.MINIO);
                uploadListVO.setFileSource(DocConstants.APPLICATION);
                uploadListVOList.add(uploadListVO);
            } catch (Exception e) {
            }
        });
        return uploadListVOList;
    }

    /**
     * 带文件更新
     */
    @Lock4j(keys = "#docBsDocumentExtend.busId")
    @Transactional(rollbackFor = Exception.class)
    public AddOrUpdateDocumentVO updateUpload(MultipartFile file1, List<MultipartFile> file2, AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend) {
        AssertUtils.isNull(docBsDocumentExtend.getBusId(), "参数错误");
        if (docBsDocumentExtend.getDocType().equals(DocConstants.COMPANY)) {
            handleCheckPermiss(docBsDocumentExtend);
            docCommonService.isEditPermiss(token, docBsDocumentExtend.getBusId());
        } else {
            if (file1 != null || !CollectionUtils.isEmpty(file2)) {
                docCommonService.checkCapacity(token);
            }
        }
        DocBsDocument document = handleCheckName(docBsDocumentExtend);
        if (document != null) {
            //名称重复，直接返回
            return docBsDocumentExtend;
        }
        DocBsDocument document2 = docBsDocumentMapper.selectById(docBsDocumentExtend.getBusId());
        docBsDocumentExtend.setFolderId(document2.getFolderId());
        Result<List<SysFileDTO>> upload = null;
        if (file1 != null) {
            List<UploadListVO> uploadListVOList = new ArrayList<>();
            try {
                UploadListVO uploadListVO = new UploadListVO();
                uploadListVO.setFileByte(file1.getBytes());
                uploadListVO.setFileName(file1.getOriginalFilename());
                uploadListVO.setStEquipmentId(DocConstants.MINIO);
                uploadListVO.setUserId(token.getId());
                uploadListVO.setFileSource(DocConstants.APPLICATION);
                uploadListVOList.add(uploadListVO);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
            upload = docDelStorageFeign.uploadBatch(uploadListVOList);
            if (!upload.isSucc()) {
                throw new SunyardException("上传失败");
            }
        }

        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getAttchIds())) {
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper
                    .selectList(new LambdaQueryWrapper<DocBsDocument>().eq(DocBsDocument::getRelDoc, docBsDocumentExtend.getBusId())
                            .in(DocBsDocument::getFileId, docBsDocumentExtend.getAttchIds()));
            docBsDocumentExtend.setAttchList(docBsDocuments);
        }

        if (!CollectionUtils.isEmpty(file2)) {
            handleAttch(file2, token, docBsDocumentExtend, upload!=null? upload.getData().get(0):null);
        }
        if (upload != null) {
            docBsDocumentExtend.setFileId(upload.getData().get(0).getId());
            docBsDocumentExtend.setDocSize(upload.getData().get(0).getSize());
            docBsDocumentExtend.setDocSuffix("." + upload.getData().get(0).getExt());
        } else {
            DocBsDocument document1 = docBsDocumentMapper.selectById(docBsDocumentExtend.getBusId());
            docBsDocumentExtend.setFileId(document1.getFileId());
            docBsDocumentExtend.setDocSize(document1.getDocSize());
            docBsDocumentExtend.setDocSuffix(document1.getDocSuffix());
            docBsDocumentExtend.setFolderId(document1.getFolderId());
        }
        update(token, docBsDocumentExtend);
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //设置子线程共享
        RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
        centerDelStorageService.delTask();
        return null;
    }

    /**
     * 修改处理附件
     */
    private void handleAttch(List<MultipartFile> file2, AccountToken token, AddOrUpdateDocumentVO docBsDocumentExtend, SysFileDTO upload) {
        List<UploadListVO> uploadListVOList = dealMultipartFile(file2, token);
        Result<List<SysFileDTO>> stSysFileResult = docDelStorageFeign.uploadBatch(uploadListVOList);
        if (!stSysFileResult.isSucc()) {
            if (upload != null) {
                //删除主文件
                DocBsTask docBsTask = new DocBsTask();
                docBsTask.setRelId(upload.getId());
                docBsTask.setTaskType(DocConstants.TASK_DEL_FILE);
                docBsTask.setTaskStatus(DocConstants.DEL_STORAGE_PENDING);
                docBsTaskMapper.insert(docBsTask);
                throw new SunyardException("上传失败");
            }
        }

        List<DocBsDocument> attchList = docBsDocumentExtend.getAttchList() == null ? new ArrayList<>() : docBsDocumentExtend.getAttchList();
        for (SysFileDTO file : stSysFileResult.getData()) {
            DocBsDocument document1 = new DocBsDocument();
            document1.setFileId(file.getId());
            document1.setDocSize(file.getSize());
            document1.setDocSuffix("." + file.getExt());
            document1.setDocName(getSuffixFile(file.getOriginalFilename()));
            attchList.add(document1);
        }
        docBsDocumentExtend.setAttchList(attchList);
    }

    /**
     * 已关联文档
     */
    public List<Long> getRelDocOn(Long busId) {
        AssertUtils.isNull(busId, "参数错误");
        //文档关联的文档
        List<DocBsDocRel> docBsDocRels = docBsDocRelMapper.selectList(new LambdaQueryWrapper<DocBsDocRel>().eq(DocBsDocRel::getDocId, busId));
        List<Long> collect = docBsDocRels.stream().map(DocBsDocRel::getRelId).collect(Collectors.toList());
        return collect;
    }

    /**
     * 企业批量删除
     */
    @Lock4j(keys = "#busIds")
    @Transactional(rollbackFor = Exception.class)
    public void delDocBatch(Long[] busIds, Integer company, AccountToken token) {
        if (company.equals(DocConstants.COMPANY)) {
            docCommonService.isMangePermiss(token, busIds);
        }
        List<DocBsRecycle> docBsRecycles = new ArrayList<>();
        for (Long busId : busIds) {
            ArrayList<Long> objects = new ArrayList<>();
            objects.add(busId);
            //新建回收站数据
            DocBsRecycle docBsRecycle = new DocBsRecycle();
            docBsRecycle.setDocId(busId);
            docBsRecycle.setRecycleDate(docCommonService.getRecycleDateByParam());
            docBsRecycle.setDelDate(new Date());
            docBsRecycles.add(docBsRecycle);
            docCommonService.handleRecycleDoc(objects, docBsRecycle.getRecycleDate(), token);
        }
        MybatisBatch<DocBsRecycle> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsRecycles);
        MybatisBatch.Method<DocBsRecycle> docMethod = new MybatisBatch.Method<>(DocBsRecycleMapper.class);
        docBatchs.execute(docMethod.insert());

    }

    /**
     * 批量移动
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveDocBatch(AccountToken token, Long[] busIds, Long folderId, Integer code) {
        AssertUtils.isNull(busIds, "参数错误");
        AssertUtils.isNull(folderId, "参数错误");

        if (code.equals(DocConstants.COMPANY)) {
            //文档权限校验
            docCommonService.isEditPermiss(token, Arrays.asList(busIds));
            //文件夹权限校验
            docCommonService.isEditPermiss(token, folderId);
        }
        for (Long busId : busIds) {
            DocBsDocument document = docBsDocumentMapper.selectOne(new LambdaQueryWrapper<DocBsDocument>()
                    .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                    .eq(DocBsDocument::getBusId, busId));
            AssertUtils.isNull(document, "参数错误");
            String[] strings = new String[]{document.getDocName()};
            List<Map> maps = checkDocumentName(folderId, strings);
            AssertUtils.notNull(maps, "已存在同名文档！");

            docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                    .set(DocBsDocument::getFolderId, folderId)
                    .eq(DocBsDocument::getBusId, busId));
        }

    }

    private void handleCheckPermiss(AddOrUpdateDocumentVO docBsDocumentExtend) {
        if (!CollectionUtils.isEmpty(docBsDocumentExtend.getRelIds())) {
            boolean b = docBsDocumentExtend.getRelIds().size() == docBsDocumentExtend.getTypes().size()
                    ? (docBsDocumentExtend.getTypes().size() == docBsDocumentExtend.getPermissTypes().size() ? true : false) : false;
            AssertUtils.isTrue(!b, "参数错误");
            List<DocBsDocumentUserDTO> docBsDocumentUserDTOList = new ArrayList<>();
            for (int i = 0; i < docBsDocumentExtend.getRelIds().size(); i++) {
                DocBsDocumentUserDTO userTeamDeptListExtends = new DocBsDocumentUserDTO();
                userTeamDeptListExtends.setRelId(docBsDocumentExtend.getRelIds().get(i));
                userTeamDeptListExtends.setType(docBsDocumentExtend.getTypes().get(i));
                userTeamDeptListExtends.setPermissType(docBsDocumentExtend.getPermissTypes().get(i));
                docBsDocumentUserDTOList.add(userTeamDeptListExtends);
            }
            docBsDocumentExtend.setUserTeamDeptListExtends(docBsDocumentUserDTOList);
        }
    }

    /**
     * 统一的查询处理
     */
    private PageInfo<DocBsDocumentDTO> getDocBsDocumentExtendPageInfo(AccountToken token, DocBsDocumentSearchVO docBsDocumentExtend, PageForm pageForm, ExtendPageDTO extendPageDTO, String searchType) {
        //todo LambdaQueryWrapper
        //queryWrapper.eq("a.is_deleted", DocConstants.DELETED_NO);
        extendPageDTO.setIsDeleted(DocConstants.DELETED_NO);
        if (DocConstants.PERSON.equals(docBsDocumentExtend.getDocType())) {
           /* queryWrapper.eq("a.doc_owner", token.getId());
            queryWrapper.eq("a.doc_type", DocConstants.PERSON);*/
            extendPageDTO.setDocOwner(token.getId());
            extendPageDTO.setDocType(docBsDocumentExtend.getDocType());
        } else {
            if (docBsDocumentExtend.getShowFlag()) {
                //过滤掉 已关联的列表
                List<Long> relBusId = docBsDocumentExtend.getRelBusId();
                if (!CollectionUtils.isEmpty(relBusId)) {
                   // queryWrapper.notIn("a.bus_id", relBusId);
                    extendPageDTO.setRelBusId(relBusId);
                }
                extendPageDTO.setShowFlag(true);
                docCommonService.queryFolderList(token, extendPageDTO, docBsDocumentExtend.getShowFlag(), "u.");
            }
            extendPageDTO.setHouseId(docBsDocumentExtend.getHouseId());
            extendPageDTO.setDocType(DocConstants.COMPANY);
            extendPageDTO.setTagId(docBsDocumentExtend.getTagId());
            if (!ObjectUtils.isEmpty(docBsDocumentExtend.getDocOwnerStr())) {
                Result<List<SysUserDTO>> result = userApi
                        .getUserDetailByName(docBsDocumentExtend.getDocOwnerStr());
                List<Long> userIds = result.getData().stream().map(SysUserDTO::getUserId)
                        .collect(Collectors.toList());
                userIds.add(-Long.MAX_VALUE);
                extendPageDTO.setUserIds(userIds);
            }
            /*queryWrapper.eq("a.house_id", docBsDocumentExtend.getHouseId());
            queryWrapper.eq("a.doc_type", DocConstants.COMPANY);
            //标签
            queryWrapper.eq(docBsDocumentExtend.getTagId() != null, "e.tag_id", docBsDocumentExtend.getTagId());
            queryWrapper.like(!StringUtils.isEmpty(docBsDocumentExtend.getDocOwnerStr()), "f.name", docBsDocumentExtend.getDocOwnerStr());*/
            //权限

            /*QueryWrapper<DocBsDocument> queryWrapper1 = queryWrapper.clone();*/
            try {
                ExtendPageDTO extendPageDTO1 = extendPageDTO.clone(extendPageDTO);

                /*queryWrapper1.eq("a.type", DocConstants.DOCUMENT)
                        .in("a.doc_status", DocConstants.DOC_STATUS_WAIT, DocConstants.DOC_STATUS_NOPUTAWAY, DocConstants.DOC_STATUS_OUT);*/
                List<DocBsDocumentDTO> list = docBsDocumentMapper.selectListExtend(extendPageDTO1);
                if (!CollectionUtils.isEmpty(list)) {
                    List<Long> collect = list.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
                   /* queryWrapper.notIn("a.bus_id", collect);*/
                    extendPageDTO.setRelBusId(collect);
                }
            }catch (Exception e){
                log.error("统一的查询处理克隆对象出现错误",e);
            }

        }

       /* queryWrapper.orderByAsc("a.type");*/

        if (StringUtils.isEmpty(docBsDocumentExtend.getDictionSuffix())) {
            return new PageInfo<>();
        }
        //后缀查询处理
        DocBsDocumentSearchDTO searchDTO = new DocBsDocumentSearchDTO();
        docCommonService.handleSuffixSearch(docBsDocumentExtend.getDictionSuffix(), searchDTO);
        extendPageDTO.setContains(searchDTO.isContains());
        extendPageDTO.setSuffixSize(searchDTO.getSuffixSize());
        extendPageDTO.setSuffixAllList(searchDTO.getSuffixAllList());
        extendPageDTO.setDicExtraList(searchDTO.getDicExtraList());
        extendPageDTO.setSuffixList(searchDTO.getSuffixList());
        extendPageDTO.setType(searchDTO.getType());
        /*if (docBsDocumentExtend.getCreateTime() != null) {
            if (docBsDocumentExtend.getCreateTime().equals(DocConstants.SORT_ASC)) {
                queryWrapper.orderByAsc("a.create_time");
            } else {
                queryWrapper.orderByDesc("a.create_time");
            }
        } else if (docBsDocumentExtend.getUpdateTime() != null) {
            if (docBsDocumentExtend.getUpdateTime().equals(DocConstants.SORT_ASC)) {
                queryWrapper.orderByAsc("a.update_time");
            } else {
                queryWrapper.orderByDesc("a.update_time");
            }
        } else {
            queryWrapper.orderByDesc("a.create_time");
        }*/

        extendPageDTO.setCreateTimeSort(docBsDocumentExtend.getCreateTime());
        extendPageDTO.setUpdateTimeSort(docBsDocumentExtend.getUpdateTime());

        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocBsDocumentDTO> docBsDocuments = docBsDocumentMapper.selectListExtendPage(extendPageDTO, searchType);
        docCommonService.handleOwnStr(docBsDocuments);
        docCommonService.getPermissMaxPage(token, docBsDocuments);
        docCommonService.handleSuffixToDic(docBsDocuments);
        //收藏
        docCommonService.handleCollection(docBsDocuments, token.getId());
        //文件大小处理
        docCommonService.handleDocSize(docBsDocuments);

        return new PageInfo<>(docBsDocuments);
    }

    private List<DocBsMessageVO> createNewDocumentMsgBean(List<Long> userId, Long houseId, Long folderId, String folderUrl) {
        List<DocBsMessageVO> list = new ArrayList<>();
        List<Long> disUserId = userId.stream().distinct().collect(Collectors.toList());
        if (org.apache.shiro.util.CollectionUtils.isEmpty(disUserId)) {
            return list;
        }
        disUserId.forEach(i -> {
            DocBsMessageVO docBsMessageVo = new DocBsMessageVO();
            docBsMessageVo.setUserId(i);
            docBsMessageVo.setMessageType(DocConstants.DOC_MESSAGE_RANGE_NEWDOCPUTON);
            docBsMessageVo.setMessageTitle("有新的文档上架");
            docBsMessageVo.setMessageContent("有新的文档上架到:" + folderUrl);
            docBsMessageVo.setDocFolder(folderUrl);
            docBsMessageVo.setDocHouseId(houseId);
            docBsMessageVo.setDocParentId(folderId);
            docBsMessageVo.setInformTime(new Date());
            list.add(docBsMessageVo);
        });
        return list;
    }

    /**
     * 拿到分享对象中所有的userid集合
     *
     */
    private List<Long> getAcceptUserId(List<DocBsDocumentUserDTO> list) {
        List<Long> userId = new ArrayList<>();
        list.forEach(item -> {
            if (DocConstants.USER.equals(item.getType())) {
                userId.add(item.getRelId());
            } else if (DocConstants.TEAM.equals(item.getType())) {
                //查询团队下所有的人
                List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getTeamId, item.getRelId()));
                userId.addAll(teamUserList.stream().map(DocSysTeamUser::getUserId).collect(Collectors.toList()));
            } else if (DocConstants.DEPT.equals(item.getType())) {
                //查询部门下所有的人
                Result<List<SysUserDTO>> deptUserList = userApi.getUserByDeptIdAndRoleId(item.getRelId(), null);
                userId.addAll(deptUserList.getData().stream().map(SysUserDTO::getUserId).collect(Collectors.toList()));
            } else if (DocConstants.INST.equals(item.getType())) {
                //查询机构下所有的人
                Result<List<SysUserDTO>> instUserList = userApi.getUsersByInstId(item.getRelId());
                userId.addAll(instUserList.getData().stream().map(SysUserDTO::getUserId).collect(Collectors.toList()));
            } else {
            }
        });
        return userId;
    }


}
