package com.sunyard.ecm.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.dto.ecm.EcmBusiStatusDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.manager.BusiOperationService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.util.EasyExcelUtils;
import com.sunyard.ecm.vo.EcmDocDefVO;
import com.sunyard.ecm.vo.EcmScanDownLoadVO;
import com.sunyard.ecm.vo.EcmStatisticsVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.module.storage.dto.ecm.DocFileZip;
import com.sunyard.module.storage.dto.ecm.DownloadFileZip;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.es.EsEcmBusi;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.mapper.es.EsEcmBusiMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.ecm.vo.SearchOptionVO;
import com.sunyard.ecm.vo.SearchVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

/**
 * @author XQZ
 * @date 2023/4/25
 * @describe 扫描列表实现类
 */
@Slf4j
@Service
public class CaptureScanService {

    @Value("${bizIndex:ecm_busi_dev}")
    private String bizIndex;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private SysBusiLogMapper ecmBusiLogMapper;
    @Resource
    private EsEcmBusiMapper esEcmBusiMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private OperateRecycleService operateRecycleService;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Resource
    private LogBusiService logBusiService;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;

    /**
     * 影像采集获取搜索框
     */
    public HashMap<String, Object> getSearchList(List<String> appTypeIds, AccountTokenExtendDTO tokenExtendDTO) {
        HashMap<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(appTypeIds)) {
            return map;
        }
        Set<String> permissionAppCodes = staticTreePermissService.getAppCodeHaveByToken(null, tokenExtendDTO, "read");
        List<String> permittedAppTypeIds = appTypeIds.stream()
                .filter(permissionAppCodes::contains)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(permittedAppTypeIds)) {
            return map;
        }
        List<SearchVO> searchVOList = new ArrayList<>();
        addAttrCol(permittedAppTypeIds, map, searchVOList,permissionAppCodes);
        return map;
    }

    /**
     * 影像采集列表
     */
    public Object searchList(BusiInfoVO busiInfoVo, AccountTokenExtendDTO tokenExtendDTO) {
        List<String> userIdsByCreate = new ArrayList<>();
        List<String> userIdsByUpdate = new ArrayList<>();
        List<Long> busiIds = new ArrayList<>();

        // 1. 获取当前用户token对应的有权限的appCode集合
        Set<String> permissionAppCodes = staticTreePermissService.getAppCodeHaveByToken(
                null,
                tokenExtendDTO,
                "read"
        );
        // 获取当前用户的机构树
        List<String> finalOrgCodeList = getOrgCodeList(busiInfoVo, tokenExtendDTO);
        // 若权限集合为空，直接返回空结果（无权限访问任何数据）
        if (CollectionUtils.isEmpty(permissionAppCodes)) {
            PageInfo pageInfo = new PageInfo<>();
            pageInfo.setTotal(0L);
            pageInfo.setList(new ArrayList<>());
            return pageInfo;
        }

        // 创建人条件检索
        if (!ObjectUtils.isEmpty(busiInfoVo.getCreateUser())) {
            Result<List<SysUserDTO>> result = userApi
                    .getUserDetailByName(busiInfoVo.getCreateUser());
            userIdsByCreate = result.getData().stream().map(SysUserDTO::getLoginName)
                    .collect(Collectors.toList());
            userIdsByCreate.add(String.valueOf(-Long.MAX_VALUE));
        }

        // 最近修改人条件检索
        if (!ObjectUtils.isEmpty(busiInfoVo.getUpdateUser())) {
            Result<List<SysUserDTO>> result = userApi
                    .getUserDetailByName(busiInfoVo.getUpdateUser());
            userIdsByUpdate = result.getData().stream().map(SysUserDTO::getLoginName)
                    .collect(Collectors.toList());
            userIdsByUpdate.add(String.valueOf(-Long.MAX_VALUE));
        }

        // 业务属性条件检索
        if (!CollectionUtils.isEmpty(busiInfoVo.getAttrList())) {
            // 验证权限内的业务类型非空
            AssertUtils.isNull(permissionAppCodes, "无权限访问指定业务类型");
            List<EcmAppAttrDTO> filterAttr = busiInfoVo.getAttrList().stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(filterAttr)) {
                // 使用权限内的appCode查询busiIds
                busiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(),
                        new ArrayList<>(permissionAppCodes));
                busiIds.add(-Long.MAX_VALUE);
            }
        }

        // 2. 处理业务类型ID：仅保留权限内的appCode及子ID
        List<String> appCodeList;
        // 原传入的appCodeList与权限集合取交集
        if (!CollectionUtils.isEmpty(busiInfoVo.getAppCodes())) {
            appCodeList = busiInfoVo.getAppCodes().stream()
                    .filter(permissionAppCodes::contains)
                    .collect(Collectors.toList());
        } else {
            appCodeList = new ArrayList<>();
        }

        // 获取子ID并过滤权限
        List<String> childIds = new ArrayList<>();
        getAllChildAppTypeIds(childIds, appCodeList);
        // 子ID也需过滤，仅保留权限内的
        childIds = childIds.stream()
                .filter(permissionAppCodes::contains)
                .collect(Collectors.toList());
        appCodeList.addAll(childIds);

        // 若最终appCodeList为空,则展示所有权限下的数据
        if (CollectionUtils.isEmpty(appCodeList)) {
            appCodeList.addAll(new ArrayList<>(permissionAppCodes));
        }

        Integer pageSize = busiInfoVo.getPageSize();
        Integer size = (busiInfoVo.getPageNum() - 1) * pageSize;
        PageHelper.startPage(busiInfoVo.getPageNum(), pageSize);
        List<Long> finalBusiIds = busiIds;
        // 3. 执行数据筛选：使用权限过滤后的appCodeList
        CompletableFuture<List<EcmBusiInfoDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> ecmBusiInfoMapper.selecAppTypetList(busiInfoVo, appCodeList,
                        finalBusiIds,finalOrgCodeList));
        // 查询数量：同样使用权限过滤后的appCodeList
        CompletableFuture<Long> countFuture = CompletableFuture.supplyAsync(
                () -> ecmBusiInfoMapper.selecAppTypetCount(busiInfoVo, appCodeList, finalBusiIds,finalOrgCodeList));

        List<EcmBusiInfoDTO> ecmBusiInfos = listFuture.join();
        Long count = countFuture.join();

        PageInfo pageInfo = new PageInfo<>(ecmBusiInfos);
        pageInfo.setTotal(count);

        if (CollectionUtils.isEmpty(ecmBusiInfos)) {
            return pageInfo;
        }

        List<Long> busiIdList = ecmBusiInfos.stream().map(EcmBusiInfoDTO::getBusiId)
                .collect(Collectors.toList());
        List<EcmBusiInfoDTO> ecmBusiInfos1 = ecmBusiInfoMapper.selectQueryDataList(busiIdList);
        handleDateToStr(ecmBusiInfos1,tokenExtendDTO,!busiInfoVo.getAppCodes().isEmpty());
        pageInfo.setList(ecmBusiInfos1);

        return pageInfo;
    }

    /**
     * 影像业务状态列表
     */
    public List<EcmBusiStatusDTO> searchBusiStatus(BusiInfoVO busiInfoVo,AccountTokenExtendDTO tokenExtendDTO) {
        List<Long> busiIds = new ArrayList<>();
        // 1. 获取当前用户token对应的有权限的appCode集合
        Set<String> permissionAppCodes = staticTreePermissService.getAppCodeHaveByToken(
                null,
                tokenExtendDTO,
                "read"
        );
        // 若权限集合为空，直接返回空结果（无权限访问任何数据）
        if (CollectionUtils.isEmpty(permissionAppCodes)) {
            return new ArrayList<>();
        }
        // 获取当前用户的机构树
        List<String> finalOrgCodeList = getOrgCodeList(busiInfoVo, tokenExtendDTO);
        //业务属性条件检索
        if (!CollectionUtils.isEmpty(busiInfoVo.getAttrList())) {
            // 验证权限内的业务类型非空
            AssertUtils.isNull(permissionAppCodes, "无权限访问指定业务类型");
            List<EcmAppAttrDTO> filterAttr = busiInfoVo.getAttrList().stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(filterAttr)) {
                // 使用权限内的appCode查询busiIds
                busiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(),
                        new ArrayList<>(permissionAppCodes));
                busiIds.add(-Long.MAX_VALUE);
            }
        }
        // 2. 处理业务类型ID：仅保留权限内的appCode及子ID
        List<String> appCodeList;
        // 原传入的appCodeList与权限集合取交集
        if (!CollectionUtils.isEmpty(busiInfoVo.getAppCodes())) {
            appCodeList = busiInfoVo.getAppCodes().stream()
                    .filter(permissionAppCodes::contains)
                    .collect(Collectors.toList());
        } else {
            appCodeList = new ArrayList<>();
        }

        // 获取子ID并过滤权限
        List<String> childIds = new ArrayList<>();
        getAllChildAppTypeIds(childIds, appCodeList);
        // 子ID也需过滤，仅保留权限内的
        childIds = childIds.stream()
                .filter(permissionAppCodes::contains)
                .collect(Collectors.toList());
        appCodeList.addAll(childIds);

        // 若最终appCodeList为空，返回空结果
        if (CollectionUtils.isEmpty(appCodeList)) {
            appCodeList.addAll(new ArrayList<>(permissionAppCodes));
        }
        List<Long> finalBusiIds = busiIds;
        CompletableFuture<List<EcmBusiStatusDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> ecmBusiInfoMapper.selecBusiStatusList(busiInfoVo, appCodeList,
                        finalBusiIds,finalOrgCodeList));
        List<EcmBusiStatusDTO> join = listFuture.join();
        return handleStatus(join);
    }

    /**
     * 获取机构list
     * @param busiInfoVo
     * @param tokenExtendDTO
     * @return
     */
    private List<String> getOrgCodeList(BusiInfoVO busiInfoVo, AccountTokenExtendDTO tokenExtendDTO) {
        List<SysOrgDTO> sysOrgDTOList = new ArrayList<>();
        Result<List<SysOrgDTO>> instResult = instApi.searchInstTree(tokenExtendDTO.getInstId());
        if (instResult.isSucc()) {
            sysOrgDTOList = instResult.getData();
        }
        List<String> orgCodeList = sysOrgDTOList.stream()
                .map(SysOrgDTO::getInstNo)
                .collect(Collectors.toList());
        List<String> orgCodes = busiInfoVo.getOrgCodes();
        return Optional.ofNullable(orgCodes)
                .filter(codes -> !codes.isEmpty())
                .map(codes -> orgCodeList.stream()
                        .filter(codes::contains)
                        .collect(Collectors.toList()))
                .orElse(orgCodeList);
    }

    private List<EcmBusiStatusDTO> handleStatus(List<EcmBusiStatusDTO> ecmBusiStatusDTO) {
        List<Integer> collect = ecmBusiStatusDTO.stream().map(EcmBusiStatusDTO::getStatus).collect(Collectors.toList());
        List<Integer> statusList = new ArrayList<>();
        statusList.add(BusiInfoConstants.BUSI_STATUS_ZERO);
        statusList.add(BusiInfoConstants.BUSI_STATUS_ONE);
        statusList.add(BusiInfoConstants.BUSI_STATUS_TWO);
        statusList.add(BusiInfoConstants.BUSI_STATUS_THREE);
        statusList.add(BusiInfoConstants.BUSI_STATUS_FOUR);
        statusList.add(BusiInfoConstants.BUSI_STATUS_FIVE);
        for(Integer status : statusList){
            if(!collect.contains(status)){
                EcmBusiStatusDTO dto = new EcmBusiStatusDTO();
                dto.setStatus(status);
                dto.setBusiNum(IcmsConstants.ZERO);
                ecmBusiStatusDTO.add(dto);
            }
        }
        return ecmBusiStatusDTO;

    }


    private void getAllChildAppTypeIds(List<String> ids, List<String> appTypeIds) {
        if (CollectionUtils.isEmpty(appTypeIds)) {
            return;
        }
        for (String a : appTypeIds) {
            LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppDef::getParent, a);
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(wrapper);
            if (ecmAppDefs.size() == IcmsConstants.ZERO) {
                ids.add(a);
            } else {
                List<String> list = new ArrayList<>();
                ecmAppDefs.forEach(e -> {
                    list.add(e.getAppCode());
                });
                getAllChildAppTypeIds(ids, list);
            }
        }
    }

    private void handleDateToStr(List<EcmBusiInfoDTO> ecmBusiInfoDTOS, AccountTokenExtendDTO token, boolean flag) {
        List<String> userIds = new ArrayList<>();
        List<String> appTypeIds = new ArrayList<>();
        for (EcmBusiInfoDTO extend : ecmBusiInfoDTOS) {
            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
                userIds.add(extend.getCreateUser());
            }
            if (!ObjectUtils.isEmpty(extend.getUpdateUser())) {
                userIds.add(extend.getCreateUser());
            }
            if (!ObjectUtils.isEmpty(extend.getAppCode())) {
                appTypeIds.add(extend.getAppCode());
            }

        }
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
        Map<String, List<EcmAppDef>> groupedByApp = ecmAppDefs.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getAppCode));
        Map<Integer, String> busiStatusMap = new HashMap<>();
        busiStatusMap.put(BusiInfoConstants.BUSI_STATUS_ZERO,BusiInfoConstants.BUSI_STATUS_ZERO_STR);
        busiStatusMap.put(BusiInfoConstants.BUSI_STATUS_ONE, BusiInfoConstants.BUSI_STATUS_ONE_STR);
        busiStatusMap.put(BusiInfoConstants.BUSI_STATUS_TWO,BusiInfoConstants.BUSI_STATUS_TWO_STR);
        busiStatusMap.put(BusiInfoConstants.BUSI_STATUS_THREE, BusiInfoConstants.BUSI_STATUS_THREE_STR);
        busiStatusMap.put(BusiInfoConstants.BUSI_STATUS_FOUR, BusiInfoConstants.BUSI_STATUS_FOUR_STR);
        busiStatusMap.put(BusiInfoConstants.BUSI_STATUS_FIVE, BusiInfoConstants.BUSI_STATUS_FIVE_STR);
        for (EcmBusiInfoDTO extend : ecmBusiInfoDTOS) {
            extend.setCreateUserName(extend.getCreateUserName());
            extend.setUpdateUserName(extend.getUpdateUserName());
            if (extend.getRightVer() == 0) {
                extend.setRightVer(null);
            }
            //业务类型名称
            if (!ObjectUtils.isEmpty(groupedByApp.get(extend.getAppCode()))) {
                extend.setAppTypeName(groupedByApp.get(extend.getAppCode()).get(0).getAppName());
            }
            //设置拓展属性
            if (flag){
                setAttr(extend,token);
            }
            //填充信息
            extend.setAppTypeName("(" + extend.getAppCode() + ")" + extend.getAppTypeName());
            extend.setStatusStr(busiStatusMap.get(extend.getStatus()));
            if (ObjectUtils.isEmpty(extend.getErrNo()) || IcmsConstants.ZERO.equals(extend.getErrNo())){
                extend.setRemark("");
            }
        }
    }

    /**
     * 设置拓展属性
     * @param token
     */
    private void setAttr(EcmBusiInfoDTO extend, AccountTokenExtendDTO token) {
        Map<String, String> map = new HashMap<>();
        //获取拓展属性
        List<EcmAppAttrDTO> appAttrExtends = busiCacheService.getAppAttrExtends(extend.getAppCode(), extend.getBusiId());
        if (appAttrExtends == null || appAttrExtends.isEmpty()) {
            extend.setAttrMap(Collections.emptyMap());
            return;
        }
        extend.setAttrList(appAttrExtends);
        for (EcmAppAttrDTO ecmAppAttrDTO : appAttrExtends) {
            map.put(ecmAppAttrDTO.getAttrCode(),ecmAppAttrDTO.getAppAttrValue());
        }
        extend.setAttrMap(map);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long busiId, AccountTokenExtendDTO tokenExtendDTO) {
        AssertUtils.isNull(busiId, "参数错误");
        EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                .getEcmBusiInfoRedisDTO(tokenExtendDTO, busiId);
        BeanUtils.copyProperties(ecmBusiInfoRedisDTO,ecmBusiInfo);
        saveDelLog(ecmBusiInfo, tokenExtendDTO);
        //清除持久化数据库数据
        deleteDBBusiData(ecmBusiInfo);
        //更新redis缓存数据
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService
                .getEcmBusiInfoRedisDTO(tokenExtendDTO, busiId);
        ecmBusiInfoRedisDTO1.setIsDeleted(StateConstants.YES);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO1);
        // 更新es数据删除状态
        updateEsBusiDeleted(Arrays.asList(busiId), tokenExtendDTO);

        // 添加操作记录表
        busiOperationService.addOperation(busiId, IcmsConstants.DELETE_BUSI, tokenExtendDTO,
                "删除业务");
        // 添加回收站
        operateRecycleService.add(ecmBusiInfo, tokenExtendDTO);
    }

    /**
     * 批量删除
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void deleteBatch(List<Long> busiIds, AccountTokenExtendDTO tokenExtendDTO) {
        AssertUtils.isNull(busiIds, "参数错误");
        List<EcmBusiInfo> ecmBusiInfoList = ecmBusiInfoMapper.selectBatchIds(busiIds);
        for (EcmBusiInfo ecmBusiInfo : ecmBusiInfoList) {
            saveDelLog(ecmBusiInfo, tokenExtendDTO);
        }
        // 批量清除持久化数据库数据
        deleteBatchDbBusiData(ecmBusiInfoList);
        // 更新es业务删除状态
        updateEsBusiDeleted(busiIds, tokenExtendDTO);
        // 批量添加操作记录表
        for (Long busiId : busiIds) {
            busiOperationService.addOperation(busiId, IcmsConstants.DELETE_BUSI, tokenExtendDTO,
                    "删除业务");
        }
        // 批量增加回收站
        operateRecycleService.addBatch(ecmBusiInfoList, tokenExtendDTO);
    }

    // 保存删除业务日志
    private void saveDelLog(EcmBusiInfo ecmBusiInfo, AccountTokenExtendDTO userId) {
        //获取业务类型实体
        LambdaQueryWrapper<EcmAppDef> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmAppDef::getAppCode, ecmBusiInfo.getAppCode());
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper);
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setBusiNo(ecmBusiInfo.getBusiNo());
        ecmBusiLog.setAppName(ecmAppDef.getAppName());
        ecmBusiLog.setAppCode(ecmAppDef.getAppCode());
        ecmBusiLog.setOrgCode(ecmBusiInfo.getOrgCode());
        ecmBusiLog.setOperatorId(userId.getUsername());
        ecmBusiLog.setOperator(userId.getName());
        ecmBusiLog.setOperatorType(BusiLogConstants.OPERATION_TYPE_THREE);
        ecmBusiLog.setOperateContent("删除业务: " + ecmBusiInfo.getBusiNo());
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 更新Es业务删除状态
     */
    private void updateEsBusiDeleted(List<Long> busiIds, AccountTokenExtendDTO tokenExtendDTO) {
        if (CollUtil.isEmpty(busiIds)) {
            return;
        }
        esEcmBusiMapper.update(null,
                new LambdaEsUpdateWrapper<EsEcmBusi>().indexName(bizIndex)
                        .set(EsEcmBusi::getIsDeleted, StateConstants.DELETED)
                        .in(EsEcmBusi::getBusiId, busiIds));
    }

    /**
     * 删除校验
     */
    public Object checkDelete(Long busiId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiId, "参数错误");
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(busiId);
        if (ObjectUtils.isEmpty(ecmBusiInfo)) {
            AssertUtils.isTrue(true, "业务不存在");
        }
        //查询业务下是否存在影像文件
        Long fileCount = StateConstants.ZERO.longValue();
        //先走缓存
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                busiId);
        if (ecmBusiInfoRedisDTO != null) {
            List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                    .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
            fileCount = CollectionUtils.isEmpty(fileInfoRedisEntities)
                    ? StateConstants.ZERO.longValue()
                    : Integer.valueOf(fileInfoRedisEntities.size()).longValue();
        } else {
            //无缓存走持久化数据库
            fileCount = ecmFileInfoMapper
                    .selectCount(new LambdaQueryWrapper<EcmFileInfo>().eq(EcmFileInfo::getBusiId, busiId));
        }
        Integer result = StateConstants.ZERO;
        if (fileCount.equals(StateConstants.ZERO.longValue())) {
            //无影像文件
            result = StateConstants.ZERO;
        } else {
            //有影像文件
            result = StateConstants.COMMON_ONE;
        }
        return result;
    }

    /**
     * 批量删除校验
     */
    @Transactional(rollbackFor = Exception.class)
    public Object checkDeleteBatch(List<Long> busiIds, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiIds, "参数错误");
        //批量查询业务下影像文件数量
        Long fileCount = StateConstants.ZERO.longValue();
        List<Long> noRedisByBusiId = new ArrayList<>();
        for (Long busiId : busiIds) {
            //走缓存
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                    busiId);
            if (ecmBusiInfoRedisDTO != null) {
                List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                        .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
                fileCount = fileCount + (CollectionUtils.isEmpty(fileInfoRedisEntities)
                        ? StateConstants.ZERO.longValue()
                        : Integer.valueOf(fileInfoRedisEntities.size()).longValue());
            } else {
                //无缓存走持久化数据库
                noRedisByBusiId.add(busiId);
            }
        }
        if (!CollectionUtils.isEmpty(noRedisByBusiId)) {
            fileCount = fileCount + ecmFileInfoMapper
                    .selectCount(new LambdaQueryWrapper<EcmFileInfo>().in(EcmFileInfo::getBusiId, noRedisByBusiId));
        }
        Integer result = StateConstants.ZERO;
        if (fileCount.equals(StateConstants.ZERO.longValue())) {
            //无影像文件
            result = StateConstants.ZERO;
        } else {
            //有影像文件
            result = StateConstants.COMMON_ONE;
        }
        return result;
    }

    /**
     * 属性按钮-获取属性信息
     */
    public List<EcmAppAttrDTO> getBusiAttrInfo(String appCode, Long busiId,
                                               AccountTokenExtendDTO token) {
        AssertUtils.isNull(appCode, "参数错误");
        AssertUtils.isNull(busiId, "参数错误");
        //先看缓存有没，没有走持久化数据库
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                busiId);
        if (ecmBusiInfoRedisDTO == null) {
            //走持久化数据库
            List<EcmAppAttr> appAttrs = ecmAppAttrMapper
                    .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
            if (CollectionUtils.isEmpty(appAttrs)) {
                return Collections.emptyList();
            }
            List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(appAttrs,
                    EcmAppAttrDTO.class);
            List<EcmBusiMetadata> ecmBusiMetadata = ecmBusiMetadataMapper
                    .selectList(new LambdaQueryWrapper<EcmBusiMetadata>().eq(EcmBusiMetadata::getBusiId, busiId));
            if (CollectionUtils.isEmpty(ecmBusiMetadata)) {
                return ecmAppAttrDTOS;
            }
            Map<Long, List<EcmBusiMetadata>> groupedByAttr = ecmBusiMetadata.stream()
                    .collect(Collectors.groupingBy(EcmBusiMetadata::getAppAttrId));
            for (EcmAppAttrDTO ecmAppAttrDTO : ecmAppAttrDTOS) {
                List<EcmBusiMetadata> busiMetadata = groupedByAttr
                        .get(ecmAppAttrDTO.getAppAttrId());
                if (!CollectionUtils.isEmpty(busiMetadata)) {
                    ecmAppAttrDTO.setAppAttrValue(busiMetadata.get(0).getAppAttrVal());
                }
            }
            return ecmAppAttrDTOS;
        } else {
            //走缓存
            if (CollectionUtils.isEmpty(ecmBusiInfoRedisDTO.getAttrList())) {
                return Collections.emptyList();
            }
            return ecmBusiInfoRedisDTO.getAttrList();
        }
    }

    /**
     * 获取回收状态业务索引号
     * @param originalBusiNo 原业务索引号
     */
    private String getRecycleBusiNo(String originalBusiNo) {
        return new StringBuilder(originalBusiNo).append("_")
                .append(LocalDateTimeUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")).toString();
    }

    private void deleteBatchDbBusiData(List<EcmBusiInfo> ecmBusiInfoList) {
        ecmBusiInfoList.forEach(ecmBusiInfo -> {
            ecmBusiInfo.setBusiNo(getRecycleBusiNo(ecmBusiInfo.getBusiNo()));
        });
        for (EcmBusiInfo d : ecmBusiInfoList) {
            ecmBusiInfoMapper.update(null,
                    new LambdaUpdateWrapper<EcmBusiInfo>()
                            .set(EcmBusiInfo::getBusiNo, d.getBusiNo())
                            .set(EcmBusiInfo::getIsDeleted, StateConstants.YES)
                            .eq(EcmBusiInfo::getBusiId, d.getBusiId()));
        }
    }

    void deleteDBBusiData(EcmBusiInfo ecmBusiInfo) {
        // 更新业务信息并删除
        Long busiId = ecmBusiInfo.getBusiId();
        String recycleBusiNo = getRecycleBusiNo(ecmBusiInfo.getBusiNo());
        EcmBusiInfo recycleEcmBusiInfo = new EcmBusiInfo();
        recycleEcmBusiInfo.setBusiId(busiId);
        recycleEcmBusiInfo.setBusiNo(recycleBusiNo);
        ecmBusiInfoMapper.updateById(recycleEcmBusiInfo);
        // 更新业务索引号
        ecmBusiInfo.setBusiNo(recycleBusiNo);
        //清除-业务信息表
        ecmBusiInfoMapper.deleteById(busiId);
    }

    private void addAttrCol(List<String> appTypeIds, HashMap<String, Object> map,
                            List<SearchVO> searchVOList, Set<String> permissionAppCodes) {
        //若传入的业务类型ID为父ID，获取所有子ID
        List<String> ids = new ArrayList<>();
        getAllChildAppTypeIds(ids, appTypeIds);
        List<String> intersection = ids.stream()
                .filter(permissionAppCodes::contains)
                .collect(Collectors.toList());
        LambdaQueryWrapper<EcmAppAttr> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EcmAppAttr::getAppCode, intersection);
        List<EcmAppAttr> result = ecmAppAttrMapper.selectList(wrapper);
        Map<String, List<EcmAppAttr>> filteredMap = result.stream()
                .collect(Collectors.groupingBy(EcmAppAttr::getAttrCode)).entrySet().stream()
                .filter(entry -> entry.getValue().size() == intersection.size())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<EcmAppAttr> resultList = filteredMap.entrySet().stream()
                .map(entry -> entry.getValue().get(0)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        for (EcmAppAttr ecmAppAttr : resultList) {
            if (IcmsConstants.ONE.equals(ecmAppAttr.getQueryShow())) {
                SearchVO vo = new SearchVO();
                if (BusiInfoConstants.DATE_TYPE.equals(ecmAppAttr.getInputType())) {
                    vo.setType("date");
                    vo.setPlaceholder("请选择" + ecmAppAttr.getAttrName());
                } else if (BusiInfoConstants.SELECT_TYPE.equals(ecmAppAttr.getInputType())) {
                    String selectNodes = ecmAppAttr.getListValue();
                    if (StrUtil.isBlank(selectNodes)) {
                        continue;
                    }
                    String[] split = selectNodes.split(";");
                    List<SearchOptionVO> voList = new ArrayList<>();
                    for (String spli : split) {
                        SearchOptionVO optionVo = new SearchOptionVO();
                        optionVo.setLabel(spli);
                        optionVo.setCode(spli);
                        optionVo.setValue(spli);
                        voList.add(optionVo);
                    }
                    vo.setType("select");
                    vo.setPlaceholder("请选择" + ecmAppAttr.getAttrName());
                    vo.setOption(voList);
                } else {
                    vo.setPlaceholder("请输入" + ecmAppAttr.getAttrName());
                }
                vo.setCode(ecmAppAttr.getAttrCode());
                vo.setLabel(ecmAppAttr.getAttrName() + "：");
                vo.setAttrFlag(true);
                vo.setAppAttrId(ecmAppAttr.getAppAttrId());
                searchVOList.add(vo);
            }
        }
        map.put("searchList", searchVOList);
        map.put("attrList", resultList);
    }


    public EcmBusiInfoRedisDTO getRecycleBusiInfo(Long busiId){
        AssertUtils.isNull(busiId,"参数为空");
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectByIdWithDeleted(busiId);
        AssertUtils.isNull(ecmBusiInfo,"未找到业务信息");
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        BeanUtils.copyProperties(ecmBusiInfo,ecmBusiInfoRedisDTO);
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(ecmBusiInfo.getAppCode());
        AssertUtils.isNull(ecmAppDef,"未找到业务类型信息");
        List<EcmAppAttrDTO> appAttrExtends = busiCacheService.getAppAttrExtends(ecmBusiInfo.getAppCode(), ecmBusiInfo.getBusiId());
        ecmBusiInfoRedisDTO.setAppTypeName(ecmAppDef.getAppName());
        ecmBusiInfoRedisDTO.setAttrList(appAttrExtends);
        return ecmBusiInfoRedisDTO;
    }


    public PageInfo getRecycleFileInfo(Long busiId, PageForm pageForm){
        AssertUtils.isNull(busiId,"参数为空");
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectByIdWithDeleted(busiId);
        AssertUtils.isNull(ecmBusiInfo,"未找到业务信息");
        PageHelper.startPage(pageForm.getPageNum(),pageForm.getPageSize());
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>().eq(EcmFileInfo::getBusiId, busiId));
        PageInfo pageInfo = new PageInfo<>(ecmFileInfos);
        if(CollectionUtils.isEmpty(ecmFileInfos)){
            return pageInfo;
        }
        List<EcmFileInfoDTO> ecmFileInfoDTOS = PageCopyListUtils.copyListProperties(ecmFileInfos, EcmFileInfoDTO.class);
        handleDocName(ecmBusiInfo, ecmFileInfoDTOS);

        pageInfo.setList(ecmFileInfoDTOS);
        return pageInfo;
    }

    public void handleDocName(EcmBusiInfo ecmBusiInfo, List<EcmFileInfoDTO> ecmFileInfoDTOS) {
        Map<String, List<EcmDocDefRelVer>> docCodeListMap = null;
        Map<String, List<EcmBusiDoc>> collect = null;
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfo.getTreeType())) {
            //查询资料类型信息
            List<EcmDocDefRelVer> ecmDocDefs = ecmDocDefRelVerMapper.selectList(new QueryWrapper<EcmDocDefRelVer>()
                    .eq("app_code", ecmBusiInfo.getAppCode())
                    .eq("right_ver", ecmBusiInfo.getRightVer()));
            //分组
            docCodeListMap = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmDocDefRelVer::getDocCode));
            AssertUtils.isNull(docCodeListMap, "EcmDocDef表数据为空");
        } else {
            //查询资料类型信息
            List<EcmBusiDoc> ecmDocDefs = ecmBusiDocMapper.selectList(new QueryWrapper<EcmBusiDoc>()
                    .eq("busi_id", ecmBusiInfo.getBusiId()));
            collect = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getDocCode));

        }
        for(EcmFileInfoDTO dto : ecmFileInfoDTOS){
            //未归类
            if (IcmsConstants.UNCLASSIFIED_ID.equals(dto.getDocCode())) {
                dto.setDocName(IcmsConstants.UNCLASSIFIED);
            } else {
                if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfo.getTreeType())) {
                    List<EcmDocDefRelVer> ecmDocDefs1 = docCodeListMap.get(dto.getDocCode());
                    AssertUtils.isNull(ecmDocDefs1, "参数错误，资料节点为空");
                    dto.setDocName(ecmDocDefs1.get(0).getDocName());
                } else {
                    //动态树
                    List<EcmBusiDoc> ecmBusiDocs = collect.get(dto.getDocCode());
                    AssertUtils.isNull(ecmBusiDocs, "参数错误，资料节点为空");
                    dto.setDocName(ecmBusiDocs.get(0).getDocName());
                }
            }
        }
    }

    /**
     * 根据busIds批量下载业务文件
     */
    public Result batchDownloadBusiFile(EcmScanDownLoadVO ecmScanDownLoadVO, AccountTokenExtendDTO token) {
        try {
            List<DownloadFileZip> files = getDownloadBusiFile(ecmScanDownLoadVO, token);
            return Result.success(files);
        } catch (Exception e) {
            log.error("获取权限数据列表异常", e);
            throw new SunyardException(e.getMessage());
        }
    }


    public List<DownloadFileZip> getDownloadBusiFile(EcmScanDownLoadVO ecmScanDownLoadVO, AccountTokenExtendDTO token) {
        // 1. 提取入参中的核心信息
        List<EcmBusiInfo> ecmBusiInfos = ecmScanDownLoadVO.getBusiInfos();
        if (CollectionUtils.isEmpty(ecmBusiInfos)) {
            return Collections.emptyList();
        }

        // 业务ID列表
        List<Long> busiIds = ecmBusiInfos.stream()
                .map(EcmBusiInfo::getBusiId)
                .collect(Collectors.toList());
        // 提取所有（appCode + rightVer）组合（去重，用于权限查询）
        List<Map<String, String>> appCodeRightVerPairs = ecmBusiInfos.stream()
                .map(info -> {
                    Map<String, String> pair = new HashMap<>(2);
                    pair.put("appCode", info.getAppCode());
                    pair.put("rightVer", String.valueOf(info.getRightVer()));
                    return pair;
                })
                .distinct()
                .collect(Collectors.toList());
        // 业务ID → 业务编号映射
        Map<Long, String> busiIdToBusiNoMap = ecmBusiInfos.stream()
                .collect(Collectors.toMap(
                        EcmBusiInfo::getBusiId,
                        EcmBusiInfo::getBusiNo,
                        (k1, k2) -> k1
                ));
        // 业务编号 → 业务ID映射（用于日志记录）
        Map<String, Long> busiNoToIdMap = ecmBusiInfos.stream()
                .collect(Collectors.toMap(
                        EcmBusiInfo::getBusiNo,
                        EcmBusiInfo::getBusiId,
                        (k1, k2) -> k1
                ));
        // 业务ID → （appCode + rightVer）映射（用于文件权限校验）
        Map<Long, Map<String, String>> busiIdToAppCodeRightVerMap = ecmBusiInfos.stream()
                .collect(Collectors.toMap(
                        EcmBusiInfo::getBusiId,
                        info -> {
                            Map<String, String> map = new HashMap<>(2);
                            map.put("appCode", info.getAppCode());
                            map.put("rightVer", String.valueOf(info.getRightVer()));
                            return map;
                        }
                ));

        //获取资料定义
        Map<String, EcmDocDef> docDefMap = busiCacheService.getDocInfoAll();

        // 2. 获取有下载权限的（appCode + rightVer + docCode）组合（核心权限校验，补充版本号）
        List<Long> roleIds = staticTreePermissService.getRoleByToken(token);
        List<String> roleIdsStr = roleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        // 构建权限查询条件：匹配角色 + （appCode+rightVer）组合 + 下载权限YES
        LambdaQueryWrapper<EcmDocrightDef> docrightQueryWrapper = new LambdaQueryWrapper<EcmDocrightDef>()
                .in(EcmDocrightDef::getRoleDimVal, roleIdsStr)
                .eq(EcmDocrightDef::getDownloadRight, StateConstants.YES.toString());
        // 批量添加（appCode + rightVer）组合条件（OR关系）
        if (!CollectionUtils.isEmpty(appCodeRightVerPairs)) {
            docrightQueryWrapper.and(wrapper -> {
                for (int i = 0; i < appCodeRightVerPairs.size(); i++) {
                    Map<String, String> pair = appCodeRightVerPairs.get(i);
                    String appCode = pair.get("appCode");
                    String rightVer = pair.get("rightVer");
                    if (i == 0) {
                        wrapper.eq(EcmDocrightDef::getAppCode, appCode)
                                .eq(EcmDocrightDef::getRightVer, rightVer);
                    } else {
                        wrapper.or()
                                .eq(EcmDocrightDef::getAppCode, appCode)
                                .eq(EcmDocrightDef::getRightVer, rightVer);
                    }
                }
            });
        }
        List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper.selectList(docrightQueryWrapper);

        // 权限缓存：用「appCode + "_" + rightVer」作为key → 允许下载的docCode集合
        Map<String, Set<String>> appCodeRightVerPermittedDocCodesMap = ecmDocrightDefs.stream()
                .collect(Collectors.groupingBy(
                        def -> def.getAppCode() + "_" + def.getRightVer(),
                        Collectors.mapping(
                                EcmDocrightDef::getDocCode,
                                Collectors.toSet()
                        )
                ));

        // 3. 查询业务对应的所有文件
        LambdaQueryWrapper<EcmFileInfo> fileInfoQueryWrapper = new LambdaQueryWrapper<EcmFileInfo>()
                .in(!busiIds.isEmpty(), EcmFileInfo::getBusiId, busiIds)
                .in(ecmScanDownLoadVO.getDocCodes() != null
                                && !ecmScanDownLoadVO.getDocCodes().isEmpty(),
                        EcmFileInfo::getDocCode,
                        ecmScanDownLoadVO.getDocCodes())
                .eq(EcmFileInfo::getState, StateConstants.NO);

        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(fileInfoQueryWrapper);

        if (CollectionUtils.isEmpty(ecmFileInfos)) {
            return Collections.emptyList();
        }

        // 4. 过滤出有下载权限的文件（按 appCode + rightVer + docCode 匹配）
        List<EcmFileInfo> permittedFiles = ecmFileInfos.stream()
                .filter(file -> {
                    Long busiId = file.getBusiId();
                    // 获取该业务的（appCode + rightVer）
                    Map<String, String> appCodeRightVerMap = busiIdToAppCodeRightVerMap.get(busiId);
                    if (appCodeRightVerMap == null) {
                        return false;
                    }
                    String appCode = appCodeRightVerMap.get("appCode");
                    String rightVer = appCodeRightVerMap.get("rightVer");
                    String docCode = file.getDocCode();

                    // 构建复合key查询权限
                    String permissionKey = appCode + "_" + rightVer;
                    Set<String> permittedDocCodes = appCodeRightVerPermittedDocCodesMap.getOrDefault(permissionKey, Collections.emptySet());
                    return permittedDocCodes.contains(docCode) || IcmsConstants.UNCLASSIFIED_ID.equals(docCode);
                })
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(permittedFiles)) {
            return Collections.emptyList();
        }

        // 5. 按「业务ID → docCode」分组，封装EcmDocFile
        Map<Long, Map<String, List<Long>>> busiIdDocCodeFileIdsMap = permittedFiles.stream()
                .collect(Collectors.groupingBy(
                        EcmFileInfo::getBusiId,
                        Collectors.groupingBy(
                                EcmFileInfo::getDocCode,
                                Collectors.mapping(
                                        EcmFileInfo::getNewFileId,
                                        Collectors.toList()
                                )
                        )
                ));
        // 6. 最终封装返回结果
        List<DownloadFileZip> result = new ArrayList<>();
        for (Map.Entry<Long, Map<String, List<Long>>> busiEntry : busiIdDocCodeFileIdsMap.entrySet()) {
            Long busiId = busiEntry.getKey();
            Map<String, List<Long>> docCodeFileIdsMap = busiEntry.getValue();

            // 构建EcmDocFile列表
            List<DocFileZip> docFileList = docCodeFileIdsMap.entrySet().stream()
                    .map(docEntry -> {
                        DocFileZip docFile = new DocFileZip();
                        EcmDocDef ecmDocDef = docDefMap.get(docEntry.getKey());
                        if(ecmDocDef != null) {
                            docFile.setDirsSecond(ecmDocDef.getDocName());
                        }else {
                            docFile.setDirsSecond(IcmsConstants.UNCLASSIFIED);
                        }
                        docFile.setFileIds(docEntry.getValue());
                        return docFile;
                    })
                    .collect(Collectors.toList());

            // 构建EcmDownloadFile
            DownloadFileZip downloadFile = new DownloadFileZip();
            String busiNo = busiIdToBusiNoMap.getOrDefault(busiId, "");
            downloadFile.setDirsFirst(busiNo);
            downloadFile.setUsername(token.getUsername());
            downloadFile.setName(token.getName());
            downloadFile.setOrgCode(token.getOrgCode());
            downloadFile.setOrgName(token.getOrgName());
            downloadFile.setDocFileList(docFileList);
            result.add(downloadFile);

            //添加日志
            List<Long> allFileIds = docFileList.stream()
                    .map(DocFileZip::getFileIds)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            logBusiService.addEcmLog(1, allFileIds, busiNoToIdMap.get(busiNo), token);
        }

        return result;
    }

    public void getBusiExcel(List<Long> busiIds, AccountTokenExtendDTO token, HttpServletResponse response){
        try {
            AssertUtils.isNull(busiIds, "busiId不能为空");
            List<EcmBusiInfoDTO> ecmBusiInfos = ecmBusiInfoMapper.selectQueryDataList(busiIds);
            AssertUtils.isTrue(ecmBusiInfos.isEmpty(), "业务信息不存在");
            //封装参数
            handleDateToStr(ecmBusiInfos, token, true);

            //获取全部拓展属性以便构建表头
            Set<String> allAttrNames = ecmBusiInfos.stream()
                    .filter(dto -> dto.getAttrList() != null)
                    .flatMap(dto -> dto.getAttrList().stream())
                    .filter(attr -> attr != null && attr.getAttrName() != null)
                    .map(EcmAppAttrDTO::getAttrName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            //获取每笔业务的拓展属性，并封装成map
            Map<Long, Map<String, String>> attrMap = new HashMap<>();
            for (EcmBusiInfoDTO dto : ecmBusiInfos) {
                if (dto.getAttrList() != null) {
                    Map<String, String> busiAttrMap = dto.getAttrList().stream()
                            .filter(attr -> attr != null && attr.getAttrName() != null)
                            .collect(Collectors.toMap(
                                    EcmAppAttrDTO::getAttrName,
                                    attr -> Objects.toString(attr.getAppAttrValue(), ""),
                                    (v1, v2) -> v1, // 重复 key 保留第一个
                                    HashMap::new
                            ));
                    attrMap.put(dto.getBusiId(), busiAttrMap);
                }
            }

            //封装表头
            List<List<String>> headList = buildExcelHead(allAttrNames);

            //封装数据行
            List<List<String>> dataList = buildExcelData(ecmBusiInfos, allAttrNames, attrMap);

            //写入excel
            EasyExcelUtils.writeToExcel(response, headList, dataList,BusiInfoConstants.BUSI_IMAGES_SCAN_EXCEL_NAME,BusiInfoConstants.BUSI_IMAGES_SCAN_SHEET);
        } catch (Exception e) {
            log.error("Excel 导出 I/O 错误", e);
        }
    }

    /**
     * 构建表头
     * @param dynamicAttrNames 动态表头数据
     * @return
     */
    private List<List<String>> buildExcelHead(Set<String> dynamicAttrNames) {
        List<List<String>> head = new ArrayList<>();
        for (String header : BusiInfoConstants.DEFAULT_EXCEL_HEADER) {
            head.add(Collections.singletonList(header));
        }
        for (String attrName : dynamicAttrNames) {
            head.add(Collections.singletonList(attrName));
        }
        return head;
    }

    private List<List<String>> buildExcelData(List<EcmBusiInfoDTO> dtos, Set<String> allAttrNames, Map<Long, Map<String, String>> attrMap) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (EcmBusiInfoDTO dto : dtos) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(index++));

            row.add(Objects.toString(dto.getAppTypeName(), ""));
            row.add(Objects.toString(dto.getBusiNo(), ""));
            row.add(Objects.toString(dto.getStatusStr(), ""));
            row.add(Objects.toString(dto.getOrgCode(), ""));
            row.add(Objects.toString(dto.getRightVer(), ""));
            row.add(Objects.toString(dto.getCreateUserName(), ""));
            row.add(dto.getCreateTime() != null ? DateUtil.format(dto.getCreateTime(), "yyyy-MM-dd HH:mm:ss") : "");
            row.add(Objects.toString(dto.getUpdateUserName(), ""));
            row.add(dto.getUpdateTime() != null ? DateUtil.format(dto.getUpdateTime(), "yyyy-MM-dd HH:mm:ss") : "");

            // 动态列
            Map<String, String> busiAttrs = attrMap.getOrDefault(dto.getBusiId(), Collections.emptyMap());
            for (String attrName : allAttrNames) {
                row.add(busiAttrs.getOrDefault(attrName, "-"));
            }

            data.add(row);
        }
        return data;
    }

    public void exportTreesToExcel(EcmStatisticsVO vo, HttpServletResponse response) {
        try {
            AssertUtils.isNull(vo.getAppCodes(), "appCode不能为空");
            HashMap<String, List<String>> listHashMap = new HashMap<>();
            List<String> appCodes = vo.getAppCodes();
            for (String appCode : appCodes) {
                EcmAppDef ecmAppDefs = ecmAppDefMapper.selectOne(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, appCode));
                List<EcmAppAttr> appAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
                if (CollectionUtils.isEmpty(appAttrs)) {
                    log.info("appCode: {} 没有业务属性", appCode);
                }
                List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(appAttrs, EcmAppAttrDTO.class);
                ecmAppAttrDTOS = ecmAppAttrDTOS.stream().sorted(Comparator.comparing(EcmAppAttrDTO::getAttrSort)).collect(Collectors.toList());
                //主属性排到第一个位置
                Collections.sort(ecmAppAttrDTOS, Comparator.comparingInt(EcmAppAttrDTO::getIsKey).reversed());
                //
                List<String> attrNames = ecmAppAttrDTOS.stream()
                        .map(EcmAppAttrDTO::getAttrName)
                        .collect(Collectors.toList());
                listHashMap.put("(" + ecmAppDefs.getAppCode() + ")" + ecmAppDefs.getAppName(),attrNames);
            }
            EasyExcelUtils.writeTreesToExcel(response, listHashMap);

        } catch (Exception e) {
            log.error("Excel 导出 I/O 错误", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }
    /**
     * 所有状态：0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结
     * 若状态为：已提交1-------可修改为》已受理 或 已办结 或 已作废；
     * 若状态为：已受理2-------可修改为》已办结 或 已作废；
     * @param busiIds
     * @param token
     * @return
     */
    public Result busiCompletion(List<Long> busiIds, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiIds, "参数错误");
        String msg = "操作成功";
        int num = 0;
        int status = 1;
        for (Long busiId : busiIds) {
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(busiId);
            Integer busiStatus = ecmBusiInfo.getStatus();
            if (BusiInfoConstants.BUSI_STATUS_ZERO.equals(busiStatus)) {
                ecmBusiInfo.setStatus(BusiInfoConstants.BUSI_STATUS_FIVE);
                //更新db
                ecmBusiInfoMapper.updateById(ecmBusiInfo);
                //添加操作记录表
                busiOperationService.addOperation(ecmBusiInfo.getBusiId(), IcmsConstants.EDIT_BUSI, token, "修改业务状态为已办结");
                //更新redis
                busiCacheService.updateRedisBusiStatus(ecmBusiInfo, token, BusiInfoConstants.BUSI_STATUS_FIVE);
                // 更新Es业务状态
                updateEsBusiStatus(busiId, BusiInfoConstants.BUSI_STATUS_FIVE);
            } else {
                num++;
                msg = "部分业务操作成功";
                status = 2;
            }
        }
        if(num > 0 && num == busiIds.size()){
            msg = "存在暂不可办结的业务状态";
            status = 0;
        }
        return Result.success(status, ResultCode.SUCCESS.getCode(), msg);
    }

    /**
     * 更新Es业务状态
     */
    private void updateEsBusiStatus(Long busiId, Integer status) {
        esEcmBusiMapper.update(null,
                new LambdaEsUpdateWrapper<EsEcmBusi>().indexName(bizIndex)
                        .set(EsEcmBusi::getIsDeleted, status)
                        .eq(EsEcmBusi::getBusiId, busiId));
    }

    /**
     * 根据appCode获取所有的DocCodes
     */
    public Result getDocListByAppcode(List<String> appCodes) {
        AssertUtils.isNull(appCodes, "业务类型列表不能为空");

        LambdaQueryWrapper<EcmAppDocRel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(EcmAppDocRel::getAppCode, appCodes)
                .orderByAsc(EcmAppDocRel::getAppCode);
        List<EcmAppDocRel> relList = ecmAppDocRelMapper.selectList(queryWrapper);

        List<String> docCodeList = relList.stream()
                .filter(rel -> rel.getDocCode() != null)
                .map(EcmAppDocRel::getDocCode)
                .distinct()
                .collect(Collectors.toList());

        Map<String, EcmDocDef> docDefMap = busiCacheService.getDocInfoAll();

        // 1. 转为带 children 的 VO 对象
        List<EcmDocDefVO> voList = docCodeList.stream()
                .map(docDefMap::get)
                .filter(Objects::nonNull)
                .map(def -> {
                    EcmDocDefVO vo = new EcmDocDefVO();
                    // 继承字段，直接拷贝
                    BeanUtils.copyProperties(def, vo);
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.toList());

        // 2. 构建树形结构
        List<EcmDocDefVO> treeData = buildDocTree(voList);

        return Result.success(treeData);
    }

    /**
     * 构建资料树（parent=0 为根节点）
     */
    private List<EcmDocDefVO> buildDocTree(List<EcmDocDefVO> nodeList) {
        // 按 docCode 建立映射，快速查找父节点
        Map<String, EcmDocDefVO> nodeMap = nodeList.stream()
                .collect(Collectors.toMap(
                        EcmDocDefVO::getDocCode,
                        v -> v,
                        (oldVal, newVal) -> oldVal
                ));

        // 挂载子节点到父节点
        for (EcmDocDefVO node : nodeList) {
            String parent = node.getParent();
            if (parent == null || "0".equals(parent)) {
                continue;
            }
            EcmDocDefVO parentNode = nodeMap.get(parent);
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            }
        }

        // 只返回 parent=0 的根节点（自带完整树）
        return nodeList.stream()
                .filter(node -> "0".equals(node.getParent()))
                .sorted(Comparator.comparing(EcmDocDefVO::getDocSort))
                .collect(Collectors.toList());
    }
}
