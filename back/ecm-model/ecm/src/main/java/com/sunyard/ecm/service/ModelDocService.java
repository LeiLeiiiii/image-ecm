package com.sunyard.ecm.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.config.properties.EcmOcrProperties;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrMulDTO;
import com.sunyard.ecm.dto.ecm.EcmMoveDtdAttrDTO;
import com.sunyard.ecm.mapper.EcmDtdAttrMapper;
import com.sunyard.ecm.mapper.EcmDtdDefMapper;
import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.vo.DeleteDocumentAttrVO;
import com.sunyard.ecm.vo.EcmDtdDefVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author： zyl
 * @create： 2023/4/20 9:21
 * @desc: 业务属性
 */
@Service
public class ModelDocService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmDtdDefMapper ecmDtdDefMapper;
    @Resource
    private EcmDtdAttrMapper ecmDtdAttrMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private EcmOcrProperties ecmOcrProperties;
    @Resource
    private ModelPermissionsService modelPermissionsService;

    /**
     * 新增文档类型管理
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addDocumentType(EcmDtdDefVO ecmDtdDefVo, String userId) {
        AssertUtils.isNull(ecmDtdDefVo.getDtdCode(), "单证类型代码不能为空");
        AssertUtils.isNull(ecmDtdDefVo.getDtdName(), "单证类型名称不能为空");
        EcmDtdDef ecmDtdDef = new EcmDtdDef();
        BeanUtils.copyProperties(ecmDtdDefVo, ecmDtdDef);
        //校验
        Result paramError = checkDtdType(ecmDtdDefVo, true);
        if (paramError != null) {
            return paramError;
        }
        Result paramError1 = checkDtdAttr(ecmDtdDefVo.getEcmDtdAttrList(),
                ecmDtdDefVo.getDtdTypeId());
        if (paramError1 != null) {
            return paramError1;
        }
        //雪花算法生成dtdTypeId
        Long dtdTypeId = snowflakeUtil.nextId();
        ecmDtdDef.setDtdTypeId(dtdTypeId);
        ecmDtdDef.setCreateUser(userId);
        ecmDtdDef.setType(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()));
        //插入影像单证类型定义表表
        ecmDtdDefMapper.insert(ecmDtdDef);
        //插入单证属性表
        List<EcmDtdAttr> ecmDtdAttrList = ecmDtdDefVo.getEcmDtdAttrList();
        if (!CollectionUtils.isEmpty(ecmDtdAttrList)) {
            Integer i = StateConstants.ZERO;
            for (EcmDtdAttr e : ecmDtdAttrList) {
                e.setDtdAttrId(snowflakeUtil.nextId());
                e.setDtdTypeId(dtdTypeId);
                e.setCreateUser(userId);
                e.setCreateTime(new Date());
                e.setAttrSort(i);
                e.setType(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()));
                if (ObjectUtils.isEmpty(e.getInputType())) {
                    e.setInputType(StateConstants.COMMON_ONE);
                }
                if (StateConstants.COMMON_THREE.equals(e.getInputType())) {
                    AssertUtils.isNull(e.getListValue(), "单证属性选项不能为空");
                }
                ++i;
            }
            insertEcmDtdAttrs(ecmDtdAttrList);
            //ecmDtdAttrMapper.insertList(ecmDtdAttrList);
        }
        return Result.success(true);
    }

    private void insertEcmDtdAttrs(List<EcmDtdAttr> ecmDtdAttrList) {
        MybatisBatch<EcmDtdAttr> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmDtdAttrList);
        MybatisBatch.Method<EcmDtdAttr> method = new MybatisBatch.Method<>(EcmDtdAttrMapper.class);
        mybatisBatch.execute(method.insert());
    }

    private Result checkDtdType(EcmDtdDefVO ecmDtdDefVo, boolean b) {
        //添加根据ocr类型查询
        List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectList(new LambdaQueryWrapper<EcmDtdDef>()
                .eq(EcmDtdDef::getType,ecmOcrProperties.getOcrDocumentType()));
        List<String> dtdNames = ecmDtdDefs.stream().map(EcmDtdDef::getDtdName)
                .collect(Collectors.toList());
        if (b) {
            List<String> dtdCodes = ecmDtdDefs.stream().map(EcmDtdDef::getDtdCode)
                    .collect(Collectors.toList());
            if (dtdCodes.contains(ecmDtdDefVo.getDtdCode())) {
                return Result.error("单证类型代码不可重复！", ResultCode.PARAM_ERROR);
            }
            if (dtdNames.contains(ecmDtdDefVo.getDtdName())) {
                return Result.error("单证类型名称不可重复！", ResultCode.PARAM_ERROR);
            }
        } else {
            Map<Long, List<EcmDtdDef>> collect = ecmDtdDefs.stream()
                    .collect(Collectors.groupingBy(EcmDtdDef::getDtdTypeId));
            if(!CollectionUtil.isEmpty(collect.keySet())){
                EcmDtdDef ecmDtdDef = collect.get(ecmDtdDefVo.getDtdTypeId()).get(StateConstants.ZERO);
                if (!ObjectUtils.isEmpty(ecmDtdDef)) {
                    if (!ecmDtdDefVo.getDtdName().equals(ecmDtdDef.getDtdName())) {
                        if (dtdNames.contains(ecmDtdDefVo.getDtdName())) {
                            return Result.error("单证类型名称不可重复！", ResultCode.PARAM_ERROR);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 编辑文档类型管理
     */
    @Transactional(rollbackFor = Exception.class)
    public Result editDocumentType(EcmDtdDefVO ecmDtdDefVo, String userId) {
        AssertUtils.isNull(ecmDtdDefVo.getDtdCode(), "单证类型代码不能为空");
        AssertUtils.isNull(ecmDtdDefVo.getDtdName(), "单证类型名称不能为空");
        EcmDtdDef ecmDtdDef = new EcmDtdDef();
        BeanUtils.copyProperties(ecmDtdDefVo, ecmDtdDef);
        //校验
        Result paramError = checkDtdType(ecmDtdDefVo, false);
        if (paramError != null) {
            return paramError;
        }
        //插入影像单证类型定义表表
        ecmDtdDef.setUpdateUser(userId);
        ecmDtdDefMapper.updateById(ecmDtdDef);
        //插入单证属性表
        List<EcmDtdAttr> ecmDtdAttrList = ecmDtdDefVo.getEcmDtdAttrList();
        //查询出编辑前的所有属性
        Map<Long, List<EcmDtdAttr>> dtdAttrIdMap = ecmDtdAttrMapper
                .selectList(new LambdaQueryWrapper<EcmDtdAttr>().eq(EcmDtdAttr::getDtdTypeId,
                        ecmDtdDefVo.getDtdTypeId()))
                .stream().collect(Collectors.groupingBy(EcmDtdAttr::getDtdAttrId));
        //全删
        ecmDtdAttrMapper.delete(
                new LambdaQueryWrapper<EcmDtdAttr>().eq(EcmDtdAttr::getDtdTypeId, ecmDtdDefVo.getDtdTypeId()));
        //重新插入
        Integer i = 0;
        for (EcmDtdAttr ecmDtdAttr : ecmDtdAttrList) {
            //是否是新增或复用生成的
            if (ObjectUtils.isEmpty(ecmDtdAttr.getDtdTypeId())) {
                ecmDtdAttr.setDtdAttrId(snowflakeUtil.nextId());
                ecmDtdAttr.setDtdTypeId(ecmDtdDef.getDtdTypeId());
                ecmDtdAttr.setAttrSort(i);
                ecmDtdAttr.setCreateUser(userId);
                ecmDtdAttr.setCreateTime(new Date());
            } else {
                AssertUtils.isNull(dtdAttrIdMap.get(ecmDtdAttr.getDtdAttrId()), "参数错误");
                //是否做了修改
                if (!EqualsBuilder.reflectionEquals(ecmDtdAttr,
                        dtdAttrIdMap.get(ecmDtdAttr.getDtdAttrId()).get(0))
                        || !ecmDtdAttr.getAttrSort().equals(i)) {
                    ecmDtdAttr.setUpdateUser(userId);
                    ecmDtdAttr.setAttrSort(i);
                    ecmDtdAttr.setUpdateTime(new Date());
                }
            }
            if (ObjectUtils.isEmpty(ecmDtdAttr.getInputType())) {
                ecmDtdAttr.setInputType(StateConstants.COMMON_ONE);
            }
            if (StateConstants.COMMON_THREE.equals(ecmDtdAttr.getInputType())) {
                AssertUtils.isNull(ecmDtdAttr.getListValue(), "单证属性选项不能为空");
            }
            i++;
        }
        if (!ObjectUtils.isEmpty(ecmDtdAttrList)) {
            insertEcmDtdAttrs(ecmDtdAttrList);
            //ecmDtdAttrMapper.insertList(ecmDtdAttrList);
        }
        return Result.success(true);
    }

    /**
     * 删除文档类型管理
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDocumentType(Long dtdTypeId, String dtdName) {
        AssertUtils.isNull(dtdTypeId, "单证属性id不能为空");
        ecmDtdDefMapper.deleteById(dtdTypeId);
        ecmDtdAttrMapper.delete(new LambdaQueryWrapper<EcmDtdAttr>().eq(EcmDtdAttr::getDtdTypeId, dtdTypeId));
        return Result.success("删除成功");
    }

    /**
     * 新增文档属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addDocumentAttr(EcmDtdAttr ecmDtdAttr, String userId) {
        AssertUtils.isNull(ecmDtdAttr.getAttrCode(), "单证属性代码不能为空");
        AssertUtils.isNull(ecmDtdAttr.getDtdTypeId(), "单证类型id不能为空");
        AssertUtils.isNull(ecmDtdAttr.getAttrName(), "单证属性名称不能为空");
        Result paramError = checkDtdAttr(Collections.singletonList(ecmDtdAttr),
                ecmDtdAttr.getDtdTypeId());
        if (paramError != null) {
            return paramError;
        }
        ecmDtdAttr.setCreateUser(userId);
        if (ObjectUtils.isEmpty(ecmDtdAttr.getInputType())) {
            ecmDtdAttr.setInputType(StateConstants.COMMON_ONE);
        }
        if (StateConstants.COMMON_THREE.equals(ecmDtdAttr.getInputType())) {
            AssertUtils.isNull(ecmDtdAttr.getListValue(), "单证属性选项不能为空");
        }
        ecmDtdAttr.setType(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()));
        ecmDtdAttrMapper.insert(ecmDtdAttr);
        //修改EcmDtdDef表
        ecmDtdDefMapper.update(null, new UpdateWrapper<EcmDtdDef>().set("update_user", userId)
                .set("update_time", new Date()).eq("dtd_type_id", ecmDtdAttr.getDtdTypeId()));
        return Result.success(true);
    }

    /**
     * 编辑文档属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result editDocumentAttr(EcmDtdAttr ecmDtdAttr, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmDtdAttr.getAttrCode(), "单证属性代码不能为空");
        AssertUtils.isNull(ecmDtdAttr.getAttrName(), "单证属性名称不能为空");
        AssertUtils.isNull(ecmDtdAttr.getDtdTypeId(), "单证类型id不能为空");
        ecmDtdAttr.setUpdateUser(token.getUsername());
        ecmDtdAttr.setUpdateTime(new Date());
        if (ObjectUtils.isEmpty(ecmDtdAttr.getInputType())) {
            ecmDtdAttr.setInputType(StateConstants.COMMON_ONE);
        }
        if (StateConstants.COMMON_THREE.equals(ecmDtdAttr.getInputType())) {
            AssertUtils.isNull(ecmDtdAttr.getListValue(), "单证属性选项不能为空");
        }
        ecmDtdAttrMapper.updateById(ecmDtdAttr);
        //修改EcmDtdDef表
        ecmDtdDefMapper.update(null,
                new UpdateWrapper<EcmDtdDef>().set("update_user", token.getUsername())
                        .set("update_time", new Date())
                        .eq("dtd_type_id", ecmDtdAttr.getDtdTypeId()));
        return Result.success(true);
    }

    private Result checkDtdAttr(List<EcmDtdAttr> ecmDtdAttrList, Long dtdTypeId) {
        List<EcmDtdAttr> ecmDtdAttrList1 = ecmDtdAttrMapper
                .selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                        .eq(EcmDtdAttr::getType,ecmOcrProperties.getOcrDocumentType())
                        .eq(EcmDtdAttr::getDtdTypeId, dtdTypeId));
        List<String> dtdAttrCodes = ecmDtdAttrList1.stream().map(EcmDtdAttr::getAttrCode)
                .collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(ecmDtdAttrList)) {
            for (EcmDtdAttr ecmDtdAttr : ecmDtdAttrList) {
                if (dtdAttrCodes.contains(ecmDtdAttr.getAttrCode())) {
                    return Result.error("单证属性代码不可重复！", ResultCode.PARAM_ERROR);
                }
            }
        }
        return null;
    }

    /**
     * 删除文档属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDocumentAttr(DeleteDocumentAttrVO deleteDocumentAttrVO,
                                     AccountTokenExtendDTO token) {
        AssertUtils.isNull(deleteDocumentAttrVO.getDtdAttrId(), "单证属性id不能为空");
        AssertUtils.isNull(deleteDocumentAttrVO.getDtdTypeId(), "单证类型id不能为空");
        ecmDtdAttrMapper.delete(new LambdaQueryWrapper<EcmDtdAttr>().in(EcmDtdAttr::getDtdAttrId,
                deleteDocumentAttrVO.getDtdAttrId()));
        ecmDtdDefMapper.update(null,
                new UpdateWrapper<EcmDtdDef>().set("update_user", token.getUsername())
                        .set("update_time", new Date())
                        .eq("dtd_type_id", deleteDocumentAttrVO.getDtdTypeId()));
        return Result.success(true);
    }

    /**
     * 查看文档属性
     */
    public Result searchDocumentAttr(Long dtdAttrId) {
        AssertUtils.isNull(dtdAttrId, "单证属性id不能为空");
        return Result.success(ecmDtdAttrMapper.selectById(dtdAttrId));
    }

    /**
     * 查看文档类型(也可用户复用确定按钮的回显数据)
     */
    public Result searchDocumentType() {
        return Result.success(
                ecmDtdDefMapper.selectList(new LambdaQueryWrapper<EcmDtdDef>()
                        .eq(EcmDtdDef::getType, ecmOcrProperties.getOcrDocumentType())
                        .orderByAsc(EcmDtdDef::getDtdSort)));
    }

    /**
     * 查看文档类型
     */
    public Result<EcmDtdDefVO> searchOneDocumentType(Long dtdTypeId) {
        EcmDtdDef ecmDtdDef = ecmDtdDefMapper.selectById(dtdTypeId);
        List<EcmDtdAttr> ecmDtdAttrList = (List<EcmDtdAttr>) multiplexDocumentArrtList(dtdTypeId)
                .getData();
        EcmDtdDefVO ecmDtdDefVo = new EcmDtdDefVO();
        BeanUtils.copyProperties(ecmDtdDef, ecmDtdDefVo);
        ecmDtdDefVo.setEcmDtdAttrList(ecmDtdAttrList);
        return Result.success(getUserNames(ecmDtdDef, ecmDtdDefVo));
    }

    private EcmDtdDefVO getUserNames(EcmDtdDef ecmDtdDef, EcmDtdDefVO ecmDtdDefVo) {
        String createUser = ecmDtdDef.getCreateUser();
        String updateUser = ecmDtdDef.getUpdateUser();
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
                if (!ObjectUtils.isEmpty(ecmDtdDef.getCreateUser())) {
                    if (e.getLoginName().equals(ecmDtdDef.getCreateUser())) {
                        ecmDtdDefVo.setCreateUserName(e.getName());
                    }
                }
                if (!ObjectUtils.isEmpty(ecmDtdDef.getUpdateUser())) {
                    if (e.getLoginName().equals(ecmDtdDef.getUpdateUser())) {
                        ecmDtdDefVo.setUpdateUserName(e.getName());
                    }
                }
            }
        }
        return ecmDtdDefVo;
    }

    /**
     * 复用-文档类型列表
     */
    public Result<PageInfo<EcmDtdAttrInfoDTO>> searchDocumentAttrList(PageForm pageForm,
                                                                      Long dtdTypeId) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                .eq(EcmDtdAttr::getDtdTypeId, dtdTypeId).orderByAsc(EcmDtdAttr::getAttrSort));
        PageInfo pageInfo = new PageInfo<>(ecmDtdAttrs);
        //获取创建人和修改人名称
        //用户id列表
        List<String> userIds = new ArrayList<>();
        ecmDtdAttrs.forEach(p -> {
            if (!org.springframework.util.ObjectUtils.isEmpty(p.getCreateUser())) {
                userIds.add(p.getCreateUser());
            }
            if (!org.springframework.util.ObjectUtils.isEmpty(p.getUpdateUser())) {
                userIds.add(p.getUpdateUser());
            }
        });
        List<EcmDtdAttrInfoDTO> extend = new ArrayList<>();
        Map<String, List<SysUserDTO>> userListByUserIds = modelPermissionsService
                .getUserListByUserIds(userIds);
        for (EcmDtdAttr e : ecmDtdAttrs) {
            EcmDtdAttrInfoDTO extend1 = new EcmDtdAttrInfoDTO();
            BeanUtils.copyProperties(e, extend1);
            //添加创建人名称
            if (!ObjectUtils.isEmpty(e.getCreateUser())
                    && !CollectionUtils.isEmpty(userListByUserIds)) {
                if (!CollectionUtils.isEmpty(userListByUserIds.get(e.getCreateUser()))) {
                    extend1.setCreateUserName(
                            userListByUserIds.get(e.getCreateUser()).get(0).getName());
                }
            }
            //最近修改人名称
            if (!ObjectUtils.isEmpty(e.getUpdateUser())) {
                if (!CollectionUtils.isEmpty(userListByUserIds.get(e.getUpdateUser()))) {
                    extend1.setUpdateUserName(
                            userListByUserIds.get(e.getUpdateUser()).get(0).getName());
                }
            }
            extend.add(extend1);
        }
        pageInfo.setList(extend);
        return Result.success(pageInfo);
    }

    /**
     * 复用-文档属性列表
     */
    public Result multiplexDocumentArrtList(Long dtdTypeId) {
        AssertUtils.isNull(dtdTypeId, "单证类型id不能为空");
        return Result.success(ecmDtdAttrMapper.selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                .eq(EcmDtdAttr::getDtdTypeId, dtdTypeId).orderByAsc(EcmDtdAttr::getAttrSort)));
    }

    /**
     * 复用-新增文档属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Result multiplexAddDocumentAttr(EcmDtdAttrMulDTO ecmAddAttrDTO,
                                           AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmAddAttrDTO.getTypeId(), "单证类型id不能为空");
        if (!CollectionUtils.isEmpty(ecmAddAttrDTO.getAttrIdList())
                && !ObjectUtils.isEmpty(ecmAddAttrDTO.getTypeId())) {
            List<EcmDtdAttr> ecmDtdAttrList = ecmDtdAttrMapper
                    .selectList(new LambdaQueryWrapper<EcmDtdAttr>().in(EcmDtdAttr::getDtdAttrId,
                            ecmAddAttrDTO.getAttrIdList()));
            //复用前的单证属性集合
            List<EcmDtdAttr> ecmDtdAttrTypeList = ecmDtdAttrMapper.selectList(
                    new LambdaQueryWrapper<EcmDtdAttr>().in(EcmDtdAttr::getDtdTypeId, ecmAddAttrDTO.getTypeId()));
            //判断单证属性代码不能重复
            //-得到复用前的单证属性代码集合
            List<String> attrCodes = ecmDtdAttrTypeList.stream().map(EcmDtdAttr::getAttrCode)
                    .collect(Collectors.toList());
            //-得到复用的的单证属性代码集合
            List<String> multiplexAttrCodes = ecmDtdAttrList.stream().map(EcmDtdAttr::getAttrCode)
                    .collect(Collectors.toList());
            Map<String, List<EcmDtdAttr>> collect = ecmDtdAttrList.stream()
                    .collect(Collectors.groupingBy(EcmDtdAttr::getAttrCode));
            ArrayList<String> intersection = (ArrayList) org.apache.commons.collections4.CollectionUtils
                    .intersection(attrCodes, multiplexAttrCodes);
            if (!CollectionUtils.isEmpty(intersection)) {
                List<String> attrName = new ArrayList<>();
                for (String s : intersection) {
                    List<EcmDtdAttr> ecmDtdAttrList1 = collect.get(s);
                    if (!CollectionUtils.isEmpty(ecmDtdAttrList1)) {
                        attrName.add(ecmDtdAttrList1.get(0).getAttrName());
                    }
                }
                return Result.error("复用的单证属性" + attrName + "重复", ResultCode.PARAM_ERROR);
            }
            Integer integer = ecmDtdAttrTypeList.stream().map(EcmDtdAttr::getAttrSort)
                    .collect(Collectors.toList()).stream().max(Comparator.comparing(x -> x))
                    .orElse(null);
            if (ObjectUtils.isEmpty(integer)) {
                integer = StateConstants.ZERO;
            }
            for (EcmDtdAttr ecmDtdAttr : ecmDtdAttrList) {
                ecmDtdAttr.setDtdAttrId(null);
                ecmDtdAttr.setDtdTypeId(ecmAddAttrDTO.getTypeId());
                ecmDtdAttr.setAttrSort(++integer);
                //todo 改批量插入
                ecmDtdAttrMapper.insert(ecmDtdAttr);
            }
            //修改EcmDtdDef表
            ecmDtdDefMapper.update(null,
                    new UpdateWrapper<EcmDtdDef>().set("update_user", token.getUsername())
                            .set("update_time", new Date())
                            .eq("dtd_type_id", ecmAddAttrDTO.getTypeId()));
        }
        return Result.success(true);
    }

    /**
     * 移动文档属性顺序
     */
    @Transactional(rollbackFor = Exception.class)
    public Result moveDocumentArrtList(EcmMoveDtdAttrDTO ecmMoveDtdAttrDTO, String userId) {
        AssertUtils.isNull(ecmMoveDtdAttrDTO.getDtdAttrId(), "单证属性id不能为空");
        AssertUtils.isNull(ecmMoveDtdAttrDTO.getTargetDtdAttrId(), "目标单证属性id不能为空");
        AssertUtils.isNull(ecmMoveDtdAttrDTO.getAttrSort(), "单证属性顺序不能为空");
        AssertUtils.isNull(ecmMoveDtdAttrDTO.getTargetDtdAttrId(), "目标单证属性顺序不能为空");
        //选中顺序减去目标顺序
        long value = ecmMoveDtdAttrDTO.getAttrSort() - ecmMoveDtdAttrDTO.getTargetAttrSort();
        LambdaQueryWrapper<EcmDtdAttr> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmDtdAttr::getDtdTypeId, ecmMoveDtdAttrDTO.getDtdTypeId());
        if (value > IcmsConstants.ZERO) {
            wrapper.between(EcmDtdAttr::getAttrSort, ecmMoveDtdAttrDTO.getTargetAttrSort(),
                    ecmMoveDtdAttrDTO.getAttrSort());
        } else {
            wrapper.between(EcmDtdAttr::getAttrSort, ecmMoveDtdAttrDTO.getAttrSort(),
                    ecmMoveDtdAttrDTO.getTargetAttrSort());
        }
        List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(ecmDtdAttrs)) {
            for (EcmDtdAttr ecmDtdAttr : ecmDtdAttrs) {
                if (ecmDtdAttr.getDtdAttrId().equals(ecmMoveDtdAttrDTO.getDtdAttrId())) {
                    ecmDtdAttr.setAttrSort(ecmMoveDtdAttrDTO.getTargetAttrSort());
                    ecmDtdAttrMapper.updateById(ecmDtdAttr);
                    continue;
                }
                //向上移动
                if (value > IcmsConstants.ZERO) {
                    ecmDtdAttr.setAttrSort(ecmDtdAttr.getAttrSort() + 1);
                    //向下移动
                } else {
                    ecmDtdAttr.setAttrSort(ecmDtdAttr.getAttrSort() - 1);
                }
                ecmDtdAttrMapper.updateById(ecmDtdAttr);
            }
        }
        //更新文档基本信息
        LambdaUpdateWrapper<EcmDtdDef> dtdWrapper = new LambdaUpdateWrapper<>();
        dtdWrapper.set(EcmDtdDef::getUpdateUser, userId);
        dtdWrapper.set(EcmDtdDef::getUpdateTime, new Date());
        dtdWrapper.eq(EcmDtdDef::getDtdTypeId, ecmMoveDtdAttrDTO.getDtdTypeId());
        ecmDtdDefMapper.update(null, dtdWrapper);
        return Result.success(true);
    }
}
