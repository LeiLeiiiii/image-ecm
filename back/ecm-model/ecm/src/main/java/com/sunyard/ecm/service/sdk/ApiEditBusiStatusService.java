package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EditBusiAttrDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WJJ
 * @since: 2025/9/19
 * @Desc: 对外接口-业务状态修改
 */
@Slf4j
@Service
public class ApiEditBusiStatusService {
    @Resource
    private OpenApiService openApiService;

    public Result busiDeblock(EditBusiAttrDTO editBusiAttrDTO) {
        return openApiService.busiDeblock(editBusiAttrDTO);
    }
}
