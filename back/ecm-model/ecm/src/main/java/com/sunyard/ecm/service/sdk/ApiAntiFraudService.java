package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.FileOcrCallBackDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ApiAntiFraudService {

    @Resource
    private OpenApiService openApiService;
    public Result extractFileTextDup(FileOcrCallBackDTO vo) {
        return openApiService.extractFileTextDup(vo);
    }
}
