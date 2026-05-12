package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmPageBaseInfoDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.manager.OpenApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiCaptureServiceTest {

    @Mock
    private OpenApiService openApiService;

    @InjectMocks
    private ApiCaptureService apiCaptureService;

    @Test
    void businessDataService_ShouldReturnDTO() {
        // 准备测试数据
        EcmRootDataDTO mockVo = mock(EcmRootDataDTO.class);
        EcmPageBaseInfoDTO mockDto = mock(EcmPageBaseInfoDTO.class);
        when(openApiService.businessDataService(mockVo, true)).thenReturn(mockDto);

        // 执行测试方法
        EcmPageBaseInfoDTO result = apiCaptureService.businessDataService(mockVo);

        // 验证结果
        assertNotNull(result);
        verify(openApiService, times(1)).businessDataService(mockVo, true);
    }
}