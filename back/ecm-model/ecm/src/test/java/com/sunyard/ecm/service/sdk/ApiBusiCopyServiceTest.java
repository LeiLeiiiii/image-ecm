package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.QueryBusiDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import com.sunyard.framework.common.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiBusiCopyServiceTest {

    @Mock
    private OpenApiService openApiService;

    @InjectMocks
    private ApiBusiCopyService apiBusiCopyService;

    @Test
    void busiDocDuplicate_ShouldReturnResult() {
        // 准备测试数据
        BusiDocDuplicateVO mockVo = mock(BusiDocDuplicateVO.class);
        Result mockResult = Result.success();
        when(openApiService.busiDocDuplicate(mockVo)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiBusiCopyService.busiDocDuplicate(mockVo);

        // 验证结果
        assertNotNull(result);
        verify(openApiService, times(1)).busiDocDuplicate(mockVo);
    }

    @Test
    void busiArchive_ShouldReturnResult() {
        // 准备测试数据
        QueryBusiDTO mockDto = mock(QueryBusiDTO.class);
        Result mockResult = Result.success();
        when(openApiService.busiArchive(mockDto)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiBusiCopyService.busiArchive(mockDto);

        // 验证结果
        assertNotNull(result);
        verify(openApiService, times(1)).busiArchive(mockDto);
    }
}