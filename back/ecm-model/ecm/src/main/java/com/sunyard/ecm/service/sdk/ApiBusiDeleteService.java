package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.manager.OpenCaptureService;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.framework.common.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author WJJ
 * @since 2025-5-15
 * @desc 影像删除实现类
 */
@Service
public class ApiBusiDeleteService {
    @Resource
    private OpenCaptureService openCaptureService;

    /**
     * 根据业务或者资料删除影像文件
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteFileByBusiOrDoc(EcmDelVO vo) {
        return openCaptureService.deleteFileByBusiOrDoc(vo);
    }
}
