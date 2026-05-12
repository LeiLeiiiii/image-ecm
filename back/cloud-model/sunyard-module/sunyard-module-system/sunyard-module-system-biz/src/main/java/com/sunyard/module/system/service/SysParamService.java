package com.sunyard.module.system.service;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.module.system.constant.ParamConstants;
import com.sunyard.module.system.enums.EnableStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.dto.SysConfigLoginDTO;
import com.sunyard.module.system.mapper.SysParamMapper;
import com.sunyard.module.system.po.SysParam;

/**
 * 系统管理-配置管理
 *
 * @Author raochangmei 2022-03-21
 */
@Service
public class SysParamService {
    @Resource
    private SysParamMapper sysParamMapper;

    /**
     * 获取登陆配置
     */
    public SysConfigLoginDTO select() {
        List<SysParam> name = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParam>()
                    .eq(SysParam::getStatus, EnableStatusEnum.ENABLED.getCode())
                    .in(SysParam::getName, ParamConstants.LOGIN_CONFIG_TYPE_BD_STR,
                            ParamConstants.LOGIN_CONFIG_TYPE_LDAP_STR));
        if (CollectionUtils.isEmpty(name)) {
            return null;
        }
        SysParam sysParam = name.get(0);
        String value = sysParam.getValue();
        SysConfigLoginDTO sysConfigLoginDTO = JSONObject.parseObject(value,
                SysConfigLoginDTO.class);
        return sysConfigLoginDTO;
    }

    /**
     * 根据key获取
     */
    public SysParam searchValueByKey(String key) {
        SysParam sysParam = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getName, key));
        return sysParam;
    }

    /**
     * 新增
     */
    public void addParam(SysParam sysParam) {
        AssertUtils.isNull(sysParam.getName(), "参数错误");
        AssertUtils.isNull(sysParam.getValue(), "参数错误");
        sysParamMapper.insert(sysParam);

    }

    /**
     * 修改
     */
    public void updateParam(SysParam sysParam) {
        AssertUtils.isNull(sysParam.getId(), "参数错误");
        AssertUtils.isNull(sysParam.getValue(), "参数错误");
        AssertUtils.isNull(sysParam.getType(), "参数错误");
        sysParamMapper.update(null,
                new LambdaUpdateWrapper<SysParam>()
                    .set(SysParam::getValue, sysParam.getValue())
                    .set(SysParam::getType, sysParam.getType())
                    .set(SysParam::getStatus, sysParam.getStatus())
                    .set(SysParam::getRemark, sysParam.getRemark())
                    .eq(SysParam::getId, sysParam.getId()));
    }

    /**
     * 获取详情
     */
    public SysParam getInfoParam(Long id) {
        AssertUtils.isNull(id, "参数错误");
        SysParam sysParam = sysParamMapper.selectById(id);
        AssertUtils.isNull(sysParam, "参数错误");
        return sysParam;
    }

    /**
     * 查询列表
     */
    public PageInfo getListParam(SysParam sysParam, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysParam> sysParams = sysParamMapper.selectList(
            new LambdaQueryWrapper<SysParam>()
                .like(StringUtils.hasText(sysParam.getRemark()), SysParam::getRemark, sysParam.getRemark())
                .eq(null != sysParam.getType(), SysParam::getType, sysParam.getType())
                .like(StringUtils.hasText(sysParam.getName()), SysParam::getName, sysParam.getName())
                .eq(null != sysParam.getSystemCode(), SysParam::getSystemCode, sysParam.getSystemCode())
                .orderByDesc(SysParam::getCreateTime));
        return new PageInfo<>(sysParams);
    }

    /**
     * 删除
     */
    public void delParam(Long id) {
        AssertUtils.isNull(id, "参数错误");
        sysParamMapper.deleteById(id);
    }

    /**
     * 更新配置
     */
    public void updateValueByKey(String key, String value) {
        sysParamMapper.update(null, new LambdaUpdateWrapper<SysParam>()
                .set(SysParam::getValue, value).eq(SysParam::getName, key));
    }

    /**
     * 修改状态和值
     * @param key key
     * @param value 值
     * @param status 状态
     */
    public void updateValueAndStatusByKey(String key, String value, Integer status) {
        sysParamMapper.update(null,
                new LambdaUpdateWrapper<SysParam>().set(SysParam::getValue, value)
                        .eq(SysParam::getName, key).set(SysParam::getStatus, status));
    }
}
