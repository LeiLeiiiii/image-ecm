package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EditBusiAttrDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WJJ
 * @since: 2025/5/15
 * @Desc: 对外接口-属性回写实现类
 */
@Slf4j
@Service
public class ApiSetBusiAttrService {
    @Resource
    private OpenApiService openApiService;

    public Result setBusiAttr(EditBusiAttrDTO editBusiAttrDTO) {
        return openApiService.setBusiAttr(editBusiAttrDTO);
    }
}
