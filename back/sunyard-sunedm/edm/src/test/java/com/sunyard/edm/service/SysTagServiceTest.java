package com.sunyard.edm.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunyard.edm.mapper.DocBsTagDocumentMapper;
import com.sunyard.edm.mapper.DocSysTagMapper;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;

@ExtendWith(MockitoExtension.class)
class SysTagServiceTest {

    @Mock
    private DocSysTagMapper mockTagMapper;
    @Mock
    private DocBsTagDocumentMapper mockTagDocumentMapper;
    @Mock
    private SnowflakeUtils mockSnowflakeUtil;

    @InjectMocks
    private SysTagService SysTagService;
}
