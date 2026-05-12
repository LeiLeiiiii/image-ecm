package com.sunyard.ecm.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDocRelDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmDimensionDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDimensionMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDimensionDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDimension;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmDimensionDef;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.util.FileSizeUtils;
import com.sunyard.ecm.vo.AddVerVO;
import com.sunyard.ecm.vo.DocRightRoleAndLotVO;
import com.sunyard.ecm.vo.DocRightVO;
import com.sunyard.ecm.vo.EcmDocTreeVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.RoleApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ty
 * @desc 资料权限接口
 * @since 2023-4-17 15:09
 */
@Service
public class ModelPermissionsService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmDimensionDefMapper ecmDimensionDefMapper;
    @Resource
    private EcmAppDimensionMapper ecmAppDimensionMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private RoleApi roleApi;
    @Resource
    private UserApi userApi;
    @Resource
    private StaticTreePermissService staticTreePermissService;

    /**
     * 新增版本
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#addVerVo.rightVer")
    public String addVer(AddVerVO addVerVo) {
        checkParamAddVer(addVerVo);
        //查询当前业务类型版本数量
        Long countByAppType = ecmAppDocrightMapper.selectCount(
                new LambdaQueryWrapper<EcmAppDocright>().eq(EcmAppDocright::getAppCode, addVerVo.getAppCode()));
        EcmAppDocright ecmAppDocright = new EcmAppDocright();
        ecmAppDocright.setCreateUser(addVerVo.getCreateUser());
        ecmAppDocright.setAppCode(addVerVo.getAppCode());
        ecmAppDocright.setRightVer(addVerVo.getRightVer());
        ecmAppDocright.setRightName(addVerVo.getRightName());
        ecmAppDocright.setRightNew(
                countByAppType.intValue() > 0 ? DocRightConstants.ZERO : DocRightConstants.ONE);
        ecmAppDocrightMapper.insert(ecmAppDocright);
        //树结构 记录版本
        insertTreeVersion(addVerVo);
        //复用已有版本 权限copy
        if (DocRightConstants.EXISTING_VER.equals(addVerVo.getAddVerType())) {
            //docRightCopy(addVerVo);
            docRightCopyNew(addVerVo);
        }
        return ecmAppDocright.getId().toString();
    }

    /**
     * 记录版本树结构
     */
    private void insertTreeVersion(AddVerVO addVerVo) {
        List<EcmAppDocRel> appCodeDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, addVerVo.getAppCode()));
        AssertUtils.isTrue(CollectionUtils.isEmpty(appCodeDocRels), "当前业务类型未关联资料,无法新增!请先关联资料!");
        //        List<EcmAppDocRelVer> list = new ArrayList<>();
        //        appCodeDocRels.forEach(
        //                ecmAppDocRel -> {
        //                    ecmAppDocRel.setId(null);
        //                    EcmAppDocRelVer ecmAppDocRelVer = new EcmAppDocRelVer();
        //                    BeanUtils.copyProperties(ecmAppDocRel, ecmAppDocRelVer);
        //                    ecmAppDocRelVer.setRightVer(addVerVo.getRightVer());
        //                    list.add(ecmAppDocRelVer);
        //                }
        //        );
        //        ecmAppDocRelVerMapper.batchInsert(list);
        List<String> docCodeIds = appCodeDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(docCodeIds);
        List<EcmDocDefRelVer> list2 = new ArrayList<>();
        ecmDocDefs.forEach(ecmDocDef -> {
            EcmDocDefRelVer ecmDocDefRelVer = new EcmDocDefRelVer();
            BeanUtils.copyProperties(ecmDocDef, ecmDocDefRelVer);
            ecmDocDefRelVer.setAppCode(addVerVo.getAppCode());
            ecmDocDefRelVer.setRightVer(addVerVo.getRightVer());
            ecmDocDefRelVer.setId(snowflakeUtil.nextId());
            list2.add(ecmDocDefRelVer);
        });

        insertEcmDocDefRelVers(list2);
        //ecmDocDefRelVerMapper.batchInsert(list2);
    }

    private void insertEcmDocDefRelVers(List<EcmDocDefRelVer> list2) {
        MybatisBatch<EcmDocDefRelVer> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list2);
        MybatisBatch.Method<EcmDocDefRelVer> method = new MybatisBatch.Method<>(
                EcmDocDefRelVerMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 获取版本号
     */
    public Integer getVerNo(String appCode) {
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocright>().eq(EcmAppDocright::getAppCode, appCode)
                        .orderByDesc(EcmAppDocright::getRightVer));
        if (CollectionUtils.isEmpty(appDocrights)) {
            return StateConstants.COMMON_ONE;
        }
        //获取最大版本号
        Integer rightVerMax = appDocrights.get(0).getRightVer();
        return rightVerMax + 1;
    }

    /**
     * 设为当前版本
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#appCode + '_' +  #id")
    public void setRightNew(Long id, String appCode, String userId) {
        AssertUtils.isNull(id, "参数错误");
        AssertUtils.isNull(appCode, "参数错误");
        ecmAppDocrightMapper.update(null,
                new UpdateWrapper<EcmAppDocright>().set("right_new", DocRightConstants.ZERO)
                        .set("update_user", userId).set("update_time", new Date())
                        .eq("app_code", appCode).eq("right_new", DocRightConstants.ONE));
        ecmAppDocrightMapper.update(null,
                new UpdateWrapper<EcmAppDocright>().set("right_new", DocRightConstants.ONE)
                        .set("update_user", userId).set("update_time", new Date()).eq("id", id));
    }

    /**
     * 业务权限版本管理列表
     */
    public PageInfo getRightVerList(AddVerVO addVerVo) {
        AssertUtils.isNull(addVerVo.getAppCode(), "参数错误");
        PageHelper.startPage(addVerVo.getPageNum(), addVerVo.getPageSize());
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocright>().eq(EcmAppDocright::getAppCode, addVerVo.getAppCode())
                        .orderByAsc(EcmAppDocright::getRightVer));
        PageInfo pageInfo = new PageInfo<>(appDocrights);
        if (CollectionUtils.isEmpty(appDocrights)) {
            return pageInfo;
        }
        List<EcmAppDocrightDTO> appDocrightExtends = PageCopyListUtils
                .copyListProperties(appDocrights, EcmAppDocrightDTO.class);
        //添加创建人、最近更新人名称、业务类型名称、角色维度是否使用、业务多维度是否使用
        handleData(appDocrightExtends, addVerVo);
        pageInfo.setList(appDocrightExtends);
        return pageInfo;
    }

    /**
     * 版本编辑
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#addVerVo.id")
    public void editVer(AddVerVO addVerVo) {
        //参数校验
        AssertUtils.isNull(addVerVo.getId(), "参数错误");
        ecmAppDocrightMapper.update(null,
                new UpdateWrapper<EcmAppDocright>().set("right_name", addVerVo.getRightName())
                        .set("update_user", addVerVo.getUpdateUser()).set("update_time", new Date())
                        .eq("id", addVerVo.getId()));
    }

    /**
     * 获取选择角色的资料权限列表
     */
    public List<EcmDocrightDefDTO> getRoleDocRightList(DocRightVO docRightVo) {
        AssertUtils.isNull(docRightVo.getId(), "参数错误");
        EcmAppDocright appDocright = ecmAppDocrightMapper.selectById(docRightVo.getId());
        //无权限版本信息直接返回
        if (ObjectUtils.isEmpty(appDocright)) {
            return Collections.emptyList();
        }
        //获取角色维度资料权限列表,type表示为1表示配置权限时调用，否则其他功能调用
        //   Integer operateFlag = IcmsConstants.ONE;
        ArrayList<String> objects = new ArrayList<>();
        Long roleId = docRightVo.getRoleId();
        objects.add(roleId.toString());
        //资料权限配置查询
        //        ecmStaticTreePermissService.getEcmDocrightDefExtendsByRole(appDocright.getAppCode(), appDocright.getRightVer(),objects,docRightVo.getCurrentUser(),operateFlag);
        return getDocRightListSetting(docRightVo, appDocright);
    }

    /**
     * 获取资料权限配置
     */
    public List<EcmDocrightDefDTO> getDocRightListSetting(DocRightVO docRightVo,
                                                          EcmAppDocright appDocright) {
        Long roleId = docRightVo.getRoleId();
        Integer rightVer = appDocright.getRightVer();
        List<EcmDocrightDefDTO> list;
        String appCode = appDocright.getAppCode();
        //查权限版本
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode).eq(EcmAppDocRel::getType,
                        IcmsConstants.ONE));
        List<String> docCodeList = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());

        List<EcmDocDefRelVer> ecmDocDefRelVers = ecmDocDefRelVerMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRelVer>().in(EcmDocDefRelVer::getDocCode, docCodeList)
                        .eq(EcmDocDefRelVer::getAppCode, appCode).eq(EcmDocDefRelVer::getRightVer, rightVer).orderByAsc(EcmDocDefRelVer::getDocSort));
        //无关联资料类型直接返回
        if (CollectionUtil.isEmpty(ecmDocDefRelVers)) {
            return Collections.emptyList();
        }
        List<String> collect1 = ecmDocDefRelVers.stream().map(EcmDocDefRelVer::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocDef>().in(EcmDocDef::getDocCode, collect1));
        //        // 根据 appDocRels 中元素的顺序对 ecmDocDefs 进行排序
        ecmDocDefs = sortedByDoc(ecmDocDefs);
        Map<String, List<EcmDocDef>> collect = ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        //查询该版本资料权限
        LambdaQueryWrapper<EcmDocrightDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmDocrightDef::getAppCode, appCode);
        wrapper.eq(EcmDocrightDef::getRightVer, rightVer);
        wrapper.eq(EcmDocrightDef::getRoleDimVal, roleId.toString());
        wrapper.in(EcmDocrightDef::getDocCode, docCodeList);
        wrapper.eq(EcmDocrightDef::getDimType, IcmsConstants.ZERO);
        List<EcmDocrightDef> ecmDocrightDefList = ecmDocrightDefMapper.selectList(wrapper);
        //判断改角色是否第一次配置

        if (CollectionUtil.isEmpty(ecmDocrightDefList)) {
            list = staticTreePermissService.getDocRightDefEmptyTemplate(ecmDocDefs, appCode,
                    rightVer);
            //空白模版中文件返回全部文件类型
            list.stream().forEach(f -> {
                f.setDocName("(" + f.getDocCode() + ")" + f.getDocName());
                f.setRightVer(rightVer);
            });
        } else {
            //获取已经关联的文件信息
            ArrayList<EcmDocrightDefDTO> objects = new ArrayList<>();

            for (EcmDocrightDef docrightDef : ecmDocrightDefList) {
                EcmDocrightDefDTO ecmDocrightDefDTO = new EcmDocrightDefDTO();
                BeanUtils.copyProperties(docrightDef, ecmDocrightDefDTO);
                List<EcmDocDef> ecmDocDefs1 = collect.get(docrightDef.getDocCode());
                ecmDocrightDefDTO.setDocName(
                        "(" + docrightDef.getDocCode() + ")" + ecmDocDefs1.get(0).getDocName());
                ecmDocrightDefDTO.setOtherUpdate(docrightDef.getOtherUpdate() + "");
                objects.add(ecmDocrightDefDTO);
            }
            // 处理 collect 中的剩余 key，确保这些也被加入到 objects 中,目的是权限配置页面上列表不同角色显示的资料类型都跟关联资料一致
            for (String docCode : collect.keySet()) {
                // 判断 collect 中的 key 是否已经在 ecmDocrightDefList 中处理过
                boolean exists = ecmDocrightDefList.stream()
                        .anyMatch(docrightDef -> docrightDef.getDocCode().equals(docCode));

                if (!exists) {
                    // 如果 docCode 不存在于 ecmDocrightDefList 中，则创建一个新的 DTO
                    EcmDocDef ecmDocDef = collect.get(docCode).get(0);
                    EcmDocrightDefDTO ecmDocrightDefDTO = new EcmDocrightDefDTO();
                    ecmDocrightDefDTO.setDocName("(" + docCode + ")" + ecmDocDef.getDocName());
                    ecmDocrightDefDTO.setRoleDimVal(roleId.toString());
                    ecmDocrightDefDTO.setAppCode(appCode);
                    ecmDocrightDefDTO.setDocCode(docCode);
                    ecmDocrightDefDTO.setRightVer(rightVer);
                    ecmDocrightDefDTO.setDimType(0);
                    ecmDocrightDefDTO.setAddRight("0");
                    ecmDocrightDefDTO.setReadRight("0");
                    ecmDocrightDefDTO.setUpdateRight("0");
                    ecmDocrightDefDTO.setDeleteRight("0");
                    ecmDocrightDefDTO.setThumRight("0");
                    ecmDocrightDefDTO.setDownloadRight("0");
                    ecmDocrightDefDTO.setPrintRight("0");
                    ecmDocrightDefDTO.setOtherUpdate("0");
                    objects.add(ecmDocrightDefDTO);
                }
            }

            //            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = JSONArray.parseArray(ecmDocrightDefList.toString(), EcmDocrightDefDTO.class);
            list = objects;
        }
        return list;
    }

    /**
     * 资料排序
     */
    private List<EcmDocDef> sortedByDoc(List<EcmDocDef> ecmDocDefs) {
        List<EcmDocDef> sortedResult = new ArrayList<>();
        //        ecmDocDefs = ecmDocDefs.stream().sorted(Comparator.comparing(EcmDocDef::getDocSort)).collect(Collectors.toList());
        Map<String, List<EcmDocDef>> groupedByDocTypeId = ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));

        //根据自己反查
        List<String> docCodes = ecmDocDefs.stream().map(EcmDocDef::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefsAll = ecmDocDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocDef>().orderByAsc(EcmDocDef::getDocSort));
        List<EcmDocDefDTO> ecmDocDefDTOS = PageCopyListUtils.copyListProperties(ecmDocDefsAll,
                EcmDocDefDTO.class);
        Map<String, List<EcmDocDefDTO>> parentListMap1 = ecmDocDefDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocDefDTO::getParent));
        //已关联
        List<EcmDocTreeDTO> list = staticTreePermissService.searchOldRelevanceInformationTreeNew(
                StateConstants.ZERO.toString(), "无", parentListMap1, docCodes, new ArrayList<>(),
                null);
        Set<String> strings = groupedByDocTypeId.keySet();

        handleTreeGetDown(list, sortedResult, groupedByDocTypeId, strings);
        return sortedResult;
    }

    private static void handleTreeGetDown(List<EcmDocTreeDTO> list, List<EcmDocDef> sortedResult,
                                          Map<String, List<EcmDocDef>> groupedByDocTypeId,
                                          Set<String> strings) {
        for (EcmDocTreeDTO appDocRel : list) {
            if (!CollectionUtils.isEmpty(appDocRel.getChildren())) {
                handleTreeGetDown(appDocRel.getChildren(), sortedResult, groupedByDocTypeId,
                        strings);
            } else {
                if (strings.contains(appDocRel.getDocCode())) {
                    List<EcmDocDef> ecmDocDefs = groupedByDocTypeId.get(appDocRel.getDocCode());
                    if (!CollectionUtils.isEmpty(ecmDocDefs)) {
                        sortedResult.add(ecmDocDefs.get(0));
                    }
                }
            }

        }
    }

    /**
     * 保存选择角色的资料权限
     */
    public void saveRoleDocRight(DocRightVO docRightVo, AccountTokenExtendDTO accountTokenExtendDTO,
                                 Integer verNo) {
        docRightVo.setCurrentUser(accountTokenExtendDTO.getUsername());
        //参数校验
        checkParamSaveRoleDocRight(docRightVo);
        //资料权限定义表插入数据
        //先删后插
        Integer rightVer = docRightVo.getDocRightList().get(0).getRightVer();
        if (verNo != null) {
            rightVer = verNo;
        } else {
            verNo = docRightVo.getRightVer();
        }
        List<Object> ecmDocrightDefs = ecmDocrightDefMapper.selectObjs(new LambdaQueryWrapper<EcmDocrightDef>()
                .select(EcmDocrightDef::getDocCode)
                .eq(EcmDocrightDef::getRoleDimVal, docRightVo.getRoleId().toString()).eq(EcmDocrightDef::getDimType, IcmsConstants.ROLE_TYPE)
                .eq(EcmDocrightDef::getAppCode, docRightVo.getAppCode()).eq(EcmDocrightDef::getRightVer, verNo));
        if(!CollectionUtils.isEmpty(ecmDocrightDefs)){
            List<EcmDocrightDefDTO> docRightList = docRightVo.getDocRightList();
            List<String> docCodes = ecmDocrightDefs.stream().map(Object::toString)
                    .collect(Collectors.toList());
            List<String> dtoDocCodes = docRightList.stream().map(EcmDocrightDefDTO::getDocCode)
                    .collect(Collectors.toList());
            List<String> onlyInDocCodes = docCodes.stream()
                    .filter(code -> !dtoDocCodes.contains(code))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(onlyInDocCodes)){
                ecmDocrightDefMapper.delete(new LambdaQueryWrapper<EcmDocrightDef>()
                        .eq(EcmDocrightDef::getDimType, IcmsConstants.ROLE_TYPE).in(EcmDocrightDef::getDocCode,onlyInDocCodes)
                        .eq(EcmDocrightDef::getAppCode, docRightVo.getAppCode()).eq(EcmDocrightDef::getRightVer, verNo));
            }
        }
        ecmDocrightDefMapper.delete(new LambdaQueryWrapper<EcmDocrightDef>()
                .eq(EcmDocrightDef::getRoleDimVal, docRightVo.getRoleId().toString()).eq(EcmDocrightDef::getDimType, IcmsConstants.ROLE_TYPE)
                .eq(EcmDocrightDef::getAppCode, docRightVo.getAppCode()).eq(EcmDocrightDef::getRightVer, verNo));
        addDocRightDefData(docRightVo, IcmsConstants.ROLE_TYPE, verNo);
        //根据业务类型和版本号更新业务版本权限信息
        updateEcmAppDocRight(docRightVo.getAppCode(), rightVer,
                accountTokenExtendDTO.getUsername());
    }

    /**
     * 更新资料版本表时间信息
     */
    public void updateEcmAppDocRight(String appCode, Integer rightVer, String userId) {
        if (appCode != null && rightVer != null) {
            LambdaUpdateWrapper<EcmAppDocright> wrapper = new LambdaUpdateWrapper();
            wrapper.eq(EcmAppDocright::getAppCode, appCode);
            wrapper.eq(EcmAppDocright::getRightVer, rightVer);
            wrapper.set(EcmAppDocright::getUpdateUser, userId);
            wrapper.set(EcmAppDocright::getUpdateTime, new Date());
            ecmAppDocrightMapper.update(null, wrapper);
        }
    }

    /**
     * 是否使用
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#addVerVo.getId + '_' + #addVerVo.getDimType")
    public void isUse(AddVerVO addVerVo) {
        AssertUtils.isNull(addVerVo.getId(), "参数错误");
        AssertUtils.isNull(addVerVo.getDimType(), "参数错误");
        AssertUtils.isNull(addVerVo.getIsUse(), "参数错误");
        EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectById(addVerVo.getId());
        UpdateWrapper<EcmDocrightDef> updateWrapper = new UpdateWrapper<EcmDocrightDef>()
                .set("update_user", addVerVo.getUpdateUser()).set("update_time", new Date())
                .eq("dim_type", addVerVo.getDimType()).eq("app_code", ecmAppDocright.getAppCode())
                .eq("right_ver", ecmAppDocright.getRightVer());
        // 查询记录数，确定是否需要执行更新操作
        Long count = ecmDocrightDefMapper.selectCount(updateWrapper);
        if (count.intValue() > 0) {
            // 执行更新操作
            ecmDocrightDefMapper.update(null, updateWrapper);
        } else {
            //无数据更新，添加一条数据，解决在没有保存业务多维度配置，无法关闭是否使用按钮
            // 执行插入操作
            insertDocrightDefByOne(addVerVo, ecmAppDocright);
        }
    }

    /**
     * 新增业务维度
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#ecmDimensionDef.createUser")
    public void addDimension(EcmDimensionDef ecmDimensionDef) {
        checkParamAddDimension(ecmDimensionDef);
        ecmDimensionDefMapper.insert(ecmDimensionDef);
    }

    /**
     * 获取维度列表
     */
    public List<EcmDimensionDefDTO> getDimensionList() {
        List<EcmDimensionDef> ecmDimensionDefs = ecmDimensionDefMapper
                .selectList(new LambdaQueryWrapper<EcmDimensionDef>().orderByDesc(EcmDimensionDef::getCreateTime));
        if (CollectionUtils.isEmpty(ecmDimensionDefs)) {
            return Collections.emptyList();
        }
        List<EcmDimensionDefDTO> ecmDimensionDefDTOS = PageCopyListUtils
                .copyListProperties(ecmDimensionDefs, EcmDimensionDefDTO.class);
        //添加用户名称、被关联状态
        addUserNameRelateStatus(ecmDimensionDefDTOS);
        return ecmDimensionDefDTOS;
    }

    /**
     * 编辑业务维度
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#ecmDimensionDef.id")
    public void editDimension(EcmDimensionDef ecmDimensionDef) {
        //校验
        checkEditDim(ecmDimensionDef);
        ecmDimensionDefMapper.update(null,
                new UpdateWrapper<EcmDimensionDef>().set("dim_name", ecmDimensionDef.getDimName())
                        .set("dim_value", ecmDimensionDef.getDimValue())
                        .set("update_user", ecmDimensionDef.getUpdateUser())
                        .set("update_time", new Date()).eq("id", ecmDimensionDef.getId()));
    }

    /**
     * 删除业务维度
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#id")
    public void delDimension(Long id) {
        AssertUtils.isNull(id, "参数错误");
        //校验维度是否已关联，存在已关联的就无法删除
        List<EcmAppDimension> appDimensions = ecmAppDimensionMapper
                .selectList(new LambdaQueryWrapper<EcmAppDimension>().eq(EcmAppDimension::getDimId, id));
        if (!CollectionUtils.isEmpty(appDimensions)) {
            AssertUtils.isTrue(true, "无法删除，维度已被关联");
        }
        ecmDimensionDefMapper.deleteById(id);
    }

    /**
     * 批量删除业务维度
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#ids")
    public void delLotDimension(Long[] ids) {
        AssertUtils.isNull(ids, "参数错误");
        List<Long> idList = Arrays.asList(ids);
        //校验维度是否已关联，存在已关联的就无法删除
        checkDelDims(idList);
        ecmDimensionDefMapper.deleteBatchIds(idList);
    }

    /**
     * 关联维度
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#docRightVo.appCode + '_' + #docRightVo.rightVer")
    public List relateDimToApp(DocRightVO docRightVo) {
        if (docRightVo.getRightVer() == null) {
            return getDimValueListByFirst(docRightVo.getRelateDimList());
        } else {
            AssertUtils.isNull(docRightVo.getAppCode(), "参数错误");
            AssertUtils.isNull(docRightVo.getRightVer(), "参数错误");
            LambdaQueryWrapper<EcmAppDocRel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StrUtil.isNotBlank(docRightVo.getAppCode()), EcmAppDocRel::getAppCode,
                    docRightVo.getAppCode());
            List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(wrapper);
            AssertUtils.isTrue(ecmAppDocRels.size() == IcmsConstants.ZERO, "当前业务未关联资料，请先进行资料关联”");
            List<EcmDimensionDefDTO> relateDimList = docRightVo.getRelateDimList();
            //先删后插
            ecmAppDimensionMapper.delete(
                    new LambdaQueryWrapper<EcmAppDimension>().eq(EcmAppDimension::getAppCode, docRightVo.getAppCode())
                            .eq(EcmAppDimension::getRightVer, docRightVo.getRightVer()));
            if (CollectionUtil.isNotEmpty(relateDimList)) {
                for (EcmDimensionDefDTO dim : relateDimList) {
                    EcmAppDimension ecmAppDimension = new EcmAppDimension();
                    ecmAppDimension.setAppCode(docRightVo.getAppCode());
                    ecmAppDimension.setRightVer(docRightVo.getRightVer());
                    ecmAppDimension.setDimId(dim.getId());
                    ecmAppDimension.setRequired(dim.getRequired());
                    ecmAppDimension.setCreateUser(docRightVo.getCurrentUser());
                    //todo 改批量插入
                    ecmAppDimensionMapper.insert(ecmAppDimension);
                }
            }
            //查询回写数据
            //查询
            LambdaQueryWrapper<EcmDocrightDef> qw = new LambdaQueryWrapper<>();
//            Map map1 = new HashMap();
//            map1.put("app_code", docRightVo.getAppCode());
//            map1.put("right_ver", docRightVo.getRightVer());
//            map1.put("dim_type", DocRightConstants.LOT_DIM);
//            qw.allEq(map1);
            qw.eq(EcmDocrightDef::getAppCode,docRightVo.getAppCode());
            qw.eq(EcmDocrightDef::getRightVer,docRightVo.getRightVer());
            qw.eq(EcmDocrightDef::getDimType,DocRightConstants.LOT_DIM);
            List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper.selectList(qw);
            //取到需要赋值的集合
            List<EcmDocrightDef> collect = ecmDocrightDefs.stream()
                    .filter(s -> s.getAddRight().equals("1") || s.getOtherUpdate().equals("1")
                            || s.getDeleteRight().equals("1") || s.getDownloadRight().equals("1")
                            || s.getPrintRight().equals("1") || s.getReadRight().equals("1")
                            || s.getThumRight().equals("1") || s.getUpdateRight().equals("1"))
                    .collect(Collectors.toList());
            //回写
            LambdaQueryWrapper<EcmAppDocright> ecmAppDocrightQueryWrapper = new LambdaQueryWrapper<>();
//            Map map2 = new HashMap();
//            map2.put("app_code", docRightVo.getAppCode());
//            map2.put("right_ver", docRightVo.getRightVer());
//            ecmAppDocrightQueryWrapper.allEq(map2);
            ecmAppDocrightQueryWrapper.eq(EcmAppDocright::getAppCode,docRightVo.getAppCode());
            ecmAppDocrightQueryWrapper.eq(EcmAppDocright::getRightVer,docRightVo.getRightVer());

            List<EcmAppDocright> appDocrights = ecmAppDocrightMapper
                    .selectList(ecmAppDocrightQueryWrapper);
            List<List<EcmDocrightDefDTO>> lotDimDocRightList = getTemplateByLotDim(
                    appDocrights.get(0), collect);

            //最后在删除权限表
            ecmDocrightDefMapper.delete(
                    new LambdaQueryWrapper<EcmDocrightDef>().eq(EcmDocrightDef::getDimType, DocRightConstants.LOT_DIM)
                            .eq(EcmDocrightDef::getAppCode, docRightVo.getAppCode())
                            .eq(EcmDocrightDef::getRightVer, docRightVo.getRightVer()));
            return lotDimDocRightList;
        }
    }

    /**
     * 获取关联维度列表
     */
    public List<EcmDimensionDefDTO> getRelateDimList(DocRightVO docRightVo) {
        AssertUtils.isNull(docRightVo.getAppCode(), "参数错误");
        AssertUtils.isNull(docRightVo.getRightVer(), "参数错误");
        List<EcmAppDimension> appDimensions = ecmAppDimensionMapper.selectList(
                new LambdaQueryWrapper<EcmAppDimension>().eq(EcmAppDimension::getAppCode, docRightVo.getAppCode())
                        .eq(EcmAppDimension::getRightVer, docRightVo.getRightVer()));
        if (CollectionUtils.isEmpty(appDimensions)) {
            return Collections.emptyList();
        }
        List<Long> dimIds = appDimensions.stream().map(EcmAppDimension::getDimId)
                .collect(Collectors.toList());
        List<EcmDimensionDef> dimensionDefs = ecmDimensionDefMapper.selectBatchIds(dimIds);
        List<EcmDimensionDefDTO> dimensionDefExtends = PageCopyListUtils
                .copyListProperties(dimensionDefs, EcmDimensionDefDTO.class);
        Map<Long, List<EcmAppDimension>> groupByDimId = appDimensions.stream()
                .collect(Collectors.groupingBy(EcmAppDimension::getDimId));
        for (EcmDimensionDefDTO extend : dimensionDefExtends) {
            if (!CollectionUtils.isEmpty(groupByDimId.get(extend.getId()))) {
                extend.setRequired(groupByDimId.get(extend.getId()).get(0).getRequired());
            }
        }
        dimensionDefExtends = dimensionDefExtends.stream()
                .sorted(Comparator.comparing(EcmDimensionDefDTO::getDimCode))
                .collect(Collectors.toList());
        return dimensionDefExtends;
    }

    /**
     * 获取业务多维度资料权限配置列表
     */
    public List<List<EcmDocrightDefDTO>> getLotDimDocRightList(Long id, String userName) {
        AssertUtils.isNull(id, "参数错误");
        //获取业务资料权限版本信息
        EcmAppDocright appDocright = ecmAppDocrightMapper.selectById(id);
        if (ObjectUtils.isEmpty(appDocright)) {
            return Collections.emptyList();
        }
        List<EcmDocrightDef> docrightDefList = ecmDocrightDefMapper.selectList(
                new LambdaQueryWrapper<EcmDocrightDef>().eq(EcmDocrightDef::getDimType, DocRightConstants.LOT_DIM)
                        .eq(EcmDocrightDef::getAppCode, appDocright.getAppCode())
                        .eq(EcmDocrightDef::getRightVer, appDocright.getRightVer()));
        if (CollectionUtils.isEmpty(docrightDefList)) {
            //返回空白模版
            return returnEmptyTemplateByLotDim(appDocright);
        } else {
            //返回已有配置数据
            return returnExistTemplateByLotDim(docrightDefList, userName);
        }
    }

    private List<List<EcmDocrightDefDTO>> returnExistTemplateByLotDim(List<EcmDocrightDef> docrightDefList,
                                                                      String userName) {
        List<EcmDocrightDefDTO> docrightDefExtendList = PageCopyListUtils
                .copyListProperties(docrightDefList, EcmDocrightDefDTO.class);
        //        docrightDefExtendList = docrightDefExtendList.stream().sorted(Comparator.comparing(EcmDocrightDefDTO::getRoleDimVal)).collect(Collectors.toList());
        //添加允许上传文件类型列表
        //        ecmStaticTreePermissService.addDocFileTypeListWithFileType(docrightDefExtendList, userName);
        //根据维度值分组
        //        Map<String, List<EcmDocrightDefDTO>> groupDocRightByDimValueBySorted = docrightDefExtendList.stream()
        //                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getRoleDimVal));
        Map<String, List<EcmDocrightDefDTO>> collect = new LinkedHashMap<>();

        //需要有序的，所以自己拼接
        for (EcmDocrightDefDTO dto : docrightDefExtendList) {
            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = collect.get(dto.getRoleDimVal());
            if (CollectionUtils.isEmpty(ecmDocrightDefDTOS)) {
                ecmDocrightDefDTOS = new ArrayList<>();
            }
            ecmDocrightDefDTOS.add(dto);
            collect.put(dto.getRoleDimVal(), ecmDocrightDefDTOS);
        }

        // 使用 TreeMap 进行排序
        //获取资料类型id
        List<String> docCodes = docrightDefExtendList.stream().map(EcmDocrightDefDTO::getDocCode)
                .distinct().collect(Collectors.toList());

        List<EcmDocDefRelVer> ecmDocDefs = ecmDocDefRelVerMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRelVer>().in(EcmDocDefRelVer::getDocCode, docCodes)
                        .eq(EcmDocDefRelVer::getRightVer, docrightDefExtendList.get(0).getRightVer()));
        Map<String, List<EcmDocDefRelVer>> groupedDocTypeById = ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDefRelVer::getDocCode));
        List<List<EcmDocrightDefDTO>> lotDimDocRightExtendList = new ArrayList<>();
        collect.forEach((dimVal, ecmDocrightDefDTOList) -> {
            ecmDocrightDefDTOList.forEach(p -> {
                //添加资料类型名称
                List<EcmDocDefRelVer> ecmDocDefs1 = groupedDocTypeById.get(p.getDocCode());
                if (!CollectionUtils.isEmpty(ecmDocDefs1)) {
                    String docName = ecmDocDefs1.get(0).getDocName();
                    p.setDocName("(" + ecmDocDefs1.get(0).getDocCode() + ")" + docName);
                }
            });
            lotDimDocRightExtendList.add(ecmDocrightDefDTOList);
        });
        return lotDimDocRightExtendList;
    }

    private List<List<EcmDocrightDefDTO>> getTemplateByLotDim(EcmAppDocright appDocright,
                                                              List<EcmDocrightDef> collect) {
        //获取关联维度信息
        List<EcmAppDimension> appDimensions = ecmAppDimensionMapper.selectList(
                new LambdaQueryWrapper<EcmAppDimension>().eq(EcmAppDimension::getAppCode, appDocright.getAppCode())
                        .eq(EcmAppDimension::getRightVer, appDocright.getRightVer()));
        if (CollectionUtils.isEmpty(appDimensions)) {
            //关联维度为空，则清空对应的资料权限
            ecmDocrightDefMapper.delete(
                    new LambdaQueryWrapper<EcmDocrightDef>().eq(EcmDocrightDef::getAppCode, appDocright.getAppCode())
                            .eq(EcmDocrightDef::getRightVer, appDocright.getRightVer())
                            .eq(EcmDocrightDef::getDimType, DocRightConstants.LOT_DIM));
            return Collections.emptyList();
        }
        List<EcmAppDocRel> appDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>()
                        .eq(EcmAppDocRel::getAppCode, appDocright.getAppCode()).eq(EcmAppDocRel::getType, IcmsConstants.ONE));
        //无关联资料类型直接返回
        if (CollectionUtils.isEmpty(appDocRels)) {
            return Collections.emptyList();
        }
        List<String> docCodes = appDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDefRelVer> ecmDocDefs = ecmDocDefRelVerMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRelVer>().in(EcmDocDefRelVer::getDocCode, docCodes)
                        .eq(EcmDocDefRelVer::getAppCode, appDocright.getAppCode())
                        .eq(EcmDocDefRelVer::getRightVer, appDocright.getRightVer()).orderByAsc(EcmDocDefRelVer::getDocSort));
        //获取对关联维度进行排列组合的维度值列表
        List<String> dimValueList = getDimValueList(appDimensions);
        //获取业务多维度资料权限列表模版
        List<List<EcmDocrightDefDTO>> lotDimDocRightExtendList = getDocRightDefEmptyTemplateByLotDim(
                appDocright, ecmDocDefs, dimValueList);
        // 遍历 lotDimDocRightExtendList
        for (List<EcmDocrightDefDTO> lot : lotDimDocRightExtendList) {
            // 遍历每个 Lot 中的 EcmDocrightDefDTO 对象
            for (EcmDocrightDefDTO p : lot) {
                String docCode = p.getDocCode();
                p.setDocName("(" + docCode + ")" + p.getDocName());
                String roleDimVal = p.getRoleDimVal();
                String[] split = roleDimVal.split(";");
                // 转换成 Set 以便检查包含关系
                Set<String> pRoleDimValSet = new HashSet<>(Arrays.asList(split));
                // 遍历 collectMap，查找匹配项
                for (EcmDocrightDef ecmDocrightDef : collect) {
                    Set<String> collectRoleDimValSet = new HashSet<>(
                            Arrays.asList(ecmDocrightDef.getRoleDimVal().split(";")));
                    // 检查 collect 中的 roleDimVal 是否是 p 的 roleDimVal 的子集
                    if (pRoleDimValSet.containsAll(collectRoleDimValSet)
                            && p.getDocCode().equals(ecmDocrightDef.getDocCode())) {
                        p.setOtherUpdate(ecmDocrightDef.getOtherUpdate());
                        p.setAddRight(ecmDocrightDef.getAddRight());
                        p.setDeleteRight(ecmDocrightDef.getDeleteRight());
                        p.setDownloadRight(ecmDocrightDef.getDownloadRight());
                        p.setPrintRight(ecmDocrightDef.getPrintRight());
                        p.setReadRight(ecmDocrightDef.getReadRight());
                        p.setThumRight(ecmDocrightDef.getThumRight());
                        p.setUpdateRight(ecmDocrightDef.getUpdateRight());
                    }
                }
            }
        }
        return lotDimDocRightExtendList;
    }

    private List<List<EcmDocrightDefDTO>> returnEmptyTemplateByLotDim(EcmAppDocright appDocright) {
        //获取关联维度信息
        List<EcmAppDimension> appDimensions = ecmAppDimensionMapper.selectList(
                new LambdaQueryWrapper<EcmAppDimension>().eq(EcmAppDimension::getAppCode, appDocright.getAppCode())
                        .eq(EcmAppDimension::getRightVer, appDocright.getRightVer()));
        if (CollectionUtils.isEmpty(appDimensions)) {
            //关联维度为空，则清空对应的资料权限
            ecmDocrightDefMapper.delete(
                    new LambdaQueryWrapper<EcmDocrightDef>().eq(EcmDocrightDef::getAppCode, appDocright.getAppCode())
                            .eq(EcmDocrightDef::getRightVer, appDocright.getRightVer())
                            .eq(EcmDocrightDef::getDimType, DocRightConstants.LOT_DIM));
            return Collections.emptyList();
        }
        List<EcmAppDocRel> appDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>()
                        .eq(EcmAppDocRel::getAppCode, appDocright.getAppCode()).eq(EcmAppDocRel::getType, IcmsConstants.ONE));
        //无关联资料类型直接返回
        if (CollectionUtils.isEmpty(appDocRels)) {
            return Collections.emptyList();
        }
        List<String> docCodes = appDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDefRelVer> ecmDocDefs = ecmDocDefRelVerMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRelVer>().in(EcmDocDefRelVer::getDocCode, docCodes)
                        .eq(EcmDocDefRelVer::getAppCode, appDocright.getAppCode())
                        .eq(EcmDocDefRelVer::getRightVer, appDocright.getRightVer()).orderByAsc(EcmDocDefRelVer::getDocSort));
        //获取对关联维度进行排列组合的维度值列表
        List<String> dimValueList = getDimValueList(appDimensions);
        //获取业务多维度资料权限列表模版
        List<List<EcmDocrightDefDTO>> lotDimDocRightExtendList = getDocRightDefEmptyTemplateByLotDim(
                appDocright, ecmDocDefs, dimValueList);
        // 遍历 lotDimDocRightExtendList
        for (List<EcmDocrightDefDTO> lot : lotDimDocRightExtendList) {
            // 遍历每个 Lot 中的 EcmDocrightDefDTO 对象
            for (EcmDocrightDefDTO p : lot) {
                String docCode = p.getDocCode();
                p.setDocName("(" + docCode + ")" + p.getDocName());
            }
        }
        return lotDimDocRightExtendList;
    }

    /**
     * 保存业务多维度资料权限配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLotDimDocRight(DocRightVO docRightVo, AccountTokenExtendDTO token,
                                   Integer verNo) {
        if (verNo != null) {
            List<EcmDimensionDefDTO> relateDimList = docRightVo.getRelateDimList();
            for (EcmDimensionDefDTO dto : relateDimList) {
                EcmAppDimension ecmAppDimension = new EcmAppDimension();
                ecmAppDimension.setAppCode(docRightVo.getAppCode());
                ecmAppDimension.setRightVer(verNo);
                ecmAppDimension.setDimId(dto.getId());
                ecmAppDimension.setRequired(dto.getRequired());
                ecmAppDimension.setCreateUser(token.getUsername());
                //todo 改批量插入
                ecmAppDimensionMapper.insert(ecmAppDimension);
            }
        }
        docRightVo.setCurrentUser(token.getUsername());
        //参数校验
        List<List<EcmDocrightDefDTO>> lotDimDocRightList = docRightVo.getLotDimDocRightList();
        AssertUtils.isNull(lotDimDocRightList, "参数错误");
        //先删后插
        //        Integer dimType = docRightVo.getLotDimDocRightList().get(0).get(0).getDimType();
        //        String appCode = docRightVo.getLotDimDocRightList().get(0).get(0).getAppCode();
        Integer rightVer = docRightVo.getLotDimDocRightList().get(0).get(0).getRightVer();
        if (verNo != null) {
            rightVer = verNo;
            docRightVo.getLotDimDocRightList().get(0).get(0).setRightVer(verNo);
        } else {
            verNo = docRightVo.getRightVer();

        }
        ecmDocrightDefMapper
                .delete(new LambdaQueryWrapper<EcmDocrightDef>().eq(EcmDocrightDef::getDimType, IcmsConstants.VOL_TYPE)
                        .eq(EcmDocrightDef::getAppCode, docRightVo.getAppCode()).eq(EcmDocrightDef::getRightVer, verNo));
        for (List<EcmDocrightDefDTO> docrightDefExtendList : lotDimDocRightList) {
            docRightVo.setDocRightList(docrightDefExtendList);
            //资料权限定义表插入数据
            addDocRightDefData(docRightVo, IcmsConstants.VOL_TYPE, verNo);
        }
        //根据业务类型和版本号更新业务版本权限信息
        updateEcmAppDocRight(docRightVo.getAppCode(), rightVer, token.getUsername());
    }

    /**
     * 业务资料权限信息
     */
    public EcmAppDocrightDTO detailsVer(Long id) {
        AssertUtils.isNull(id, "参数错误");
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocright>().eq(EcmAppDocright::getId, id));
        if (CollectionUtils.isEmpty(appDocrights)) {
            return null;
        }
        List<EcmAppDocrightDTO> appDocrightExtends = PageCopyListUtils
                .copyListProperties(appDocrights, EcmAppDocrightDTO.class);
        //添加创建人、最近更新人名称、业务类型名称、角色维度是否使用、业务多维度是否使用
        AddVerVO addVerVo = new AddVerVO();
        addVerVo.setAppCode(appDocrights.get(0).getAppCode());
        handleData(appDocrightExtends, addVerVo);
        return appDocrightExtends.get(0);
    }

    private void insertDocrightDefByOne(AddVerVO addVerVo, EcmAppDocright ecmAppDocright) {
        EcmDocrightDef ecmDocrightDef = new EcmDocrightDef();
        //ecmDocrightDef.setIsUse(addVerVo.getIsUse());
        ecmDocrightDef.setCreateUser(addVerVo.getUpdateUser());
        ecmDocrightDef.setCreateTime(new Date());
        ecmDocrightDef.setDimType(addVerVo.getDimType());
        ecmDocrightDef.setAppCode(ecmAppDocright.getAppCode());
        ecmDocrightDef.setRightVer(ecmAppDocright.getRightVer());
        ecmDocrightDef.setRoleDimVal("null");
        ecmDocrightDef.setDocCode("0");
        ecmDocrightDef.setAddRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setDeleteRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setUpdateRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setReadRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setThumRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setPrintRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setDownloadRight(DocRightConstants.ZERO.toString());
        ecmDocrightDef.setOtherUpdate(DocRightConstants.ZERO.toString());
        //        ecmDocrightDef.setMaxPages(DocRightConstants.ONE_THOUSAND);
        //        ecmDocrightDef.setMinPages(DocRightConstants.ZERO);
        ecmDocrightDefMapper.insert(ecmDocrightDef);
    }

    private List<List<EcmDocrightDefDTO>> getDocRightDefEmptyTemplateByLotDim(EcmAppDocright appDocright,
                                                                              List<EcmDocDefRelVer> ecmDocDefs,
                                                                              List<String> dimValueList) {
        List<List<EcmDocrightDefDTO>> lotDimDocRightExtendList = new ArrayList<>();
        for (String dimValue : dimValueList) {
            List<EcmDocrightDefDTO> docrightDefExtendList = new ArrayList<>();
            for (EcmDocDefRelVer def : ecmDocDefs) {
                EcmDocrightDefDTO docrightDefExtend = new EcmDocrightDefDTO();
                docrightDefExtend.setRoleDimVal(dimValue);
                docrightDefExtend.setAppCode(appDocright.getAppCode());
                docrightDefExtend.setRightVer(appDocright.getRightVer());
                docrightDefExtend.setDimType(DocRightConstants.LOT_DIM);
                docrightDefExtend.setIsUse(DocRightConstants.ONE);
                docrightDefExtend.setDocCode(def.getDocCode());
                docrightDefExtend.setDocName(def.getDocName());
                docrightDefExtend.setAddRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setDeleteRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setUpdateRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setReadRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setThumRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setPrintRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setDownloadRight(DocRightConstants.ZERO.toString());
                docrightDefExtend.setOtherUpdate(DocRightConstants.ZERO.toString());
                //                docrightDefExtend.setMaxPages(DocRightConstants.ONE_THOUSAND);
                //                docrightDefExtend.setMinPages(DocRightConstants.ZERO);
                //                docrightDefExtend.setEcmFileTypeDefList(getAllFileType());
                docrightDefExtendList.add(docrightDefExtend);
            }
            lotDimDocRightExtendList.add(docrightDefExtendList);
        }
        return lotDimDocRightExtendList;
    }

    private List<String> getDimValueListByFirst(List<EcmDimensionDefDTO> dimensionDefs) {
        if (CollectionUtil.isEmpty(dimensionDefs)) {
            return new ArrayList<>();
        }
        dimensionDefs = dimensionDefs.stream()
                .sorted(Comparator.comparing(EcmDimensionDef::getDimCode))
                .collect(Collectors.toList());
        String[][] array = new String[dimensionDefs.size()][];
        for (int i = 0; i < dimensionDefs.size(); i++) {
            String[] strs = dimensionDefs.get(i).getDimValue().split(";");
            array[i] = new String[strs.length];
            for (int j = 0; j < strs.length; j++) {
                array[i][j] = strs[j];
            }
        }
        //获取集合
        ArrayList<String> list = FileSizeUtils.generateList(array);
        //非必选关联维度值
        List<String> notRequiredValues1 = new ArrayList<>();
        //分组
        Map<Long, List<EcmDimensionDef>> groupDimById = dimensionDefs.stream()
                .collect(Collectors.groupingBy(EcmDimensionDef::getId));
        dimensionDefs.forEach(p -> {
            List<EcmDimensionDef> dimensionDefs1 = groupDimById.get(p.getDimCode());
            if (!CollectionUtils.isEmpty(dimensionDefs1)) {
                if (!DocRightConstants.ONE.equals(p.getRequired())) {
                    notRequiredValues1.add(p.getDimCode());
                }
            }
        });
        //增加为空的数据
        if (!CollectionUtils.isEmpty(notRequiredValues1)) {
            String[][] array1 = new String[dimensionDefs.size()][];
            for (int i = 0; i < dimensionDefs.size(); i++) {
                if (notRequiredValues1.contains(String.valueOf(dimensionDefs.get(i).getId()))) {
                    array1[i] = new String[1];
                    array1[i][0] = "暂无";
                } else {
                    String[] strs = dimensionDefs.get(i).getDimValue().split(";");
                    array1[i] = new String[strs.length];
                    for (int j = 0; j < strs.length; j++) {
                        array1[i][j] = strs[j];
                    }
                }
            }
            //获取集合
            ArrayList<String> list2 = FileSizeUtils.generateList(array1);
            list.addAll(list2);
        }
        return list;
    }

    private List<String> getDimValueList(List<EcmAppDimension> appDimensions) {
        //所有关联维度id
        List<Long> relateDimIds = appDimensions.stream().map(EcmAppDimension::getDimId)
                .collect(Collectors.toList());
        //获取关联维度信息
        List<EcmDimensionDef> dimensionDefs = ecmDimensionDefMapper.selectBatchIds(relateDimIds);
        dimensionDefs = dimensionDefs.stream()
                .sorted(Comparator.comparing(EcmDimensionDef::getDimCode))
                .collect(Collectors.toList());
        String[][] array = new String[dimensionDefs.size()][];
        for (int i = 0; i < dimensionDefs.size(); i++) {
            String[] strs = dimensionDefs.get(i).getDimValue().split(";");
            array[i] = new String[strs.length];
            for (int j = 0; j < strs.length; j++) {
                array[i][j] = strs[j];
            }
        }
        //获取集合
        ArrayList<String> list = FileSizeUtils.generateList(array);
        //非必选关联维度值
        List<Long> notRequiredValues1 = new ArrayList<>();
        //分组
        Map<Long, List<EcmDimensionDef>> groupDimById = dimensionDefs.stream()
                .collect(Collectors.groupingBy(EcmDimensionDef::getId));
        appDimensions.forEach(p -> {
            List<EcmDimensionDef> dimensionDefs1 = groupDimById.get(p.getDimId());
            if (!CollectionUtils.isEmpty(dimensionDefs1)) {
                if (!DocRightConstants.ONE.equals(p.getRequired())) {
                    notRequiredValues1.add(p.getDimId());
                }
            }
        });
        //增加为空的数据
        if (!CollectionUtils.isEmpty(notRequiredValues1)) {
            String[][] array1 = new String[dimensionDefs.size()][];
            for (int i = 0; i < dimensionDefs.size(); i++) {
                if (notRequiredValues1.contains(dimensionDefs.get(i).getId())) {
                    array1[i] = new String[1];
                    array1[i][0] = "暂无";
                } else {
                    String[] strs = dimensionDefs.get(i).getDimValue().split(";");
                    array1[i] = new String[strs.length];
                    for (int j = 0; j < strs.length; j++) {
                        array1[i][j] = strs[j];
                    }
                }
            }
            //获取集合
            ArrayList<String> list2 = FileSizeUtils.generateList(array1);
            list.addAll(list2);
        }
        return list;
    }

    private void checkEditDim(EcmDimensionDef ecmDimensionDef) {
        AssertUtils.isNull(ecmDimensionDef.getId(), "参数错误");
        AssertUtils.isNull(ecmDimensionDef.getDimName(), "维度类型不能为空");
        AssertUtils.isNull(ecmDimensionDef.getDimValue(), "维度值不能为空");
    }

    private void checkDelDims(List<Long> ids) {
        List<EcmAppDimension> appDimensions = ecmAppDimensionMapper
                .selectList(new LambdaQueryWrapper<EcmAppDimension>().in(EcmAppDimension::getDimId, ids));
        if (CollectionUtils.isEmpty(appDimensions)) {
            return;
        }
        List<Long> relateIds = appDimensions.stream().map(EcmAppDimension::getDimId)
                .collect(Collectors.toList());
        List<EcmDimensionDef> ecmDimensionDefs = ecmDimensionDefMapper.selectBatchIds(relateIds);
        List<String> relateDimNames = ecmDimensionDefs.stream().map(EcmDimensionDef::getDimName)
                .collect(Collectors.toList());
        String relateDimNameString = String.join("，", relateDimNames);
        AssertUtils.isTrue(true, "无法批量删除，存在维度" + "{" + relateDimNameString + "}" + "已被关联");
    }

    private void addUserNameRelateStatus(List<EcmDimensionDefDTO> ecmDimensionDefDTOS) {
        List<String> userIds = new ArrayList<>();
        ecmDimensionDefDTOS.forEach(p -> {
            if (!ObjectUtils.isEmpty(p.getCreateUser())) {
                userIds.add(p.getCreateUser());
            }
            if (!ObjectUtils.isEmpty(p.getUpdateUser())) {
                userIds.add(p.getUpdateUser());
            }
        });
        Map<String, List<SysUserDTO>> groupedByUserId = getUserListByUserIds(userIds);
        List<Long> dimIds = ecmDimensionDefDTOS.stream().map(EcmDimensionDef::getId)
                .collect(Collectors.toList());
        List<EcmAppDimension> appDimensions = ecmAppDimensionMapper
                .selectList(new LambdaQueryWrapper<EcmAppDimension>().in(EcmAppDimension::getDimId, dimIds));
        Map<Long, List<EcmAppDimension>> groupByDimId = appDimensions.stream()
                .collect(Collectors.groupingBy(EcmAppDimension::getDimId));
        for (EcmDimensionDefDTO extend : ecmDimensionDefDTOS) {
            //添加创建人名称
            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
                if (!CollectionUtils.isEmpty(groupedByUserId.get(extend.getCreateUser()))) {
                    extend.setCreateUserName(
                            groupedByUserId.get(extend.getCreateUser()).get(0).getName());
                }
            }
            //最近修改人名称
            if (!ObjectUtils.isEmpty(extend.getUpdateUser())) {
                if (!CollectionUtils.isEmpty(groupedByUserId.get(extend.getUpdateUser()))) {
                    extend.setUpdateUserName(
                            groupedByUserId.get(extend.getUpdateUser()).get(0).getName());
                }
            }
            //添加被关联状态
            if (CollectionUtils.isEmpty(groupByDimId.get(extend.getId()))) {
                //未被关联
                extend.setRelateStatus(DocRightConstants.ZERO);
            } else {
                //已被关联
                extend.setRelateStatus(DocRightConstants.ONE);
            }
        }
    }

    private void checkParamAddDimension(EcmDimensionDef ecmDimensionDef) {
        AssertUtils.isNull(ecmDimensionDef.getDimCode(), "维度代码不能为空");
        AssertUtils.isNull(ecmDimensionDef.getDimName(), "维度类型不能为空");
        AssertUtils.isNull(ecmDimensionDef.getDimValue(), "维度值不能为空");
        //维度代码唯一
        Long dimCodeCount = ecmDimensionDefMapper.selectCount(
                new LambdaQueryWrapper<EcmDimensionDef>().eq(EcmDimensionDef::getDimCode, ecmDimensionDef.getDimCode()));
        AssertUtils.isTrue(dimCodeCount.intValue() > 0, "维度代码已存在");
    }

    private void addDocRightDefData(DocRightVO docRightVo, Integer roleType, Integer verNo) {
        List<EcmDocrightDefDTO> docRightList = docRightVo.getDocRightList();
        for (EcmDocrightDefDTO docrightDefExtend : docRightList) {
            EcmDocrightDef docrightDef = new EcmDocrightDef();
            BeanUtils.copyProperties(docrightDefExtend, docrightDef);
            docrightDef.setOtherUpdate(!StringUtils.isEmpty(docrightDefExtend.getOtherUpdate())
                    ? docrightDefExtend.getOtherUpdate()
                    : StateConstants.NO.toString());
            if (ObjectUtils.isEmpty(docrightDef.getCreateUser())) {
                docrightDef.setCreateUser(docRightVo.getCurrentUser());
                docrightDef.setCreateTime(new Date());
            } else {
                docrightDef.setUpdateUser(docRightVo.getCurrentUser());
                docrightDef.setUpdateTime(new Date());
            }
            if (docRightVo.getRoleId() != null) {
                docrightDef.setRoleDimVal(docRightVo.getRoleId().toString());
            }
            docrightDef.setAppCode(docRightVo.getAppCode());
            docrightDef.setDimType(roleType);
            docrightDef.setRightVer(verNo);
            if (docRightVo.getRightVer() == null) {
                docrightDef.setDocrightId(null);
            }
            // TODO 循环插入
            ecmDocrightDefMapper.insert(docrightDef);
            //todo 改批量插入
            docrightDefExtend.setDocrightId(docrightDef.getDocrightId());
        }
    }

    private void checkParamSaveRoleDocRight(DocRightVO docRightVo) {
        AssertUtils.isNull(docRightVo.getRoleId(), "角色不能为空");
        AssertUtils.isNull(docRightVo.getDocRightList(), "当前业务未关联资料，请先进行资料关联");
    }

    private void handleData(List<EcmAppDocrightDTO> appDocrightExtends, AddVerVO addVerVo) {
        List<String> userIds = new ArrayList<>();
        for (EcmAppDocrightDTO extend : appDocrightExtends) {
            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
                userIds.add(extend.getCreateUser());
            }
            if (!ObjectUtils.isEmpty(extend.getUpdateUser())) {
                userIds.add(extend.getCreateUser());
            }
        }
        Map<String, List<SysUserDTO>> groupedByUserId = this.getUserListByUserIds(userIds);
        EcmAppDef appDef = ecmAppDefMapper.selectById(addVerVo.getAppCode());

        for (EcmAppDocrightDTO extend : appDocrightExtends) {
            //业务类型名称
            if (!ObjectUtils.isEmpty(appDef)) {
                extend.setAppTypeName(appDef.getAppName());
            }
            if (groupedByUserId == null) {
                continue;
            }
            //添加创建人
            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
                if (!CollectionUtils.isEmpty(groupedByUserId.get(extend.getCreateUser()))) {
                    extend.setCreateUserName(
                            groupedByUserId.get(extend.getCreateUser()).get(0).getName());
                }
            }
            //最近修改人
            if (!ObjectUtils.isEmpty(extend.getUpdateUser())) {
                if (!CollectionUtils.isEmpty(groupedByUserId.get(extend.getUpdateUser()))) {
                    extend.setUpdateUserName(
                            groupedByUserId.get(extend.getUpdateUser()).get(0).getName());
                }
            }
        }

    }

    /**
     * 根据userId列表获取用户信息列表
     */
    public Map<String, List<SysUserDTO>> getUserListByUserIds(List<String> userIds) {
        Result<List<SysUserDTO>> result = userApi
                .getUserListByUsernames(userIds.toArray(new String[0]));
        List<SysUserDTO> userList = new ArrayList<>();
        if (ResultCode.SUCCESS.getCode().equals(result.getCode())) {
            userList = result.getData();
        } else {
            throw new SunyardException(result.getMsg());
        }
        if (ObjectUtils.isEmpty(userList)) {
            return null;
        }
        Map<String, List<SysUserDTO>> groupedByUserId = userList.stream()
                .collect(Collectors.groupingBy(SysUserDTO::getLoginName));
        return groupedByUserId;
    }

    /**
     * 删除版本
     */
    public void delVer(Integer rightVer, String appCode) {
        AssertUtils.isNull(rightVer, "版本号不能为空");
        AssertUtils.isNull(appCode, "所属业务类型不能为空");
        Long l = ecmBusiInfoMapper.selectCountAll(rightVer, appCode);
        AssertUtils.isTrue(l > 0, "当前版本已被使用无法删除");
        //删除版本
        ecmAppDocrightMapper.delete(new LambdaUpdateWrapper<EcmAppDocright>()
                .eq(EcmAppDocright::getRightVer, rightVer).eq(EcmAppDocright::getAppCode, appCode));
        //删除关联的版本资料
        ecmDocDefRelVerMapper.delete(new LambdaUpdateWrapper<EcmDocDefRelVer>()
                .eq(EcmDocDefRelVer::getRightVer, rightVer)
                .eq(EcmDocDefRelVer::getAppCode, appCode));
        //删除角色资料权限表
        ecmDocrightDefMapper.delete(new LambdaUpdateWrapper<EcmDocrightDef>()
                .eq(EcmDocrightDef::getRightVer, rightVer).eq(EcmDocrightDef::getAppCode, appCode));
    }

    /**
     * 同时保存角色和多维度
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleAndLotDimDocRight(DocRightRoleAndLotVO docRightVo,
                                          AccountTokenExtendDTO token) {
        Integer verNo = null;
        List<DocRightVO> otherRole = new ArrayList<>();
        List<DocRightVO> shareTreeotherRole = new ArrayList<>();
        String appCode = docRightVo.getAppCode();
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        Integer isShareTree = ecmAppDef.getIsShareTree();

        // ============ 公共：提前拿到树节点docCode列表 ============
        EcmDocTreeVO vo = new EcmDocTreeVO();
        vo.setList(docRightVo.getEcmDocTreeVO());
        vo.setAppCode(appCode);
        List<EcmAppDocRel> ecmAppDocRels = relevanceInformation(vo);
        List<String> docCodeList = ecmAppDocRels.stream()
                .map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());

        if (docRightVo.getRightVer() == null) {
            // ---------- 新建版本 ----------
            verNo = getVerNo(appCode);

            Long countByAppType = ecmAppDocrightMapper
                    .selectCount(new LambdaQueryWrapper<EcmAppDocright>().eq(EcmAppDocright::getAppCode, appCode));
            EcmAppDocright ecmAppDocright = new EcmAppDocright();
            ecmAppDocright.setCreateUser(token.getUsername());
            ecmAppDocright.setAppCode(appCode);
            ecmAppDocright.setLotDimUse(docRightVo.getLotDimUse());
            ecmAppDocright.setRightVer(verNo);
            ecmAppDocright.setRightNew(
                    countByAppType.intValue() > 0 ? DocRightConstants.ZERO : DocRightConstants.ONE);
            ecmAppDocrightMapper.insert(ecmAppDocright);

            // 过滤树数据
            filterDocRightByDocCode(docRightVo, docCodeList);

            // 处理当前角色权限 + 共享树其他角色
            if (docRightVo.getRoleDocRight() != null) {
                List<EcmDocrightDefDTO> collect1 = docRightVo.getRoleDocRight().getDocRightList();
                if (collect1 != null) {
                    List<EcmDocrightDefDTO> copyRightList = PageCopyListUtils.copyListProperties(
                            collect1, EcmDocrightDefDTO.class
                    );
                    // 共享树：给所有权限
                    if (IcmsConstants.ONE.equals(isShareTree)) {
                        for (EcmDocrightDefDTO docRight : collect1) {
                            setAllRightOne(docRight);
                        }
                        // 收集所有其他角色 → shareTreeotherRole
                        fillShareTreeOtherRole(appCode, verNo, docRightVo.getRoleDocRight().getRoleId(),
                                copyRightList, shareTreeotherRole);
                    }
                    docRightVo.getRoleDocRight().setDocRightList(collect1);

                    // 非共享树：处理原有其他角色
                    List<EcmDocrightDefDTO> ecmDocrightDefDTOS1 = PageCopyListUtils
                            .copyListProperties(collect1, EcmDocrightDefDTO.class);
                    Integer rightVer = verNo;
                    List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper
                            .selectList(new LambdaUpdateWrapper<EcmDocrightDef>()
                                    .ne(EcmDocrightDef::getRoleDimVal,
                                            docRightVo.getRoleDocRight().getRoleId().toString())
                                    .eq(EcmDocrightDef::getDimType, IcmsConstants.ZERO)
                                    .eq(EcmDocrightDef::getAppCode, appCode)
                                    .eq(EcmDocrightDef::getRightVer, rightVer));
                    Map<String, List<EcmDocrightDef>> collect2 = ecmDocrightDefs.stream()
                            .collect(Collectors.groupingBy(EcmDocrightDef::getRoleDimVal));
                    for (String roles : collect2.keySet()) {
                        DocRightVO vo1 = new DocRightVO();
                        vo1.setRoleId(Long.parseLong(roles));
                        vo1.setAppCode(appCode);
                        vo1.setRightVer(verNo);
                        List<EcmDocrightDef> ecmDocrightDefs1 = collect2.get(roles);
                        List<EcmDocrightDef> collect3 = ecmDocrightDefs1.stream()
                                .filter(s -> docCodeList.contains(s.getDocCode()))
                                .collect(Collectors.toList());
                        List<String> collect5 = ecmDocrightDefs1.stream()
                                .map(EcmDocrightDef::getDocCode).collect(Collectors.toList());
                        List<EcmDocrightDefDTO> collect6 = ecmDocrightDefDTOS1.stream()
                                .filter(s -> !collect5.contains(s.getDocCode()))
                                .collect(Collectors.toList());
                        List<EcmDocrightDefDTO> ecmDocrightDefDTOS = PageCopyListUtils
                                .copyListProperties(collect3, EcmDocrightDefDTO.class);
                        ArrayList<EcmDocrightDefDTO> objects = new ArrayList<>();
                        String flag = IcmsConstants.ZERO_STR;
                        if (IcmsConstants.ONE.equals(isShareTree)) {
                            flag = IcmsConstants.ONE_STR;
                        }
                        for (EcmDocrightDefDTO dto : collect6) {
                            EcmDocrightDefDTO dto1 = new EcmDocrightDefDTO();
                            BeanUtils.copyProperties(dto, dto1);
                            dto1.setAppCode(appCode);
                            dto1.setRoleDimVal(vo1.getRoleId().toString());
                            dto1.setRightVer(verNo > 1 ? verNo - 1 : verNo);
                            setAllRight(dto1, flag);
                            objects.add(dto1);
                        }
                        ecmDocrightDefDTOS.addAll(objects);
                        for (EcmDocrightDefDTO ecmDocrightDef : ecmDocrightDefDTOS) {
                            ecmDocrightDef.setDocrightId(null);
                        }
                        vo1.setDocRightList(ecmDocrightDefDTOS);
                        otherRole.add(vo1);
                    }
                }
            }

            getAndInsertEcmDocDefRelVerList(verNo, appCode, ecmAppDocRels);

        } else {
            // ---------- 编辑已有版本---------
            Integer rightVer = docRightVo.getRightVer();
            verNo = rightVer; // 编辑时verNo就是当前版本

            // 更新主表
            ecmAppDocrightMapper.update(null,
                    new LambdaUpdateWrapper<EcmAppDocright>()
                            .eq(EcmAppDocright::getRightVer, rightVer)
                            .eq(EcmAppDocright::getAppCode, appCode)
                            .set(EcmAppDocright::getLotDimUse, docRightVo.getLotDimUse()));

            // 删旧关联、重存树
            ecmDocDefRelVerMapper.delete(new LambdaUpdateWrapper<EcmDocDefRelVer>()
                    .eq(EcmDocDefRelVer::getRightVer, rightVer)
                    .eq(EcmDocDefRelVer::getAppCode, appCode));
            getAndInsertEcmDocDefRelVerList(rightVer, appCode, ecmAppDocRels);

            // 过滤树数据
            filterDocRightByDocCode(docRightVo, docCodeList);

            // 处理当前角色权限
            if (docRightVo.getRoleDocRight() != null) {
                List<EcmDocrightDefDTO> collect1 = docRightVo.getRoleDocRight().getDocRightList();
                if (collect1 != null) {
                    List<EcmDocrightDefDTO> copyRightList = PageCopyListUtils.copyListProperties(
                            collect1, EcmDocrightDefDTO.class
                    );

                    // 共享树：给所有权限 + 收集其他角色（关键补全）
                    if (IcmsConstants.ONE.equals(isShareTree)) {
                        for (EcmDocrightDefDTO docRight : collect1) {
                            setAllRightOne(docRight);
                        }
                        // 编辑时也重新生成 shareTreeotherRole
                        fillShareTreeOtherRole(appCode, rightVer, docRightVo.getRoleDocRight().getRoleId(),
                                copyRightList, shareTreeotherRole);
                    }
                    docRightVo.getRoleDocRight().setDocRightList(collect1);

                    // 非共享树：原有其他角色逻辑保留（跟新建一致）
                    List<EcmDocrightDefDTO> ecmDocrightDefDTOS1 = PageCopyListUtils
                            .copyListProperties(collect1, EcmDocrightDefDTO.class);
                    List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper
                            .selectList(new LambdaUpdateWrapper<EcmDocrightDef>()
                                    .ne(EcmDocrightDef::getRoleDimVal,
                                            docRightVo.getRoleDocRight().getRoleId().toString())
                                    .eq(EcmDocrightDef::getDimType, IcmsConstants.ZERO)
                                    .eq(EcmDocrightDef::getAppCode, appCode)
                                    .eq(EcmDocrightDef::getRightVer, rightVer));
                    Map<String, List<EcmDocrightDef>> collect2 = ecmDocrightDefs.stream()
                            .collect(Collectors.groupingBy(EcmDocrightDef::getRoleDimVal));
                    for (String roles : collect2.keySet()) {
                        DocRightVO vo1 = new DocRightVO();
                        vo1.setRoleId(Long.parseLong(roles));
                        vo1.setAppCode(appCode);
                        vo1.setRightVer(rightVer);
                        List<EcmDocrightDef> ecmDocrightDefs1 = collect2.get(roles);
                        List<EcmDocrightDef> collect3 = ecmDocrightDefs1.stream()
                                .filter(s -> docCodeList.contains(s.getDocCode()))
                                .collect(Collectors.toList());
                        List<String> collect5 = ecmDocrightDefs1.stream()
                                .map(EcmDocrightDef::getDocCode).collect(Collectors.toList());
                        List<EcmDocrightDefDTO> collect6 = ecmDocrightDefDTOS1.stream()
                                .filter(s -> !collect5.contains(s.getDocCode()))
                                .collect(Collectors.toList());
                        List<EcmDocrightDefDTO> ecmDocrightDefDTOS = PageCopyListUtils
                                .copyListProperties(collect3, EcmDocrightDefDTO.class);
                        ArrayList<EcmDocrightDefDTO> objects = new ArrayList<>();
                        String flag = IcmsConstants.ZERO_STR;
                        if (IcmsConstants.ONE.equals(isShareTree)) {
                            flag = IcmsConstants.ONE_STR;
                        }
                        for (EcmDocrightDefDTO dto : collect6) {
                            EcmDocrightDefDTO dto1 = new EcmDocrightDefDTO();
                            BeanUtils.copyProperties(dto, dto1);
                            dto1.setAppCode(appCode);
                            dto1.setRoleDimVal(vo1.getRoleId().toString());
                            dto1.setRightVer(rightVer);
                            setAllRight(dto1, flag);
                            objects.add(dto1);
                        }
                        ecmDocrightDefDTOS.addAll(objects);
                        for (EcmDocrightDefDTO ecmDocrightDef : ecmDocrightDefDTOS) {
                            ecmDocrightDef.setDocrightId(null);
                        }
                        vo1.setDocRightList(ecmDocrightDefDTOS);
                        otherRole.add(vo1);
                    }
                }
            }
        }

        // ============ 统一保存 ============
        if (docRightVo.getRoleDocRight() != null) {
            docRightVo.getRoleDocRight().setAppCode(appCode);
            docRightVo.getRoleDocRight().setRightVer(verNo);
            saveRoleDocRight(docRightVo.getRoleDocRight(), token, verNo);

            // 共享树走 shareTreeotherRole，非共享树走 otherRole
            if (IcmsConstants.ONE.equals(isShareTree)) {
                for (DocRightVO vc : shareTreeotherRole) {
                    List<EcmDocrightDefDTO> copyRightListNew = vc.getDocRightList().stream()
                            .peek(dto -> dto.setDocrightId(null))
                            .collect(Collectors.toList());
                    vc.setDocRightList(copyRightListNew);
                    saveRoleDocRight(vc, token, verNo);
                }
            } else {
                for (DocRightVO vc : otherRole) {
                    saveRoleDocRight(vc, token, verNo);
                }
            }
        }

        // 多维度
        if (docRightVo.getLotDicRight() != null
                && !CollectionUtils.isEmpty(docRightVo.getLotDicRight().getLotDimDocRightList())
                && StateConstants.YES.equals(docRightVo.getLotDimUse())) {
            docRightVo.getLotDicRight().setAppCode(appCode);
            docRightVo.getLotDicRight().setRightVer(verNo);
            saveLotDimDocRight(docRightVo.getLotDicRight(), token, verNo);
        }
    }

    /**
     * 根据树节点过滤权限数据
     */
    private void filterDocRightByDocCode(DocRightRoleAndLotVO docRightVo, List<String> docCodeList) {
        if (docRightVo.getLotDicRight() != null) {
            List<EcmDocrightDefDTO> docRightList = docRightVo.getLotDicRight().getDocRightList();
            if (docRightList != null) {
                List<EcmDocrightDefDTO> collect1 = docRightList.stream()
                        .filter(s -> docCodeList.contains(s.getDocCode()))
                        .collect(Collectors.toList());
                docRightVo.getLotDicRight().setDocRightList(collect1);
            }
        }
    }

    /**
     * 给权限全设为1
     */
    private void setAllRightOne(EcmDocrightDefDTO docRight) {
        docRight.setAddRight("1");
        docRight.setReadRight("1");
        docRight.setUpdateRight("1");
        docRight.setDeleteRight("1");
        docRight.setThumRight("1");
        docRight.setPrintRight("1");
        docRight.setDownloadRight("1");
        docRight.setOtherUpdate("1");
    }

    /**
     * 给权限全设为指定值
     */
    private void setAllRight(EcmDocrightDefDTO dto, String flag) {
        dto.setAddRight(flag);
        dto.setReadRight(flag);
        dto.setUpdateRight(flag);
        dto.setDeleteRight(flag);
        dto.setThumRight(flag);
        dto.setDownloadRight(flag);
        dto.setPrintRight(flag);
        dto.setOtherUpdate(flag);
    }

    /**
     * 填充共享树其他角色权限
     */
    private void fillShareTreeOtherRole(String appCode, Integer verNo, Long currentRoleId,
                                        List<EcmDocrightDefDTO> copyRightList,
                                        List<DocRightVO> shareTreeotherRole) {
        SysRoleDTO sysRoleDTO = new SysRoleDTO();
        sysRoleDTO.setSystemCode(RoleConstants.ICMS.toString());
        sysRoleDTO.setPageSize(IcmsConstants.ZERO);
        Result result = roleApi.searchListInUsePage(sysRoleDTO);
        if (result.isSucc() && result.getData() != null) {
            Map data = (Map) result.getData();
            List<Map> list = (List<Map>) data.get("list");
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(s -> {
                    Long roleId = Long.parseLong(s.get("roleId").toString());
                    if (!roleId.equals(currentRoleId)) {
                        DocRightVO docRightVO = new DocRightVO();
                        docRightVO.setRoleId(roleId);
                        docRightVO.setAppCode(appCode);
                        docRightVO.setRightVer(verNo);
                        docRightVO.setDocRightList(copyRightList);
                        shareTreeotherRole.add(docRightVO);
                    }
                });
            }
        }
    }

    private void getAndInsertEcmDocDefRelVerList(Integer rightVer, String appCode,
                                                 List<EcmAppDocRel> ecmAppDocRels) {
        List<String> docCodeIds = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(docCodeIds);
        List<EcmDocDefRelVer> list2 = new ArrayList<>();
        ecmDocDefs.forEach(ecmDocDef -> {
            EcmDocDefRelVer ecmDocDefRelVer = new EcmDocDefRelVer();
            BeanUtils.copyProperties(ecmDocDef, ecmDocDefRelVer);
            ecmDocDefRelVer.setAppCode(appCode);
            ecmDocDefRelVer.setRightVer(rightVer);
            ecmDocDefRelVer.setId(snowflakeUtil.nextId());
            list2.add(ecmDocDefRelVer);
        });
        insertEcmDocDefRelVers(list2);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<EcmAppDocRel> relevanceInformation(EcmDocTreeVO ecmDocTreeVo) {
        AssertUtils.isNull(ecmDocTreeVo.getAppCode(), "业务类型id不能为空");
        List<EcmAppDocRel> ecmAppDocRels = new ArrayList<>();
        String appCode = ecmDocTreeVo.getAppCode();
        //保存版本
        List<EcmAppDocRel> ecmAppDocRels1 = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode));
        List<String> collect = ecmAppDocRels1.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        Map<String, List<EcmAppDocRel>> longListMap = ecmAppDocRels1.stream()
                .collect(Collectors.groupingBy(EcmAppDocRel::getDocCode));
        if (!com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(ecmDocTreeVo.getList())) {
            readTree(ecmDocTreeVo.getList(), ecmAppDocRels, appCode, collect, longListMap);
            if (!com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(ecmAppDocRels)) {
                // docCode 列表
                List<String> docCodeList = ecmAppDocRels.stream()
                        .map(EcmAppDocRel::getDocCode)
                        .collect(Collectors.toList());
                ecmAppDocRelMapper.delete(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, ecmDocTreeVo.getAppCode()).in(EcmAppDocRel::getDocCode,docCodeList));
                insertEcmAppDocRels(ecmAppDocRels);
                //ecmAppDocRelMapper.insertList(ecmAppDocRels);
            }
        }
        return ecmAppDocRels;
    }

    private void insertEcmAppDocRels(List<EcmAppDocRel> ecmAppDocRels) {
        MybatisBatch<EcmAppDocRel> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmAppDocRels);
        MybatisBatch.Method<EcmAppDocRel> method = new MybatisBatch.Method<>(
                EcmAppDocRelMapper.class);
        mybatisBatch.execute(method.insert());
    }

    private void readTree(List<EcmAppDocRelDTO> ecmAppDocRelDTOS, List<EcmAppDocRel> ecmAppDocRels,
                          String appCode, List<String> collect,
                          Map<String, List<EcmAppDocRel>> longListMap) {
        Integer i = 0;
        Float j = 0.0F;
        for (EcmAppDocRelDTO e : ecmAppDocRelDTOS) {
            EcmAppDocRel ecmAppDocRel = new EcmAppDocRel();
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(e.getChildren())) {
                //                if (!collect.contains(e.getDocCode())){
                ecmAppDocRel.setId(snowflakeUtil.nextId());
                ecmAppDocRel.setDocSort(e.getDocSort());
                ecmAppDocRel.setAppCode(appCode);
                ecmAppDocRel.setDocCode(e.getDocCode());
                ecmAppDocRel.setType(StateConstants.COMMON_ONE);
                ecmAppDocRels.add(ecmAppDocRel);
                //                }else {
                //                    ecmAppDocRel.setId(longListMap.get(e.getDocCode()).get(0).getId());
                //                    ecmAppDocRel.setAppCode(appCode);
                //                    ecmAppDocRel.setDocCode(e.getDocCode());
                //                    ecmAppDocRel.setDocSort(i);
                //                    ecmAppDocRels.add(ecmAppDocRel);
                ////                }
            } else {
                //代表新的排序
                i = 0;
                ecmAppDocRel.setId(snowflakeUtil.nextId());
                ecmAppDocRel.setDocSort(j);
                j += 1.0f;
                ecmAppDocRel.setAppCode(appCode);
                ecmAppDocRel.setDocCode(e.getDocCode());
                ecmAppDocRel.setType(StateConstants.ZERO);
                ecmAppDocRels.add(ecmAppDocRel);
                readTree(e.getChildren(), ecmAppDocRels, appCode, collect, longListMap);
            }
            ++i;
        }
    }

    public List<Map> getRoleList(SysRoleDTO sysRoleDTO, String appCode, Integer rightVer) {
        List<Map> ret = new ArrayList<>();
        Result result = roleApi.searchListInUsePage(sysRoleDTO);
        if (result.isSucc() && result.getData() != null) {
            Map data = (Map) result.getData();
            List<Map> list = (List<Map>) data.get("list");
            if (!CollectionUtils.isEmpty(list)) {
//                List<Long> collect = new ArrayList<>();
                List<String> collect = new ArrayList<>();
//                list.forEach(s -> collect.add(Long.parseLong(s.get("roleId").toString())));
                list.forEach(s -> collect.add(s.get("roleId").toString()));
                List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper
                        .selectList(new LambdaUpdateWrapper<EcmDocrightDef>()
                                .eq(EcmDocrightDef::getRightVer, rightVer)
                                .eq(EcmDocrightDef::getAppCode, appCode)
                                .eq(EcmDocrightDef::getDimType, IcmsConstants.ROLE_TYPE)
                                .in(EcmDocrightDef::getRoleDimVal, collect));
                Map<String, List<EcmDocrightDef>> collect1 = ecmDocrightDefs.stream()
                        .collect(Collectors.groupingBy(EcmDocrightDef::getRoleDimVal));
                for (Map dto : list) {
                    List<EcmDocrightDef> ecmDocrightDefs1 = collect1.get(dto.get("roleId"));
                    if (StringUtils.isEmpty(rightVer)) {
                        dto.put("isUse", false);
                    } else {
                        dto.put("isUse", !CollectionUtils.isEmpty(ecmDocrightDefs1));
                    }
                    ret.add(dto);
                }
            }
        }
        return ret;
    }

    /**
     * 复制版本需组合以前版本资料和最新关联资料
     */
    private void docRightCopyNew(AddVerVO addVerVo) {
        //复用已有版本的资料权限数据到新的版本中
        List<EcmDocrightDef> docrightDefs = ecmDocrightDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocrightDef>().eq(EcmDocrightDef::getAppCode, addVerVo.getAppCode())
                        .eq(EcmDocrightDef::getRightVer, addVerVo.getSelectVerNo()));
        if (CollectionUtils.isEmpty(docrightDefs)) {
            return;
        }
        //同一个版本选中同一个角色
        String roleDimVal = docrightDefs.get(0).getRoleDimVal();
        Map<String, List<EcmDocrightDef>> docMap = docrightDefs.stream()
                .collect(Collectors.groupingBy(EcmDocrightDef::getDocCode));
        List<EcmDocDefRelVer> ecmDocDefs = new ArrayList<>();
        //查询最新版本资料
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, addVerVo.getAppCode())
                        .eq(EcmAppDocRel::getType, IcmsConstants.ONE));
        if (CollectionUtil.isNotEmpty(ecmAppDocRels)) {
            List<String> docCodeList = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                    .collect(Collectors.toList());
            ecmDocDefs = ecmDocDefRelVerMapper.selectList(new LambdaQueryWrapper<EcmDocDefRelVer>()
                    .in(EcmDocDefRelVer::getDocCode, docCodeList).eq(EcmDocDefRelVer::getAppCode, addVerVo.getAppCode())
                    .eq(EcmDocDefRelVer::getRightVer, addVerVo.getRightVer()));
        }
        //匹配关联中包含复制对象版本中的资料，匹配到则取出该资料权限，否则用重置数据
        AssertUtils.isNull(ecmDocDefs, "版本版未关联资料");
        for (EcmDocDefRelVer ecmDocDefRelVer : ecmDocDefs) {
            Set<String> docSet = docMap.keySet();
            if (docSet.contains(ecmDocDefRelVer.getDocCode())) {
                List<EcmDocrightDef> ecmDocrightDefList = docMap.get(ecmDocDefRelVer.getDocCode());
                EcmDocrightDef def = ecmDocrightDefList.get(0);
                //重置数据并插入新数据
                //                List<EcmDocFileTypeLimit> docFileTypeLimits1 = new ArrayList<>();
                //                if (!CollectionUtils.isEmpty(groupFileLimitByDocRightId)) {
                //                    docFileTypeLimits1 = groupFileLimitByDocRightId.get(def.getDocrightId());
                //                }
                def.setDocrightId(null);
                def.setCreateUser(addVerVo.getCreateUser());
                def.setCreateTime(new Date());
                def.setUpdateUser(null);
                def.setUpdateTime(null);
                def.setRightVer(addVerVo.getRightVer());
                //todo 改批量插入
                ecmDocrightDefMapper.insert(def);
                //                if (!CollectionUtils.isEmpty(docFileTypeLimits1)) {
                //                    for (EcmDocFileTypeLimit limit : docFileTypeLimits1) {
                //                        limit.setId(null);
                //                        limit.setDocrightId(def.getDocrightId());
                //                        limit.setCreateUser(addVerVo.getCreateUser());
                //                        limit.setCreateTime(new Date());
                //                        limit.setUpdateUser(null);
                //                        limit.setUpdateTime(null);
                //                        ecmDocFileTypeLimitMapper.insert(limit);
                //                    }
                //                }
            } else {
                EcmDocrightDef ecmDocrightDef = new EcmDocrightDef();
                //ecmDocrightDef.setIsUse(IcmsConstants.ONE);
                ecmDocrightDef.setCreateUser(addVerVo.getCreateUser());
                ecmDocrightDef.setDimType(DocRightConstants.ZERO);
                ecmDocrightDef.setCreateTime(new Date());
                ecmDocrightDef.setAppCode(ecmDocDefRelVer.getAppCode());
                ecmDocrightDef.setRightVer(addVerVo.getRightVer());
                ecmDocrightDef.setRoleDimVal(roleDimVal);
                ecmDocrightDef.setDocCode(ecmDocDefRelVer.getDocCode());
                ecmDocrightDef.setAddRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setDeleteRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setUpdateRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setReadRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setThumRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setPrintRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setDownloadRight(DocRightConstants.ZERO.toString());
                ecmDocrightDef.setOtherUpdate(DocRightConstants.ZERO.toString());
                //                ecmDocrightDef.setMaxPages(DocRightConstants.ONE_THOUSAND);
                //                ecmDocrightDef.setMinPages(DocRightConstants.ZERO);
                ecmDocrightDefMapper.insert(ecmDocrightDef);
            }
        }
    }

    private void checkParamAddVer(AddVerVO addVerVo) {
        AssertUtils.isNull(addVerVo.getRightVer(), "版本号不能为空");
        AssertUtils.isNull(addVerVo.getAddVerType(), "新增版本类型不能为空");
        AssertUtils.isNull(addVerVo.getAppCode(), "参数错误");
        if (DocRightConstants.EXISTING_VER.equals(addVerVo.getAddVerType())) {
            AssertUtils.isNull(addVerVo.getSelectVerNo(), "选择版本不能为空");
        }
    }
}
