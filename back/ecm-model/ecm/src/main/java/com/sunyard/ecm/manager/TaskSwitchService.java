package com.sunyard.ecm.manager;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
//import com.sunyard.ecm.mapper.EcmDocDynaPlagMapper;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Wenbiwen
 * @create： 2025/3/20
 */
@Service
public class TaskSwitchService {
//    @Resource
//    private EcmDocDynaPlagMapper docDynaPlagMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private BusiCacheService busiCacheService;

    /**
     * 查询配置开关状态
     */
    public Map<Integer, Boolean> queryAllSwitches(String docCode) {
        // 参数校验
        AssertUtils.isNull(docCode, "文档ID不能为空");

        // 处理未分类文档的特殊情况
        if (IcmsConstants.UNCLASSIFIED_ID.equals(docCode)) {
            return null;
        }
        // 构建返回结果
        Map<Integer, Boolean> switchStatus = new LinkedHashMap<>();

        EcmDocDef docDef = busiCacheService.getIntelligentProcessingEcmDocDef(docCode);

        // 处理空记录情况
        if (docDef == null) {
            return null;
        }

        switchStatus.put(IcmsConstants.REGULARIZE,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsRegularized()));
        switchStatus.put(IcmsConstants.OBSCURE,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsObscured()));
        switchStatus.put(IcmsConstants.REMAKE,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsRemade()));
        switchStatus.put(IcmsConstants.PLAGIARISM,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsPlagiarism()));
        switchStatus.put(IcmsConstants.AUTOMATIC_CLASSIFICATION,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsAutoClassified()));
        switchStatus.put(IcmsConstants.REFLECTIVE,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsReflective()));
        switchStatus.put(IcmsConstants.MISS_CORNER,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsCornerMissing()));
        switchStatus.put(IcmsConstants.PLAGIARISM_TEXT,
                !IcmsConstants.STATE_CLOSE.equals(docDef.getIsPlagiarismText()));
        return switchStatus;
    }


    /**
     * 判断 OCR 识别功能是否开启
     */
    public Boolean isOcrEnabled() {
        // 调用远程接口查询 OCR 相关配置
        SysParamDTO data = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString()).getData();

        // 如果查询结果为空，则默认 OCR 关闭
        if (data == null || StringUtils.isBlank(data.getValue())) {
            return false;
        }

        // 解析 JSON 获取 OCR 配置
        JSONObject jsonObject = JSONObject.parseObject(data.getValue());
        Integer ocrConfigStatus = jsonObject.getInteger("ocrConfigStatus");

        // 判断 OCR 开关是否开启
        return ocrConfigStatus != null && ocrConfigStatus.equals(IcmsConstants.STATE_OPEN);
    }

}
