package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmBusiInfoDataDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.vo.QueryDataVO;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WJJ
 * @since: 2025/5/15
 * @Desc: 对外接口-资源获取实现类
 */
@Slf4j
@Service
public class ApiQueryService {
    @Resource
    private OpenApiService openApiService;
    public List<QueryDataVO> queryData(EcmRootDataDTO ecmRootDataDTO) {
        return openApiService.queryData(ecmRootDataDTO);
    }

    public Result statisticsDocFileNUm(EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        return openApiService.statisticsDocFileNUm(ecmBusiInfoDataDTO);
    }

}
