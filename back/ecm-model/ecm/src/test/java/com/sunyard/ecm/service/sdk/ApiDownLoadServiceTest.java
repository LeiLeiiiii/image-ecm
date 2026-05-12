package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmDownloadFileDTO;
import com.sunyard.ecm.manager.CaptureService;
import com.sunyard.framework.common.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiDownLoadServiceTest {

    @Mock
    private CaptureService captureService;

    @InjectMocks
    private ApiDownLoadService apiDownLoadService;

    @Test
    void getFileInfoByBusiOrDoc_ShouldReturnResult() {
        // 准备测试数据
        EcmDownloadFileDTO mockDto = mock(EcmDownloadFileDTO.class);
        Result mockResult = Result.success();
        when(captureService.getFileInfoByBusiOrDoc(mockDto)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiDownLoadService.getFileInfoByBusiOrDoc(mockDto);

        // 验证结果
        assertNotNull(result);
        verify(captureService, times(1)).getFileInfoByBusiOrDoc(mockDto);
    }
}