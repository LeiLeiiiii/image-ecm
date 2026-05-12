package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmPageBaseInfoDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.manager.OpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author： WJJ
 * @Description：影像复用
 * @create： 2025/5/15
 */
@Slf4j
@Service
public class ApiCaptureService {
    @Resource
    private OpenApiService openApiService;

    /**
     *影像复制
     */
    public EcmPageBaseInfoDTO businessDataService(EcmRootDataDTO vo) {
        return openApiService.businessDataService(vo, true);
    }
}
