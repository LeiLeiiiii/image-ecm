package com.sunyard.ecm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmBusiCacheStrategyDTO;
import com.sunyard.ecm.dto.ecm.EcmStorageQueDTO;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.vo.EcmBusiCacheStrategyVO;
import com.sunyard.ecm.vo.EcmBusiMQListVO;
import com.sunyard.ecm.vo.EcmBusiStorageListVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mq.util.MqUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.storage.api.StorageEquipmentApi;
import com.sunyard.module.storage.dto.EquipmentDTO;
import com.sunyard.module.storage.vo.EquipmentVO;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;

/**
 * @author： zyl
 * @create： 2023/4/13 16:25
 * @desc: 文件存储管理实现类
 */
@Service
public class SysStorageService {

    @Resource
    private ParamApi paramApi;
    @Value("${spring.rabbitmq.mgmt-host:}")
    private String mgmtHost;
    @Value("${spring.rabbitmq.host:}")
    private String host;
    @Value("${spring.rabbitmq.apiport}")
    private String port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private StorageEquipmentApi equipmentApi;

    /**
     * 查询业务列表
     */
    public PageInfo<EcmBusiStorageListVO> getBusiStorageList(PageForm page) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<EcmBusiStorageListVO> ecmAppDefs = ecmAppDefMapper.selectLastList();
        List<Long> equipmentIds = ecmAppDefs.stream().map(EcmBusiStorageListVO::getEquipmentId)
                .collect(Collectors.toList());
        EquipmentVO equipmentVO = new EquipmentVO();
        equipmentVO.setIds(equipmentIds);
        Result<List<EquipmentDTO>> equipmentList = equipmentApi.getEquipmentList(equipmentVO);
        if (equipmentList.isSucc() && !CollectionUtils.isEmpty(equipmentList.getData())) {
            Map<Long, List<EquipmentDTO>> collect = equipmentList.getData().stream()
                    .collect(Collectors.groupingBy(EquipmentDTO::getId));
            for (EcmBusiStorageListVO vo : ecmAppDefs) {
                List<EquipmentDTO> equipmentDtos = collect.get(vo.getEquipmentId());
                if (CollectionUtils.isEmpty(equipmentDtos)) {
                    continue;
                }
                EquipmentDTO equipmentDto = equipmentDtos.get(0);
                vo.setStorageType(equipmentDto.getStorageType());
                vo.setStorageDeviceName(equipmentDto.getEquipmentName());
                vo.setStorageDeviceId(equipmentDto.getEquipmentCode());
                vo.setStorageUrl(equipmentDto.getBasePath());
                vo.setCreateTime(equipmentDto.getCreateTime());
                vo.setBucket(equipmentDto.getBucket());
            }
        }
        return new PageInfo<>(ecmAppDefs);
    }

    /**
     * 得到业务存储设备信息
     */
    public EcmBusiCacheStrategyVO getBusiStorageStrategy() {
        EcmBusiCacheStrategyVO vo = new EcmBusiCacheStrategyVO();
        Result<SysParamDTO> sysParamDTOResult = paramApi
                .searchValueByKey(IcmsConstants.CACHE_CLEAR_DAY_THRESHOLD);
        if (sysParamDTOResult.isSucc()) {
            vo.setClearTime(sysParamDTOResult.getData().getValue());
        }
        Result<SysParamDTO> max = paramApi
                .searchValueByKey(IcmsConstants.CACHE_CLEAR_FILEMAX_THRESHOLD);
        if (max.isSucc()) {
            vo.setClearThreshold(max.getData().getValue());
        }
        return vo;
    }

    /**
     * 得到存储设备列表
     */
    public Result<List<EcmStorageQueDTO>> getStorageDeviceList() {
        Result<List<EquipmentDTO>> equipmentList = equipmentApi.getEquipmentList(new EquipmentVO());
        Result<List<EcmStorageQueDTO>> result = new Result<>();
        result.setCode(equipmentList.getCode());
        if (equipmentList.isSucc()) {
            List<EcmStorageQueDTO> ecmEquipmentDTOS = PageCopyListUtils
                    .copyListProperties(equipmentList.getData(), EcmStorageQueDTO.class);
            for (EcmStorageQueDTO dto : ecmEquipmentDTOS) {
                dto.setUniqId(dto.getId() + "");
            }
            result.setData(ecmEquipmentDTOS);
        }
        return result;
    }

    /**
     * 新增业务存储设备信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void addBusiRelationStorage(EcmBusiStorageListVO vo) {
        AssertUtils.isNull(vo.getEquipmentId(), "参数有误");
        AssertUtils.isNull(vo.getAppCodes(), "参数有误");
        ecmAppDefMapper.update(null,
                new LambdaUpdateWrapper<EcmAppDef>()
                        .set(EcmAppDef::getEquipmentId, vo.getEquipmentId())
                        .in(EcmAppDef::getAppCode, vo.getAppCodes()));

    }

    /**
     * 修改缓存配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBusiCacheStrategy(EcmBusiCacheStrategyDTO ecmBusiCacheStrategyDTO) {
        paramApi.updateValueByKey(IcmsConstants.CACHE_CLEAR_DAY_THRESHOLD,
                ecmBusiCacheStrategyDTO.getClearTime());
        paramApi.updateValueByKey(IcmsConstants.CACHE_CLEAR_FILEMAX_THRESHOLD,
                ecmBusiCacheStrategyDTO.getClearThreshold());
    }

    /**
     * 获取消息队列
     */
    public List<EcmStorageQueDTO> getMQSettingList() {
        String targetHost = org.springframework.util.StringUtils.hasText(mgmtHost) ? mgmtHost : host;
        String apiMessage = MqUtils.getApiMessage(targetHost, port, username.toCharArray(), password.toCharArray());
        JSONArray jsonArray = JSONArray.parseArray(apiMessage);
        ArrayList<EcmStorageQueDTO> objects = new ArrayList<>();
        if (!CollectionUtils.isEmpty(jsonArray)) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                EcmStorageQueDTO vo = new EcmStorageQueDTO();
                //过滤掉其他服务使用的队列信息
                if (!(jsonObject.getString("name").contains("queue_afm")
                        || jsonObject.getString("name").contains("queue_storage"))) {
                    vo.setUniqId(jsonObject.getString("name"));
                    vo.setMqName(jsonObject.getString("name"));
                    vo.setMqAddress(jsonObject.getString("vhost"));
                    objects.add(vo);
                }
            }
        }
        return objects;
    }

    /**
     * 获取业务队列表
     */
    public PageInfo getBusiMQList(PageForm page) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<EcmBusiStorageListVO> ecmAppDefs = ecmAppDefMapper.selectLastList();
        return new PageInfo<>(ecmAppDefs);
    }

    /**
     * 更新队列
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBusiMQ(EcmBusiMQListVO vo) {
        AssertUtils.isNull(vo.getMqName(), "参数有误");
        AssertUtils.isNull(vo.getAppCodes(), "参数有误");

        ecmAppDefMapper.update(null,
                new LambdaUpdateWrapper<EcmAppDef>().set(EcmAppDef::getQueueName, vo.getMqName())
                        .in(EcmAppDef::getAppCode, vo.getAppCodes()));
    }

    /**
     * 更新队列状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBusiMQStatus(EcmBusiMQListVO vo) {
        AssertUtils.isNull(vo.getAppCode(), "参数有误");
        AssertUtils.isNull(vo.getStatus(), "参数有误");
        ecmAppDefMapper.update(null,
                new LambdaUpdateWrapper<EcmAppDef>().set(EcmAppDef::getArriveInform, vo.getStatus())
                        .eq(EcmAppDef::getAppCode, vo.getAppCode()));
    }
}
