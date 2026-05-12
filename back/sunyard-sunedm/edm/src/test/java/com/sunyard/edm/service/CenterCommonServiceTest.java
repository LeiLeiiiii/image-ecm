package com.sunyard.edm.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsDocumentTreeMapper;

@ExtendWith(MockitoExtension.class)
class CenterCommonServiceTest {

    @Mock
    private DocBsDocumentMapper docBsDocumentMapper;
    @Mock
    private DocBsDocumentTreeMapper docBsDocumentTreeMapper;

    @InjectMocks
    private CenterCommonService centerCommonService;

}
