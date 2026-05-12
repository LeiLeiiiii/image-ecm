package com.sunyard.sunafm.service;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.sunafm.constant.AfmConstant;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2024/3/20 14:05:53
 * @title
 * @description 系统管理/阈值配置实现类
 */
@Service
public class SysThresholdService {
    @Resource
    private ParamApi paramApi;

    /**
     * 获取配置的相似度
     */
    public void updateSimpleDefult(String value) {
        AssertUtils.isNull(value, "参数有误");
        JSONObject systemFileNet = getSystemFileNet();
        double v = Double.parseDouble(value) / 100;
        systemFileNet.put(AfmConstant.FILE_NUM_SYSTEM, AfmConstant.FILE_NUM_MILVUS_DEFULT);
        systemFileNet.put(AfmConstant.FILE_SIMILARITY_SYSTEM, v);
        paramApi.updateValueByKey(AfmConstant.AFM_PARAM_SYSTEM, JSONObject.toJSONString(systemFileNet));
    }

    /**
     * 获取默认配置
     */
    private JSONObject getSystemFileNet() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(AfmConstant.FILE_NUM_SYSTEM, AfmConstant.FILE_NUM_MILVUS_DEFULT);
        jsonObject.put(AfmConstant.FILE_SIMILARITY_SYSTEM, AfmConstant.FILE_SIMILARITY_DEFULT);
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(AfmConstant.AFM_PARAM_SYSTEM);
        if (sysParamDTOResult.isSucc()) {
            String value = sysParamDTOResult.getData().getValue();
            jsonObject = JSONObject.parseObject(value);
        }
        return jsonObject;
    }
}
