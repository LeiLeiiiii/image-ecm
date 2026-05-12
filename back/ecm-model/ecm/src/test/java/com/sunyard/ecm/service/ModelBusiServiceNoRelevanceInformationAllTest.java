package com.sunyard.ecm.service;

import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmAddAppParamsDTO;
import com.sunyard.ecm.dto.ecm.EcmAddAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmStorageQueDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"fileIndex=ecm_file_test"})
public class ModelBusiServiceNoRelevanceInformationAllTest{
    @Mock
    private EcmAppDefMapper ecmAppDefMapper;
    @Mock
    private EcmAppAttrMapper ecmAppAttrMapper;

    private EcmDocDef ecmDocDef;

    private EcmDocDefDTO ecmDocDefDTO;

    @Mock
    private EcmAppDocRelMapper ecmAppDocRelMapper;

    @Mock
    private EcmDocDefMapper ecmDocDefMapper;

    @Mock
    private StaticTreePermissService staticTreePermissService;

    @InjectMocks
    private ModelBusiService modelBusiService;

    private SysParamDTO sysParamDTO;

    private SysStrategyDTO sysStrategyDTO;

    private String appCode;

    private List<EcmStorageQueDTO> equipmentList;

    private List<EcmStorageQueDTO> queueList;

    @Mock
    private ParamApi paramApi;

    @Mock
    private SysStorageService sysStorageService;

    private EcmAppDocRel ecmAppDocRel;
    private List<EcmAppDocRel> mockAppDocRels;
    private List<EcmDocDef> mockDocDefs;
    private List<EcmDocTreeDTO> expectedTreeResult;
    private AccountTokenExtendDTO token;
    private EcmAddAttrDTO ecmAddAttrDTO;
    private List<EcmAppAttr> existingAttrs;
    private List<EcmAppAttr> sourceAttrs;
    private List<EcmAppAttrDTO> attrIdList;
    @BeforeEach
    void setUp() {
        appCode = "testAppCode";
    
        ecmAppDocRel = new EcmAppDocRel();
        ecmAppDocRel.setAppCode(appCode);
        ecmAppDocRel.setDocCode("doc1");
    
        ecmDocDef = new EcmDocDef();
        ecmDocDef.setDocCode("doc1");
        ecmDocDef.setDocName("Test Doc");
        ecmDocDef.setParent("0");
    
        ecmDocDefDTO = new EcmDocDefDTO();
        ecmDocDefDTO.setDocCode("doc1");
        ecmDocDefDTO.setDocName("Test Doc");
        ecmDocDefDTO.setParent("0");


        // Setup mock data
        EcmAppDocRel rel1 = new EcmAppDocRel();
        rel1.setAppCode(appCode);
        rel1.setDocCode("DOC001");
        rel1.setDocSort(1f);

        EcmAppDocRel rel2 = new EcmAppDocRel();
        rel2.setAppCode(appCode);
        rel2.setDocCode("DOC002");
        rel2.setDocSort(2f);

        mockAppDocRels = Arrays.asList(rel1, rel2);

        EcmAppAttrDTO rel3 = new EcmAppAttrDTO();
        rel3.setAppCode(appCode);
        rel3.setAttrCode("DOC002");
        rel3.setAttrName("DOC002");
        EcmAppAttrDTO rel4 = new EcmAppAttrDTO();
        rel4.setAppCode(appCode);
        rel4.setAttrCode("DOC003");
        rel4.setAttrName("DOC003");
        attrIdList = Arrays.asList(rel3, rel4);
        EcmDocDef doc1 = new EcmDocDef();
        doc1.setDocCode("DOC001");
        doc1.setParent("0");

        EcmDocDef doc2 = new EcmDocDef();
        doc2.setDocCode("DOC002");
        doc2.setParent("0");

        mockDocDefs = Arrays.asList(doc1, doc2);

        expectedTreeResult = Collections.singletonList(new EcmDocTreeDTO());

        token = new AccountTokenExtendDTO();
        token.setUsername("testUser");

        ecmAddAttrDTO = new EcmAddAttrDTO();
        ecmAddAttrDTO.setTypeId("TEST_TYPE");

        existingAttrs = new ArrayList<>();
        sourceAttrs = new ArrayList<>();

        sysStrategyDTO = new SysStrategyDTO();

        sysParamDTO = new SysParamDTO();
        sysParamDTO.setValue("{\"someField\":\"testValue\"}");

        equipmentList = new ArrayList<>();
        equipmentList.add(new EcmStorageQueDTO());

        queueList = new ArrayList<>();
        queueList.add(new EcmStorageQueDTO());
    }

    @Test
    void searchNoRelevanceInformationAllShouldReturnCorrectDataWhenDataExists() {
        // Mock data
        List<EcmAppDocRel> appDocRels = Collections.singletonList(ecmAppDocRel);
        List<EcmDocDef> docDefs = Collections.singletonList(ecmDocDef);
        List<EcmDocDefDTO> docDefDTOs = Collections.singletonList(ecmDocDefDTO);
    
        // Mock behavior
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);
        when(ecmDocDefMapper.selectList(null)).thenReturn(docDefs);
    
        // Execute
        Map<String, Object> result = modelBusiService.searchNoRelevanceInformationAll(appCode);
    
        // Verify
        assertNotNull(result);
        assertEquals(1, ((List)result.get("allRelevance")).size());
        verify(ecmAppDocRelMapper).selectList(any());
        verify(ecmDocDefMapper).selectList(null);
        verify(staticTreePermissService, times(1))
            .searchOldRelevanceInformationTreeNew(anyString(), anyString(), anyMap(), anyList(), anyList(), anyMap());
    }

    @Test
    void searchNoRelevanceInformationAllShouldThrowExceptionWhenAppCodeIsNull() {
        assertThrows(IllegalArgumentException.class, 
            () -> modelBusiService.searchNoRelevanceInformationAll(null),
            "业务类型id不能为空");
    }

    @Test
    void searchNoRelevanceInformationAllShouldReturnEmptyMapsWhenNoDataFound() {
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ecmDocDefMapper.selectList(null)).thenReturn(Collections.emptyList());
    
        Map<String, Object> result = modelBusiService.searchNoRelevanceInformationAll(appCode);
    
        assertNotNull(result);
        assertTrue(((List)result.get("noRelevance")).isEmpty());
        assertTrue(((List)result.get("yesRelevance")).isEmpty());
        assertTrue(((List)result.get("allRelevance")).isEmpty());
    
        verify(ecmAppDocRelMapper).selectList(any());
        verify(ecmDocDefMapper).selectList(null);
        verify(staticTreePermissService, times(1))
            .searchOldRelevanceInformationTreeNew(anyString(), anyString(), anyMap(), anyList(), anyList(), anyMap());
    }


    @Test
    void searchOldRelevanceInformation_ShouldThrowException_WhenAppCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            modelBusiService.searchOldRelevanceInformation(null);
        });
    }

    @Test
    void searchOldRelevanceInformation_ShouldReturnEmptyList_WhenNoAppDocRelsFound() {
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<EcmDocTreeDTO> result = modelBusiService.searchOldRelevanceInformation(appCode);

        assertTrue(result.isEmpty());
        verify(ecmAppDocRelMapper).selectList(any());
    }

    @Test
    void searchOldRelevanceInformation_ShouldReturnTree_WhenDataExists() {
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(mockAppDocRels);
        when(ecmDocDefMapper.selectList(null)).thenReturn(mockDocDefs);
        when(staticTreePermissService.searchOldRelevanceInformationTreeNew(
                eq("0"), eq("无"), any(Map.class), any(List.class), any(List.class), any(Map.class))).thenReturn(expectedTreeResult);

        List<EcmDocTreeDTO> result = modelBusiService.searchOldRelevanceInformation(appCode);

        assertFalse(result.isEmpty());
        assertEquals(expectedTreeResult, result);

        verify(ecmAppDocRelMapper).selectList(any());
        verify(ecmDocDefMapper).selectList(null);
        verify(staticTreePermissService).searchOldRelevanceInformationTreeNew(
                eq("0"), eq("无"), any(Map.class), any(List.class), any(List.class), any(Map.class));
    }

    @Test
    void searchOldRelevanceInformation_ShouldHandleEmptyDocDefs() {
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(mockAppDocRels);
        when(ecmDocDefMapper.selectList(null)).thenReturn(new ArrayList<>());

        List<EcmDocTreeDTO> result = modelBusiService.searchOldRelevanceInformation(appCode);

        assertNotNull(result);
        verify(ecmAppDocRelMapper).selectList(any());
        verify(ecmDocDefMapper).selectList(null);
        verify(staticTreePermissService).searchOldRelevanceInformationTreeNew(
                eq("0"), eq("无"), any(Map.class), any(List.class), any(List.class), any(Map.class));
    }




    @Test
    void multiplexAddBusiAttr_shouldReturnErrorWhenDuplicateAttrCodesExist() {
        // Setup test data
        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAttrCode("ATTR1");
        attr1.setAttrName("Attribute1");
        attr1.setIsKey(0);
        attr1.setAppAttrId(1l);

        EcmAppAttr existingAttr = new EcmAppAttr();
        existingAttr.setAttrCode("DOC003");
        existingAttr.setAttrName("ExistingAttribute");
        existingAttr.setAppAttrId(2l);

        ecmAddAttrDTO.setAttrIdList(attrIdList);
        sourceAttrs.add(attr1);
        existingAttrs.add(existingAttr);

        // Mock behavior
        when(ecmAppAttrMapper.selectList(any())).thenReturn(existingAttrs);

        // Execute and verify
        Result result = modelBusiService.multiplexAddBusiAttr(ecmAddAttrDTO, token);
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("复用的业务属性[DOC003]重复"));
    }

    @Test
    void multiplexAddBusiAttr_shouldReturnErrorWhenSourceHasMultiplePrimaryKeys() {
        // Setup test data
        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAttrCode("ATTR1");
        attr1.setIsKey(1);

        EcmAppAttr attr2 = new EcmAppAttr();
        attr2.setAttrCode("ATTR2");
        attr2.setIsKey(1);

        EcmAppAttrDTO rel3 = new EcmAppAttrDTO();
        rel3.setAppCode(appCode);
        rel3.setAttrCode("DOC002");
        rel3.setAttrName("DOC002");
        rel3.setIsKey(1);
        EcmAppAttrDTO rel4 = new EcmAppAttrDTO();
        rel4.setAppCode(appCode);
        rel4.setAttrCode("DOC003");
        rel4.setAttrName("DOC003");
        rel4.setIsKey(1);
        List<EcmAppAttrDTO> list = Arrays.asList(rel3, rel4);
        ecmAddAttrDTO.setAttrIdList(list);
        sourceAttrs.add(attr1);
        sourceAttrs.add(attr2);

        assertThrows(IllegalArgumentException.class,
                () -> modelBusiService.multiplexAddBusiAttr(ecmAddAttrDTO, token),
                "被复用的业务属性不止一个主键，无法复用");
    }

    @Test
    void multiplexAddBusiAttr_shouldReturnErrorWhenBothHavePrimaryKeys() {
        // Setup test data
        EcmAppAttr sourceAttr = new EcmAppAttr();
        sourceAttr.setAttrCode("ATTR1");
        sourceAttr.setIsKey(1);
        sourceAttr.setAppAttrId(1l);

        EcmAppAttr existingAttr = new EcmAppAttr();
        existingAttr.setAttrCode("ATTR2");
        existingAttr.setIsKey(1);
        existingAttr.setAppAttrId(1l);


        EcmAppAttrDTO rel3 = new EcmAppAttrDTO();
        rel3.setAppCode(appCode);
        rel3.setAttrCode("DOC002");
        rel3.setAttrName("DOC002");
        rel3.setIsKey(1);
        EcmAppAttrDTO rel4 = new EcmAppAttrDTO();
        rel4.setAppCode(appCode);
        rel4.setAttrCode("DOC003");
        rel4.setAttrName("DOC003");
        rel4.setIsKey(0);
        List<EcmAppAttrDTO> list = Arrays.asList(rel3, rel4);
        ecmAddAttrDTO.setAttrIdList(list);
        sourceAttrs.add(sourceAttr);
        existingAttrs.add(existingAttr);

        // Mock behavior
        when(ecmAppAttrMapper.selectList(any())).thenReturn(existingAttrs);

        assertThrows(IllegalArgumentException.class,
                () -> modelBusiService.multiplexAddBusiAttr(ecmAddAttrDTO, token),
                "该业务类型已定义主键，无法复用主键属性");

    }

    @Test
    void multiplexAddBusiAttr_shouldSuccessfullyAddAttributes() {
        // Setup test data
        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAttrCode("ATTR1");
        attr1.setAttrName("Attribute1");
        attr1.setIsKey(0);

        ecmAddAttrDTO.setAttrIdList(attrIdList);
        sourceAttrs.add(attr1);

        // Mock behavior
        when(ecmAppAttrMapper.selectList(any())).thenReturn(existingAttrs);
        when(ecmAppDefMapper.update(any(), any())).thenReturn(1);

        // Execute and verify
        Result result = modelBusiService.multiplexAddBusiAttr(ecmAddAttrDTO, token);
        assertTrue(result.isSucc());
        verify(ecmAppAttrMapper, times(2)).insert(any(EcmAppAttr.class));
        verify(ecmAppDefMapper, times(1)).update(any(), any());
    }


    @Test
    void getAppParamsSuccess() {
        // Mock paramApi call
        Result<SysParamDTO> paramResult = Result.success(sysParamDTO);
        when(paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString()))
                .thenReturn(paramResult);

        // Mock sysStorageService calls
        Result<List<EcmStorageQueDTO>> deviceListResult = Result.success(equipmentList);
        when(sysStorageService.getStorageDeviceList()).thenReturn(deviceListResult);
        when(sysStorageService.getMQSettingList()).thenReturn(queueList);

        // Execute
        Result<EcmAddAppParamsDTO> result = modelBusiService.getAppParams();

        // Verify
        assertTrue(result.isSucc());
        assertNotNull(result.getData());
        assertEquals(equipmentList, result.getData().getEquipmentList());
        assertEquals(queueList, result.getData().getQueueList());

        // Verify interactions
        verify(paramApi).searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        verify(sysStorageService).getStorageDeviceList();
        verify(sysStorageService).getMQSettingList();
    }

    @Test
    void getAppParamsDeviceListNotSuccess() {
        // Mock paramApi call
        Result<SysParamDTO> paramResult = Result.success(sysParamDTO);
        when(paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString()))
                .thenReturn(paramResult);

        // Mock sysStorageService to return unsuccessful result
        Result<List<EcmStorageQueDTO>> deviceListResult = Result.error("Error",400);
        when(sysStorageService.getStorageDeviceList()).thenReturn(deviceListResult);
        when(sysStorageService.getMQSettingList()).thenReturn(queueList);

        // Execute
        Result<EcmAddAppParamsDTO> result = modelBusiService.getAppParams();

        // Verify
        assertTrue(result.isSucc());
        assertNotNull(result.getData());
        assertNull(result.getData().getEquipmentList());
        assertEquals(queueList, result.getData().getQueueList());
    }

    @Test
    void getAppParamsMQSettingListThrowsException() {
        // Mock paramApi call
        Result<SysParamDTO> paramResult = Result.success(sysParamDTO);
        when(paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString()))
                .thenReturn(paramResult);

        // Mock sysStorageService calls
        Result<List<EcmStorageQueDTO>> deviceListResult = Result.success(equipmentList);
        when(sysStorageService.getStorageDeviceList()).thenReturn(deviceListResult);
        when(sysStorageService.getMQSettingList()).thenThrow(new RuntimeException("Test exception"));

        // Execute
        Result<EcmAddAppParamsDTO> result = modelBusiService.getAppParams();

        // Verify
        assertTrue(result.isSucc());
        assertNotNull(result.getData());
        assertEquals(equipmentList, result.getData().getEquipmentList());
        assertNull(result.getData().getQueueList());
    }
}