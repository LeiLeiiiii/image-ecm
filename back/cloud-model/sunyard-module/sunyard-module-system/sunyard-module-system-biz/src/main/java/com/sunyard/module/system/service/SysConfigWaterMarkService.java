package com.sunyard.module.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.sunyard.module.system.constant.DictConstants;
import com.sunyard.module.system.constant.ParamConstants;
import com.sunyard.module.system.enums.EnableStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.mapper.SysParamMapper;
import com.sunyard.module.system.po.SysConfigWatermark;
import com.sunyard.module.system.po.SysDictionary;
import com.sunyard.module.system.po.SysParam;

/**
 * 系统管理-日志管理
 *
 * @author wubingyang
 * @date 2021/7/20 21:44
 */
@Service
public class SysConfigWaterMarkService {
    @Resource
    private SysParamMapper sysParamMapper;
    @Resource
    private SysDictionaryService sysDictionaryService;

    /**
     * 配置详情
     *
     * @return Result
     */
    public SysConfigWatermark select() {
        List<SysParam> sysParams = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParam>()
                    .eq(SysParam::getName, ParamConstants.WATERMARK_PARAM));

        if (CollectionUtils.isEmpty(sysParams)) {
            return null;
        }
        String value = sysParams.get(0).getValue();
        SysConfigWatermark sysConfigWatermark = JSONObject.parseObject(value,
                SysConfigWatermark.class);
        return sysConfigWatermark;
    }

    /**
     * 查看水印配置
     * @return Result
     */
    public Map selectWaterMark() {
        List<SysParam> sysParams = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParam>()
                    .eq(SysParam::getName, ParamConstants.WATERMARK_PARAM_SCREEN));
        Map map = new HashMap<>(6);

        if (!CollectionUtils.isEmpty(sysParams)) {
            String value = sysParams.get(0).getValue();
            JSONObject jsonObject = JSONObject.parseObject(value);
            map.put("screen", jsonObject);
        }

        List<SysParam> file = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParam>()
                    .eq(SysParam::getName, ParamConstants.WATERMARK_PARAM_FILE));

        if (!CollectionUtils.isEmpty(file)) {
            String value = file.get(0).getValue();
            JSONObject jsonObject = JSONObject.parseObject(value);
            map.put("file", jsonObject);
        }

        List<SysDictionary> sysDictionaries = sysDictionaryService
                .selectValueByParentKey(DictConstants.COMMON_WATERMARK_FONTSIZE, null);

        List<SysDictionary> sysDictionaries1 = sysDictionaryService
                .selectValueByParentKey(DictConstants.COMMON_WATERMARK_FAMILYNAME, null);

        map.put("fontSize", sysDictionaries);
        map.put("familyName", sysDictionaries1);

        return map;
    }

    /**
     * 报错配置
     * @param config 水印配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(SysConfigWatermark config) {
        List<SysParam> sysParams = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParam>()
                    .eq(SysParam::getName, ParamConstants.WATERMARK_PARAM));
        if (CollectionUtils.isEmpty(sysParams)) {
            SysParam sysParam = new SysParam();
            sysParam.setName(ParamConstants.WATERMARK_PARAM);
            sysParam.setValue(JSONObject.toJSONString(config));
            sysParam.setStatus(EnableStatusEnum.ENABLED.getCode());
            sysParam.setRemark("水印配置");
            sysParamMapper.insert(sysParam);
        } else {
            String value = sysParams.get(0).getValue();
            SysConfigWatermark sysConfigWatermark = JSONObject.parseObject(value,
                    SysConfigWatermark.class);
            Assert.notNull(sysConfigWatermark, "保存失败");
            sysParamMapper.update(null,
                    new LambdaUpdateWrapper<SysParam>()
                        .set(SysParam::getStatus, EnableStatusEnum.ENABLED.getCode())
                        .set(SysParam::getValue, JSONObject.toJSONString(config))
                        .eq(SysParam::getId, sysParams.get(0).getId()));
        }
    }

    /**
     * 修改水印配置
     * @param config 水印配置
     * @param type 类型
     */
    public void saveWaterMarkConfig(String config, String type) {
        AssertUtils.isTrue(!(ParamConstants.WATERMARK_PARAM_SCREEN.equals(type)
                || ParamConstants.WATERMARK_PARAM_FILE.equals(type)), "类型有误");
        List<SysParam> sysParams = sysParamMapper
                .selectList(new LambdaQueryWrapper<SysParam>()
                    .eq(SysParam::getName, type));

        if (CollectionUtils.isEmpty(sysParams)) {
            SysParam sysParam = new SysParam();
            sysParam.setName(type);
            sysParam.setStatus(EnableStatusEnum.ENABLED.getCode());
            sysParam.setValue(config);
            sysParamMapper.insert(sysParam);
        } else {
            sysParams.get(0).setValue(config);
            sysParamMapper.updateById(sysParams.get(0));
        }
    }

}
