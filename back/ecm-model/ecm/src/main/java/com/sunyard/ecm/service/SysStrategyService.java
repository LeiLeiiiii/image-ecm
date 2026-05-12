package com.sunyard.ecm.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.sunyard.module.system.api.MenuApi;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmStrategytypeAppcodeMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmStrategytypeAppcode;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;
import com.sunyard.ecm.vo.SysStrategyVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * @author scm
 * @since 2023/7/27 14:05
 * @desc 策略管理实现类
 */
@Slf4j
@Service
public class SysStrategyService{

    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmStrategytypeAppcodeMapper ecmStrategytypeAppcodeMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private MenuApi menuApi;
    @Resource
    private ModelBusiService modelBusiService;

    /**
     * 业务类型资料树查询接口
     * @param appTypeIds 已指定的业务类型
     * @return List<EcmAppDefAttrVO> 业务类型资料树
     */
    public List<EcmAppDefAttrVO> getAppTypeTree(List<String> appTypeIds,
                                                AccountTokenExtendDTO token) {
        if (ObjectUtils.isEmpty(appTypeIds)) {
            return modelBusiService.searchBusiTypeTree(null, token);
        } else {
            LambdaQueryWrapper<EcmAppDef> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(EcmAppDef::getAppCode, appTypeIds);
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(queryWrapper);
            List<EcmAppDef> ecmAppDefs1 = new ArrayList<>(ecmAppDefs);
            for (EcmAppDef ecmAppDef : ecmAppDefs) {
                if (StateConstants.PARENT_APP_CODE_DEFAULT.equals(ecmAppDef.getParent())) {
                    selectSon(ecmAppDefs1, ecmAppDef.getAppCode());
                } else {
                    selectFather(ecmAppDefs1, ecmAppDef.getParent());
                }
            }
            //根据父ID进行分组
            Map<String, List<EcmAppDef>> collect = ecmAppDefs1.stream()
                    .collect(Collectors.groupingBy(EcmAppDef::getParent));
            //按照给定的顺序值排序
            collect.values()
                    .forEach(list -> list.sort(Comparator.comparing(EcmAppDef::getAppSort)));
            return busiTypeTreeNew(collect, StateConstants.PARENT_APP_CODE_DEFAULT, "无", null);
        }
    }

    /**
     * 策略管理配置查询接口
     * @return SysStrategyDTO 策略配置
     */
    public SysStrategyDTO queryConfig() {
        Result<SysParamDTO> result = paramApi
                .searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        if (result.isSucc() && result.getData() != null) {
            String value = result.getData().getValue();
            SysStrategyDTO sysStrategyDTO = JSONObject.parseObject(value, SysStrategyDTO.class);
            //添加 OCR总识别业务类型ID 和 混贴拆分业务类型ID
            //查询所有配置
            List<EcmStrategytypeAppcode> list = ecmStrategytypeAppcodeMapper.selectList(new LambdaQueryWrapper<>());
            if (!CollectionUtils.isEmpty(list)) {
                //根据策略类型分组
                Map<String, List<EcmStrategytypeAppcode>> collect = list.stream()
                        .collect(Collectors.groupingBy(EcmStrategytypeAppcode::getStrategyType));
                if (!ObjectUtils.isEmpty(collect)) {
                    sysStrategyDTO.setOcrConfigIds(
                            !ObjectUtils.isEmpty(collect.get(IcmsConstants.OCR_CONFIG))
                                    ? collect.get(IcmsConstants.OCR_CONFIG).stream()
                                            .map(EcmStrategytypeAppcode::getAppCode).collect(
                                                    Collectors.toList())
                                    : null);
                    sysStrategyDTO.setSplitIds(
                            !ObjectUtils.isEmpty(collect.get(IcmsConstants.SPLIT_CONFIG))
                                    ? collect.get(IcmsConstants.SPLIT_CONFIG).stream()
                                            .map(EcmStrategytypeAppcode::getAppCode).collect(
                                                    Collectors.toList())
                                    : null);
                }
            }
            Integer zipScale = sysStrategyDTO.getZipScale();
            if (zipScale != null) {
                double decimal = (double) zipScale / 100;
                decimal = Math.round(decimal * 100.0) / 100.0;
                sysStrategyDTO.setFinalZipScale(decimal);
            }
            return sysStrategyDTO;
        }
        return null;
    }

    /**
     * 策略管理配置更新接口
     * @param vo 更新内容
     * @param token 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateConfig(SysStrategyVO vo, AccountTokenExtendDTO token) {
        Result<SysParamDTO> result = paramApi
                .searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = result.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        BeanUtil.copyProperties(vo, sysStrategyVO,
                CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        //先删除原来所有影像策略类型业务代码关联的数据
        ecmStrategytypeAppcodeMapper.delete(new LambdaQueryWrapper<>());
        //要进行OCR识别的业务类型code集合
        List<String> ocrConfigIds = sysStrategyVO.getOcrConfigIds();
        //要进行混贴拆分的业务类型code集合
        List<String> splitIds = sysStrategyVO.getSplitIds();
        //策略管理配置业务的集合
        List<EcmStrategytypeAppcode> ecmStrategytypeAppcodeList = new ArrayList<>();
        //影像策略类型业务代码关联对象
        if (!CollectionUtils.isEmpty(ocrConfigIds)) {
            for (String ocrConfigId : ocrConfigIds) {
                EcmStrategytypeAppcode ecmStrategytypeAppcode = new EcmStrategytypeAppcode();
                ecmStrategytypeAppcode.setId(snowflakeUtil.nextId());
                ecmStrategytypeAppcode.setStrategyType(IcmsConstants.OCR_CONFIG);
                ecmStrategytypeAppcode.setAppCode(ocrConfigId);
                ecmStrategytypeAppcode.setCreateUser(token.getUsername());
                ecmStrategytypeAppcodeList.add(ecmStrategytypeAppcode);
            }
        }
        if (!CollectionUtils.isEmpty(splitIds)) {
            for (String splitId : splitIds) {
                EcmStrategytypeAppcode ecmStrategytypeAppcode = new EcmStrategytypeAppcode();
                ecmStrategytypeAppcode.setId(snowflakeUtil.nextId());
                ecmStrategytypeAppcode.setStrategyType(IcmsConstants.SPLIT_CONFIG);
                ecmStrategytypeAppcode.setAppCode(splitId);
                ecmStrategytypeAppcode.setCreateUser(token.getUsername());
                ecmStrategytypeAppcodeList.add(ecmStrategytypeAppcode);
            }
        }
        //批量插入影像策略类型业务代码关联表
        if (!ObjectUtils.isEmpty(ecmStrategytypeAppcodeList)){
            MybatisBatch<EcmStrategytypeAppcode> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmStrategytypeAppcodeList);
            MybatisBatch.Method<EcmStrategytypeAppcode> method = new MybatisBatch.Method<>(EcmStrategytypeAppcodeMapper.class);
            mybatisBatch.execute(method.insert());
        }
        sysStrategyVO.setOcrConfigIds(null);
        sysStrategyVO.setSplitIds(null);
        String newValue = JSON.toJSONString(sysStrategyVO);
        paramApi.updateValueAndStatusByKey(StrategyConstantsEnum.OCR_STRATEGY.toString(), newValue,
                IcmsConstants.ONE);
        return true;
    }

    /**
     *  获取ecm菜单配置
     * @return
     */
    public List<String> queryEcmEnumConfig() {
        List<String> ecmEnumConfig = new ArrayList<>();
        //底座ecm菜单code = 3
        Result<List<String>> result = menuApi.getPermsByMenuSystem(IcmsConstants.THREE);
        if (result.isSucc() && result.getData() != null) {
            ecmEnumConfig = result.getData();
        }
        return ecmEnumConfig;
    }

    /**
     * 业务类型树构建
     * @param collect1 业务节点集合
     * @param parentId 父ID
     * @param parentName 父节点名称
     * @param appCode 节点ID
     * @return 业务类型树
     */
    private List<EcmAppDefAttrVO> busiTypeTreeNew(Map<String, List<EcmAppDef>> collect1,
                                                  String parentId, String parentName,
                                                  String appCode) {
        List<EcmAppDefAttrVO> ecmAppDefAttrVOS = new ArrayList<>();
        //得到该子节点的类的信息
        collect1.forEach((k, v) -> {
            if (k.equals(parentId)) {
                for (EcmAppDef e : v) {
                    EcmAppDefAttrVO ecmAppDefAttrVo = new EcmAppDefAttrVO();
                    ecmAppDefAttrVo.setId(e.getAppCode());
                    ecmAppDefAttrVo.setAppCode(e.getAppCode());
                    ecmAppDefAttrVo.setAppName(e.getAppName());
                    ecmAppDefAttrVo.setLabel(e.getAppName());
                    ecmAppDefAttrVo.setParentName(parentName);
                    ecmAppDefAttrVo.setParent(parentId);
                    ecmAppDefAttrVo.setDisabled(
                            !com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(appCode)
                                    && e.getAppCode().equals(appCode));
                    ecmAppDefAttrVo.setAppSort(e.getAppSort());
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
                    }
                    ecmAppDefAttrVOS.add(ecmAppDefAttrVo);
                }
            }
        });
        return ecmAppDefAttrVOS;
    }

    /**
     * 业务类型树子类递归
     * @param list 存放数组
     * @param appCode 父ID
     */
    private void selectSon(List<EcmAppDef> list, String appCode) {
        LambdaQueryWrapper<EcmAppDef> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmAppDef::getParent, appCode);
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(queryWrapper);
        if (!ObjectUtils.isEmpty(ecmAppDefs)) {
            list.addAll(ecmAppDefs);
            for (EcmAppDef ecmAppDef : ecmAppDefs) {
                selectSon(list, ecmAppDef.getAppCode());
            }
        }
    }

    /**
     * 业务类型树父类递归
     * @param list 存放数组
     * @param appCode 父ID
     */
    private void selectFather(List<EcmAppDef> list, String appCode) {
        LambdaQueryWrapper<EcmAppDef> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmAppDef::getAppCode, appCode);
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper);
        if (!ObjectUtils.isEmpty(ecmAppDef)) {
            list.add(ecmAppDef);
            selectFather(list, ecmAppDef.getParent());
        }
    }

}
