package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.QueryBusiDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import com.sunyard.framework.common.result.Result;
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
public class ApiBusiCopyService {
    @Resource
    private OpenApiService openApiService;

    /**
     *影像复制
     */
    public Result busiDocDuplicate(BusiDocDuplicateVO busiDocDuplicateVo) {
        return openApiService.busiDocDuplicate(busiDocDuplicateVo);
    }

    /**
     *归档
     */
    public Result busiArchive(QueryBusiDTO queryBusiDTO) {
        return openApiService.busiArchive(queryBusiDTO);
    }
}
