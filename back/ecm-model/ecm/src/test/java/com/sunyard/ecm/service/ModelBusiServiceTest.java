package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import lombok.var;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"fileIndex=ecm_file_test"})
public class ModelBusiServiceTest{
    @Mock
    private EcmAsyncTaskMapper ecmAsyncTaskMapper;
    @Mock
    private EsEcmFileMapper esEcmFileMapper;
    @InjectMocks
    private ModelBusiService modelBusiService;

    private final String testAppCode = "TEST_APP_CODE";

    private final Integer testRightVer = 1;

    @Mock
    private EcmAppDocRelMapper ecmAppDocRelMapper;

    @Mock
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;

    @Mock
    private EcmDocDefMapper ecmDocDefMapper;

    @Test
    void searchOldRelevanceInformation1ShouldReturnEmptyListWhenNoAppDocRelsFound() {
        // Arrange
        when(ecmAppDocRelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<EcmDocTreeDTO> result = modelBusiService.searchOldRelevanceInformation1(testAppCode, testRightVer);

        // Assert
        assertTrue(result.isEmpty());
        verify(ecmAppDocRelMapper).selectList(any(LambdaQueryWrapper.class));
        verifyNoInteractions(ecmDocDefRelVerMapper, ecmDocDefMapper);
    }

    @Test
    void searchOldRelevanceInformation1ShouldReturnTreeStructureWhenDataExists() {
        // Arrange
        // Mock app doc rels
        EcmAppDocRel ecmAppDocRel = new EcmAppDocRel();
        ecmAppDocRel.setAppCode(testAppCode);
        ecmAppDocRel.setDocCode("DOC1");
        ecmAppDocRel.setDocSort(1f);
        EcmAppDocRel ecmAppDocRel1 = new EcmAppDocRel();
        ecmAppDocRel1.setAppCode(testAppCode);
        ecmAppDocRel1.setDocCode("DOC2");
        ecmAppDocRel1.setDocSort(2f);
        List<EcmAppDocRel> appDocRels = Arrays.asList(ecmAppDocRel,ecmAppDocRel1);
        when(ecmAppDocRelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(appDocRels);

        // Mock doc def rel vers
        EcmDocDefRelVer ecmDocDefRelVer = new EcmDocDefRelVer();
        ecmDocDefRelVer.setParent("0");
        ecmDocDefRelVer.setDocSort(1f);
        ecmDocDefRelVer.setDocCode("DOC1");ecmDocDefRelVer.setAppCode(testAppCode);ecmDocDefRelVer.setRightVer(testRightVer);
        EcmDocDefRelVer ecmDocDefRelVer1 = new EcmDocDefRelVer();
        ecmDocDefRelVer1.setParent("0");
        ecmDocDefRelVer1.setDocSort(2f);
        ecmDocDefRelVer1.setDocCode("DOC2");ecmDocDefRelVer1.setAppCode(testAppCode);ecmDocDefRelVer1.setRightVer(testRightVer);
        List<EcmDocDefRelVer> docDefRelVers = Arrays.asList(ecmDocDefRelVer,ecmDocDefRelVer1);
        when(ecmDocDefRelVerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(docDefRelVers);

        // Mock doc defs
        EcmDocDef ecmDocDef = new EcmDocDef();
        ecmDocDef.setParent("0");
        ecmDocDef.setDocCode("DOC1");ecmDocDef.setIsParent(1);ecmDocDef.setIsAutoClassified(0);ecmDocDef.setAutoClassificationId("CLASS1");
        ecmDocDef.setDocSort(1f);
        EcmDocDef ecmDocDef1 = new EcmDocDef();
        ecmDocDef1.setParent("0");
        ecmDocDef1.setDocCode("DOC2");ecmDocDef1.setIsParent(0);ecmDocDef1.setIsAutoClassified(1);ecmDocDef1.setAutoClassificationId("CLASS2");
        ecmDocDef1.setDocSort(2f);
        List<EcmDocDef> docDefs = Arrays.asList(ecmDocDef,ecmDocDef1);
        when(ecmDocDefMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(docDefs);

        // Mock copy list properties
        EcmDocDefDTO ecmDocDefDTO = new EcmDocDefDTO();
        ecmDocDefDTO.setDocCode("DOC1");
        ecmDocDefDTO.setIsParent(1);ecmDocDefDTO.setIsAutoClassified(0);ecmDocDefDTO.setAutoClassificationId("CLASS1");ecmDocDefDTO.setDocSort(1f);
        EcmDocDefDTO ecmDocDefDTO1 = new EcmDocDefDTO();
        ecmDocDefDTO1.setDocCode("DOC2");ecmDocDefDTO1.setIsParent(0);ecmDocDefDTO1.setIsAutoClassified(1);ecmDocDefDTO1.setAutoClassificationId("CLASS2");
        ecmDocDefDTO1.setDocSort(2f);
        List<EcmDocDefDTO> docDefDTOs = Arrays.asList(ecmDocDefDTO,ecmDocDefDTO1);
        try (var mockedStatic = mockStatic(PageCopyListUtils.class)) {
            mockedStatic.when(() -> PageCopyListUtils.copyListProperties(docDefRelVers, EcmDocDefDTO.class))
                    .thenReturn(docDefDTOs);

            // Act
            List<EcmDocTreeDTO> result = modelBusiService.searchOldRelevanceInformation1(testAppCode, testRightVer);


            // Verify basic structure
            Map<String, List<EcmDocTreeDTO>> groupedByParent = result.stream()
                    .collect(Collectors.groupingBy(EcmDocTreeDTO::getParent));

            // Verify properties
            List<EcmDocTreeDTO> rootNodes = groupedByParent.get("0");
            rootNodes.forEach(node -> {
                assertTrue(node.getDocCode().equals("DOC1") || node.getDocCode().equals("DOC2"));
                assertEquals(node.getIsParent(), node.getDocCode().equals("DOC1") ? 1 : 0);
            });

            // Verify interactions
            verify(ecmAppDocRelMapper).selectList(any(LambdaQueryWrapper.class));
            verify(ecmDocDefRelVerMapper).selectList(any(LambdaQueryWrapper.class));
            verify(ecmDocDefMapper).selectList(any(LambdaQueryWrapper.class));
//            mockedStatic.verify(() -> PageCopyListUtils.copyListProperties(docDefRelVers, EcmDocDefDTO.class));
        }
    }

    @Test
    void searchOldRelevanceInformation1ShouldThrowExceptionWhenAppCodeIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelBusiService.searchOldRelevanceInformation1(null, testRightVer);
        });

        verifyNoInteractions(ecmAppDocRelMapper, ecmDocDefRelVerMapper, ecmDocDefMapper);
    }

    @Test
    void searchOldRelevanceInformation1ShouldHandleEmptyDocDefRelVers() {
        // Arrange
        EcmAppDocRel ecmAppDocRel = new EcmAppDocRel();
        ecmAppDocRel.setAppCode(testAppCode);ecmAppDocRel.setDocCode("DOC1");ecmAppDocRel.setDocSort(1f);
        List<EcmAppDocRel> appDocRels = Arrays.asList(
                ecmAppDocRel
        );
        when(ecmAppDocRelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(appDocRels);

        // Mock empty doc def rel vers
        when(ecmDocDefRelVerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<EcmDocTreeDTO> result = modelBusiService.searchOldRelevanceInformation1(testAppCode, testRightVer);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(ecmAppDocRelMapper).selectList(any(LambdaQueryWrapper.class));
        verify(ecmDocDefRelVerMapper).selectList(any(LambdaQueryWrapper.class));
    }



    @Test
    void getProcessingStatus_ShouldReturnDefaultStatus_WhenNoAsyncTaskFound() {
        // Arrange
        Long fileId = 123L;
        when(ecmAsyncTaskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        Result<List<Map<String, Object>>> result = modelBusiService.getProcessingStatus(fileId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSucc());
        List<Map<String, Object>> statusList = result.getData();
        assertEquals(5, statusList.size());

        // Verify default status is set (IcmsConstants.ASYNC_TASK_STATUS_INIT)
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(0), statusList.get(0).get("status"));
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(1), statusList.get(1).get("status"));
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(2), statusList.get(2).get("status"));
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(3), statusList.get(3).get("status"));
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(5), statusList.get(4).get("status"));

        verify(ecmAsyncTaskMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void getProcessingStatus_ShouldReturnTaskStatus_WhenAsyncTaskFound() {
        // Arrange
        Long fileId = 123L;
        String taskType = "ABCDEF"; // 6 characters to cover all status positions
        EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
        ecmAsyncTask.setTaskType(taskType);

        when(ecmAsyncTaskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(ecmAsyncTask);

        // Act
        Result<List<Map<String, Object>>> result = modelBusiService.getProcessingStatus(fileId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSucc());
        List<Map<String, Object>> statusList = result.getData();
        assertEquals(5, statusList.size());

        // Verify status is taken from taskType
        assertEquals('A', statusList.get(0).get("status")); // type 1
        assertEquals('B', statusList.get(1).get("status")); // type 2
        assertEquals('C', statusList.get(2).get("status")); // type 3
        assertEquals('D', statusList.get(3).get("status")); // type 4
        assertEquals('F', statusList.get(4).get("status")); // type 6 (skipping position 4)

        verify(ecmAsyncTaskMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void getProcessingStatus_ShouldUseDefault_WhenTaskTypeIsShort() {
        // Arrange
        Long fileId = 123L;
        String taskType = "0000001"; // less than 6 characters
        EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
        ecmAsyncTask.setTaskType(taskType);

        when(ecmAsyncTaskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(ecmAsyncTask);

        // Act
        Result<List<Map<String, Object>>> result = modelBusiService.getProcessingStatus(fileId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSucc());
        List<Map<String, Object>> statusList = result.getData();
        assertEquals(5, statusList.size());

        // Verify default status is used for missing positions
        assertEquals('0', statusList.get(0).get("status")); // from taskType
        assertEquals('0', statusList.get(1).get("status")); // from taskType
        assertEquals('0', statusList.get(2).get("status")); // from taskType
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(3), statusList.get(3).get("status")); // default
        assertEquals(IcmsConstants.ASYNC_TASK_STATUS_INIT.charAt(5), statusList.get(4).get("status")); // default

        verify(ecmAsyncTaskMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void getFileExifFromES_ShouldReturnSuccessWithExifData_WhenFileExists() {
        // Arrange
        Long fileId = 123L;
        String exifJson = "{\"width\":1024,\"height\":768}";
        EsEcmFile esEcmFile = new EsEcmFile();
        esEcmFile.setFileId(fileId.toString());
        esEcmFile.setExif(exifJson);

        List<EsEcmFile> esEcmFiles = new ArrayList<>();
        esEcmFiles.add(esEcmFile);

        when(esEcmFileMapper.selectList(any(LambdaEsQueryWrapper.class)))
                .thenReturn(esEcmFiles);

        // Act
        Result<HashMap> result = modelBusiService.getFileExifFromES(fileId);

        // Assert
        assertTrue(result.isSucc());
        HashMap<String, Object> exifMap = result.getData();
        assertEquals(1024, exifMap.get("width"));
        assertEquals(768, exifMap.get("height"));
        verify(esEcmFileMapper).selectList(any(LambdaEsQueryWrapper.class));
    }

    @Test
    void getFileExifFromES_ShouldReturnSuccessWithNull_WhenFileNotExists() {
        // Arrange
        Long fileId = 456L;

        when(esEcmFileMapper.selectList(any(LambdaEsQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        Result<HashMap> result = modelBusiService.getFileExifFromES(fileId);

        // Assert
        assertTrue(result.isSucc());
        assertNull(result.getData());
        verify(esEcmFileMapper).selectList(any(LambdaEsQueryWrapper.class));
    }

    @Test
    void getFileExifFromES_ShouldReturnSuccessWithNull_WhenFileExistsButNoExif() {
        // Arrange
        Long fileId = 789L;
        EsEcmFile esEcmFile = new EsEcmFile();
        esEcmFile.setFileId(fileId.toString());
        esEcmFile.setExif(null);

        List<EsEcmFile> esEcmFiles = new ArrayList<>();
        esEcmFiles.add(esEcmFile);

        when(esEcmFileMapper.selectList(any(LambdaEsQueryWrapper.class)))
                .thenReturn(esEcmFiles);

        // Act
        Result<HashMap> result = modelBusiService.getFileExifFromES(fileId);

        // Assert
        assertTrue(result.isSucc());
        assertNull(result.getData());
        verify(esEcmFileMapper).selectList(any(LambdaEsQueryWrapper.class));
    }

    @Test
    void getFileExifFromES_ShouldReturnSuccessWithEmptyMap_WhenFileExistsButExifIsEmpty() {
        // Arrange
        Long fileId = 1011L;
        EsEcmFile esEcmFile = new EsEcmFile();
        esEcmFile.setFileId(fileId.toString());
        esEcmFile.setExif("{}");

        List<EsEcmFile> esEcmFiles = new ArrayList<>();
        esEcmFiles.add(esEcmFile);

        when(esEcmFileMapper.selectList(any(LambdaEsQueryWrapper.class)))
                .thenReturn(esEcmFiles);

        // Act
        Result<HashMap> result = modelBusiService.getFileExifFromES(fileId);

        // Assert
        assertTrue(result.isSucc());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        verify(esEcmFileMapper).selectList(any(LambdaEsQueryWrapper.class));
    }


}