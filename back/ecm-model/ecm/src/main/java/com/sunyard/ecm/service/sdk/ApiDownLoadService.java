package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmDownloadFileDTO;
import com.sunyard.ecm.manager.CaptureService;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WJJ
 * @since: 2025/5/15
 * @Desc: 对外接口-文件下载实现类
 */
@Service
@Slf4j
public class ApiDownLoadService {
    @Resource
    private CaptureService captureService;

    public Result getFileInfoByBusiOrDoc(EcmDownloadFileDTO ecmDownloadFileDTO) {
        return captureService.getFileInfoByBusiOrDoc(ecmDownloadFileDTO);
    }
}
