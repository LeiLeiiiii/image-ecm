package com.sunyard.edm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunyard.edm.dto.DocBsHomeDTO;
import com.sunyard.edm.dto.DocBsMessageDTO;
import com.sunyard.edm.mapper.DocBsMessageMapper;
import com.sunyard.edm.mapper.DocBsShapeInsideUserMapper;
import com.sunyard.edm.mapper.DocBsShapeMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.framework.common.result.Result;

@ExtendWith(MockitoExtension.class)
class WorkbenchServiceTest {

    @InjectMocks
    private WorkbenchService workbenchService;
    @Mock
    private DocSysTeamUserMapper mockDocSysTeamUserMapper;
    @Mock
    private DocBsShapeMapper mockDocBsShapeMapper;
    @Mock
    private DocBsShapeInsideUserMapper mockDocBsShapeInsideUserMapper;
    @Mock
    private DocBsMessageMapper mockDocBsMessageMapper;

    @Test
    void testQueryMessage() {
        // Setup
        // Configure DocBsMessageMapper.searchListHome(...).
        final DocBsHomeDTO docBsHomeDTO = new DocBsHomeDTO();
        docBsHomeDTO.setId(0L);
        docBsHomeDTO.setContent("content");
        docBsHomeDTO.setIsRead(0);
        docBsHomeDTO.setTime(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        final List<DocBsHomeDTO> docBsHomeDTOS = Collections.singletonList(docBsHomeDTO);
        when(mockDocBsMessageMapper.searchListHome(any(Long.class)))
                .thenReturn(docBsHomeDTOS);

        // Run the test
        final Result<List<DocBsHomeDTO>> result = workbenchService.queryMessage(0L);

        // Verify the results
    }

    @Test
    void testQueryMessage_DocBsMessageMapperReturnsNoItems() {
        // Setup
        when(mockDocBsMessageMapper.searchListHome(any(Long.class)))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<DocBsHomeDTO>> result = workbenchService.queryMessage(0L);

        // Verify the results
    }

    @Test
    void testQueryMessageAll() {
        // Setup
        // Configure DocBsMessageMapper.searchExtend(...).
        final DocBsMessageDTO docBsMessageDTO = new DocBsMessageDTO();
        docBsMessageDTO.setType("type");
        docBsMessageDTO.setIsRead(0);
        docBsMessageDTO.setTitle("title");
        docBsMessageDTO.setContent("content");
        docBsMessageDTO.setUrl("url");
        docBsMessageDTO.setDocumentHouseId(0L);
        docBsMessageDTO.setLastFolderId(0L);
        docBsMessageDTO.setTime(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        final List<DocBsMessageDTO> docBsMessageDTOS = Collections.singletonList(docBsMessageDTO);

        // Verify the results
    }

    @Test
    void testQueryMessageAll_DocBsMessageMapperReturnsNoItems() {

        // Verify the results
    }
}
