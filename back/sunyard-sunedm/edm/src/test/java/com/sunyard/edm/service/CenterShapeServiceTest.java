package com.sunyard.edm.service;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunyard.edm.dto.DocBsShapeAcceptDTO;
import com.sunyard.edm.mapper.DocBsShapeInsideUserMapper;
import com.sunyard.framework.common.result.Result;

@ExtendWith(MockitoExtension.class)
class CenterShapeServiceTest {
    @Mock
    private DocBsShapeInsideUserMapper mockDocBsShapeInsideUserMapper;
    @InjectMocks
    private CenterShapeService docBsShapeServiceImplUnderTest;

    @Test
    void testQueryAccept_DocBsShapeInsideUserMapperReturnsNoItems() {
        when(mockDocBsShapeInsideUserMapper.searchListByShapeId(0L))
                .thenReturn(Collections.emptyList());

        final Result<List<DocBsShapeAcceptDTO>> result = docBsShapeServiceImplUnderTest
                .queryAccept(0L);
    }
}
