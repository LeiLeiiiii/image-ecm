package com.sunyard.edm.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunyard.edm.mapper.DocBsCollectionMapper;
import com.sunyard.framework.common.result.Result;

@ExtendWith(MockitoExtension.class)
class CenterCollectionServiceTest {

    @Mock
    private DocBsCollectionMapper docBsCollectionMapper;

    @InjectMocks
    private CenterCollectionService centerCollectionService;

    @Test
    void testCancelCollection() {
        // Setup
        when(docBsCollectionMapper.deleteById(0L)).thenReturn(0);

        // Run the test
        final Result result = centerCollectionService.cancelCollection(0L);

        // Verify the results
        verify(docBsCollectionMapper).deleteById(0L);
    }
}
