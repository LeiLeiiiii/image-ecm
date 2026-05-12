package com.sunyard.ecm.service.sdk;

import com.sunyard.ecm.dto.FileOcrCallBackDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.framework.common.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ApiAntiFraudServiceTest {


    @Mock
    private OpenApiService openApiService;

    @InjectMocks
    private ApiAntiFraudService apiAntiFraudService;

    @Test
    void extractFileTextDup_success_returnResult() {
        // 1. 准备测试数据
        FileOcrCallBackDTO testVo = new FileOcrCallBackDTO();
        // 给 vo 设置一些测试属性（比如 testVo.setFileId("123");）
        Result<String> mockResult = Result.success("测试提取文本");

        // 2. 模拟依赖方法的行为：当调用 openApiService.extractFileTextDup(testVo) 时，返回 mockResult
        when(openApiService.extractFileTextDup(eq(testVo))).thenReturn(mockResult);

        // 3. 执行待测试方法
        Result<?> actualResult = apiAntiFraudService.extractFileTextDup(testVo);

        // 4. 验证结果：返回值与 mock 的结果一致
        assertEquals(mockResult, actualResult);
        // 验证 openApiService 的 extractFileTextDup 方法确实被调用了一次，且参数是 testVo
        verify(openApiService).extractFileTextDup(eq(testVo));
    }
}