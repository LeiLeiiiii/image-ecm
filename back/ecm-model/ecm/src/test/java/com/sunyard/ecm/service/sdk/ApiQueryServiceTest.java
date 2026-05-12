package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmBusiInfoDataDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.vo.QueryDataVO;
import com.sunyard.framework.common.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiQueryServiceTest {

    @Mock
    private OpenApiService openApiService;

    @InjectMocks
    private ApiQueryService apiQueryService;

    @Test
    void queryData_ShouldReturnList() {
        // 准备测试数据
        EcmRootDataDTO mockDto = mock(EcmRootDataDTO.class);
        List<QueryDataVO> mockList = Collections.singletonList(mock(QueryDataVO.class));
        when(openApiService.queryData(mockDto)).thenReturn(mockList);

        // 执行测试方法
        List<QueryDataVO> result = apiQueryService.queryData(mockDto);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(openApiService, times(1)).queryData(mockDto);
    }

    @Test
    void statisticsDocFileNUm_ShouldReturnResult() {
        // 准备测试数据
        EcmBusiInfoDataDTO mockDto = mock(EcmBusiInfoDataDTO.class);
        Result mockResult = Result.success();
        when(openApiService.statisticsDocFileNUm(mockDto)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiQueryService.statisticsDocFileNUm(mockDto);

        // 验证结果
        assertNotNull(result);
        verify(openApiService, times(1)).statisticsDocFileNUm(mockDto);
    }
}