package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EditBusiAttrDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.framework.common.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiSetBusiAttrServiceTest {

    @Mock
    private OpenApiService openApiService;

    @InjectMocks
    private ApiSetBusiAttrService apiSetBusiAttrService;

    @Test
    void setBusiAttr_ShouldReturnResult() {
        // 准备测试数据
        EditBusiAttrDTO mockDto = mock(EditBusiAttrDTO.class);
        Result mockResult = Result.success();
        when(openApiService.setBusiAttr(mockDto)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiSetBusiAttrService.setBusiAttr(mockDto);

        // 验证结果
        assertNotNull(result);
        verify(openApiService, times(1)).setBusiAttr(mockDto);
    }
}