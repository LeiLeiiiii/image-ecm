package com.sunyard.ecm.service;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDefDTO;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDefRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiStatisticsMapper;
import com.sunyard.ecm.mapper.EcmUserBusiFileStatisticsMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDefRel;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.apache.ibatis.session.ExecutorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"fileIndex=ecm_file_test"})
public class ModelBusiServiceAddBusiTypeTest{
    @InjectMocks
    private ModelBusiService modelBusiService;

    private EcmAppDefAttrVO ecmAppDefAttrVo;
    private String userId = "testUser";
    @Mock
    private EcmAppDefMapper ecmAppDefMapper;

    private String appCode;
    @Mock
    private EcmAppDefRelMapper ecmAppDefRelMapper;
    @Mock
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Mock
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Mock
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Mock
    private EcmAppDocRelMapper ecmAppDocRelMapper;

    @Mock
    private SnowflakeUtils snowflakeUtil;

    @Mock
    private EcmBusiStatisticsMapper ecmBusiStatisticsMapper;
    @Mock
    private EcmUserBusiFileStatisticsMapper ecmUserBusiFileStatisticsMapper;
    private AccountTokenExtendDTO token;

    private List<EcmAppDef> mockAppDefs;

    @Mock
    private StaticTreePermissService staticTreePermissService;

    @Mock
    private org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory;

    @Mock
    private UserApi userApi;

    private String parentName;

    private EcmAppDef ecmAppDef;

    private List<EcmAppAttr> ecmAppAttrs;

    private String parentId;
    private List<EcmAppDefRel> appDefRels;

    private Set<String> mockAppCodes;
    @BeforeEach
    void setUp() {
        ecmAppDefAttrVo = new EcmAppDefAttrVO();
        ecmAppDefAttrVo.setAppCode("TEST001");
        ecmAppDefAttrVo.setAppName("Test Business");
        ecmAppDefAttrVo.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        ecmAppDefAttrVo.setIsParent(0);
        MockitoAnnotations.openMocks(this);

        token = new AccountTokenExtendDTO();

        // Prepare mock data
        mockAppDefs = new ArrayList<>();
        EcmAppDef parent = new EcmAppDef();
        parent.setAppCode("1");
        parent.setAppName("Root");
        parent.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        parent.setAppSort(1f);
        parent.setIsParent(1);

        EcmAppDef child1 = new EcmAppDef();
        child1.setAppCode("2");
        child1.setAppName("Child1");
        child1.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        child1.setAppSort(2f);
        child1.setIsParent(0);

        EcmAppDef child2 = new EcmAppDef();
        child2.setAppCode("3");
        child2.setAppName("Child2");
        child2.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        child2.setAppSort(3f);
        child2.setIsParent(0);

        mockAppDefs.add(parent);
        mockAppDefs.add(child1);
        mockAppDefs.add(child2);

        appCode = "TEST_APP";
        parentId = "PARENT_001";
        parentName = "Parent Name";

        ecmAppDef = new EcmAppDef();
        ecmAppDef.setAppCode(appCode);
        ecmAppDef.setCreateUser("creator");
        ecmAppDef.setUpdateUser("updater");

        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAppCode(appCode);
        attr1.setAttrSort(1);
        EcmAppAttr attr2 = new EcmAppAttr();
        attr2.setAppCode(appCode);
        attr2.setAttrSort(2);
        ecmAppAttrs = Arrays.asList(attr1, attr2);

        EcmAppDefRel rel1 = new EcmAppDefRel();
        rel1.setAppCode(appCode);
        rel1.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        EcmAppDefRel rel2 = new EcmAppDefRel();
        rel2.setAppCode(appCode);
        rel2.setParent("PARENT_001");
        appDefRels = Arrays.asList(rel1, rel2);

        token = new AccountTokenExtendDTO();
        mockAppDefs = new ArrayList<>();
        mockAppCodes = new HashSet<>();
    }

    @Test
    public void testAddBusiTypeInvalidQulity() {
        EcmAppDefAttrVO vo = new EcmAppDefAttrVO();
        vo.setAppCode("TEST001");
        vo.setAppName("Test Business");
        vo.setQulity(190f);

        Result result = modelBusiService.addBusiType(vo, "user001");

        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("压缩质量不能超过100且不能小于1"));
    }

    @Test
    public void testAddBusiTypeSuccess() {
        // Prepare test data
        EcmAppDefAttrVO vo = new EcmAppDefAttrVO();
        vo.setAppCode("TEST001");
        vo.setAppName("Test Business");
        vo.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        vo.setIsParent(1);

        List<EcmAppAttr> attrList = new ArrayList<>();
        EcmAppAttr attr = new EcmAppAttr();
        attr.setAttrCode("ATTR001");
        attr.setAttrName("Test Attribute");
        attr.setIsKey(0);
        attrList.add(attr);
        vo.setEcmAppAttrList(attrList);

        String userId = "user001";

        // Mock dependencies
        when(ecmAppDefMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppAttrMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(snowflakeUtil.nextId()).thenReturn(123456L);
        // 或者使用更精确的参数匹配
        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mock(org.apache.ibatis.session.SqlSession.class));

        // Execute
        Result result = modelBusiService.addBusiType(vo, userId);

        // Verify
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMsg());
        verify(ecmAppDefMapper, times(1)).insert(any(EcmAppDef.class));
        verify(ecmAppDefRelMapper, atLeastOnce()).insert(any(EcmAppDefRel.class));
    }


    @Test
    public void testAddBusiTypeDuplicateAppCode() {
        EcmAppDefAttrVO vo = new EcmAppDefAttrVO();
        vo.setAppCode("TEST001");
        vo.setAppName("Test Business");

        EcmAppDef existing = new EcmAppDef();
        existing.setAppCode("TEST001");
        when(ecmAppDefMapper.selectList(any())).thenReturn(Collections.singletonList(existing));

        Result result = modelBusiService.addBusiType(vo, "user001");

        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("业务类型代码不能重复"));
    }

    @Test
    public void testAddBusiTypeInvalidParent() {
        EcmAppDefAttrVO vo = new EcmAppDefAttrVO();
        vo.setAppCode("TEST001");
        vo.setAppName("Test Business");
        vo.setParent("INVALID");
        vo.setIsParent(0);

        when(ecmAppDefMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(new EcmAppAttr()));

        Result result = modelBusiService.addBusiType(vo, "user001");

        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("该业务类型已经关联了属性"));
    }



    @Test
    void editBusiType_ShouldReturnSuccess_WhenValidInput() {
        // Arrange
        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAppAttrId(1L);
        attr1.setAttrCode("ATTR1");
        attr1.setAttrName("Attribute 1");
        attr1.setIsKey(0);

        EcmAppAttr attr2 = new EcmAppAttr();
        attr2.setAppAttrId(null);
        attr2.setAttrCode("ATTR2");
        attr2.setAttrName("Attribute 2");
        attr2.setIsKey(0);

        ecmAppDefAttrVo.setEcmAppAttrList(Arrays.asList(attr1, attr2));

        when(ecmAppDocRelMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(snowflakeUtil.nextId()).thenReturn(2L);
        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mock(org.apache.ibatis.session.SqlSession.class));

        // Act
        Result result = modelBusiService.editBusiType(ecmAppDefAttrVo, userId);

        // Assert
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        verify(ecmAppDefMapper).updateById(any());
        verify(ecmAppDefRelMapper).delete(any());
        verify(ecmAppDefRelMapper, atLeastOnce()).insert(any());
    }


    @Test
    void editBusiType_ShouldReturnError_WhenHasDuplicateAttrCodes() {
        // Arrange
        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAppAttrId(1L);
        attr1.setAttrCode("DUPLICATE");
        attr1.setAttrName("Attribute 1");

        EcmAppAttr attr2 = new EcmAppAttr();
        attr2.setAppAttrId(2L);
        attr2.setAttrCode("DUPLICATE");
        attr2.setAttrName("Attribute 2");

        ecmAppDefAttrVo.setEcmAppAttrList(Arrays.asList(attr1, attr2));

        when(ecmAppDocRelMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act
        Result result = modelBusiService.editBusiType(ecmAppDefAttrVo, userId);

        // Assert
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("业务属性代码重复"));
    }

    @Test
    void editBusiType_ShouldReturnError_WhenHasAssociatedDocuments() {
        // Arrange
        EcmAppDocRel docRel = new EcmAppDocRel();
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(Collections.singletonList(docRel));

        // Act
        Result result = modelBusiService.editBusiType(ecmAppDefAttrVo, userId);

        // Assert
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("无法作为父节点，该业务类型已关联资料"));
    }

    @Test
    void editBusiType_ShouldReturnError_WhenHasAssociatedAttributes() {
        // Arrange
        EcmAppAttr attr = new EcmAppAttr();
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(attr));

        // Act
        Result result = modelBusiService.editBusiType(ecmAppDefAttrVo, userId);

        // Assert
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("该业务类型已经关联了属性"));
    }

    @Test
    void editBusiType_ShouldReturnError_WhenInvalidQulity() {
        // Arrange
        ecmAppDefAttrVo.setQulity(101f);

        // Act
        Result result = modelBusiService.editBusiType(ecmAppDefAttrVo, userId);

        // Assert
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("压缩质量不能超过100且不能小于1"));
    }

    @Test
    void editBusiType_ShouldNotProcessAttrs_WhenIsParent() {
        // Arrange
        ecmAppDefAttrVo.setIsParent(1);

        // Act
        Result result = modelBusiService.editBusiType(ecmAppDefAttrVo, userId);

        // Assert
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        verify(ecmAppDefMapper).updateById(any());
        verify(ecmAppDefRelMapper, never()).delete(any());
        verify(ecmAppAttrMapper, never()).delete(any());
    }


    @Test
    void deleteBusiTypeShouldReturnErrorWhenBusinessExists() {
        EcmAppDefDTO ecmAppDefDTO = new EcmAppDefDTO();
        ecmAppDefDTO.setAppCode("testCode");

        when(ecmBusiInfoMapper.selectList(any())).thenReturn(Collections.singletonList(new EcmBusiInfo()));

        Result result = modelBusiService.deleteBusiType(ecmAppDefDTO);

        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("无法删除，该业务类型下已存在业务", result.getMsg());
    }

    @Test
    void deleteBusiTypeShouldReturnErrorWhenBusinessExistsInRecycle() {
        EcmAppDefDTO ecmAppDefDTO = new EcmAppDefDTO();
        ecmAppDefDTO.setAppCode("testCode");

        when(ecmBusiInfoMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmBusiInfoMapper.selectListInRecycle(any())).thenReturn(Collections.singletonList("testCode"));

        Result result = modelBusiService.deleteBusiType(ecmAppDefDTO);

        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("无法删除，该业务类型下回收站已存在业务", result.getMsg());
    }

    @Test
    void deleteBusiTypeShouldReturnErrorWhenHasChildNodes() {
        EcmAppDefDTO ecmAppDefDTO = new EcmAppDefDTO();
        ecmAppDefDTO.setAppCode("testCode");
        ecmAppDefDTO.setChildren(Collections.singletonList("childCode"));

        EcmAppDef childNode = new EcmAppDef();
        childNode.setIsParent(0);

        when(ecmBusiInfoMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmBusiInfoMapper.selectListInRecycle(any())).thenReturn(new ArrayList<>());
        when(ecmAppDefMapper.selectList(any())).thenReturn(Collections.singletonList(childNode));

        Result result = modelBusiService.deleteBusiType(ecmAppDefDTO);

        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("无法删除，该目录下存在子节点", result.getMsg());
    }

    @Test
    void deleteBusiTypeShouldSuccessWhenAllConditionsMet() {
        EcmAppDefDTO ecmAppDefDTO = new EcmAppDefDTO();
        ecmAppDefDTO.setAppCode("testCode");

        when(ecmBusiInfoMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmBusiInfoMapper.selectListInRecycle(any())).thenReturn(new ArrayList<>());

        Result result = modelBusiService.deleteBusiType(ecmAppDefDTO);

        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMsg());

        verify(ecmAppDefMapper, times(1)).deleteById("testCode");
        verify(ecmAppDefRelMapper, times(1)).delete(any());
        verify(ecmAppAttrMapper, times(1)).delete(any());
        verify(ecmBusiStatisticsMapper, times(1)).delete(any());
        verify(ecmUserBusiFileStatisticsMapper, times(1)).delete(any());
        verify(ecmAppDocrightMapper, times(1)).delete(any());
        verify(ecmAppDocRelMapper, times(1)).delete(any());
    }


    @Test
    void testSearchBusiTypeTreeWithEmptyPermission() {
        // Given
        String appCode = "1";
        String right = "read";

        when(staticTreePermissService.getAppCodeHaveByToken(appCode, token, right))
                .thenReturn(Collections.emptySet());
        when(ecmAppDefMapper.selectList(any()))
                .thenReturn(Collections.emptyList());

        // When
        List<EcmAppDefAttrVO> result = modelBusiService.searchBusiTypeTree(appCode, token, true, right);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(staticTreePermissService, times(1)).getAppCodeHaveByToken(appCode, token, right);
        verify(ecmAppDefMapper, times(1)).selectList(any());
    }


    @Test
    void searchBusiTypeInfoSuccess() {
        when(ecmAppDefMapper.selectById(appCode)).thenReturn(ecmAppDef);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(ecmAppAttrs);
        when(ecmAppDefRelMapper.selectList(any())).thenReturn(appDefRels);

        SysUserDTO creator = new SysUserDTO();
        creator.setLoginName("creator");
        creator.setName("Creator Name");
        SysUserDTO updater = new SysUserDTO();
        updater.setLoginName("updater");
        updater.setName("Updater Name");
        List<SysUserDTO> userList = Arrays.asList(creator, updater);
        Result<List<SysUserDTO>> userResult = Result.success(userList);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        EcmAppDefAttrVO result = modelBusiService.searchBusiTypeInfo(appCode, parentId, parentName);

        assertNotNull(result);
        assertEquals(appCode, result.getAppCode());
        assertEquals(parentId, result.getParent());
        assertEquals(parentName, result.getParentName());
        assertEquals(2, result.getEcmAppAttrList().size());
        assertEquals(2, result.getParents().size());
        assertTrue(result.getParents().contains("PARENT_001"));
        assertEquals("Creator Name", result.getCreateUserName());
        assertEquals("Updater Name", result.getUpdateUserName());

        verify(ecmAppDefMapper).selectById(appCode);
        verify(ecmAppAttrMapper).selectList(any());
        verify(ecmAppDefRelMapper).selectList(any());
        verify(userApi).getUserListByUsernames(any());
    }


    @Test
    void searchBusiTypeInfoNoAppAttrs() {
        when(ecmAppDefMapper.selectById(appCode)).thenReturn(ecmAppDef);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppDefRelMapper.selectList(any())).thenReturn(appDefRels);

        SysUserDTO creator = new SysUserDTO();
        creator.setLoginName("creator");
        creator.setName("Creator Name");
        List<SysUserDTO> userList = Arrays.asList(creator);
        Result<List<SysUserDTO>> userResult = Result.success(userList);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        EcmAppDefAttrVO result = modelBusiService.searchBusiTypeInfo(appCode, parentId, parentName);

        assertNotNull(result);
        assertTrue(result.getEcmAppAttrList().isEmpty());
        assertEquals(2, result.getParents().size());

        verify(ecmAppDefMapper).selectById(appCode);
        verify(ecmAppAttrMapper).selectList(any());
        verify(ecmAppDefRelMapper).selectList(any());
        verify(userApi).getUserListByUsernames(any());
    }

    @Test
    void searchBusiTypeInfoNoAppDefRels() {
        when(ecmAppDefMapper.selectById(appCode)).thenReturn(ecmAppDef);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(ecmAppAttrs);
        when(ecmAppDefRelMapper.selectList(any())).thenReturn(new ArrayList<>());

        SysUserDTO creator = new SysUserDTO();
        creator.setLoginName("creator");
        creator.setName("Creator Name");
        List<SysUserDTO> userList = Arrays.asList(creator);
        Result<List<SysUserDTO>> userResult = Result.success(userList);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        EcmAppDefAttrVO result = modelBusiService.searchBusiTypeInfo(appCode, parentId, parentName);

        assertNotNull(result);
        assertEquals(2, result.getEcmAppAttrList().size());
        assertTrue(result.getParents().isEmpty());

        verify(ecmAppDefMapper).selectById(appCode);
        verify(ecmAppAttrMapper).selectList(any());
        verify(ecmAppDefRelMapper).selectList(any());
        verify(userApi).getUserListByUsernames(any());
    }

    @Test
    void searchBusiTypeInfoUserApiFailure() {
        when(ecmAppDefMapper.selectById(appCode)).thenReturn(ecmAppDef);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(ecmAppAttrs);
        when(ecmAppDefRelMapper.selectList(any())).thenReturn(appDefRels);

        Result<List<SysUserDTO>> userResult = Result.error("User API error",400);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        assertThrows(Exception.class, () -> {
            modelBusiService.searchBusiTypeInfo(appCode, parentId, parentName);
        });

        verify(ecmAppDefMapper).selectById(appCode);
        verify(ecmAppAttrMapper).selectList(any());
        verify(ecmAppDefRelMapper).selectList(any());
        verify(userApi).getUserListByUsernames(any());
    }


    @Test
    void searchBusiTypeParentTreeWithFlagTrueAndHasPermissionShouldReturnTree() {
        // Arrange
        mockAppCodes.add("PARENT_1");
        mockAppCodes.add("PARENT_2");

        EcmAppDef parent1 = new EcmAppDef();
        parent1.setAppCode("PARENT_1");
        parent1.setAppName("Parent 1");
        parent1.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        parent1.setIsParent(IcmsConstants.ONE);
        parent1.setAppSort(1f);

        EcmAppDef parent2 = new EcmAppDef();
        parent2.setAppCode("PARENT_2");
        parent2.setAppName("Parent 2");
        parent2.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        parent2.setIsParent(IcmsConstants.ONE);
        parent2.setAppSort(2f);

        mockAppDefs.add(parent1);
        mockAppDefs.add(parent2);

        when(staticTreePermissService.getAppCodeHaveByToken(any(), any(), any()))
                .thenReturn(mockAppCodes);
        when(ecmAppDefMapper.selectList(any()))
                .thenReturn(mockAppDefs);

        // Act
        List<EcmAppDefAttrVO> result = modelBusiService.searchBusiTypeParentTree(
                "TEST_CODE", token, true, "READ");

        // Assert
        assertEquals(2, result.size());
        assertEquals("(PARENT_1)Parent 1", result.get(0).getLabel());
        assertEquals("(PARENT_2)Parent 2", result.get(1).getLabel());
        verify(staticTreePermissService).getAppCodeHaveByToken("TEST_CODE", token, "READ");
        verify(ecmAppDefMapper).selectList(any());
    }

    @Test
    void searchBusiTypeParentTreeWithFlagFalseShouldIgnorePermissionCheck() {
        // Arrange
        EcmAppDef parent = new EcmAppDef();
        parent.setAppCode("PARENT");
        parent.setAppName("Parent");
        parent.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        parent.setIsParent(IcmsConstants.ONE);
        parent.setAppSort(1f);

        mockAppDefs.add(parent);

        when(ecmAppDefMapper.selectList(any()))
                .thenReturn(mockAppDefs);

        // Act
        List<EcmAppDefAttrVO> result = modelBusiService.searchBusiTypeParentTree(
                "TEST_CODE", token, false, "READ");

        // Assert
        assertEquals(1, result.size());
        assertEquals("(PARENT)Parent", result.get(0).getLabel());
        verify(staticTreePermissService, never()).getAppCodeHaveByToken(any(), any(), any());
        verify(ecmAppDefMapper).selectList(any());
    }

    @Test
    void searchBusiTypeParentTreeWithDisabledNodeShouldSetDisabledFlag() {
        // Arrange
        EcmAppDef parent = new EcmAppDef();
        parent.setAppCode("PARENT");
        parent.setAppName("Parent");
        parent.setParent(StateConstants.PARENT_APP_CODE_DEFAULT);
        parent.setIsParent(IcmsConstants.ONE);
        parent.setAppSort(1f);

        mockAppDefs.add(parent);

        when(ecmAppDefMapper.selectList(any()))
                .thenReturn(mockAppDefs);

        // Act
        List<EcmAppDefAttrVO> result = modelBusiService.searchBusiTypeParentTree(
                "PARENT", token, false, "READ");

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDisabled());
    }

    @Test
    void searchBusiTypeParentTreeWithFlagTrueAndEmptyPermissionShouldReturnEmptyList() {
        // Arrange
        when(staticTreePermissService.getAppCodeHaveByToken(any(), any(), any()))
                .thenReturn(Collections.emptySet());
        when(ecmAppDefMapper.selectList(any()))
                .thenReturn(Collections.emptyList());

        // Act
        List<EcmAppDefAttrVO> result = modelBusiService.searchBusiTypeParentTree(
                "TEST_CODE", token, true, "READ");

        // Assert
        assertTrue(result.isEmpty());
        verify(staticTreePermissService).getAppCodeHaveByToken("TEST_CODE", token, "READ");
        verify(ecmAppDefMapper).selectList(any());
    }

}