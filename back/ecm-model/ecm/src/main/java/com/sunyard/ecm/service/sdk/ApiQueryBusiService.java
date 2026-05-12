package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmBusiInfoDataDTO;
import com.sunyard.ecm.dto.QueryBusiDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.vo.QueryBusiInfoVO;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WJJ
 * @since: 2025/9/19
 * @Desc: 对外接口-业务状态修改
 */
@Slf4j
@Service
public class ApiQueryBusiService {
    @Resource
    private OpenApiService openApiService;

    public List<QueryBusiInfoVO> queryBusi(QueryBusiDTO queryBusiDTO) {
        return openApiService.queryBusi(queryBusiDTO);
    }

    public Result ecmBusiInfoCheck(EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        return openApiService.ecmBusiInfoCheck(ecmBusiInfoDataDTO);
    }
}
