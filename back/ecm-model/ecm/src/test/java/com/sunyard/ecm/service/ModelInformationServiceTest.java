//package com.sunyard.ecm.service;
//
//import com.sunyard.ecm.constant.IcmsConstants;
//import com.sunyard.ecm.constant.StateConstants;
//import com.sunyard.ecm.dto.AccountTokenExtendDTO;
//import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
//import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
//import com.sunyard.ecm.enums.StrategyConstantsEnum;
//import com.sunyard.ecm.manager.BusiCacheService;
//import com.sunyard.ecm.mapper.EcmAppDefRelMapper;
//import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
//import com.sunyard.ecm.mapper.EcmBusiDocMapper;
//import com.sunyard.ecm.mapper.EcmDocDefMapper;
//import com.sunyard.ecm.mapper.EcmDocDefRelMapper;
//import com.sunyard.ecm.mapper.EcmDocDynaPlagMapper;
//import com.sunyard.ecm.mapper.EcmDocPlagCheMapper;
//import com.sunyard.ecm.po.EcmAppDefRel;
//import com.sunyard.ecm.po.EcmDocDef;
//import com.sunyard.ecm.po.EcmDocDefRel;
//import com.sunyard.ecm.po.EcmDocDynaPlag;
//import com.sunyard.ecm.po.EcmDocPlagChe;
//import com.sunyard.framework.common.result.Result;
//import com.sunyard.framework.common.result.ResultCode;
//import com.sunyard.module.system.api.DictionaryApi;
//import com.sunyard.module.system.api.ParamApi;
//import com.sunyard.module.system.api.UserApi;
//import com.sunyard.module.system.api.dto.SysParamDTO;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class ModelInformationServiceTest{
//
//    @Mock
//    private UserApi userApi;
//
//    private SysParamDTO sysParamDTO;
//    @Mock
//    private EcmAppDocRelMapper ecmAppDocRelMapper;
//
//    @InjectMocks
//    private ModelInformationService modelInformationService;
//
//    private EcmDocDefDTO validEcmDocDefDTO;
//    @Mock
//    private BusiCacheService busiCacheService;
//    @Mock
//    private ParamApi paramApi;
//    @Mock
//    private EcmDocDefMapper ecmDocDefMapper;
//
//    @Mock
//    private EcmDocDefRelMapper ecmDocDefRelMapper;
//
//    @Mock
//    private DictionaryApi dictionaryApi;
//    @Mock
//    private EcmDocDynaPlagMapper ecmDocDynaPlagMapper;
//    @Mock
//    private EcmDocPlagCheMapper ecmDocPlagCheMapper;
//    @Mock
//    private EcmAppDefRelMapper ecmAppDefRelMapper;
//    @Mock
//    private EcmBusiDocMapper ecmBusiDocMapper;
//
//    private EcmDocDef validEcmDocDef;
//
//    private String userId = "testUser";
//
//    @BeforeEach
//    void setUp() {
//        validEcmDocDefDTO = new EcmDocDefDTO();
//        validEcmDocDefDTO.setDocCode("DOC001");
//        validEcmDocDefDTO.setDocName("Test Document");
//        validEcmDocDefDTO.setParent(IcmsConstants.DOC_LEVEL_FIRST);
//        validEcmDocDefDTO.setMaxLen(100);
//        validEcmDocDefDTO.setMinLen(10);
//        validEcmDocDefDTO.setParents(Collections.singletonList("DOC002"));
//
//        validEcmDocDef = new EcmDocDef();
//        validEcmDocDef.setDocCode("DOC001");
//        validEcmDocDef.setDocName("Test Document");
//        validEcmDocDef.setParent(IcmsConstants.DOC_LEVEL_FIRST);
//        validEcmDocDef.setMaxFiles(100);
//        validEcmDocDef.setMinFiles(10);
//
//        sysParamDTO = new SysParamDTO();
//        sysParamDTO.setValue("{\"commit\":\"0\",\"encryptStatus\":true,\"ocrConfigStatus\":true,\"ocrFlatIds\":[],\"ocrFlatStatus\":true,\"ocrIdentifyIds\":[],\"ocrIdentifyStatus\":true,\"ocrSortIds\":[],\"ocrSortStatus\":true,\"splitStatus\":false,\"zipBound\":500,\"zipScale\":80,\"zipStatus\":true}");
//    }
//
//    @Test
//    void deleteInformationTypeHasChildren() {
//        // Mock dependencies
//        EcmDocDef childDef = new EcmDocDef();
//        childDef.setIsParent(StateConstants.ZERO);
////        when(validEcmDocDefDTO.getDocCode()).thenReturn("DOC001");
////        when(validEcmDocDefDTO.getParents()).thenReturn(Collections.singletonList("DOC002"));
//        when(ecmDocDefMapper.selectList(any())).thenReturn(Collections.singletonList(childDef));
//
//        // Execute
//        Result result = modelInformationService.deleteInformationType(validEcmDocDefDTO);
//
//        // Verify
//        assertFalse(result.isSucc());
//        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
//        verify(ecmDocDefMapper, never()).deleteById(any());
//    }
//
//    @Test
//    void searchInformationTypeSuccess() {
//        // Mock dependencies
//        when(ecmDocDefMapper.selectById("DOC001")).thenReturn(validEcmDocDef);
//        when(ecmDocDefRelMapper.selectList(any())).thenReturn(Collections.emptyList());
//        when(userApi.getUserListByUsernames(any())).thenReturn(Result.success(Collections.emptyList()));
//        Result<SysParamDTO> paramResult = Result.success(sysParamDTO);
//        when(paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString()))
//                .thenReturn(paramResult);
//        // Execute
//        Result<EcmDocDefDTO> result = modelInformationService.searchInformationType("DOC001", "PARENT", "Parent Name");
//
//        // Verify
//        assertTrue(result.isSucc());
//        assertNotNull(result.getData());
//        assertEquals("Test Document", result.getData().getDocName());
//    }
//
//    @Test
//    void insertAdditionalDataSuccess() {
//        // Prepare test data
//        EcmDocTreeDTO leafNode = new EcmDocTreeDTO();
//        leafNode.setDocCode("DOC001");
//
//        Map<String, List<EcmDocPlagChe>> relDocCodes = new HashMap<>();
//        EcmDocPlagChe plagChe = new EcmDocPlagChe();
//        plagChe.setRelDocCode("DOC002");
//        plagChe.setQueryType(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL);
//        relDocCodes.put("DOC001", Collections.singletonList(plagChe));
//
//        Map<String, List<EcmDocDef>> groupedByDocCode = new HashMap<>();
//        EcmDocDef def = new EcmDocDef();
//        def.setDocName("Test Doc");
//        groupedByDocCode.put("DOC002", Collections.singletonList(def));
//
//        Map<String, List<EcmDocDynaPlag>> dynaType = new HashMap<>();
//
//        // Execute
//        modelInformationService.insertAdditionalData(leafNode, relDocCodes, groupedByDocCode, dynaType);
//
//        // Verify
//        assertNotNull(leafNode.getPlagiarismCheckPolicy());
//        assertEquals(1, leafNode.getPlagiarismCheckPolicy().getRelDocNames().size());
//    }
//
//    @Test
//    void searchDynamicPlagiarismSuccess() {
//        // Mock dependencies
//        List<EcmDocDynaPlag> dynaPlags = new ArrayList<>();
//        EcmDocDynaPlag dynaPlag = new EcmDocDynaPlag();
//        dynaPlag.setDocCode("DOC001");
//        dynaPlag.setDocName("Dynamic Doc");
//        dynaPlags.add(dynaPlag);
//
//        when(ecmDocDynaPlagMapper.selectList(null)).thenReturn(dynaPlags);
//
//        EcmDocPlagChe plagChe = new EcmDocPlagChe();
//        plagChe.setDocCode("DOC001");
//        plagChe.setQueryType(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL);
//        when(ecmDocPlagCheMapper.selectList(any())).thenReturn(Collections.singletonList(plagChe));
//
//        EcmDocDef def = new EcmDocDef();
//        def.setDocName("Test Doc");
//        when(ecmDocDefMapper.selectList(any())).thenReturn(Collections.singletonList(def));
//        EcmAppDefRel ecmDocDefRel = new EcmAppDefRel();
//        ecmDocDefRel.setAppCode("APP001");
//        ecmDocDefRel.setParent("0");
//        when(ecmAppDefRelMapper.selectList(any())).thenReturn(Collections.singletonList(ecmDocDefRel));
//
//        // Execute
//        Result<List<EcmDocTreeDTO>> result = modelInformationService.searchDynamicPlagiarism();
//
//        // Verify
//        assertTrue(result.isSucc());
//        assertEquals(1, result.getData().size());
//        assertEquals("Dynamic Doc", result.getData().get(0).getDocName());
//    }
//
//    @Test
//    void rebuildEcmAppDefRelSuccess() {
//        // Mock dependencies
//        when(ecmAppDefRelMapper.selectList(null)).thenReturn(Collections.emptyList());
//        when(ecmAppDefRelMapper.insert(any(EcmAppDefRel.class))).thenReturn(1);
//        EcmAppDefRel ecmDocDefRel = new EcmAppDefRel();
//        ecmDocDefRel.setAppCode("PARENT001");
//        ecmDocDefRel.setParent("0");
//        when(ecmAppDefRelMapper.selectList(any())).thenReturn(Collections.singletonList(ecmDocDefRel));
//        // Execute
//        modelInformationService.rebuildEcmAppDefRel("APP001", "PARENT001");
//
//        // Verify
//        verify(ecmAppDefRelMapper).delete(any());
//
//    }
//
//    @Test
//    void editInformationTypeSuccess() {
//        // Mock dependencies
////        when(ecmDocDefMapper.selectById("DOC001")).thenReturn(validEcmDocDef);
//        when(ecmDocDefMapper.selectList(any())).thenReturn(Collections.emptyList());
//        when(dictionaryApi.getDictionaryAll(any(), any())).thenReturn(Result.success(new HashMap<>()));
//        when(ecmDocDefMapper.updateById(any(EcmDocDef.class))).thenReturn(1);
//        when(ecmDocDefRelMapper.delete(any())).thenReturn(1);
//        when(ecmDocDefRelMapper.selectList(any())).thenReturn(Collections.emptyList());
//        when(busiCacheService.setDocInfo(any(),any())).thenReturn(true);
//        // Execute
//        Result result = modelInformationService.editInformationType(validEcmDocDefDTO, userId);
//
//        // Verify
//        assertTrue(result.isSucc());
//        verify(ecmDocDefMapper).updateById(any(EcmDocDef.class));
//        verify(ecmDocDefRelMapper).delete(any());
//        verify(ecmDocDefRelMapper).insert(any(EcmDocDefRel.class));
//    }
//
//    @Test
//    void addInformationTypeSuccess() {
//        // Mock dependencies
//        when(ecmDocDefMapper.selectList(any())).thenReturn(Collections.emptyList());
//        when(ecmDocDefMapper.insert(any(EcmDocDef.class))).thenReturn(1);
//        when(ecmDocDefRelMapper.selectList(null)).thenReturn(Collections.emptyList());
//
//        // Execute
//        Result result = modelInformationService.addInformationType(validEcmDocDefDTO, userId);
//
//        // Verify
//        assertTrue(result.isSucc());
//        verify(ecmDocDefMapper).insert(any(EcmDocDef.class));
//        verify(ecmDocDefRelMapper).insert(any(EcmDocDefRel.class));
//    }
//
//    @Test
//    void searchInformationTypeTreeSuccess() {
//        // Mock dependencies
//        List<EcmDocDef> docDefs = new ArrayList<>();
//        EcmDocDef def1 = new EcmDocDef();
//        def1.setDocCode("DOC001");
//        def1.setParent("0");
//        def1.setDocSort(1.0f);
//        docDefs.add(def1);
//
//        EcmDocDef def2 = new EcmDocDef();
//        def2.setDocCode("DOC002");
//        def2.setParent("DOC001");
//        def2.setDocSort(2.0f);
//        docDefs.add(def2);
//
//        when(ecmDocDefMapper.selectList(null)).thenReturn(docDefs);
//
//        // Execute
//        Result<List<EcmDocTreeDTO>> result = modelInformationService.searchInformationTypeTree(null);
//
//        // Verify
//        assertTrue(result.isSucc());
//        assertEquals(1, result.getData().size());
//    }
//
//    @Test
//    void getDocTreeByVerAndAppCodeNewVersion() {
//        // Execute
//        Map result = modelInformationService.getDocTreeByVerAndAppCode("APP001", null);
//
//        // Verify
//        assertNotNull(result);
//        assertTrue((Boolean) result.get("isNew"));
//        assertNotNull(result.get("tree"));
//    }
//
//    @Test
//    void dragTreeType1Success() {
//        // Mock dependencies
//        when(ecmDocDefMapper.selectById("DOC001")).thenReturn(validEcmDocDef);
//        when(ecmDocDefMapper.update(any(), any())).thenReturn(1);
//        when(ecmDocDefRelMapper.update(any(), any())).thenReturn(1);
//
//        // Execute
//        Result result = modelInformationService.dragTree("DOC001", 1.0f, null, null, "PARENT001", "PARENT002", 1,
//            new AccountTokenExtendDTO() {{ setUsername(userId); }});
//
//        // Verify
//        assertTrue(result.isSucc());
//        verify(ecmDocDefMapper).update(any(), any());
//        verify(ecmDocDefRelMapper).update(any(), any());
//    }
//
//    @Test
//    void deleteInformationTypeSuccess() {
//        // Mock dependencies
//        when(validEcmDocDefDTO.getParents()).thenReturn(Collections.singletonList("DOC001"));
//        when(ecmAppDocRelMapper.selectList(any())).thenReturn(Collections.emptyList());
//        when(ecmBusiDocMapper.selectList(any())).thenReturn(Collections.emptyList());
//        when(ecmDocDefRelMapper.delete(any())).thenReturn(1);
//        when(ecmDocDefMapper.deleteById("DOC001")).thenReturn(1);
//
//        // Execute
//        Result result = modelInformationService.deleteInformationType(validEcmDocDefDTO);
//
//        // Verify
//        assertTrue(result.isSucc());
//        verify(ecmDocDefMapper).deleteById("DOC001");
//    }
//
//    @Test
//    void searchInformationParentTypeTreeSuccess() {
//        // Mock dependencies
//        List<EcmDocDef> docDefs = new ArrayList<>();
//        EcmDocDef def1 = new EcmDocDef();
//        def1.setDocCode("DOC001");
//        def1.setParent("0");
//        def1.setIsParent(IcmsConstants.ONE);
//        def1.setDocSort(1.0f);
//        docDefs.add(def1);
//
//        when(ecmDocDefMapper.selectList(any())).thenReturn(docDefs);
//
//        // Execute
//        Result<List<EcmDocTreeDTO>> result = modelInformationService.searchInformationParentTypeTree(null);
//
//        // Verify
//        assertTrue(result.isSucc());
//        assertEquals(1, result.getData().size());
//    }
//
//    @Test
//    void addInformationTypeDuplicateDocCode() {
//        // Mock dependencies
//        EcmDocDef existingDef = new EcmDocDef();
//        existingDef.setDocCode("DOC001");
//        when(ecmDocDefMapper.selectList(any())).thenReturn(Collections.singletonList(existingDef));
//
//        // Execute
//        Result result = modelInformationService.addInformationType(validEcmDocDefDTO, userId);
//
//        // Verify
//        assertFalse(result.isSucc());
//        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
//        verify(ecmDocDefMapper, never()).insert(any());
//    }
//
//}