package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.EcmBusiInfoDataDTO;
import com.sunyard.ecm.dto.QueryBusiDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.vo.QueryBusiInfoVO;
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
public class ApiQueryBusiServiceTest {

    @Mock
    private OpenApiService openApiService;

    @InjectMocks
    private ApiQueryBusiService apiQueryBusiService;

    @Test
    void queryBusi_ShouldReturnList() {
        // 准备测试数据
        QueryBusiDTO mockDto = mock(QueryBusiDTO.class);
        List<QueryBusiInfoVO> mockList = Collections.singletonList(mock(QueryBusiInfoVO.class));
        when(openApiService.queryBusi(mockDto)).thenReturn(mockList);

        // 执行测试方法
        List<QueryBusiInfoVO> result = apiQueryBusiService.queryBusi(mockDto);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(openApiService, times(1)).queryBusi(mockDto);
    }

    @Test
    void ecmBusiInfoCheck_ShouldReturnResult() {
        // 准备测试数据
        EcmBusiInfoDataDTO mockDto = mock(EcmBusiInfoDataDTO.class);
        Result mockResult = Result.success();
        when(openApiService.ecmBusiInfoCheck(mockDto)).thenReturn(mockResult);

        // 执行测试方法
        Result result = apiQueryBusiService.ecmBusiInfoCheck(mockDto);

        // 验证结果
        assertNotNull(result);
        verify(openApiService, times(1)).ecmBusiInfoCheck(mockDto);
    }
}