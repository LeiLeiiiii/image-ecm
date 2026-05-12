package com.sunyard.ecm.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmAddAppParamsDTO;
import com.sunyard.ecm.dto.ecm.EcmAddAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmStorageQueDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAIBridgeSyncMapper;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDefRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiStatisticsMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmUserBusiFileStatisticsMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAIBridgeSync;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDefRel;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiStatistics;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmUserBusiFileStatistics;
import com.sunyard.ecm.vo.DeleteBusiAttrVO;
import com.sunyard.ecm.vo.EcmAIBridgeResultVO;
import com.sunyard.ecm.vo.EcmAIBridgeTypeVO;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.RoleApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author： zyl
 * @create： 2023/4/13 16:25
 * @desc: 影像业务处理实现类
 */
@Slf4j
@Service
public class ModelBusiService {

    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmUserBusiFileStatisticsMapper ecmUserBusiFileStatisticsMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmAppDefRelMapper ecmAppDefRelMapper;
    @Resource
    private EcmAsyncTaskMapper ecmAsyncTaskMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmBusiStatisticsMapper ecmBusiStatisticsMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private ParamApi paramApi;
    @Resource
    private RoleApi roleApi;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private ModelPermissionsService modelPermissionsService;
    @Resource
    private SysStorageService sysStorageService;
    @Resource
    private EcmAIBridgeSyncMapper ecmAIBridgeSyncMapper;

    /**
     * 新增业务类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addBusiType(EcmAppDefAttrVO ecmAppDefAttrVo, String userId) {
        AssertUtils.isNull(ecmAppDefAttrVo.getAppCode(), "业务代码不能为空！");
        AssertUtils.isNull(ecmAppDefAttrVo.getAppName(), "业务名称不能为空！");
        AssertUtils.isTrue(validateByteLength(ecmAppDefAttrVo.getAppName()), "业务名称长度超出最长长度！");
        if(StateConstants.PARENT_APP_CODE_DEFAULT.equals(ecmAppDefAttrVo.getAppCode().toLowerCase())){
            log.error("业务代码不能为{}", ecmAppDefAttrVo.getAppCode());
            return Result.error("业务代码不能为"+ecmAppDefAttrVo.getAppCode(), ResultCode.PARAM_ERROR);
        }
        if (ObjectUtils.isEmpty(ecmAppDefAttrVo.getParent())) {
            ecmAppDefAttrVo.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        }
        //返回错误提示
        Result<Object> paramError = getObjectResult(ecmAppDefAttrVo, true);
        if (paramError != null) {
            return paramError;
        }
        //处理顺序号
        dealAppSort(ecmAppDefAttrVo);
        EcmAppDef ecmAppDef = new EcmAppDef();
        BeanUtils.copyProperties(ecmAppDefAttrVo, ecmAppDef);
        //插入业务类型
        ecmAppDef.setCreateUser(userId);
        if (ecmAppDefAttrVo.getQulity() != null) {
            ecmAppDef.setQulity(ecmAppDefAttrVo.getQulity() / IcmsConstants.HUNDRED);
        }
//        ecmAppDef.setRoleIds(String.join(",",ecmAppDefAttrVo.getRoleIdList()));
        ecmAppDef.setRoleIds(ObjectUtils.isEmpty(ecmAppDefAttrVo.getRoleIdList()) ? null : String.join(",", ecmAppDefAttrVo.getRoleIdList()));
        try {
            ecmAppDefMapper.insert(ecmAppDef);
        } catch (DuplicateKeyException e) {
            log.error("插入数据失败，主键重复：{}", ecmAppDef.getAppCode(), e);
            throw new SunyardException("当前业务类型已存在");
        } catch (Exception e) {
            // 处理其他异常
            log.error("插入数据异常", e);
            throw new RuntimeException("插入失败，请稍后重试");
        }
        //插入闭包表
        //将该节点和其所有父节点存入闭包表
        addEcmAppDefRel(ecmAppDefAttrVo, ecmAppDef, ecmAppDef.getAppCode());
        //插入业务类型属性
        List<EcmAppAttr> ecmAppAttrList = ecmAppDefAttrVo.getEcmAppAttrList();
        int i = 0;
        if (!ObjectUtils.isEmpty(ecmAppAttrList)) {
            for (EcmAppAttr ecmAppAttr : ecmAppAttrList) {
                ecmAppAttr.setAppAttrId(snowflakeUtil.nextId());
                ecmAppAttr.setAppCode(ecmAppDef.getAppCode());
                ecmAppAttr.setCreateUser(userId);
                ecmAppAttr.setCreateTime(new Date());
                ecmAppAttr.setStatus(StateConstants.YES);
                ecmAppAttr.setAttrSort(i++);
                if (ObjectUtils.isEmpty(ecmAppAttr.getIsKey())) {
                    ecmAppAttr.setIsKey(StateConstants.ZERO);
                }
                if (ObjectUtils.isEmpty(ecmAppAttr.getIsNull())) {
                    ecmAppAttr.setIsNull(StateConstants.ZERO);
                }
                if (ObjectUtils.isEmpty(ecmAppAttr.getTreeShow())) {
                    ecmAppAttr.setTreeShow(StateConstants.COMMON_ONE);
                }
                if (ObjectUtils.isEmpty(ecmAppAttr.getInputType())) {
                    ecmAppAttr.setInputType(StateConstants.COMMON_ONE);
                }
                if (ObjectUtils.isEmpty(ecmAppAttr.getQueryShow())) {
                    ecmAppAttr.setQueryShow(StateConstants.COMMON_ONE);
                }
                if (ecmAppAttr.getInputType() == 3) {
                    AssertUtils.isNull(ecmAppAttr.getListValue(), "业务属性选项不能为空");
                }
            }
            dealAppAttrPriKey(ecmAppAttrList);
            insertEcmAppAttrs(ecmAppAttrList);
        }
        return Result.success("新增成功");
    }

    private void insertEcmAppAttrs(List<EcmAppAttr> ecmAppAttrList) {
        MybatisBatch<EcmAppAttr> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmAppAttrList);
        MybatisBatch.Method<EcmAppAttr> method = new MybatisBatch.Method<>(EcmAppAttrMapper.class);
        mybatisBatch.execute(method.insert());
    }

    private void dealAppAttrPriKey(List<EcmAppAttr> ecmAppAttrList) {
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrList.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey()))
                .collect(Collectors.toList());
        if (ecmAppAttrs.size() > StateConstants.COMMON_ONE) {
            AssertUtils.notNull(ecmAppAttrs, "每笔业务有且仅有一个主键");
        }
        List<EcmAppAttr> isArchiveds = ecmAppAttrList.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsArchived()))
                .collect(Collectors.toList());
        if (isArchiveds.size() > StateConstants.COMMON_ONE) {
            AssertUtils.notNull(isArchiveds, "每笔业务只能有一个归档标使");
        }
    }

    private void addEcmAppDefRel(EcmAppDefAttrVO ecmAppDefAttrVo, EcmAppDef ecmAppDef,
                                 String appCode) {
        //插入前端传的父节点
        if (ObjectUtils.isEmpty(ecmAppDefAttrVo.getParent())) {
            ecmAppDefAttrVo.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        }
        //插入父节点为自己
        EcmAppDefRel ecmAppDefRel11 = new EcmAppDefRel();
        ecmAppDefRel11.setAppCode(ecmAppDef.getAppCode());
        ecmAppDefRel11.setParent(ecmAppDefAttrVo.getParent());
        ecmAppDefRelMapper.insert(ecmAppDefRel11);
        if (!StateConstants.PARENT_APP_CODE_DEFAULT.equals(ecmAppDefAttrVo.getParent())) {
            //该节点的父节点的所有父节点集合
            List<EcmAppDefRel> ecmAppDefRels = ecmAppDefRelMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EcmAppDefRel::getAppCode))
                    .get(ecmAppDefAttrVo.getParent());
            AssertUtils.isNull(ecmAppDefRels, "参数错误");
            List<EcmAppDefRel> lis=new ArrayList<>();
            for (EcmAppDefRel e : ecmAppDefRels) {
                EcmAppDefRel ecmAppDefRel1 = new EcmAppDefRel();
                ecmAppDefRel1.setAppCode(appCode);
                ecmAppDefRel1.setParent(e.getParent());
                lis.add(ecmAppDefRel1);
            }
            MybatisBatch<EcmAppDefRel> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                    lis);
            MybatisBatch.Method<EcmAppDefRel> method = new MybatisBatch.Method<>(EcmAppDefRelMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    private Result<Object> getObjectResult(EcmAppDefAttrVO ecmAppDefAttrVo, Boolean b) {
        if (ecmAppDefAttrVo.getQulity() != null) {
            if (ecmAppDefAttrVo.getQulity() > IcmsConstants.MAXQULITY
                    || ecmAppDefAttrVo.getQulity() < IcmsConstants.ONE) {
                return Result.error("压缩质量不能超过100且不能小于1", ResultCode.PARAM_ERROR);
            }
        }

        //查询有没有关联资料
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(
                new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, ecmAppDefAttrVo.getParent()));
        //查询有没有关联属性
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, ecmAppDefAttrVo.getParent()));
        if (ecmAppDefAttrVo.getEcmAppAttrList() != null) {
            //查询业务属性代码有无重复
            List<String> attrCodes = ecmAppDefAttrVo.getEcmAppAttrList().stream()
                    .map(EcmAppAttr::getAttrCode).collect(Collectors.toList());
            //查询出前端传过的重复的业务属性代码集合
            List<String> collect1 = attrCodes.stream()
                    .collect(Collectors.groupingByConcurrent(i -> i)).entrySet().stream()
                    .filter(e -> e.getValue().size() > 1).map(e -> e.getKey())
                    .collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(collect1)) {
                return Result.error("业务属性代码为" + collect1 + "的业务属性代码重复！", ResultCode.PARAM_ERROR);
            }
        }
        //查询业务代码有没有重复
        if (b) {
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(null);
            List<String> collect = ecmAppDefs.stream().map(EcmAppDef::getAppCode)
                    .collect(Collectors.toList());
            if (collect.contains(ecmAppDefAttrVo.getAppCode())) {
                return Result.error("业务类型代码不能重复！", ResultCode.PARAM_ERROR);
            }
        }
        if (!ObjectUtils.isEmpty(ecmAppDocRels)) {
            return Result.error("无法作为父节点，该业务类型已关联资料", ResultCode.PARAM_ERROR);
        }
        if (!ObjectUtils.isEmpty(ecmAppAttrs)) {
            return Result.error("该业务类型已经关联了属性，无法作为父节点！", ResultCode.PARAM_ERROR);
        }
        if (IcmsConstants.ONE.equals(ecmAppDefAttrVo.getIsApiArchived())) {
            if(!StringUtils.hasText(ecmAppDefAttrVo.getArchiveAppCode())){
                return Result.error("是接口归档时，归档业务属性不可为空！", ResultCode.PARAM_ERROR);
            }
        }
        //判断资料类型是否在最子级父级目录
        //isParent:0代表为资料类型，需校验;1为父级目录无需校验
        /*if (StateConstants.ZERO.equals(ecmAppDefAttrVo.getIsParent())
                && !StateConstants.ZERO.toString().equals(ecmAppDefAttrVo.getParent())) {
            String parent = ecmAppDefAttrVo.getParent();
            List<EcmAppDef> parentEcmAppDefs = ecmAppDefMapper
                    .selectList(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getParent, parent).eq(EcmAppDef::getIsParent,
                            StateConstants.COMMON_ONE));
            if (!CollectionUtil.isEmpty(parentEcmAppDefs)) {
                return Result.error("业务类型只可在最子节点的“父级目录”下！", ResultCode.PARAM_ERROR);
            }
        }*/
        return null;
    }

    private void dealAppSort(EcmAppDefAttrVO ecmAppDefAttrVo) {
        //查询该节点的父节点下的所有节点，用于顺序号的调整
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(
                new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getParent, ecmAppDefAttrVo.getParent()));
        if (!ObjectUtils.isEmpty(ecmAppDefs)) {
            //筛选出顺序号
            List<Float> appSorts = ecmAppDefs.stream().map(EcmAppDef::getAppSort)
                    .collect(Collectors.toList());
            //            if (appSorts.contains(ecmAppDefAttrVo.getAppSort())){
            //                for (EcmAppDef ecmAppDef: appDefList) {
            //                    if (ecmAppDef.getAppSort()>= ecmAppDefAttrVo.getAppSort()){
            //                        Float docSort = ecmAppDef.getAppSort();
            //                        ecmAppDef.setAppSort(++docSort);
            //                        ecmAppDefMapper.updateById(ecmAppDef);
            //                    }
            //                }
            //            }
            //如果该顺序号为空并且是新增、自动递增
            if (ObjectUtils.isEmpty(ecmAppDefAttrVo.getAppSort())) {
                Float appSort = appSorts.stream().max(Comparator.comparing(x -> x)).orElse(null);
                if (ObjectUtils.isEmpty(appSort)) {
                    ecmAppDefAttrVo.setAppSort(1.0F);
                } else {
                    ecmAppDefAttrVo.setAppSort(++appSort);
                }
            }
        } else {
            ecmAppDefAttrVo.setAppSort(1.0F);
        }

    }

    /**
     * 编辑业务类型
     */
    @Transactional(rollbackFor = Exception.class)
    @WebsocketNoticeAnnotation(msgType = "all")
    public Result editBusiType(EcmAppDefAttrVO ecmAppDefAttrVo, String userId) {
        AssertUtils.isNull(ecmAppDefAttrVo.getAppCode(), "业务类型代码不能为空！");
        AssertUtils.isNull(ecmAppDefAttrVo.getAppName(), "业务类型名称不能为空！");
        if (ObjectUtils.isEmpty(ecmAppDefAttrVo.getParent())) {
            ecmAppDefAttrVo.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        }
        Result<Object> paramError = getObjectResult(ecmAppDefAttrVo, false);
        if (paramError != null) {
            return paramError;
        }
        //        dealAppSort(ecmAppDefAttrVo);
        EcmAppDef ecmAppDef = new EcmAppDef();
        BeanUtils.copyProperties(ecmAppDefAttrVo, ecmAppDef);
        ecmAppDef.setUpdateUser(userId);
        if (ecmAppDefAttrVo.getQulity() != null) {
            ecmAppDef.setQulity(ecmAppDefAttrVo.getQulity() / IcmsConstants.HUNDRED);
        }
//        ecmAppDef.setRoleIds(String.join(",",ecmAppDefAttrVo.getRoleIdList()));
        ecmAppDef.setRoleIds(ObjectUtils.isEmpty(ecmAppDefAttrVo.getRoleIdList()) ? null : String.join(",", ecmAppDefAttrVo.getRoleIdList()));
        ecmAppDefMapper.updateById(ecmAppDef);

        //查询修改前的业务属性
        //若为父级目录则无需此操作
        if (StateConstants.ZERO.equals(ecmAppDefAttrVo.getIsParent())) {
            //删除原来的闭包关系
            ecmAppDefRelMapper.delete(
                    new LambdaQueryWrapper<EcmAppDefRel>().eq(EcmAppDefRel::getAppCode, ecmAppDefAttrVo.getAppCode()));
            addEcmAppDefRel(ecmAppDefAttrVo, ecmAppDef, ecmAppDef.getAppCode());

            List<EcmAppAttr> ecmAppAttrList = ecmAppDefAttrVo.getEcmAppAttrList();
            List<Long> appAttrIds = ecmAppAttrMapper
                    .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode,
                            ecmAppDefAttrVo.getAppCode()))
                    .stream().map(EcmAppAttr::getAppAttrId).collect(Collectors.toList());
            List<Long> collect = ecmAppAttrList.stream().map(EcmAppAttr::getAppAttrId)
                    .collect(Collectors.toList());
            //得到已经被删除的appAttrId集合
            appAttrIds.removeAll(collect);
            if (!ObjectUtils.isEmpty(appAttrIds)) {
                ecmAppAttrMapper
                        .delete(new LambdaQueryWrapper<EcmAppAttr>().in(EcmAppAttr::getAppAttrId, appAttrIds));
            }
            int j = 0;
            int n = 0;
            ArrayList<EcmAppAttr> adds = new ArrayList<>();
            ArrayList<EcmAppAttr> updates = new ArrayList<>();

            for (EcmAppAttr ecmAppAttr : ecmAppAttrList) {
                if (ObjectUtils.isEmpty(ecmAppAttr.getAppAttrId())) {
                    ecmAppAttr.setAppAttrId(snowflakeUtil.nextId());
                    ecmAppAttr.setAppCode(ecmAppDef.getAppCode());
                    ecmAppAttr.setCreateUser(userId);
                    ecmAppAttr.setAttrSort(n++);
                    ecmAppAttr.setStatus(StateConstants.YES);
                    ecmAppAttr.setCreateTime(new Date());
                    if (ObjectUtils.isEmpty(ecmAppAttr.getIsKey())) {
                        ecmAppAttr.setIsKey(StateConstants.ZERO);
                    }
                    if (ObjectUtils.isEmpty(ecmAppAttr.getIsNull())) {
                        ecmAppAttr.setIsNull(StateConstants.ZERO);
                    }
                    if (ObjectUtils.isEmpty(ecmAppAttr.getTreeShow())) {
                        ecmAppAttr.setTreeShow(StateConstants.COMMON_ONE);
                    }
                    if (ObjectUtils.isEmpty(ecmAppAttr.getInputType())) {
                        ecmAppAttr.setInputType(StateConstants.COMMON_ONE);
                    }
                    if (ecmAppAttr.getInputType() == 3) {
                        AssertUtils.isNull(ecmAppAttr.getListValue(), "业务属性选项不能为空");
                    }
                    adds.add(ecmAppAttr);
                } else {
                    ecmAppAttr.setAttrSort(j);
                    ++j;
                    ecmAppAttr.setUpdateUser(userId);
                    ecmAppAttr.setUpdateTime(new Date());
                    updates.add(ecmAppAttr);
                }
                ++n;
            }
            if (!ObjectUtils.isEmpty(ecmAppAttrList)) {
                dealAppAttrPriKey(ecmAppAttrList);
            }
            if (!CollectionUtils.isEmpty(adds)) {
                insertEcmAppAttrs(adds);
                //ecmAppAttrMapper.insertList(adds);
            }

            if (!CollectionUtils.isEmpty(updates)) {
                ArrayList<EcmAppAttr> ecmAppAttrsList = new ArrayList<>();
                for (EcmAppAttr d : updates) {
                    ecmAppAttrsList.add(d);
//                    ecmAppAttrMapper.updateById(d);
                }
                //批量修改
                MybatisBatch<EcmAppAttr> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmAppAttrsList);
                MybatisBatch.Method<EcmAppAttr> method = new MybatisBatch.Method<>(EcmAppAttrMapper.class);
                mybatisBatch.execute(method.updateById());
            }
        }
        return Result.success("修改成功");
    }

    /**
     * 删除业务类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteBusiType(EcmAppDefDTO ecmAppDefDTO) {
        AssertUtils.isNull(ecmAppDefDTO.getAppCode(), "业务类型id不能为空！");
        String appCode = ecmAppDefDTO.getAppCode();
        //该业务类型下存不存在业务
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper
                .selectList(new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getAppCode, appCode));
        //该业务类型回收站是否存在该业务类型
        List<String> recycleAppCodes = ecmBusiInfoMapper.selectListInRecycle(appCode);

        if (!ObjectUtils.isEmpty(ecmBusiInfos)) {
            return Result.error("无法删除，该业务类型下已存在业务", ResultCode.PARAM_ERROR);
        }
        if (!ObjectUtils.isEmpty(recycleAppCodes)) {
            return Result.error("无法删除，该业务类型下回收站已存在业务", ResultCode.PARAM_ERROR);
        }
        if (CollectionUtil.isNotEmpty(ecmAppDefDTO.getChildren())) {
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(
                    new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode, ecmAppDefDTO.getChildren()));
            List<EcmAppDef> collect = ecmAppDefs.stream()
                    .filter(ecmAppDef -> StateConstants.ZERO.equals(ecmAppDef.getIsParent()))
                    .collect(Collectors.toList());
            if (collect.size() > StateConstants.ZERO) {
                return Result.error("无法删除，该目录下存在子节点", ResultCode.PARAM_ERROR);
            }
        }
        //删除业务类型表
        ecmAppDefMapper.deleteById(appCode);
        //删除业务类型表闭包表
        ecmAppDefRelMapper.delete(new LambdaQueryWrapper<EcmAppDefRel>().eq(EcmAppDefRel::getAppCode, appCode));
        //删除业务属性表
        ecmAppAttrMapper.delete(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
        //删除业务统计表
        ecmBusiStatisticsMapper
                .delete(new LambdaQueryWrapper<EcmBusiStatistics>().eq(EcmBusiStatistics::getAppCode, appCode));
        ecmUserBusiFileStatisticsMapper.delete(new LambdaQueryWrapper<EcmUserBusiFileStatistics>()
                .eq(EcmUserBusiFileStatistics::getAppCode, appCode));

        //删除业务类型关联的资料权限
        ecmAppDocrightMapper.delete(
                new LambdaQueryWrapper<EcmAppDocright>().eq(EcmAppDocright::getAppCode, appCode));
        ecmAppDocRelMapper.delete(
                new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode));
        return Result.success("删除成功");
    }

    /**
     * 查询业务类型树
     */
    public List<EcmAppDefAttrVO> searchBusiTypeTree(String appCode, AccountTokenExtendDTO token) {
        return searchBusiTypeTree(appCode, token, true, null);
    }

    /**
     * 查询业务类型树(过滤掉空父级)
     */
    public List<EcmAppDefAttrVO> searchBusiTypeTreeByHaveChild(String appCode, AccountTokenExtendDTO token) {
        List<EcmAppDefAttrVO> ecmAppDefAttrVOS = searchBusiTypeTree(appCode, token, false, null);
        return filterAppNodesRecursive(ecmAppDefAttrVOS,true);
    }
    /**
     * 查询父级目录业务类型树
     */
    public List<EcmAppDefAttrVO> searchBusiTypeParentTree(String appCode,
                                                          AccountTokenExtendDTO token) {
        return searchBusiTypeParentTree(appCode, token, false, null);
    }

    /**
     * 查询业务类型树
     */
    public List<EcmAppDefAttrVO> searchBusiTypeTree(String appCode, AccountTokenExtendDTO token,
                                                    Boolean flag, String right) {
        Set<String> collect = null;
        if (flag) {
            collect = staticTreePermissService.getAppCodeHaveByToken(appCode, token, right);
        }
        List<EcmAppDef> ecmAppDefAttrVos = ecmAppDefMapper
                .selectList(new LambdaQueryWrapper<EcmAppDef>()
                        //这里必须用collect!=null,因为还有一种情况是需要校验权限，但是查到的权限是null,即无权限的情况
                        .in(CollectionUtil.isNotEmpty(collect), EcmAppDef::getAppCode, collect));
        Map<String, List<EcmAppDef>> collect1 = ecmAppDefAttrVos.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getParent));
        collect1.values().forEach(list -> list.sort(Comparator.comparing(EcmAppDef::getAppSort)));
        return busiTypeTreeNew(collect1, StateConstants.PARENT_APP_CODE_DEFAULT, "无", appCode);
    }

    public List<EcmAppDefAttrVO> searchBusiTypeParentTree(String appCode,
                                                          AccountTokenExtendDTO token, Boolean flag,
                                                          String right) {
        Set<String> collect = null;
        if (flag) {
            collect = staticTreePermissService.getAppCodeHaveByToken(appCode, token, right);
        }
        List<EcmAppDef> ecmAppDefAttrVos = ecmAppDefMapper.selectList(
                new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getIsParent, IcmsConstants.ONE)
                        //这里必须用collect!=null,因为还有一种情况是需要校验权限，但是查到的权限是null,即无权限的情况
                        .in(CollectionUtil.isNotEmpty(collect), EcmAppDef::getAppCode, collect));
        Map<String, List<EcmAppDef>> collect1 = ecmAppDefAttrVos.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getParent));
        collect1.values().forEach(list -> list.sort(Comparator.comparing(EcmAppDef::getAppSort)));
        return busiTypeTreeNew(collect1, StateConstants.PARENT_APP_CODE_DEFAULT, "无", appCode);
    }

    private List<EcmAppDefAttrVO> busiTypeTreeNew(Map<String, List<EcmAppDef>> collect1,
                                                  String parentId, String parentName,
                                                  String appCode) {
        List<EcmAppDefAttrVO> ecmAppDefAttrVOS = new ArrayList<>();
        //得到该子节点的类的信息
        collect1.forEach((k, v) -> {
            if (k.equals(parentId)) {
                for (EcmAppDef e : v) {
                    EcmAppDefAttrVO ecmAppDefAttrVo = new EcmAppDefAttrVO();
                    ecmAppDefAttrVo.setEquipmentId(e.getEquipmentId());
                    ecmAppDefAttrVo.setQueueName(e.getQueueName());
                    ecmAppDefAttrVo.setId(e.getAppCode());
                    ecmAppDefAttrVo.setAppCode(e.getAppCode());
                    ecmAppDefAttrVo.setAppName(e.getAppName());
                    ecmAppDefAttrVo.setLabel(e.getAppName());
                    ecmAppDefAttrVo.setParentName(parentName);
                    ecmAppDefAttrVo.setParent(parentId);
                    ecmAppDefAttrVo.setDisabled(
                            !ObjectUtils.isEmpty(appCode) && e.getAppCode().equals(appCode));
                    ecmAppDefAttrVo.setAppSort(e.getAppSort());
                    ecmAppDefAttrVo.setIsParent(e.getIsParent());
                    final Integer[] i = { 1 };
                    collect1.forEach((k1, v1) -> {
                        //判断该子类有没有子类
                        if (k1.equals(e.getAppCode())) {
                            ecmAppDefAttrVo.setChildren(busiTypeTreeNew(collect1, e.getAppCode(),
                                    e.getAppName(), appCode));
                            ecmAppDefAttrVo.setType(StateConstants.COMMON_ONE);
                            i[0] = 0;
                        }
                    });
                    if (i[0] == 1) {
                        ecmAppDefAttrVo.setType(StateConstants.ZERO);
                        ecmAppDefAttrVOS.remove(ecmAppDefAttrVo);
                    }
                    ecmAppDefAttrVOS.add(ecmAppDefAttrVo);
                }
            }
        });
        ecmAppDefAttrVOS.forEach(ecmAppDefAttrVO -> {
            String label = "(" + ecmAppDefAttrVO.getAppCode() + ")" + ecmAppDefAttrVO.getAppName();
            ecmAppDefAttrVO.setLabel(label);
            ecmAppDefAttrVO.setAppName(label);
        });
        return ecmAppDefAttrVOS;
    }

    /**
     * 查询单个业务类型信息
     */
    public EcmAppDefAttrVO searchBusiTypeInfo(String appCode, String parentId, String parentName) {
        EcmAppDefAttrVO ecmAppDefAttrVo = new EcmAppDefAttrVO();
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        BeanUtils.copyProperties(ecmAppDef, ecmAppDefAttrVo);
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper
                .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode)
                        .orderByAsc(EcmAppAttr::getAttrSort));
        List<EcmAppDefRel> appTypeId1 = ecmAppDefRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDefRel>().eq(EcmAppDefRel::getAppCode, appCode));
        List<String> parents = appTypeId1.stream().filter(s -> !s.getParent().equals(StateConstants.PARENT_APP_CODE_DEFAULT))
                .map(EcmAppDefRel::getParent).collect(Collectors.toList());
        ecmAppDefAttrVo.setParents(parents);
        ecmAppDefAttrVo.setEcmAppAttrList(ecmAppAttrs);
        String directParentId = ecmAppDef.getParent();
        if (!StateConstants.PARENT_APP_CODE_DEFAULT.equals(directParentId)) {
            ecmAppDefAttrVo.setParent(directParentId);
            EcmAppDef parentDef = ecmAppDefMapper.selectById(directParentId);
            ecmAppDefAttrVo.setParentName(parentDef != null ? parentDef.getAppName() : null);
        } else {
            ecmAppDefAttrVo.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
            ecmAppDefAttrVo.setParentName("无");
        }
        ecmAppDefAttrVo.setIsShareTree(ecmAppDef.getIsShareTree());
        return getUserNames(ecmAppDef, ecmAppDefAttrVo);
    }

    private EcmAppDefAttrVO getUserNames(EcmAppDef ecmAppDef, EcmAppDefAttrVO ecmAppDefAttrVo) {
        String createUser = ecmAppDef.getCreateUser();
        String updateUser = ecmAppDef.getUpdateUser();
        List<String> userIds = new ArrayList<>();
        if (!ObjectUtils.isEmpty(createUser)) {
            userIds.add(createUser);
        }
        if (!ObjectUtils.isEmpty(updateUser)) {
            userIds.add(updateUser);
        }
        Result<List<SysUserDTO>> result = userApi
                .getUserListByUsernames(userIds.toArray(new String[0]));
        List<SysUserDTO> list = new ArrayList<>();
        if (result.isSucc()) {
            list = result.getData();
        } else {
            throw new SunyardException(result.getMsg());
        }
        if (!ObjectUtils.isEmpty(list)) {
            for (SysUserDTO e : list) {
                if (!ObjectUtils.isEmpty(ecmAppDef.getCreateUser())) {
                    if (e.getLoginName().equals(ecmAppDef.getCreateUser())) {
                        ecmAppDefAttrVo.setCreateUserName(e.getName());
                    }
                }
                if (!ObjectUtils.isEmpty(ecmAppDef.getUpdateUser())) {
                    if (e.getLoginName().equals(ecmAppDef.getUpdateUser())) {
                        ecmAppDefAttrVo.setUpdateUserName(e.getName());
                    }
                }
            }
        }
        return ecmAppDefAttrVo;
    }

    /**
     * 新增业务属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addBusiAttr(EcmAppAttr ecmAppAttr, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmAppAttr.getAttrCode(), "业务属性代码不能为空");
        AssertUtils.isNull(ecmAppAttr.getAttrName(), "业务属性名称不能为空");
        AssertUtils.isNull(ecmAppAttr.getAppCode(), "业务类型id不能为空");
        if (ObjectUtils.isEmpty(ecmAppAttr.getInputType())) {
            ecmAppAttr.setInputType(StateConstants.COMMON_ONE);
        }
        if (ecmAppAttr.getInputType() == 3) {
            AssertUtils.isNull(ecmAppAttr.getListValue(), "业务属性选项不能为空");
        }
        //查询业务属性代码有无重复
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper
                .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, ecmAppAttr.getAppCode()));
        List<String> oldAttrCode = ecmAppAttrs.stream().map(EcmAppAttr::getAttrCode)
                .collect(Collectors.toList());
        if (oldAttrCode.contains(ecmAppAttr.getAttrCode())) {
            return Result.error("业务属性代码为" + ecmAppAttr.getAttrCode() + "的业务属性代码重复！",
                    ResultCode.PARAM_ERROR);
        }
        //修改EcmAppDef表
        ecmAppDefMapper.update(null,
                new UpdateWrapper<EcmAppDef>().set("update_user", token.getUsername())
                        .set("update_time", new Date()).eq("app_code", ecmAppAttr.getAppCode()));
        //处理业务属性顺序号
        List<Integer> collect = ecmAppAttrs.stream().map(EcmAppAttr::getAttrSort)
                .collect(Collectors.toList());
        //处理业务属性主键
        dealAppAttrPriKey(ecmAppAttr);
        ecmAppAttr.setAttrSort(dealSort(collect));
        ecmAppAttr.setCreateUser(token.getUsername());
        ecmAppAttrMapper.insert(ecmAppAttr);
        return Result.success(ecmAppAttr);
    }

    private void dealAppAttrPriKey(EcmAppAttr ecmAppAttr) {
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, ecmAppAttr.getAppCode()).eq(EcmAppAttr::getIsKey, StateConstants.COMMON_ONE));
        if (!CollectionUtils.isEmpty(ecmAppAttrs)) {
            List<EcmAppAttr> collect = ecmAppAttrs.stream()
                    .filter(p -> p.getAppAttrId().equals(ecmAppAttr.getAppAttrId()))
                    .collect(Collectors.toList());
            if (ecmAppAttrs.size() >= StateConstants.COMMON_ONE
                    && StateConstants.COMMON_ONE.equals(ecmAppAttr.getIsKey())
                    && CollectionUtils.isEmpty(collect)) {
                AssertUtils.notNull(ecmAppAttrs, "该业务类型已定义主键");
            }
        }
        List<EcmAppAttr> ecmAppAttrs1 = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, ecmAppAttr.getAppCode()).eq(EcmAppAttr::getIsArchived, StateConstants.COMMON_ONE));
        if (!CollectionUtils.isEmpty(ecmAppAttrs1)) {
            List<EcmAppAttr> collect = ecmAppAttrs1.stream()
                    .filter(p -> p.getAppAttrId().equals(ecmAppAttr.getAppAttrId()))
                    .collect(Collectors.toList());
            if (ecmAppAttrs1.size() >= StateConstants.COMMON_ONE
                    && StateConstants.COMMON_ONE.equals(ecmAppAttr.getIsArchived())
                    && CollectionUtils.isEmpty(collect)) {
                AssertUtils.notNull(ecmAppAttrs1, "该业务类型已定义归档标识");
            }
        }
    }

    private Integer dealSort(List<Integer> collect) {
        if (ObjectUtils.isEmpty(collect)) {
            return StateConstants.COMMON_ONE;
        } else {
            Integer sortMax = collect.stream().max(Comparator.comparing(x -> x)).orElse(null);
            return ++sortMax;
        }
    }

    /**
     * 编辑业务属性
     */
    @Transactional(rollbackFor = Exception.class)
    @WebsocketNoticeAnnotation(msgType = "all")
    public Result editBusiAttr(EcmAppAttr ecmAppAttr, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmAppAttr.getAttrCode(), "业务属性代码不能为空");
        AssertUtils.isNull(ecmAppAttr.getAttrName(), "业务属性名称不能为空");
        AssertUtils.isNull(ecmAppAttr.getAppCode(), "业务类型id不能为空");
        if (ecmAppAttr.getInputType() == 3) {
            AssertUtils.isNull(ecmAppAttr.getListValue(), "业务属性选项不能为空");
        }
        ecmAppAttr.setUpdateTime(new Date());
        ecmAppAttr.setUpdateUser(token.getUsername());
        dealAppAttrPriKey(ecmAppAttr);
        ecmAppAttrMapper.updateById(ecmAppAttr);
        //修改EcmAppDef表
        ecmAppDefMapper.update(null,
                new UpdateWrapper<EcmAppDef>().set("update_user", token.getUsername())
                        .set("update_time", new Date()).eq("app_code", ecmAppAttr.getAppCode()));
        return Result.success(true);
    }

    /**
     * 删除业务属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteBusiAttr(DeleteBusiAttrVO deleteBusiAttrVO, AccountTokenExtendDTO token) {
        AssertUtils.isNull(deleteBusiAttrVO.getAppCode(), "业务类型id不能为空");
        AssertUtils.isNull(deleteBusiAttrVO.getAppAttrId(), "业务属性id不能为空");
        ecmAppAttrMapper.deleteBatchIds(deleteBusiAttrVO.getAppAttrId());
        ecmAppDefMapper.update(null,
                new UpdateWrapper<EcmAppDef>().set("update_time", new Date())
                        .set("update_user", token.getUsername())
                        .eq("app_code", deleteBusiAttrVO.getAppCode()));
        return Result.success(true);
    }

    /**
     * 查看单个业务属性详情
     */
    public EcmAppAttr searchOneBusiAttr(Long appAttrId) {
        AssertUtils.isNull(appAttrId, "业务属性id不能为空");
        EcmAppAttr ecmAppAttr = ecmAppAttrMapper.selectById(appAttrId);
        return ecmAppAttr;
    }

    /**
     * 查询业务类型下的属性列表
     */
    public PageInfo<EcmAppAttrDTO> searchBusiTypeAttrList(PageForm pageForm, String appCode) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode).orderByAsc(EcmAppAttr::getAttrSort));
        PageInfo pageInfo = new PageInfo<>(ecmAppAttrs);
        //获取创建人和修改人名称
        //用户id列表
        List<String> userIds = new ArrayList<>();
        ecmAppAttrs.forEach(p -> {
            if (!org.springframework.util.ObjectUtils.isEmpty(p.getCreateUser())) {
                userIds.add(p.getCreateUser());
            }
            if (!org.springframework.util.ObjectUtils.isEmpty(p.getUpdateUser())) {
                userIds.add(p.getUpdateUser());
            }
        });
        List<EcmAppAttrDTO> ecmAppAttrDTOS = new ArrayList<>();
        Map<String, List<SysUserDTO>> userListByUserIds = modelPermissionsService
                .getUserListByUserIds(userIds);
        if (userListByUserIds != null) {
            for (EcmAppAttr e : ecmAppAttrs) {
                EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
                BeanUtils.copyProperties(e, ecmAppAttrDTO);
                //添加创建人名称
                if (!ObjectUtils.isEmpty(e.getCreateUser())
                        && !CollectionUtils.isEmpty(userListByUserIds)) {
                    if (!CollectionUtils.isEmpty(userListByUserIds.get(e.getCreateUser()))) {
                        ecmAppAttrDTO.setCreateUserName(
                                userListByUserIds.get(e.getCreateUser()).get(0).getName());
                    }
                }
                //最近修改人名称
                if (!ObjectUtils.isEmpty(e.getUpdateUser())) {
                    if (!CollectionUtils.isEmpty(userListByUserIds.get(e.getUpdateUser()))) {
                        ecmAppAttrDTO.setUpdateUserName(
                                userListByUserIds.get(e.getUpdateUser()).get(0).getName());
                    }
                }
                ecmAppAttrDTOS.add(ecmAppAttrDTO);
            }
        } else {
            for (EcmAppAttr e : ecmAppAttrs) {
                EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
                BeanUtils.copyProperties(e, ecmAppAttrDTO);
                ecmAppAttrDTOS.add(ecmAppAttrDTO);
            }
        }
        List<EcmAppAttrDTO> resultList = new ArrayList<>();
        //按照属性顺序排序(主属性放到第一个位置)
        if (CollectionUtil.isNotEmpty(ecmAppAttrDTOS)) {
            Optional<EcmAppAttrDTO> first = ecmAppAttrDTOS.stream()
                    .filter(f -> IcmsConstants.ONE.equals(f.getIsKey())).findFirst();
            if (first.isPresent()) {
                EcmAppAttrDTO ecmAppAttrDTO = first.get();
                resultList.add(ecmAppAttrDTO);
            }
            List<EcmAppAttrDTO> list = ecmAppAttrDTOS.stream()
                    .filter(f -> !IcmsConstants.ONE.equals(f.getIsKey()))
                    .collect(Collectors.toList());
            ecmAppAttrDTOS = list.stream().sorted(Comparator.comparing(EcmAppAttrDTO::getAttrSort))
                    .collect(Collectors.toList());
            resultList.addAll(ecmAppAttrDTOS);
        }
        pageInfo.setList(resultList);
        return pageInfo;
    }

    /**
     * 复用-业务属性树展示(与业务类型中所选内容联动)
     */
    public List<EcmAppAttrDTO> multiplexBusiAttrAllList(String appCode) {
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper
                .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));

        List<EcmAppAttr> resultList = new ArrayList<>();
        //按照属性顺序排序(主属性放到第一个位置)
        if (CollectionUtil.isNotEmpty(ecmAppAttrs)) {
            Optional<EcmAppAttr> first = ecmAppAttrs.stream()
                    .filter(f -> IcmsConstants.ONE.equals(f.getIsKey())).findFirst();
            if (first.isPresent()) {
                EcmAppAttr ecmAppAttr = first.get();
                resultList.add(ecmAppAttr);
            }
            List<EcmAppAttr> list = ecmAppAttrs.stream()
                    .filter(f -> !IcmsConstants.ONE.equals(f.getIsKey()))
                    .collect(Collectors.toList());
            ecmAppAttrs = list.stream().sorted(Comparator.comparing(EcmAppAttr::getAttrSort))
                    .collect(Collectors.toList());
            resultList.addAll(ecmAppAttrs);
        }
        List<EcmAppAttrDTO> ret = new ArrayList<>();
        for (EcmAppAttr ecmAppAttr : resultList) {
            EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
            BeanUtils.copyProperties(ecmAppAttr, ecmAppAttrDTO);
            ecmAppAttrDTO.setLabel(ecmAppAttrDTO.getAttrName());
            ret.add(ecmAppAttrDTO);
        }
        return ret;
    }

    /**
     * 查询没有联资料树和已关联资料树
     */
    public Map searchNoRelevanceInformationAll(String appCode) {
        AssertUtils.isNull(appCode, "业务类型id不能为空");
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode));
        Map<String, List<EcmAppDocRel>> collect = ecmAppDocRels.stream()
                .collect(Collectors.groupingBy(EcmAppDocRel::getDocCode));
        List<String> docCodes = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        List<EcmDocDefDTO> ecmDocDefDTOS = PageCopyListUtils.copyListProperties(ecmDocDefs,
                EcmDocDefDTO.class);
        Map<String, List<EcmDocDefDTO>> parentListMap1 = ecmDocDefDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocDefDTO::getParent));
        List<EcmDocTreeDTO> list1 = new ArrayList();
        List<EcmDocTreeDTO> list2 = new ArrayList();
        for (EcmDocDefDTO extend : ecmDocDefDTOS) {
            extend.setLabel(extend.getDocName());
            extend.setId(extend.getDocCode());
        }
        //已关联
        staticTreePermissService.searchOldRelevanceInformationTreeNew(
                StateConstants.ZERO.toString(), "无", parentListMap1, docCodes, list1, collect);
        //未关联
        searchNoRelevanceInformationTreeNew(StateConstants.ZERO.toString(), "无", parentListMap1,
                docCodes, list2);
        HashMap map = new HashMap();
        map.put("noRelevance", list2);
        map.put("yesRelevance", list1);
        map.put("allRelevance", ecmDocDefDTOS);
        return map;
    }

    private List<EcmDocTreeDTO> searchNoRelevanceInformationTreeNew(String parentId,
                                                                    String parentName,
                                                                    Map<String, List<EcmDocDefDTO>> groupingByParent,
                                                                    List<String> docCodes,
                                                                    List<EcmDocTreeDTO> list2) {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //得到该子节点的类的信息
        groupingByParent.forEach((k, v) -> {
            final Integer[] j = { 0 };
            if (parentId.equals(k)) {
                for (EcmDocDefDTO e : v) {
                    //得到该子节点的类的信息
                    EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setDocName(e.getDocName());
                    ecmDocTreeDTO.setId(e.getDocCode());
                    ecmDocTreeDTO.setLabel(e.getDocName());
                    ecmDocTreeDTO.setDocSort(e.getDocSort());
                    ecmDocTreeDTO.setParent(parentId);
                    ecmDocTreeDTO.setParentName(parentName);
                    //查询该子节点的子节点列表
                    final Integer[] i = { 0 };
                    groupingByParent.forEach((k1, v1) -> {
                        if (k1.equals(e.getDocCode())) {
                            List<EcmDocTreeDTO> ecmDocTreeExtends1 = searchNoRelevanceInformationTreeNew(
                                    e.getDocCode(), e.getDocName(), groupingByParent, docCodes,
                                    list2);
                            ecmDocTreeDTO.setChildren(ecmDocTreeExtends1);
                            ecmDocTreeDTO.setType(RoleConstants.ZERO);
                            i[0] = 1;
                        }
                    });
                    if (i[0] == 0) {
                        if (!docCodes.contains(e.getDocCode())) {
                            j[0] = 1;
                            ecmDocTreeDTO.setType(RoleConstants.ONE);
                        }
                    }
                    if (!ObjectUtils.isEmpty(ecmDocTreeDTO.getChildren()) || j[0] == 1) {
                        ecmDocTreeDTOS.add(ecmDocTreeDTO);
                        list2.add(ecmDocTreeDTO);
                        j[0] = 0;
                    }
                }
            }
        });
        return ecmDocTreeDTOS;
    }

    /**
     * 查询已关联资料
     */
    public List<EcmDocTreeDTO> searchOldRelevanceInformation(String appCode) {
        AssertUtils.isNull(appCode, "业务类型id不能为空");
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(
                new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode).orderByAsc(EcmAppDocRel::getDocSort));
        Map<String, List<EcmAppDocRel>> collect = ecmAppDocRels.stream()
                .collect(Collectors.groupingBy(EcmAppDocRel::getDocCode));
        List<String> docCodes = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        List<EcmDocDefDTO> ecmDocDefDTOS = PageCopyListUtils.copyListProperties(ecmDocDefs,
                EcmDocDefDTO.class);
        Map<String, List<EcmDocDefDTO>> parentListMap1 = ecmDocDefDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocDefDTO::getParent));
        return staticTreePermissService.searchOldRelevanceInformationTreeNew(
                StateConstants.ZERO.toString(), "无", parentListMap1, docCodes, new ArrayList<>(),
                collect);
    }

    /**
     * 复用-新增业务属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result multiplexAddBusiAttr(EcmAddAttrDTO ecmAddAttrDTO, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmAddAttrDTO.getAttrIdList(), "业务属性id数组不能为空");
        AssertUtils.isNull(ecmAddAttrDTO.getTypeId(), "业务类型id不能为空");
        //查询被复用的属性信息
        List<EcmAppAttr> ecmAppAttrs = PageCopyListUtils
                .copyListProperties(ecmAddAttrDTO.getAttrIdList(), EcmAppAttr.class);
        Map<String, List<EcmAppAttr>> collect1 = ecmAppAttrs.stream()
                .collect(Collectors.groupingBy(EcmAppAttr::getAttrCode));
        //被复用的属性的属性代码集合
        List<String> collect = ecmAppAttrs.stream().map(EcmAppAttr::getAttrCode)
                .collect(Collectors.toList());
        //查询已有的属性信息
        List<EcmAppAttr> appAttrs = ecmAppAttrMapper.selectList(
                new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, ecmAddAttrDTO.getTypeId()));
        //已有的属性的属性代码集合
        List<String> attrCodes = appAttrs.stream().map(EcmAppAttr::getAttrCode)
                .collect(Collectors.toList());
        //
        ArrayList<String> intersection = (ArrayList) org.apache.commons.collections4.CollectionUtils
                .intersection(attrCodes, collect);
        if (!CollectionUtils.isEmpty(intersection)) {
            List<String> name = new ArrayList<>();
            for (String s : intersection) {
                List<EcmAppAttr> ecmAppAttrs1 = collect1.get(s);
                if (!CollectionUtils.isEmpty(ecmAppAttrs1)) {
                    name.add(ecmAppAttrs1.get(StateConstants.ZERO).getAttrName());
                }
            }
            return Result.error("复用的业务属性" + name + "重复", ResultCode.PARAM_ERROR);
        }
        //修改EcmAppDef表
        ecmAppDefMapper.update(null,
                new UpdateWrapper<EcmAppDef>().set("update_user", token.getUsername())
                        .set("update_time", new Date()).eq("app_code", ecmAddAttrDTO.getTypeId()));
        //判断属性主键
        //查看被复用的业务属性有没有主键
        List<EcmAppAttr> collect2 = ecmAppAttrs.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey()))
                .collect(Collectors.toList());
        //查看要复用的业务属性有没有主键
        List<EcmAppAttr> collect3 = appAttrs.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey()))
                .collect(Collectors.toList());
        if (collect2.size() > StateConstants.COMMON_ONE) {
            AssertUtils.notNull(collect2, "被复用的业务属性不止一个主键，无法复用");
        }
        if (!CollectionUtils.isEmpty(collect2) && !CollectionUtils.isEmpty(collect3)) {
            AssertUtils.notNull(collect3, "该业务类型已定义主键，无法复用主键属性");
        }
        //校验归档标识
        //查看被复用的业务属性有没有归档标识
        List<EcmAppAttr> collect4 = ecmAppAttrs.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsArchived()))
                .collect(Collectors.toList());
        //查看要复用的业务属性有没有归档标识
        List<EcmAppAttr> collect5 = appAttrs.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsArchived()))
                .collect(Collectors.toList());
        if (collect4.size() > StateConstants.COMMON_ONE) {
            AssertUtils.notNull(collect4, "被复用的业务属性不止一个归档标识，无法复用");
        }
        if (!CollectionUtils.isEmpty(collect4) && !CollectionUtils.isEmpty(collect5)) {
            AssertUtils.notNull(collect5, "该业务类型已定义归档标识，无法复用归档标识属性");
        }
        for (EcmAppAttr ecmAppAttr : ecmAppAttrs) {
            ecmAppAttr.setAppCode(ecmAddAttrDTO.getTypeId());
            ecmAppAttr.setAppAttrId(null);
            //todo 改批量插入
            ecmAppAttrMapper.insert(ecmAppAttr);
        }
        return Result.success(true);
    }

    /**
     * 获取业务配置参数
     */
    public Result<EcmAddAppParamsDTO> getAppParams() {
        EcmAddAppParamsDTO ecmAddAppParamsDTO = new EcmAddAppParamsDTO();
        //获取全局配置参数
        Result<SysParamDTO> result = paramApi
                .searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        String value = result.getData().getValue();
        SysStrategyDTO sysStrategyDTO = JSONObject.parseObject(value, SysStrategyDTO.class);
        if (sysStrategyDTO != null) {
            BeanUtils.copyProperties(sysStrategyDTO, ecmAddAppParamsDTO);
        }
        //获取设备列表
        Result<List<EcmStorageQueDTO>> storageDeviceList = sysStorageService.getStorageDeviceList();
        if (storageDeviceList.isSucc()) {
            List<EcmStorageQueDTO> equipmentList = storageDeviceList.getData();
            ecmAddAppParamsDTO.setEquipmentList(equipmentList);
        }
        //获取队列列表
        List<EcmStorageQueDTO> mqSettingList = new ArrayList<>();
        try {
            mqSettingList = sysStorageService.getMQSettingList();
            ecmAddAppParamsDTO.setQueueList(mqSettingList);
        } catch (Exception e) {
            log.error("队列列表获取有误");
        }
        return Result.success(ecmAddAppParamsDTO);
    }

    /**
     * 获取全局压缩配置参数
     */
    public Result<SysStrategyDTO> getAppZipParams() {
        //获取全局配置参数
        Result<SysParamDTO> result = paramApi
                .searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        String value = result.getData().getValue();
        SysStrategyDTO sysStrategyDTO = JSONObject.parseObject(value, SysStrategyDTO.class);
        return Result.success(sysStrategyDTO);
    }

    /**
     * 业务资料调整后的逻辑
     * 查询已关联资料
     *
     * @param appCode  业务类型
     * @param rightVer 业务对应版本
     * @return
     */
    public List<EcmDocTreeDTO> searchOldRelevanceInformation1(String appCode, Integer rightVer) {
        AssertUtils.isNull(appCode, "业务类型id不能为空");
        List<String> docCodeList = new ArrayList<>();
        List<EcmDocDefRelVer> ecmDocDefs = new ArrayList<>();
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(
                new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode).orderByAsc(EcmAppDocRel::getDocSort));
        if (org.springframework.util.CollectionUtils.isEmpty(ecmAppDocRels)) {
            return Collections.emptyList();
        }
        docCodeList = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());

        ecmDocDefs = ecmDocDefRelVerMapper.selectList(new LambdaQueryWrapper<EcmDocDefRelVer>()
                .in(EcmDocDefRelVer::getDocCode, docCodeList).eq(EcmDocDefRelVer::getAppCode, appCode).eq(EcmDocDefRelVer::getRightVer, rightVer));
        List<EcmDocDefDTO> ecmDocDefDTOS = PageCopyListUtils.copyListProperties(ecmDocDefs,
                EcmDocDefDTO.class);
        List<EcmDocDef> ecmDocDefList = ecmDocDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocDef>().in(EcmDocDef::getDocCode, docCodeList));
        //赋值isParent
        Map<String, List<EcmDocDef>> collect1 = ecmDocDefList.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        ecmDocDefDTOS.forEach(s -> {
            List<EcmDocDef> ecmDocDefs1 = collect1.get(s.getDocCode());
            s.setIsParent(ecmDocDefs1.get(0).getIsParent());
            //增加自动分类相关属性
            s.setIsAutoClassified(ecmDocDefs1.get(0).getIsAutoClassified());
            s.setAutoClassificationId(ecmDocDefs1.get(0).getAutoClassificationId());
        });
        Map<String, List<EcmDocDefDTO>> collect = ecmDocDefDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocDefDTO::getDocCode));
        List<String> docCodes = ecmDocDefDTOS.stream()
                .sorted(Comparator.comparing(EcmDocDefDTO::getDocSort))
                .map(EcmDocDefDTO::getDocCode).collect(Collectors.toList());
        //        ecmDocDefDTOS = ecmDocDefDTOS.stream().sorted(Comparator.comparing(EcmDocDefDTO::getDocSort)).collect(Collectors.toList());
        Map<String, List<EcmDocDefDTO>> parentListMap1 = ecmDocDefDTOS.stream().filter(s->s.getParent()!=null)
                .collect(Collectors.groupingBy(EcmDocDefDTO::getParent));
        return searchOldRelevanceInformationTreeNew1(StateConstants.ZERO.toString(), "无",
                parentListMap1, docCodes, new ArrayList<>(), collect);
    }

    private List<EcmDocTreeDTO> searchOldRelevanceInformationTreeNew1(String parentId,
                                                                      String parentName,
                                                                      Map<String, List<EcmDocDefDTO>> groupingByParent,
                                                                      List<String> docCodes,
                                                                      List<EcmDocTreeDTO> list1,
                                                                      Map<String, List<EcmDocDefDTO>> collect) {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //得到该子节点的类的信息
        groupingByParent.forEach((k, v) -> {
            final Integer[] j = { 0 };
            if (parentId.equals(k)) {
                for (EcmDocDefDTO e : v) {
                    //得到该子节点的类的信息
                    EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setId(e.getDocCode());
                    ecmDocTreeDTO.setLabel(e.getDocName());
                    ecmDocTreeDTO.setDocName(e.getDocName());
                    ecmDocTreeDTO.setDocSort(e.getDocSort());
                    ecmDocTreeDTO.setIsAutoClassified(e.getIsAutoClassified());
                    ecmDocTreeDTO.setAutoClassificationId(e.getAutoClassificationId());
                    List<EcmDocDefDTO> ecmDocDefs = collect.get(e.getDocCode());
                    if (!ObjectUtils.isEmpty(ecmDocDefs)) {
                        ecmDocTreeDTO.setDocSort(
                                Float.valueOf(ecmDocDefs.get(StateConstants.ZERO).getDocSort()));
                    }
                    ecmDocTreeDTO.setParent(parentId);
                    ecmDocTreeDTO.setParentName(parentName);
                    ecmDocTreeDTO.setIsParent(e.getIsParent());
                    //查询该子节点的子节点列表
                    final Integer[] i = { 0 };
                    groupingByParent.forEach((k1, v1) -> {
                        if (k1.equals(e.getDocCode())) {
                            List<EcmDocTreeDTO> ecmDocTreeExtends1 = searchOldRelevanceInformationTreeNew1(
                                    e.getDocCode(), e.getDocName(), groupingByParent, docCodes,
                                    list1, collect);
                            ecmDocTreeDTO.setChildren(ecmDocTreeExtends1);
                            ecmDocTreeDTO.setType(RoleConstants.ZERO);
                            i[0] = 1;
                        }
                    });
                    if (i[0] == 0) {
                        if (docCodes.contains(e.getDocCode())) {
                            j[0] = 1;
                            ecmDocTreeDTO.setType(RoleConstants.ONE);
                        }
                    }
                    if (!ObjectUtils.isEmpty(ecmDocTreeDTO.getChildren()) || j[0] == 1) {
                        ecmDocTreeDTOS.add(ecmDocTreeDTO);
                        list1.add(ecmDocTreeDTO);
                        j[0] = 0;
                    }
                }
            }
        });
        return ecmDocTreeDTOS;
    }

    /**
     * 获取Exif信息
     */
    public Result<HashMap> getFileExifFromES(Long fileId) {
        //从es中读取fileExif
        List<EsEcmFile> esEcmFiles = esEcmFileMapper
                .selectList(new LambdaEsQueryWrapper<EsEcmFile>().indexName(fileIndex).match(
                        !org.springframework.util.ObjectUtils.isEmpty(fileId), EsEcmFile::getFileId,
                        fileId));
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(esEcmFiles)) {
            EsEcmFile esEcmFile = esEcmFiles.get(0);
            String exif = esEcmFile.getExif();
            // String 转map
            HashMap hashMap = JSON.parseObject(exif, HashMap.class);

            return Result.success(hashMap);
        } else {
            return Result.success(null);
        }
    }


    private   List<EcmAppDefAttrVO> filterAppNodesRecursive(List<EcmAppDefAttrVO> originalList, boolean isRootLevel) {
        if (originalList == null || originalList.isEmpty()) {
            return new ArrayList<>();
        }

        List<EcmAppDefAttrVO> filteredList = new ArrayList<>();
        for (EcmAppDefAttrVO node : originalList) {
            if (IcmsConstants.ONE.equals(node.getIsParent())) {
                // 递归过滤当前节点的子节点
                List<EcmAppDefAttrVO> filteredChildren = filterAppNodesRecursive(node.getChildren(), false);

                boolean keepNode = false;
                // 根节点规则：isParent=1 且 子节点非空
                if (isRootLevel) {
                    keepNode = !CollectionUtils.isEmpty(filteredChildren);
                } else {
                    // 非根节点规则：包含至少一个 isParent=0 的子节点
                    keepNode = filteredChildren.stream()
                            .anyMatch(child -> IcmsConstants.ZERO.equals(child.getIsParent()));
                }

                if (keepNode) {
                    node.setChildren(filteredChildren);
                    filteredList.add(node);
                }
            } else if (IcmsConstants.ZERO.equals(node.getIsParent())) {
                // 所有 isParent=0 的节点（叶子节点）直接保留
                filteredList.add(node);
            }
        }
        return filteredList;
    }

    /**
     * 获取文件智能化处理状态
     */
    public Result<List<Map<String, Object>>> getProcessingStatus(Long fileId) {
        EcmAsyncTask ecmAsyncTask = ecmAsyncTaskMapper.selectOne(
                new LambdaQueryWrapper<EcmAsyncTask>().eq(EcmAsyncTask::getFileId, fileId));

        String processingConfig = (ecmAsyncTask != null) ? ecmAsyncTask.getTaskType()
                : IcmsConstants.ASYNC_TASK_STATUS_INIT;
        if (processingConfig.length() < IcmsConstants.ASYNC_TASK_STATUS_INIT.length()) {
            processingConfig = IcmsConstants.ASYNC_TASK_STATUS_INIT;
        }

        List<Map<String, Object>> statusList = new ArrayList<>();

        // 直接添加OCR文档识别状态
        Map<String, Object> ocrMap = new HashMap<>();
        ocrMap.put("type", IcmsConstants.TYPE_ONE);
        ocrMap.put("status", processingConfig.charAt(0));
        statusList.add(ocrMap);

        // 直接添加转正检测状态
        Map<String, Object> map2 = new HashMap<>();
        map2.put("type", IcmsConstants.TYPE_TWO);
        map2.put("status", processingConfig.charAt(1));
        statusList.add(map2);

        // 直接添加质量检测状态
        Map<String, Object> map3 = new HashMap<>();
        map3.put("type", IcmsConstants.TYPE_THREE);
        //模糊+反光+缺角
        map3.put("status", ""+processingConfig.charAt(2)+processingConfig.charAt(7)+processingConfig.charAt(8));
        statusList.add(map3);

        // 直接添加查重检测状态
        Map<String, Object> map4 = new HashMap<>();
        map4.put("type", IcmsConstants.TYPE_FOUR);
        map4.put("status", processingConfig.charAt(3));
        statusList.add(map4);

        // 直接添加翻拍检测状态
        Map<String, Object> map5 = new HashMap<>();
        map5.put("type", IcmsConstants.TYPE_SIX);
        map5.put("status", processingConfig.charAt(5));
        statusList.add(map5);

        //添加文本查重检测状态
        // 直接添加翻拍检测状态
        Map<String, Object> map6 = new HashMap<>();
        map6.put("type", IcmsConstants.TYPE_TEN);
        map6.put("status", processingConfig.charAt(9));
        statusList.add(map6);

        return Result.success(statusList);
    }

    public List<Map> getRoleList(SysRoleDTO sysRoleDTO, String appCode) {
        List<Map> ret = new ArrayList<>();

        // 1. 调用接口获取角色列表
        Result result = roleApi.searchListInUsePage(sysRoleDTO);
        if (result.isSucc() && result.getData() != null) {
            Map data = (Map) result.getData();
            List<Map> list = (List<Map>) data.get("list");

            if (!CollectionUtil.isEmpty(list)) {
                // 2. 初始化角色ID列表
                List<String> roleIdList = new ArrayList<>();

                // 3. 兼容ecmAppDef不存在
                EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
                if (ecmAppDef != null) {
                    String roleIds = ecmAppDef.getRoleIds();
                    // 再判断roleIds是否非空，避免split空字符串产生[""]的情况
                    if (StrUtil.isNotBlank(roleIds)) {
                        // 拆分后过滤空字符串
                        roleIdList = StrUtil.split(roleIds, ",");
                    }
                }

                // 4. 遍历角色列表，添加checked字段
                for (Map dto : list) {
                    // 防护：先判断roleId是否存在，避免空指针
                    Object roleIdObj = dto.get("roleId");
                    String roleId = roleIdObj != null ? roleIdObj.toString() : "";
                    // 空列表时contains返回false，checked自然为false，符合业务逻辑
                    dto.put("checked", roleIdList.contains(roleId));
                    ret.add(dto);
                }
            }
        }

        return ret;
    }

    /**
     * 获取AI桥接流程类型和业务类型树
     *
     * @param token 用户token
     * @param delegateType 流程类型（可选）
     * @param typeBig 业务类型（可选）
     * @return AI桥接类型树结果Map
     */
    public Map<String, EcmAIBridgeResultVO> getAIBridgeTypeTree(AccountTokenExtendDTO token, String delegateType, String typeBig) {
        Map<String, EcmAIBridgeResultVO> resultMap = new HashMap<>();

        LambdaQueryWrapper<EcmAIBridgeSync> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(delegateType) || StrUtil.isNotBlank(typeBig)) {
            if (StrUtil.isNotBlank(delegateType)) {
                wrapper.eq(EcmAIBridgeSync::getDelegateType, delegateType);
            }
            if (StrUtil.isNotBlank(typeBig)) {
                wrapper.eq(EcmAIBridgeSync::getTypeBig, typeBig);
            }
        } else {
            String userCode = getUserCode(token);
            log.info("当前的userCode : {}", userCode);
            wrapper.eq(EcmAIBridgeSync::getUserShowId, userCode);
        }

        List<EcmAIBridgeSync> list = ecmAIBridgeSyncMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(list)) {
            return resultMap;
        }

        Map<String, String> delegateTypeMap = new LinkedHashMap<>();
        Map<String, String> typeBigMap = new LinkedHashMap<>();

        if (StrUtil.isNotBlank(delegateType) || StrUtil.isNotBlank(typeBig)) {
            if (StrUtil.isNotBlank(delegateType) && StrUtil.isNotBlank(typeBig)) {
                delegateTypeMap.put(list.get(0).getDelegateType(), list.get(0).getDelegateTypeName());
                typeBigMap.put(list.get(0).getTypeBig(), list.get(0).getTypeBigName());
            } else if (StrUtil.isNotBlank(delegateType)) {
                delegateTypeMap.put(list.get(0).getDelegateType(), list.get(0).getDelegateTypeName());
                String userCode = getUserCode(token);
                LambdaQueryWrapper<EcmAIBridgeSync> typeBigWrapper = new LambdaQueryWrapper<>();
                typeBigWrapper.eq(EcmAIBridgeSync::getUserShowId, userCode);
                List<EcmAIBridgeSync> typeBigList = ecmAIBridgeSyncMapper.selectList(typeBigWrapper);
                if (!CollectionUtils.isEmpty(typeBigList)) {
                    for (EcmAIBridgeSync t : typeBigList) {
                        if (StrUtil.isNotBlank(t.getTypeBig())) {
                            typeBigMap.put(t.getTypeBig(), t.getTypeBigName());
                        }
                    }
                }
            } else {
                typeBigMap.put(list.get(0).getTypeBig(), list.get(0).getTypeBigName());
                String userCode = getUserCode(token);
                LambdaQueryWrapper<EcmAIBridgeSync> delegateTypeWrapper = new LambdaQueryWrapper<>();
                delegateTypeWrapper.eq(EcmAIBridgeSync::getUserShowId, userCode);
                List<EcmAIBridgeSync> delegateTypeList = ecmAIBridgeSyncMapper.selectList(delegateTypeWrapper);
                if (!CollectionUtils.isEmpty(delegateTypeList)) {
                    for (EcmAIBridgeSync t : delegateTypeList) {
                        if (StrUtil.isNotBlank(t.getDelegateType())) {
                            delegateTypeMap.put(t.getDelegateType(), t.getDelegateTypeName());
                        }
                    }
                }
            }
        } else {
            delegateTypeMap = list.stream()
                .filter(t -> StrUtil.isNotBlank(t.getDelegateType()))
                .collect(Collectors.toMap(
                    EcmAIBridgeSync::getDelegateType,
                    EcmAIBridgeSync::getDelegateTypeName,
                    (v1, v2) -> v1
                ));

            typeBigMap = list.stream()
                .filter(t -> StrUtil.isNotBlank(t.getTypeBig()))
                .collect(Collectors.toMap(
                    EcmAIBridgeSync::getTypeBig,
                    EcmAIBridgeSync::getTypeBigName,
                    (v1, v2) -> v1
                ));
        }

        List<EcmAIBridgeTypeVO> delegateTypeVOList = new ArrayList<>();
        for (Map.Entry<String, String> entry : delegateTypeMap.entrySet()) {
            EcmAIBridgeTypeVO vo = new EcmAIBridgeTypeVO();
            vo.setValue(entry.getKey());
            vo.setLabel(entry.getValue());
            delegateTypeVOList.add(vo);
        }

        List<EcmAIBridgeTypeVO> typeBigVOList = new ArrayList<>();
        for (Map.Entry<String, String> entry : typeBigMap.entrySet()) {
            EcmAIBridgeTypeVO vo = new EcmAIBridgeTypeVO();
            vo.setValue(entry.getKey());
            vo.setLabel(entry.getValue());
            typeBigVOList.add(vo);
        }

        EcmAIBridgeResultVO delegateResult = new EcmAIBridgeResultVO();
        delegateResult.setLabel("aibridge流程");
        delegateResult.setChildren(delegateTypeVOList);
        resultMap.put(IcmsConstants.BUSINESS_PROCESS, delegateResult);

        EcmAIBridgeResultVO typeBigResult = new EcmAIBridgeResultVO();
        typeBigResult.setLabel("业务类型");
        typeBigResult.setChildren(typeBigVOList);
        resultMap.put(IcmsConstants.BUSINESS_TYPE, typeBigResult);

        return resultMap;
    }

    /**
     * 获取用户ID
     * 对内接口：直接从token.getId()获取
     * 对外接口：通过userApi.getUserDetail获取
     *
     * @param token 用户token
     * @return 用户ID
     */
    private String getUserCode(AccountTokenExtendDTO token) {
        // 对内接口：token.getId()有值
        if (token.getId() != null) {
            return token.getUsername().toString();
        }
        // 对外接口：通过userApi.getUserDetail获取
        Result<SysUserDTO> result = userApi.getUserDetail(token.getUsername());
        if (result.isSucc() && result.getData() != null) {
            return result.getData().getLoginName().toString();
        }
        return null;
    }

    public Map<String, Boolean> getAIBridgeTypeTreeCheck(AccountTokenExtendDTO token, String delegateType, String typeBig) {
        HashMap<String, Boolean> returnMap = new HashMap<>();

        LambdaQueryWrapper<EcmAIBridgeSync> wrapper = new LambdaQueryWrapper<EcmAIBridgeSync>();

        if (StrUtil.isNotBlank(delegateType) || StrUtil.isNotBlank(typeBig)) {
            if (StrUtil.isNotBlank(delegateType)) {
                wrapper.eq(EcmAIBridgeSync::getDelegateType, delegateType);
            }
            if (StrUtil.isNotBlank(typeBig)) {
                wrapper.eq(EcmAIBridgeSync::getTypeBig, typeBig);
            }
        } else {
            String userCode = getUserCode(token);
            log.info("当前的userCode : {}", userCode);
            wrapper.eq(EcmAIBridgeSync::getUserShowId, userCode);
        }

        Long count = ecmAIBridgeSyncMapper.selectCount(wrapper);
        returnMap.put(IcmsConstants.RIGHT_CHECK, count != 0);
        return returnMap;
    }

    private boolean validateByteLength(String appName) {
        // APP_NAME 字段限制 50 字节（GBK）
        if (appName != null) {
            int byteLength = appName.getBytes(Charset.forName("GBK")).length;
            if (byteLength > 50) {
                return true;
            }
        }
        return false;
    }
}
