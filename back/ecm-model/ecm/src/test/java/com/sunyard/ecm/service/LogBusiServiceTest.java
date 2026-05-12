package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.SysBusiLogDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmFileBatchOperationMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmFileBatchOperation;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.SearchBusiLogVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.spring.annotation.MapperScan;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@MapperScan
@ExtendWith(MockitoExtension.class)
class LogBusiServiceTest {

    @Mock
    private SnowflakeUtils snowflakeUtil;

    @Mock
    private EcmAppDefMapper ecmAppDefMapper;

    @Mock
    private EcmFileBatchOperationMapper ecmFileBatchOperationMapper;

    @Mock
    private SysBusiLogMapper busiLogMapper;

    @Mock
    private EcmBusiInfoMapper busiInfoMapper;

    @Mock
    private EcmAppDefMapper appDefMapper;

    @Mock
    private EcmFileInfoMapper fileInfoMapper;

    @Mock
    private EcmBusiDocMapper ecmBusiDocMapper;

    @Mock
    private SysBusiLogMapper sysBusiLogMapper;

    @Mock
    private BusiCacheService busiCacheService;

    @Mock
    private SqlSessionFactory sqlSessionFactory;
    @Mock
    private SqlSession sqlSession;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LogBusiService logBusiService;

    private AccountTokenExtendDTO token;
    private SearchBusiLogVO searchBusiLogVO;

    @BeforeEach
    void setUp() {
        token = new AccountTokenExtendDTO();
        token.setUsername("testUser");
        token.setName("Test User");

        searchBusiLogVO = new SearchBusiLogVO();
        searchBusiLogVO.setPageNum(1);
        searchBusiLogVO.setPageSize(10);
    }

    @Test
    void testQueryBusiLog_NoAppType() {
        // 准备数据
        EcmBusiLog log1 = new EcmBusiLog();
        log1.setId(1L);
        EcmBusiLog log2 = new EcmBusiLog();
        log2.setId(2L);
        List<EcmBusiLog> logs = Arrays.asList(log1, log2);

        // 模拟行为
        when(sysBusiLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(logs);

        // 执行测试
        Object result = logBusiService.queryBusiLog(searchBusiLogVO);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof PageInfo);
        PageInfo pageInfo = (PageInfo) result;
        assertEquals(2, pageInfo.getList().size());
        assertTrue(pageInfo.getList().get(0) instanceof SysBusiLogDTO);

        // 验证调用
        verify(sysBusiLogMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testQueryBusiLog_WithAppType() {
        // 准备数据
        searchBusiLogVO.setAppType(Arrays.asList("APP1", "APP2"));

        EcmBusiLog log1 = new EcmBusiLog();
        log1.setId(1L);
        List<EcmBusiLog> logs = Collections.singletonList(log1);

        EcmAppDef appDef1 = new EcmAppDef();
        appDef1.setAppCode("APP1");
        EcmAppDef appDef2 = new EcmAppDef();
        appDef2.setAppCode("APP2");
        List<EcmAppDef> appDefs = Arrays.asList(appDef1, appDef2);


        // 执行测试
        Object result = logBusiService.queryBusiLog(searchBusiLogVO);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof PageInfo);

        // 验证调用
        verify(ecmAppDefMapper, times(2)).selectList(any(LambdaQueryWrapper.class));
        verify(sysBusiLogMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testExportBusiLog_WithLogIds() throws IOException {
        // 准备数据
        List<Long> logIds = Arrays.asList(1L, 2L);

        EcmBusiLog log1 = new EcmBusiLog();
        log1.setId(1L);
        EcmBusiLog log2 = new EcmBusiLog();
        log2.setId(2L);
        List<EcmBusiLog> logs = Arrays.asList(log1, log2);

        // 模拟行为
        when(sysBusiLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(logs);

        // 执行测试
        logBusiService.exportBusiLog(response, logIds);

        // 验证调用
        verify(sysBusiLogMapper).selectList(any(LambdaQueryWrapper.class));
        verify(response).setContentType("application/vnd.ms-excel");
        verify(response).setCharacterEncoding("utf-8");
    }

    @Test
    void testExportBusiLog_WithoutLogIds() throws IOException {
        // 准备数据
        EcmBusiLog log1 = new EcmBusiLog();
        log1.setId(1L);
        EcmBusiLog log2 = new EcmBusiLog();
        log2.setId(2L);
        List<EcmBusiLog> logs = Arrays.asList(log1, log2);

        // 模拟行为
        when(sysBusiLogMapper.selectList(isNull())).thenReturn(logs);

        // 执行测试
        logBusiService.exportBusiLog(response, null);

        // 验证调用
        verify(sysBusiLogMapper).selectList(isNull());
        verify(response).setContentType("application/vnd.ms-excel");
        verify(response).setCharacterEncoding("utf-8");
    }

    @Test
    void testExportBusiLog_IOException() throws IOException {
        // 准备数据
        EcmBusiLog log1 = new EcmBusiLog();
        log1.setId(1L);
        List<EcmBusiLog> logs = Collections.singletonList(log1);

        // 模拟行为
        when(sysBusiLogMapper.selectList(isNull())).thenReturn(logs);
        doThrow(new IOException("Test IO Exception")).when(response).getOutputStream();

        // 执行测试并验证异常
        assertThrows(IOException.class, () -> {
            logBusiService.exportBusiLog(response, null);
        });

        // 验证调用
        verify(sysBusiLogMapper).selectList(isNull());
    }

    @Test
    void testAddEcmLog_PrintType() {
        // 准备数据
        Integer type = 0; // 打印操作
        List<Long> fileIds = Arrays.asList(1L, 2L);
        Long busiId = 100L;

        EcmBusiInfoRedisDTO busiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        busiInfoRedisDTO.setBusiNo("BUSI001");
        busiInfoRedisDTO.setAppCode("ceshifj");
        busiInfoRedisDTO.setOrgCode("ORG001");

        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppName("Test App");

        // 模拟行为
        when(busiCacheService.getEcmBusiInfoRedisDTO(token, busiId)).thenReturn(busiInfoRedisDTO);
        when(ecmAppDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDef);
        when(appDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDef);
//        when(busiLogMapper.insert(any(EcmBusiLog.class))).thenReturn(1);
        when(busiLogMapper.insert(any(EcmBusiLog.class))).thenAnswer(invocation->{
            EcmBusiLog insertedLog = invocation.getArgument(0);
            insertedLog.setId(100l);
            return 1;
        });

        when(snowflakeUtil.nextId()).thenReturn(1L, 2L);
        SqlSession mockSession = mock(org.apache.ibatis.session.SqlSession.class);
        when(mockSession.getMapper(EcmFileBatchOperationMapper.class)).thenReturn(ecmFileBatchOperationMapper);
        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mockSession);

        // 执行测试
        Result result = logBusiService.addEcmLog(type, fileIds, busiId, token);

        // 验证结果
        assertTrue(result.isSucc());
        assertEquals(true, result.getData());

        // 验证调用
        verify(busiCacheService).getEcmBusiInfoRedisDTO(token, busiId);
        verify(ecmAppDefMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(busiLogMapper).insert(any(EcmBusiLog.class));
        verify(ecmFileBatchOperationMapper, times(2)).insert(any(EcmFileBatchOperation.class));
    }

    @Test
    void testAddEcmLog_DownloadType() {
        // 准备数据
        Integer type = 1; // 下载操作
        List<Long> fileIds = Arrays.asList(1L, 2L);
        Long busiId = 100L;

        EcmBusiInfoRedisDTO busiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        busiInfoRedisDTO.setBusiNo("BUSI001");
        busiInfoRedisDTO.setAppCode("ceshifj");
        busiInfoRedisDTO.setOrgCode("ORG001");

        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppName("Test App");

        // 模拟行为
        when(busiCacheService.getEcmBusiInfoRedisDTO(token, busiId)).thenReturn(busiInfoRedisDTO);
//        when(ecmAppDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDef);
        when(appDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDef);
        when(busiLogMapper.insert(any(EcmBusiLog.class))).thenAnswer(invocation->{
            EcmBusiLog insertedLog = invocation.getArgument(0);
            insertedLog.setId(100l);
            return 1;
        });
        when(snowflakeUtil.nextId()).thenReturn(1L, 2L);

        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mock(org.apache.ibatis.session.SqlSession.class));

        // 执行测试
        Result result = logBusiService.addEcmLog(type, fileIds, busiId, token);

        // 验证结果
        assertTrue(result.isSucc());
        assertEquals(true, result.getData());

        // 验证调用
        verify(busiCacheService).getEcmBusiInfoRedisDTO(token, busiId);
        verify(busiLogMapper).insert(any(EcmBusiLog.class));
    }

    @Test
    void testAddEcmLog_InvalidType() {
        // 准备数据
        Integer type = 3; // 无效操作类型
        List<Long> fileIds = Arrays.asList(1L, 2L);
        Long busiId = 100L;

        // 执行测试
        Result result = logBusiService.addEcmLog(type, fileIds, busiId, token);

        // 验证结果
        assertFalse(result.isSucc());
        assertEquals(ResultCode.PARAM_ERROR, result.getCode());

        // 验证调用
        verifyNoInteractions(busiCacheService, ecmAppDefMapper, busiLogMapper, ecmFileBatchOperationMapper);
    }

    @Test
    void testSearchLogDetails_MergeOperation() {
        // 准备数据
        Long ecmBusiLogId = 1L;
        String operateContent = "合并文件:2个文件至新业务";

        EcmFileBatchOperation operation1 = new EcmFileBatchOperation();
        operation1.setFileId(1L);
        EcmFileBatchOperation operation2 = new EcmFileBatchOperation();
        operation2.setFileId(2L);
        List<EcmFileBatchOperation> operations = Arrays.asList(operation1, operation2);

        EcmFileInfo fileInfo1 = new EcmFileInfo();
        fileInfo1.setNewFileName("file1.pdf");
        EcmFileInfo fileInfo2 = new EcmFileInfo();
        fileInfo2.setNewFileName("file2.pdf");

        // 模拟行为
        when(ecmFileBatchOperationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(operations);
        when(fileInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(fileInfo1, fileInfo2);

        // 执行测试
        String result = logBusiService.searchLogDetails(ecmBusiLogId, operateContent, token);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("合并文件"));
        assertTrue(result.contains("file1.pdf"));
        assertTrue(result.contains("file2.pdf"));
        assertTrue(result.contains("新业务"));

        // 验证调用
        verify(ecmFileBatchOperationMapper).selectList(any(LambdaQueryWrapper.class));
        verify(fileInfoMapper, times(2)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testSearchLogDetails_ReuseOperation() {
        // 准备数据
        Long ecmBusiLogId = 1L;
        String operateContent = "复用文件:2个文件至新业务";

        EcmFileBatchOperation operation1 = new EcmFileBatchOperation();
        operation1.setFileId(1L);
        EcmFileBatchOperation operation2 = new EcmFileBatchOperation();
        operation2.setFileId(2L);
        List<EcmFileBatchOperation> operations = Arrays.asList(operation1, operation2);

        EcmFileInfo fileInfo1 = new EcmFileInfo();
        fileInfo1.setFileId(1L);
        fileInfo1.setBusiId(100L);
        fileInfo1.setDocCode("DOC001");
        fileInfo1.setNewFileName("file1.pdf");

        EcmFileInfo fileInfo2 = new EcmFileInfo();
        fileInfo2.setFileId(2L);
        fileInfo2.setBusiId(100L);
        fileInfo2.setDocCode("DOC002");
        fileInfo2.setNewFileName("file2.pdf");

        List<EcmFileInfo> fileInfos = Arrays.asList(fileInfo1, fileInfo2);

        EcmBusiInfoRedisDTO busiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        busiInfoRedisDTO.setAppTypeName("Test App");

        EcmBusiDocRedisDTO docRedisDTO1 = new EcmBusiDocRedisDTO();
        docRedisDTO1.setDocCode("DOC001");
        docRedisDTO1.setDocName("Document 1");

        EcmBusiDocRedisDTO docRedisDTO2 = new EcmBusiDocRedisDTO();
        docRedisDTO2.setDocCode("DOC002");
        docRedisDTO2.setDocName("Document 2");

        List<EcmBusiDocRedisDTO> docRedisDTOs = Arrays.asList(docRedisDTO1, docRedisDTO2);
        busiInfoRedisDTO.setEcmBusiDocRedisDTOS(docRedisDTOs);

        // 模拟行为
        when(ecmFileBatchOperationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(operations);
        when(fileInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(fileInfos);
        when(busiCacheService.getEcmBusiInfoRedisDTO(eq(token), eq(100L))).thenReturn(busiInfoRedisDTO);

        // 执行测试
        String result = logBusiService.searchLogDetails(ecmBusiLogId, operateContent, token);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("复用文件"));
        assertTrue(result.contains("Test App"));
        assertTrue(result.contains("Document 1"));
        assertTrue(result.contains("Document 2"));
        assertTrue(result.contains("file1.pdf"));
        assertTrue(result.contains("file2.pdf"));
        assertTrue(result.contains("新业务"));

        // 验证调用
        verify(ecmFileBatchOperationMapper).selectList(any(LambdaQueryWrapper.class));
        verify(fileInfoMapper).selectList(any(LambdaQueryWrapper.class));
        verify(busiCacheService).getEcmBusiInfoRedisDTO(eq(token), eq(100L));
    }

    @Test
    void testAddLog_Success() {
        // 准备数据
        Long busiId = 100L;
        String operateContent = "测试操作";
        String errorInfo = "";

        EcmBusiInfo busiInfo = new EcmBusiInfo();
        busiInfo.setBusiNo("BUSI001");
        busiInfo.setAppCode("ceshifj");
        busiInfo.setOrgCode("ORG001");

        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppCode("ceshifj");
        appDef.setAppName("Test App");

        // 模拟行为
        when(busiInfoMapper.selectByIdWithDeleted(busiId)).thenReturn(busiInfo);
        when(appDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDef);
        when(busiLogMapper.insert(any(EcmBusiLog.class))).thenReturn(1);

        // 执行测试
        Result result = logBusiService.addLog(busiId, operateContent, errorInfo, token, BusiLogConstants.OPERATION_TYPE_ZERO);

        // 验证结果
        assertTrue(result.isSucc());

        // 验证调用
        verify(busiInfoMapper).selectByIdWithDeleted(busiId);
        verify(appDefMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(busiLogMapper).insert(any(EcmBusiLog.class));
    }

    @Test
    void testAddLog_Failure() {
        // 准备数据
        Long busiId = 100L;
        String operateContent = "测试操作";
        String errorInfo = "";

        EcmBusiInfo busiInfo = new EcmBusiInfo();
        busiInfo.setBusiNo("BUSI001");
        busiInfo.setAppCode("ceshifj");
        busiInfo.setOrgCode("ORG001");

        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppCode("ceshifj");
        appDef.setAppName("Test App");

        // 模拟行为
        when(busiInfoMapper.selectByIdWithDeleted(busiId)).thenReturn(busiInfo);
        when(appDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDef);
        when(busiLogMapper.insert(any(EcmBusiLog.class))).thenReturn(0);

        // 执行测试
        Result result = logBusiService.addLog(busiId, operateContent, errorInfo, token,BusiLogConstants.OPERATION_TYPE_ZERO);

        // 验证结果
        assertFalse(result.isSucc());
        assertEquals(411, result.getCode());

        // 验证调用
        verify(busiInfoMapper).selectByIdWithDeleted(busiId);
        verify(appDefMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(busiLogMapper).insert(any(EcmBusiLog.class));
    }

    @Test
    void testAddLog_NullBusiId() {
        // 准备数据
        Long busiId = null;
        String operateContent = "测试操作";
        String errorInfo = "";

        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            logBusiService.addLog(busiId, operateContent, errorInfo, token,BusiLogConstants.OPERATION_TYPE_ZERO);
        });

        // 验证调用
        verifyNoInteractions(busiInfoMapper, appDefMapper, busiLogMapper);
    }
}