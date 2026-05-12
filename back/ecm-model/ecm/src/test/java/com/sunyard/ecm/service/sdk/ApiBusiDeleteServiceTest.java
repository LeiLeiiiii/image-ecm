package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.manager.OpenCaptureService;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.framework.common.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiBusiDeleteServiceTest {

    @Mock
    private OpenCaptureService openCaptureService;

    @InjectMocks
    private ApiBusiDeleteService apiBusiDeleteService;

    @Test
    void deleteFileByBusiOrDoc_ShouldReturnResult() {
        // 准备测试数据
        EcmDelVO mockVo = mock(EcmDelVO.class);
        Result mockResult = Result.success();
        when(openCaptureService.deleteFileByBusiOrDoc(mockVo)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiBusiDeleteService.deleteFileByBusiOrDoc(mockVo);

        // 验证结果
        assertNotNull(result);
        verify(openCaptureService, times(1)).deleteFileByBusiOrDoc(mockVo);
    }
}