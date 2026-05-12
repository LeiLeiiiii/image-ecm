package com.sunyard.ecm.service;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDocRelDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmDimensionDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDimensionMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDimensionDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDimension;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmDimensionDef;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.vo.AddVerVO;
import com.sunyard.ecm.vo.DocRightRoleAndLotVO;
import com.sunyard.ecm.vo.DocRightVO;
import com.sunyard.ecm.vo.EcmDocTreeVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.RoleApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModelPermissionsServiceTest {

    private AddVerVO addVerVo;
    private DocRightVO docRightVO;
    private AccountTokenExtendDTO accountToken;
    private EcmDimensionDef dimensionDef;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @Mock
    private EcmAppDocrightMapper ecmAppDocrightMapper;

    @Spy
    @InjectMocks
    private ModelPermissionsService modelPermissionsService;

    @Mock
    private EcmAppDocRelMapper ecmAppDocRelMapper;

    @Mock
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;

    @Mock
    private SnowflakeUtils snowflakeUtil;

    @Mock
    private EcmDocDefMapper ecmDocDefMapper;

    @Mock
    private EcmDocrightDefMapper ecmDocrightDefMapper;

    @Mock
    private EcmBusiInfoMapper ecmBusiInfoMapper;

    @Mock
    private EcmAppDefMapper ecmAppDefMapper;

    @Mock
    private EcmDimensionDefMapper ecmDimensionDefMapper;

    @Mock
    private EcmAppDimensionMapper ecmAppDimensionMapper;

    @Mock
    private RoleApi roleApi;

    @Mock
    private UserApi userApi;

    @Mock
    private StaticTreePermissService staticTreePermissService;

    private final Long testId = 1L;
    private final String testAppCode = "TEST_APP";
    private final String testUserId = "testUser";

    @BeforeEach
    void setUp() {
        addVerVo = new AddVerVO();
        addVerVo.setAppCode("TEST_APP");
        addVerVo.setRightVer(1);
        addVerVo.setRightName("Test Right");
        addVerVo.setCreateUser("testUser");
        addVerVo.setAddVerType(DocRightConstants.EXISTING_VER);
        addVerVo.setSelectVerNo(0); // For EXISTING_VER case

        docRightVO = new DocRightVO();
        docRightVO.setAppCode("TEST_APP");
        docRightVO.setRightVer(1);
        docRightVO.setRoleId(1L);
        docRightVO.setCurrentUser("testUser");

        accountToken = new AccountTokenExtendDTO();
        accountToken.setUsername("testUser");

        dimensionDef = new EcmDimensionDef();
        dimensionDef.setDimCode("DIM001");
        dimensionDef.setDimName("测试维度");
        dimensionDef.setDimValue("值1;值2");
        dimensionDef.setCreateUser("testUser");
    }

    // 原有的测试方法保持不变...

    @Test
    void testGetVerNo() {
        // Arrange
        List<EcmAppDocright> appDocrights = new ArrayList<>();
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setRightVer(1);
        appDocrights.add(appDocright);
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(appDocrights);

        // Act
        Integer result = modelPermissionsService.getVerNo("APP001");

        // Assert
        assertEquals(2, result);
    }

    @Test
    void testGetVerNo_WhenNoVersionsExist() {
        // Arrange
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act
        Integer result = modelPermissionsService.getVerNo("APP001");

        // Assert
        assertEquals(StateConstants.COMMON_ONE, result);
    }

    @Test
    void testGetRightVerList() {
        // Arrange
        addVerVo.setPageNum(1);
        addVerVo.setPageSize(10);

        List<EcmAppDocright> appDocrights = new ArrayList<>();
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        appDocright.setRightName("测试版本");
        appDocright.setCreateUser("testUser");
        appDocright.setUpdateUser("testUser");
        appDocrights.add(appDocright);
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(appDocrights);

        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppName("测试应用");
        when(ecmAppDefMapper.selectById(anyString())).thenReturn(appDef);

        Result<List<SysUserDTO>> userResult = new Result<>();
        List<SysUserDTO> users = new ArrayList<>();
        SysUserDTO user = new SysUserDTO();
        user.setLoginName("testUser");
        user.setName("测试用户");
        users.add(user);
        userResult.setData(users);
        userResult.setCode(200);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        // Act
        PageInfo result = modelPermissionsService.getRightVerList(addVerVo);

        // Assert
        assertNotNull(result);
        assertFalse(result.getList().isEmpty());
    }

    @Test
    void testEditVer() {
        // Arrange
        addVerVo.setId(1L);
        addVerVo.setRightName("编辑后的版本");
        addVerVo.setUpdateUser("testUser");

        doReturn(1).when(ecmAppDocrightMapper).update(any(), any());

        // Act
        modelPermissionsService.editVer(addVerVo);

        // Assert
        verify(ecmAppDocrightMapper, times(1)).update(any(), any());
    }

    @Test
    void testEditVer_WhenIdIsNull() {
        // Arrange
        addVerVo.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.editVer(addVerVo);
        });

        verify(ecmAppDocrightMapper, never()).update(any(), any());
    }

    @Test
    void testDetailsVer() {
        // Arrange
        List<EcmAppDocright> appDocrights = new ArrayList<>();
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        appDocright.setCreateUser("testUser");
        appDocright.setUpdateUser("testUser");
        appDocrights.add(appDocright);
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(appDocrights);

        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppName("测试应用");
        when(ecmAppDefMapper.selectById(anyString())).thenReturn(appDef);

        Result<List<SysUserDTO>> userResult = new Result<>();
        List<SysUserDTO> users = new ArrayList<>();
        SysUserDTO user = new SysUserDTO();
        user.setLoginName("testUser");
        user.setName("测试用户");
        users.add(user);
        userResult.setData(users);
        userResult.setCode(200);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        // Act
        EcmAppDocrightDTO result = modelPermissionsService.detailsVer(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testDetailsVer_WhenIdIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.detailsVer(null);
        });
    }

    @Test
    void testDelVer() {
        // Arrange
        when(ecmBusiInfoMapper.selectCountAll(anyInt(), anyString())).thenReturn(0L);
        when(ecmAppDocrightMapper.delete(any())).thenReturn(1);
        when(ecmDocDefRelVerMapper.delete(any())).thenReturn(1);
        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);

        // Act
        modelPermissionsService.delVer(1, "APP001");

        // Assert
        verify(ecmAppDocrightMapper, times(1)).delete(any());
        verify(ecmDocDefRelVerMapper, times(1)).delete(any());
        verify(ecmDocrightDefMapper, times(1)).delete(any());
    }

    @Test
    void testDelVer_WhenVersionIsUsed() {
        // Arrange
        when(ecmBusiInfoMapper.selectCountAll(anyInt(), anyString())).thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delVer(1, "APP001");
        });

        verify(ecmAppDocrightMapper, never()).delete(any());
        verify(ecmDocDefRelVerMapper, never()).delete(any());
        verify(ecmDocrightDefMapper, never()).delete(any());
    }

    @Test
    void testAddDimension() {
        // Arrange
        when(ecmDimensionDefMapper.selectCount(any())).thenReturn(0L);

        // Act
        modelPermissionsService.addDimension(dimensionDef);

        // Assert
        verify(ecmDimensionDefMapper, times(1)).insert(dimensionDef);
    }

    @Test
    void testAddDimension_WhenDimCodeExists() {
        // Arrange
        when(ecmDimensionDefMapper.selectCount(any())).thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addDimension(dimensionDef);
        });

        verify(ecmDimensionDefMapper, never()).insert(dimensionDef);
    }

    @Test
    void testGetDimensionList() {
        // Arrange
        List<EcmDimensionDef> dimensionDefs = new ArrayList<>();
        dimensionDefs.add(dimensionDef);
        when(ecmDimensionDefMapper.selectList(any())).thenReturn(dimensionDefs);

        Result<List<SysUserDTO>> userResult = new Result<>();
        List<SysUserDTO> users = new ArrayList<>();
        SysUserDTO user = new SysUserDTO();
        user.setLoginName("testUser");
        user.setName("测试用户");
        users.add(user);
        userResult.setData(users);
        userResult.setCode(200);
        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);

        when(ecmAppDimensionMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        List<EcmDimensionDefDTO> result = modelPermissionsService.getDimensionList();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testEditDimension() {
        // Arrange
        dimensionDef.setId(1L);
        dimensionDef.setUpdateUser("testUser");

        when(ecmDimensionDefMapper.update(any(), any())).thenReturn(1);

        // Act
        modelPermissionsService.editDimension(dimensionDef);

        // Assert
        verify(ecmDimensionDefMapper, times(1)).update(any(), any());
    }

    @Test
    void testDelDimension() {
        // Arrange
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        modelPermissionsService.delDimension(1L);

        // Assert
        verify(ecmDimensionDefMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDelDimension_WhenDimensionIsRelated() {
        // Arrange
        List<EcmAppDimension> appDimensions = new ArrayList<>();
        EcmAppDimension appDimension = new EcmAppDimension();
        appDimension.setDimId(1L);
        appDimensions.add(appDimension);
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(appDimensions);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delDimension(1L);
        });

        verify(ecmDimensionDefMapper, never()).deleteById(1L);
    }

    @Test
    void testDelLotDimension() {
        // Arrange
        Long[] ids = {1L, 2L};
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        modelPermissionsService.delLotDimension(ids);

        // Assert
        verify(ecmDimensionDefMapper, times(1)).deleteBatchIds(Arrays.asList(ids));
    }

    @Test
    void testGetRoleDocRightList() {
        // Arrange
        docRightVO.setId(1L);

        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        List<EcmAppDocRel> appDocRels = new ArrayList<>();
        EcmAppDocRel docRel = new EcmAppDocRel();
        docRel.setDocCode("DOC001");
        appDocRels.add(docRel);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer = new EcmDocDefRelVer();
        docDefRelVer.setDocCode("DOC001");
        docDefRelVer.setDocName("测试文档");
        docDefRelVers.add(docDefRelVer);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        List<EcmDocDef> docDefs = new ArrayList<>();
        EcmDocDef docDef = new EcmDocDef();
        docDef.setDocCode("DOC001");
        docDef.setDocName("测试文档");
        docDef.setParent("0"); // Ensure non-null parent
        docDefs.add(docDef);

        // Mock all ecmDocDefMapper.selectList calls to return data with non-null parent
        when(ecmDocDefMapper.selectList(any())).thenReturn(docDefs);

        List<EcmDocTreeDTO> treeList = new ArrayList<>();
        EcmDocTreeDTO treeDto = new EcmDocTreeDTO();
        treeDto.setDocCode("DOC001");
        treeDto.setChildren(null);
        treeList.add(treeDto);
        when(staticTreePermissService.searchOldRelevanceInformationTreeNew(any(), any(), any(), any(), any(), any())).thenReturn(treeList);

        List<EcmDocrightDefDTO> emptyTemplate = new ArrayList<>();
        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
        dto.setDocCode("DOC001");
        dto.setDocName("测试文档");
        emptyTemplate.add(dto);
        when(staticTreePermissService.getDocRightDefEmptyTemplate(any(), any(), any())).thenReturn(emptyTemplate);

        // Act
        List<EcmDocrightDefDTO> result = modelPermissionsService.getRoleDocRightList(docRightVO);

        // Assert
        assertNotNull(result);
    }

    @Test
//    void testSaveRoleDocRight() {
//        // Arrange
//        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
//        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
//        dto.setDocCode("DOC001");
//        dto.setAddRight("1");
//        dto.setReadRight("1");
//        dto.setRightVer(1);
//        docRightList.add(dto);
//        docRightVO.setDocRightList(docRightList);
//
//        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
//        doReturn(1).when(ecmAppDocrightMapper).update(any(), any());
//
//        // Act
//        modelPermissionsService.saveRoleDocRight(docRightVO, accountToken, null);
//
//        // Assert
//        verify(ecmDocrightDefMapper, times(1)).delete(any());
//        verify(ecmDocrightDefMapper, times(1)).insert(any(EcmDocrightDef.class));
//    }

//    @Test
    void testGetLotDimDocRightList() {
        // Arrange
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        when(ecmDocrightDefMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        List<List<EcmDocrightDefDTO>> result = modelPermissionsService.getLotDimDocRightList(1L, "testUser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

//    @Test
//    void testSaveLotDimDocRight() {
//        // Arrange
//        List<List<EcmDocrightDefDTO>> lotDimDocRightList = new ArrayList<>();
//        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
//        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
//        dto.setDocCode("DOC001");
//        dto.setRightVer(1);
//        dto.setAppCode("APP001");
//        docRightList.add(dto);
//        lotDimDocRightList.add(docRightList);
//        docRightVO.setLotDimDocRightList(lotDimDocRightList);
//
//        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
//        doReturn(1).when(ecmAppDocrightMapper).update(any(), any());
//
//        // Act
//        modelPermissionsService.saveLotDimDocRight(docRightVO, accountToken, null);
//
//        // Assert
//        verify(ecmDocrightDefMapper, times(1)).delete(any());
//        verify(ecmDocrightDefMapper, times(1)).insert(any(EcmDocrightDef.class));
//    }

    @Test
    void testIsUse() {
        // Arrange
        addVerVo.setId(1L);
        addVerVo.setDimType(1);
        addVerVo.setIsUse(1);
        addVerVo.setUpdateUser("testUser");

        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        when(ecmDocrightDefMapper.selectCount(any())).thenReturn(1L);
        when(ecmDocrightDefMapper.update(any(), any())).thenReturn(1);

        // Act
        modelPermissionsService.isUse(addVerVo);

        // Assert
        verify(ecmDocrightDefMapper, times(1)).update(any(), any());
    }

    @Test
    void testIsUse_WhenNoRecordsExist() {
        // Arrange
        addVerVo.setId(1L);
        addVerVo.setDimType(1);
        addVerVo.setIsUse(1);
        addVerVo.setUpdateUser("testUser");

        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        when(ecmDocrightDefMapper.selectCount(any())).thenReturn(0L);
        when(ecmDocrightDefMapper.insert(any(EcmDocrightDef.class))).thenReturn(1);

        // Act
        modelPermissionsService.isUse(addVerVo);

        // Assert
        verify(ecmDocrightDefMapper, times(1)).insert(any(EcmDocrightDef.class));
    }

    @Test
    void testGetUserListByUserIds() {
        // Arrange
        List<String> userIds = Arrays.asList("user1", "user2");

        Result<List<SysUserDTO>> result = new Result<>();
        List<SysUserDTO> users = new ArrayList<>();
        SysUserDTO user1 = new SysUserDTO();
        user1.setLoginName("user1");
        user1.setName("用户1");
        users.add(user1);
        SysUserDTO user2 = new SysUserDTO();
        user2.setLoginName("user2");
        user2.setName("用户2");
        users.add(user2);
        result.setData(users);
        result.setCode(200);
        when(userApi.getUserListByUsernames(any())).thenReturn(result);

        // Act
        Map<String, List<SysUserDTO>> resultMap = modelPermissionsService.getUserListByUserIds(userIds);

        // Assert
        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());
        assertTrue(resultMap.containsKey("user1"));
        assertTrue(resultMap.containsKey("user2"));
    }

    @Test
    void testGetRoleList() {
        // Arrange
        SysRoleDTO sysRoleDTO = new SysRoleDTO();

        Result result = new Result();
        Map<String, Object> data = new HashMap<>();
        List<Map> list = new ArrayList<>();
        Map role = new HashMap();
        role.put("roleId", 1L);
        role.put("roleName", "测试角色");
        list.add(role);
        data.put("list", list);
        result.setData(data);
        result.setCode(200);
        when(roleApi.searchListInUsePage(any())).thenReturn(result);

        when(ecmDocrightDefMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        List<Map> result1 = modelPermissionsService.getRoleList(sysRoleDTO, "APP001", 1);

        // Assert
        assertNotNull(result1);
        assertFalse(result1.isEmpty());
    }

//    @Test
//    void testAddVer_WithBlankVersion() {
//        // Arrange
//        addVerVo.setAddVerType(DocRightConstants.BLANK_VER);
//        addVerVo.setSelectVerNo(null);
//
//        List<EcmAppDocRel> appDocRels = new ArrayList<>();
//        EcmAppDocRel docRel = new EcmAppDocRel();
//        docRel.setDocCode("DOC001");
//        appDocRels.add(docRel);
//        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);
//
//        when(ecmAppDocrightMapper.selectCount(any())).thenReturn(0L);
//        when(ecmAppDocrightMapper.insert(any())).thenAnswer(invocation -> {
//            EcmAppDocright appDocright = invocation.getArgument(0);
//            appDocright.setId(1L);
//            return 1;
//        });
//        when(snowflakeUtil.nextId()).thenReturn(1L);
//
//        List<EcmDocDef> docDefs = new ArrayList<>();
//        EcmDocDef docDef = new EcmDocDef();
//        docDef.setDocCode("DOC001");
//        docDef.setDocName("测试文档");
//        docDef.setDocSort(1.0f);
//        docDef.setParent("0");
//        docDefs.add(docDef);
//        when(ecmDocDefMapper.selectBatchIds(any())).thenReturn(docDefs);
//
//        SqlSession sqlSession = mock(SqlSession.class);
//        when(sqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(sqlSession);
//        when(sqlSession.getMapper(any())).thenReturn(ecmDocDefRelVerMapper);
//
//        // Act
//        String result = modelPermissionsService.addVer(addVerVo);
//
//        // Assert
//        assertNotNull(result);
//        verify(ecmAppDocrightMapper, times(1)).insert(any());
//    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testAddVer_WithExistingVersion() {
        // Arrange
        addVerVo.setAddVerType(DocRightConstants.EXISTING_VER);
        addVerVo.setSelectVerNo(0);

        List<EcmAppDocRel> appDocRels = new ArrayList<>();
        EcmAppDocRel docRel = new EcmAppDocRel();
        docRel.setDocCode("DOC001");
        appDocRels.add(docRel);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);

        when(ecmAppDocrightMapper.selectCount(any())).thenReturn(1L);
        when(ecmAppDocrightMapper.insert(any())).thenAnswer(invocation -> {
            EcmAppDocright appDocright = invocation.getArgument(0);
            appDocright.setId(1L);
            return 1;
        });
        when(snowflakeUtil.nextId()).thenReturn(1L);

        List<EcmDocDef> docDefs = new ArrayList<>();
        EcmDocDef docDef = new EcmDocDef();
        docDef.setDocCode("DOC001");
        docDef.setDocName("测试文档");
        docDef.setDocSort(1.0f);
        docDef.setParent("0");
        docDefs.add(docDef);
        when(ecmDocDefMapper.selectBatchIds(any())).thenReturn(docDefs);

        // Mock SqlSession for MybatisBatch (if needed)
        SqlSession sqlSession = mock(SqlSession.class);
        when(sqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(sqlSession);
        when(sqlSession.getMapper(EcmDocDefRelVerMapper.class)).thenReturn(ecmDocDefRelVerMapper);
        when(sqlSession.flushStatements()).thenReturn(new ArrayList<>());
        doNothing().when(sqlSession).commit();
        doNothing().when(sqlSession).close();

        List<EcmDocrightDef> docrightDefs = new ArrayList<>();
        EcmDocrightDef docrightDef = new EcmDocrightDef();
        docrightDef.setDocCode("DOC001");
        docrightDef.setRoleDimVal("1");
        docrightDef.setAppCode("TEST_APP");
        docrightDef.setRightVer(0);
        docrightDef.setAddRight("0");
        docrightDef.setDeleteRight("0");
        docrightDef.setUpdateRight("0");
        docrightDef.setReadRight("0");
        docrightDef.setThumRight("0");
        docrightDef.setPrintRight("0");
        docrightDef.setDownloadRight("0");
        docrightDef.setOtherUpdate("0");
        docrightDefs.add(docrightDef);
        when(ecmDocrightDefMapper.selectList(any())).thenReturn(docrightDefs);
        when(ecmDocrightDefMapper.insert(any())).thenReturn(1);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer = new EcmDocDefRelVer();
        docDefRelVer.setDocCode("DOC001");
        docDefRelVer.setAppCode("TEST_APP");
        docDefRelVer.setRightVer(1);
        docDefRelVers.add(docDefRelVer);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        // Act
        String result = modelPermissionsService.addVer(addVerVo);

        // Assert
        assertNotNull(result);
        verify(ecmAppDocrightMapper, times(1)).insert(any());
    }

    @Test
    void testSetRightNew() {
        // Arrange
        doReturn(1).when(ecmAppDocrightMapper).update(any(), any());

        // Act
        modelPermissionsService.setRightNew(1L, "APP001", "testUser");

        // Assert
        verify(ecmAppDocrightMapper, times(2)).update(any(), any());
    }

    @Test
    void testSetRightNew_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.setRightNew(null, "APP001", "testUser");
        });
    }

    @Test
    void testGetRelateDimList() {
        // Arrange
        docRightVO.setAppCode("APP001");
        docRightVO.setRightVer(1);

        List<EcmAppDimension> appDimensions = new ArrayList<>();
        EcmAppDimension appDimension = new EcmAppDimension();
        appDimension.setDimId(1L);
        appDimension.setRequired(1);
        appDimensions.add(appDimension);
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(appDimensions);

        List<EcmDimensionDef> dimensionDefs = new ArrayList<>();
        EcmDimensionDef dimensionDef = new EcmDimensionDef();
        dimensionDef.setId(1L);
        dimensionDef.setDimCode("DIM001");
        dimensionDef.setDimName("测试维度");
        dimensionDefs.add(dimensionDef);
        when(ecmDimensionDefMapper.selectBatchIds(any())).thenReturn(dimensionDefs);

        // Act
        List<EcmDimensionDefDTO> result = modelPermissionsService.getRelateDimList(docRightVO);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetRelateDimList_Empty() {
        // Arrange
        docRightVO.setAppCode("APP001");
        docRightVO.setRightVer(1);
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        List<EcmDimensionDefDTO> result = modelPermissionsService.getRelateDimList(docRightVO);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

//    @Test
//    void testRelateDimToApp_WithRightVer() {
//        // Arrange
//        docRightVO.setAppCode("hz");
//        docRightVO.setRightVer(5);
//        docRightVO.setCurrentUser("testUser");
//
//        List<EcmDimensionDefDTO> relateDimList = new ArrayList<>();
//        EcmDimensionDefDTO dimDto = new EcmDimensionDefDTO();
//        dimDto.setId(1L);
//        dimDto.setRequired(1);
//        relateDimList.add(dimDto);
//        docRightVO.setRelateDimList(relateDimList);
//
//        List<EcmAppDocRel> appDocRels = new ArrayList<>();
//        EcmAppDocRel docRel = new EcmAppDocRel();
//        docRel.setDocCode("sfz");
//        appDocRels.add(docRel);
//        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);
//
//        when(ecmAppDimensionMapper.delete(any())).thenReturn(1);
//        when(ecmAppDimensionMapper.insert(any())).thenReturn(1);
//        when(ecmDocrightDefMapper.selectList(any())).thenReturn(new ArrayList<>());
//        when(ecmAppDocrightMapper.selectList(any())).thenReturn(new ArrayList<>());
//        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
//
//        // Act
//        List result = modelPermissionsService.relateDimToApp(docRightVO);
//
//        // Assert
//        assertNotNull(result);
//    }

    @Test
    void testRelateDimToApp_WithoutRightVer() {
        // Arrange
        docRightVO.setRightVer(null);
        List<EcmDimensionDefDTO> relateDimList = new ArrayList<>();
        EcmDimensionDefDTO dimDto = new EcmDimensionDefDTO();
        dimDto.setId(1L);
        dimDto.setDimValue("value1;value2");
        dimDto.setRequired(1);
        relateDimList.add(dimDto);
        docRightVO.setRelateDimList(relateDimList);

        // Act
        List result = modelPermissionsService.relateDimToApp(docRightVO);

        // Assert
        assertNotNull(result);
    }

//    @Test
//    void testSaveRoleAndLotDimDocRight_CreateNewVersion() {
//        // Arrange
//        DocRightRoleAndLotVO docRightRoleAndLotVO = new DocRightRoleAndLotVO();
//        docRightRoleAndLotVO.setAppCode("APP001");
//        docRightRoleAndLotVO.setRightVer(null);
//        docRightRoleAndLotVO.setLotDimUse(1);
//
//        DocRightVO roleDocRight = new DocRightVO();
//        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
//        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
//        dto.setDocCode("DOC001");
//        dto.setRightVer(1);
//        dto.setAddRight("1");
//        dto.setReadRight("1");
//        dto.setUpdateRight("0");
//        dto.setDeleteRight("0");
//        dto.setThumRight("0");
//        dto.setDownloadRight("0");
//        dto.setPrintRight("0");
//        dto.setOtherUpdate("0");
//        docRightList.add(dto);
//        roleDocRight.setDocRightList(docRightList);
//        roleDocRight.setRoleId(1L);
//        docRightRoleAndLotVO.setRoleDocRight(roleDocRight);
//
//        List<EcmAppDocRelDTO> ecmDocTreeVO = new ArrayList<>();
//        EcmAppDocRelDTO docRelDTO = new EcmAppDocRelDTO();
//        docRelDTO.setDocCode("DOC001");
//        docRelDTO.setChildren(null);
//        docRelDTO.setDocSort(1.0f);
//        ecmDocTreeVO.add(docRelDTO);
//        docRightRoleAndLotVO.setEcmDocTreeVO(ecmDocTreeVO);
//
//        when(ecmAppDocrightMapper.selectCount(any())).thenReturn(0L);
//        when(ecmAppDocrightMapper.insert(any())).thenAnswer(invocation -> {
//            EcmAppDocright appDocright = invocation.getArgument(0);
//            appDocright.setId(1L);
//            return 1;
//        });
//        when(snowflakeUtil.nextId()).thenReturn(1L);
//        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());
//        when(ecmAppDocRelMapper.delete(any())).thenReturn(1);
//
//        // Mock SqlSession for MybatisBatch
//        SqlSession sqlSession = mock(SqlSession.class);
//        when(sqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(sqlSession);
//        when(sqlSession.getMapper(EcmAppDocRelMapper.class)).thenReturn(ecmAppDocRelMapper);
//        // Mock required SqlSession methods
//        when(sqlSession.flushStatements()).thenReturn(new ArrayList<>());
//        doNothing().when(sqlSession).commit();
//        doNothing().when(sqlSession).close();
//
//        List<EcmDocDef> docDefs = new ArrayList<>();
//        EcmDocDef docDef = new EcmDocDef();
//        docDef.setDocCode("DOC001");
//        docDef.setDocName("测试文档");
//        docDef.setDocSort(1.0f);
//        docDef.setParent("0");
//        docDefs.add(docDef);
//        when(ecmDocDefMapper.selectBatchIds(any())).thenReturn(docDefs);
//
//        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
//        when(ecmDocrightDefMapper.insert(any())).thenReturn(1);
//        doReturn(1).when(ecmAppDocrightMapper).update(any(), any());
//
//        // Act
//        modelPermissionsService.saveRoleAndLotDimDocRight(docRightRoleAndLotVO, accountToken);
//
//        // Assert
//        verify(ecmAppDocrightMapper, atLeastOnce()).insert(any());
//    }

//    @Test
//    void testSaveRoleAndLotDimDocRight_UpdateExistingVersion() {
//        // Arrange
//        DocRightRoleAndLotVO docRightRoleAndLotVO = new DocRightRoleAndLotVO();
//        docRightRoleAndLotVO.setAppCode("APP001");
//        docRightRoleAndLotVO.setRightVer(1);
//        docRightRoleAndLotVO.setLotDimUse(1);
//
//        DocRightVO roleDocRight = new DocRightVO();
//        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
//        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
//        dto.setDocCode("DOC001");
//        dto.setRightVer(1);
//        docRightList.add(dto);
//        roleDocRight.setDocRightList(docRightList);
//        roleDocRight.setRoleId(1L);
//        docRightRoleAndLotVO.setRoleDocRight(roleDocRight);
//
//        List<EcmAppDocRelDTO> ecmDocTreeVO = new ArrayList<>();
//        EcmAppDocRelDTO docRelDTO = new EcmAppDocRelDTO();
//        docRelDTO.setDocCode("DOC001");
//        docRelDTO.setChildren(null);
//        docRelDTO.setDocSort(1.0f);
//        ecmDocTreeVO.add(docRelDTO);
//        docRightRoleAndLotVO.setEcmDocTreeVO(ecmDocTreeVO);
//
//        doReturn(1).when(ecmAppDocrightMapper).update(any(), any());
//        when(ecmDocDefRelVerMapper.delete(any())).thenReturn(1);
//        when(snowflakeUtil.nextId()).thenReturn(1L);
//        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());
//        when(ecmAppDocRelMapper.delete(any())).thenReturn(1);
//
//        SqlSession sqlSession = mock(SqlSession.class);
//        when(sqlSessionFactory.openSession(ExecutorType.BATCH)).thenReturn(sqlSession);
//        when(sqlSession.getMapper(any())).thenReturn(ecmAppDocRelMapper, ecmDocDefRelVerMapper);
//
//        List<EcmDocDef> docDefs = new ArrayList<>();
//        EcmDocDef docDef = new EcmDocDef();
//        docDef.setDocCode("DOC001");
//        docDef.setDocName("测试文档");
//        docDefs.add(docDef);
//        when(ecmDocDefMapper.selectBatchIds(any())).thenReturn(docDefs);
//
//        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
//        when(ecmDocrightDefMapper.insert(any())).thenReturn(1);
//
//        // Act
//        modelPermissionsService.saveRoleAndLotDimDocRight(docRightRoleAndLotVO, accountToken);
//
//        // Assert
//        verify(ecmAppDocrightMapper, atLeastOnce()).update(any(), any());
//    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testRelevanceInformation() {
        // Arrange
        EcmDocTreeVO ecmDocTreeVO = new EcmDocTreeVO();
        ecmDocTreeVO.setAppCode("APP001");

        List<EcmAppDocRelDTO> list = new ArrayList<>();
        EcmAppDocRelDTO docRelDTO = new EcmAppDocRelDTO();
        docRelDTO.setDocCode("DOC001");
        docRelDTO.setDocSort(1.0f);
        docRelDTO.setChildren(null);
        list.add(docRelDTO);
        ecmDocTreeVO.setList(list);

        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppDocRelMapper.delete(any())).thenReturn(1);
        when(snowflakeUtil.nextId()).thenReturn(1L);

        // Mock SqlSession for MybatisBatch
        SqlSession sqlSession = mock(SqlSession.class);
        when(sqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(sqlSession);
        when(sqlSession.getMapper(EcmAppDocRelMapper.class)).thenReturn(ecmAppDocRelMapper);
        // Mock required SqlSession methods
        when(sqlSession.flushStatements()).thenReturn(new ArrayList<>());
        doNothing().when(sqlSession).commit();
        doNothing().when(sqlSession).close();


        // Act
        List<EcmAppDocRel> result = modelPermissionsService.relevanceInformation(ecmDocTreeVO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetUserListByUserIds_Failed() {
        // Arrange
        List<String> userIds = Arrays.asList("user1", "user2");

        Result<List<SysUserDTO>> result = new Result<>();
        result.setCode(500);
        result.setMsg("Error");
        when(userApi.getUserListByUsernames(any())).thenReturn(result);

        // Act & Assert
        assertThrows(SunyardException.class, () -> {
            modelPermissionsService.getUserListByUserIds(userIds);
        });
    }

    @Test
    void testGetUserListByUserIds_EmptyResult() {
        // Arrange
        List<String> userIds = Arrays.asList("user1", "user2");

        Result<List<SysUserDTO>> result = new Result<>();
        result.setCode(200);
        result.setData(null);
        when(userApi.getUserListByUsernames(any())).thenReturn(result);

        // Act
        Map<String, List<SysUserDTO>> resultMap = modelPermissionsService.getUserListByUserIds(userIds);

        // Assert
        assertNull(resultMap);
    }

    @Test
    void testGetRoleDocRightList_WithExistingData() {
        // Arrange
        docRightVO.setId(1L);

        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        List<EcmAppDocRel> appDocRels = new ArrayList<>();
        EcmAppDocRel docRel = new EcmAppDocRel();
        docRel.setDocCode("DOC001");
        appDocRels.add(docRel);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer = new EcmDocDefRelVer();
        docDefRelVer.setDocCode("DOC001");
        docDefRelVer.setDocName("测试文档");
        docDefRelVers.add(docDefRelVer);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        List<EcmDocDef> docDefs = new ArrayList<>();
        EcmDocDef docDef = new EcmDocDef();
        docDef.setDocCode("DOC001");
        docDef.setDocName("测试文档");
        docDef.setParent("0"); // Ensure non-null parent
        docDefs.add(docDef);

        // Mock all ecmDocDefMapper.selectList calls to return data with non-null parent
        when(ecmDocDefMapper.selectList(any())).thenReturn(docDefs);

        List<EcmDocTreeDTO> treeList = new ArrayList<>();
        EcmDocTreeDTO treeDto = new EcmDocTreeDTO();
        treeDto.setDocCode("DOC001");
        treeDto.setChildren(null);
        treeList.add(treeDto);
        when(staticTreePermissService.searchOldRelevanceInformationTreeNew(any(), any(), any(), any(), any(), any())).thenReturn(treeList);

        List<EcmDocrightDef> docrightDefs = new ArrayList<>();
        EcmDocrightDef docrightDef = new EcmDocrightDef();
        docrightDef.setDocCode("DOC001");
        docrightDef.setOtherUpdate("1");
        docrightDefs.add(docrightDef);
        when(ecmDocrightDefMapper.selectList(any())).thenReturn(docrightDefs);

        // Act
        List<EcmDocrightDefDTO> result = modelPermissionsService.getRoleDocRightList(docRightVO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetLotDimDocRightList_WithExistingData() {
        // Arrange
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        List<EcmDocrightDef> docrightDefs = new ArrayList<>();
        EcmDocrightDef docrightDef = new EcmDocrightDef();
        docrightDef.setDocCode("DOC001");
        docrightDef.setRoleDimVal("dim1;dim2");
        docrightDef.setRightVer(1);
        docrightDefs.add(docrightDef);
        when(ecmDocrightDefMapper.selectList(any())).thenReturn(docrightDefs);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer = new EcmDocDefRelVer();
        docDefRelVer.setDocCode("DOC001");
        docDefRelVer.setDocName("测试文档");
        docDefRelVers.add(docDefRelVer);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        // Act
        List<List<EcmDocrightDefDTO>> result = modelPermissionsService.getLotDimDocRightList(1L, "testUser");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSaveLotDimDocRight_WithRelateDimList() {
        // Arrange
        List<List<EcmDocrightDefDTO>> lotDimDocRightList = new ArrayList<>();
        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
        dto.setDocCode("DOC001");
        dto.setRightVer(1);
        dto.setAppCode("APP001");
        dto.setAddRight("1");
        dto.setReadRight("1");
        dto.setUpdateRight("0");
        dto.setDeleteRight("0");
        dto.setThumRight("0");
        dto.setDownloadRight("0");
        dto.setPrintRight("0");
        dto.setOtherUpdate("0");
        docRightList.add(dto);
        lotDimDocRightList.add(docRightList);
        docRightVO.setLotDimDocRightList(lotDimDocRightList);
        docRightVO.setAppCode("APP001");

        // 设置relateDimList，确保不为null
        List<EcmDimensionDefDTO> relateDimList = new ArrayList<>();
        EcmDimensionDefDTO dimDto = new EcmDimensionDefDTO();
        dimDto.setId(1L);
        dimDto.setRequired(1);
        dimDto.setDimCode("DIM001");
        dimDto.setDimValue("val1;val2");
        relateDimList.add(dimDto);
        docRightVO.setRelateDimList(relateDimList);

        // Mock所有相关操作，确保不调用实际的Mapper方法
        when(ecmAppDimensionMapper.insert(any())).thenReturn(1);
        // Mock delete操作，避免LambdaQueryWrapper问题
        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
        when(ecmDocrightDefMapper.insert(any())).thenReturn(1);
        // Mock私有方法updateEcmAppDocRight来避免LambdaUpdateWrapper问题
        doNothing().when(modelPermissionsService).updateEcmAppDocRight(any(), any(), any());

        // Act
        modelPermissionsService.saveLotDimDocRight(docRightVO, accountToken, 1);

        // Assert
        verify(ecmAppDimensionMapper, times(1)).insert(any());
        verify(ecmDocrightDefMapper, times(1)).delete(any());
        verify(ecmDocrightDefMapper, times(1)).insert(any());
        verify(modelPermissionsService, times(1)).updateEcmAppDocRight(any(), any(), any());
    }

    @Test
    void testGetDocRightListSetting_WithMultipleDocCodes() {
        // Arrange
        docRightVO.setId(1L);
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);

        List<EcmAppDocRel> appDocRels = new ArrayList<>();
        EcmAppDocRel docRel1 = new EcmAppDocRel();
        docRel1.setDocCode("DOC001");
        EcmAppDocRel docRel2 = new EcmAppDocRel();
        docRel2.setDocCode("DOC002");
        appDocRels.add(docRel1);
        appDocRels.add(docRel2);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer1 = new EcmDocDefRelVer();
        docDefRelVer1.setDocCode("DOC001");
        docDefRelVer1.setDocName("文档类型1");
        docDefRelVer1.setDocSort(1.0f);
        EcmDocDefRelVer docDefRelVer2 = new EcmDocDefRelVer();
        docDefRelVer2.setDocCode("DOC002");
        docDefRelVer2.setDocName("文档类型2");
        docDefRelVer2.setDocSort(2.0f);
        docDefRelVers.add(docDefRelVer1);
        docDefRelVers.add(docDefRelVer2);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        List<EcmDocDef> docDefs = new ArrayList<>();
        EcmDocDef docDef1 = new EcmDocDef();
        docDef1.setDocCode("DOC001");
        docDef1.setDocName("文档类型1");
        docDef1.setDocSort(1.0f);
        docDef1.setParent("0");
        EcmDocDef docDef2 = new EcmDocDef();
        docDef2.setDocCode("DOC002");
        docDef2.setDocName("文档类型2");
        docDef2.setDocSort(2.0f);
        docDef2.setParent("0");
        docDefs.add(docDef1);
        docDefs.add(docDef2);
        when(ecmDocDefMapper.selectList(any())).thenReturn(docDefs);

        List<EcmDocrightDef> docrightDefs = new ArrayList<>();
        EcmDocrightDef docrightDef = new EcmDocrightDef();
        docrightDef.setDocCode("DOC001");
        docrightDef.setOtherUpdate("1");
        docrightDefs.add(docrightDef);
        when(ecmDocrightDefMapper.selectList(any())).thenReturn(docrightDefs);

        List<EcmDocDefDTO> docDefDTOs = new ArrayList<>();
        EcmDocDefDTO docDefDTO1 = new EcmDocDefDTO();
        docDefDTO1.setDocCode("DOC001");
        docDefDTO1.setParent("0");
        EcmDocDefDTO docDefDTO2 = new EcmDocDefDTO();
        docDefDTO2.setDocCode("DOC002");
        docDefDTO2.setParent("0");
        docDefDTOs.add(docDefDTO1);
        docDefDTOs.add(docDefDTO2);

        List<EcmDocTreeDTO> treeList = new ArrayList<>();
        EcmDocTreeDTO tree1 = new EcmDocTreeDTO();
        tree1.setDocCode("DOC001");
        tree1.setChildren(null);
        EcmDocTreeDTO tree2 = new EcmDocTreeDTO();
        tree2.setDocCode("DOC002");
        tree2.setChildren(null);
        treeList.add(tree1);
        treeList.add(tree2);
        when(staticTreePermissService.searchOldRelevanceInformationTreeNew(any(), any(), any(), any(), any(), any())).thenReturn(treeList);

        // Act
        List<EcmDocrightDefDTO> result = modelPermissionsService.getDocRightListSetting(docRightVO, appDocright);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testCheckParamAddVer_MissingRightVer() {
        // Arrange
        addVerVo.setRightVer(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }

    @Test
    void testCheckParamAddVer_MissingAddVerType() {
        // Arrange
        addVerVo.setAddVerType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }

    @Test
    void testCheckParamAddVer_MissingSelectVerNo() {
        // Arrange
        addVerVo.setAddVerType(DocRightConstants.EXISTING_VER);
        addVerVo.setSelectVerNo(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }

    @Test
    void testAddVer_NoRelatedDocuments() {
        // Arrange
        addVerVo.setAddVerType(DocRightConstants.BLANK_VER);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }

    @Test
    void testSaveRoleDocRight_WithNullVerNo() {
        // Arrange
        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
        EcmDocrightDefDTO dto = new EcmDocrightDefDTO();
        dto.setDocCode("DOC001");
        dto.setAddRight("1");
        dto.setReadRight("1");
        dto.setRightVer(1);
        dto.setUpdateRight("0");
        dto.setDeleteRight("0");
        dto.setThumRight("0");
        dto.setDownloadRight("0");
        dto.setPrintRight("0");
        dto.setOtherUpdate("0");
        docRightList.add(dto);
        docRightVO.setDocRightList(docRightList);
        docRightVO.setRightVer(2);

        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);
        when(ecmDocrightDefMapper.insert(any())).thenReturn(1);
        // Mock私有方法updateEcmAppDocRight来避免LambdaUpdateWrapper问题
        doNothing().when(modelPermissionsService).updateEcmAppDocRight(any(), any(), any());

        // Act
        modelPermissionsService.saveRoleDocRight(docRightVO, accountToken, null);

        // Assert
        verify(ecmDocrightDefMapper, times(1)).delete(any());
        verify(ecmDocrightDefMapper, times(1)).insert(any(EcmDocrightDef.class));
        verify(modelPermissionsService, times(1)).updateEcmAppDocRight(any(), any(), any());
    }

    @Test
    void testRelateDimToApp_NoRelatedDocuments() {
        // Arrange
        docRightVO.setRightVer(1);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.relateDimToApp(docRightVO);
        });
    }

    @Test
    void testGetLotDimDocRightList_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.getLotDimDocRightList(null, "testUser");
        });
    }

    @Test
    void testGetLotDimDocRightList_NullAppDocright() {
        // Arrange
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(null);

        // Act
        List<List<EcmDocrightDefDTO>> result = modelPermissionsService.getLotDimDocRightList(1L, "testUser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRelateDimToApp_WithExistingPermissions() {
        // Arrange
        docRightVO.setAppCode("APP001");
        docRightVO.setRightVer(1);
        docRightVO.setCurrentUser("testUser");

        List<EcmDimensionDefDTO> relateDimList = new ArrayList<>();
        EcmDimensionDefDTO dimDto = new EcmDimensionDefDTO();
        dimDto.setId(1L);
        dimDto.setRequired(1);
        relateDimList.add(dimDto);
        docRightVO.setRelateDimList(relateDimList);

        List<EcmAppDocRel> appDocRels = new ArrayList<>();
        EcmAppDocRel docRel = new EcmAppDocRel();
        docRel.setDocCode("DOC001");
        appDocRels.add(docRel);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);

        when(ecmAppDimensionMapper.delete(any())).thenReturn(1);
        when(ecmAppDimensionMapper.insert(any())).thenReturn(1);

        List<EcmDocrightDef> docrightDefs = new ArrayList<>();
        EcmDocrightDef docrightDef = new EcmDocrightDef();
        docrightDef.setDocCode("DOC001");
        docrightDef.setRoleDimVal("1;2");
        docrightDef.setAddRight("1");
        docrightDef.setDeleteRight("1");
        docrightDef.setUpdateRight("1");
        docrightDef.setReadRight("1");
        docrightDef.setThumRight("1");
        docrightDef.setDownloadRight("1");
        docrightDef.setPrintRight("1");
        docrightDef.setOtherUpdate("1");
        docrightDefs.add(docrightDef);
        when(ecmDocrightDefMapper.selectList(any())).thenReturn(docrightDefs);

        List<EcmAppDocright> appDocrights = new ArrayList<>();
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        appDocrights.add(appDocright);
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(appDocrights);

        List<EcmAppDimension> appDimensions = new ArrayList<>();
        EcmAppDimension appDimension = new EcmAppDimension();
        appDimension.setDimId(1L);
        appDimensions.add(appDimension);
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(appDimensions);

        List<EcmDimensionDef> dimensionDefs = new ArrayList<>();
        EcmDimensionDef dimensionDef = new EcmDimensionDef();
        dimensionDef.setId(1L);
        dimensionDef.setDimCode("DIM001");
        dimensionDef.setDimValue("val1;val2");
        dimensionDefs.add(dimensionDef);
        when(ecmDimensionDefMapper.selectBatchIds(any())).thenReturn(dimensionDefs);

        List<EcmAppDocRel> appDocRels2 = new ArrayList<>();
        EcmAppDocRel docRel2 = new EcmAppDocRel();
        docRel2.setDocCode("DOC001");
        appDocRels2.add(docRel2);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels2);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer = new EcmDocDefRelVer();
        docDefRelVer.setDocCode("DOC001");
        docDefRelVer.setDocName("文档");
        docDefRelVers.add(docDefRelVer);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        when(ecmDocrightDefMapper.delete(any())).thenReturn(1);

        // Act
        List result = modelPermissionsService.relateDimToApp(docRightVO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetRoleList_WithExistingRights() {
        // Arrange
        SysRoleDTO sysRoleDTO = new SysRoleDTO();

        Result result = new Result();
        Map<String, Object> data = new HashMap<>();
        List<Map> list = new ArrayList<>();
        Map role = new HashMap();
        role.put("roleId", 1L);
        role.put("roleName", "测试角色");
        list.add(role);
        data.put("list", list);
        result.setData(data);
        result.setCode(200);
        when(roleApi.searchListInUsePage(any())).thenReturn(result);

        List<EcmDocrightDef> docrightDefs = new ArrayList<>();
        EcmDocrightDef docrightDef = new EcmDocrightDef();
        docrightDef.setRoleDimVal("1");
        docrightDefs.add(docrightDef);
        when(ecmDocrightDefMapper.selectList(any())).thenReturn(docrightDefs);

        // Act
        List<Map> result1 = modelPermissionsService.getRoleList(sysRoleDTO, "APP001", 1);

        // Assert
        assertNotNull(result1);
        assertFalse(result1.isEmpty());
        assertEquals(false, result1.get(0).get("isUse"));
    }

    @Test
    void testGetRoleList_WithNullRightVer() {
        // Arrange
        SysRoleDTO sysRoleDTO = new SysRoleDTO();

        Result result = new Result();
        Map<String, Object> data = new HashMap<>();
        List<Map> list = new ArrayList<>();
        Map role = new HashMap();
        role.put("roleId", 1L);
        role.put("roleName", "测试角色");
        list.add(role);
        data.put("list", list);
        result.setData(data);
        result.setCode(200);
        when(roleApi.searchListInUsePage(any())).thenReturn(result);

        // Act
        List<Map> result1 = modelPermissionsService.getRoleList(sysRoleDTO, "APP001", null);

        // Assert
        assertNotNull(result1);
        assertFalse(result1.isEmpty());
        assertEquals(false, result1.get(0).get("isUse"));
    }

    @Test
    void testGetDimensionList_EmptyList() {
        // Arrange
        when(ecmDimensionDefMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act
        List<EcmDimensionDefDTO> result = modelPermissionsService.getDimensionList();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

//    @Test
//    void testGetDimensionList_WithUserApiFailure() {
//        // Arrange
//        List<EcmDimensionDef> dimensionDefs = new ArrayList<>();
//        EcmDimensionDef dimensionDef = new EcmDimensionDef();
//        dimensionDef.setCreateUser("testUser");
//        dimensionDefs.add(dimensionDef);
//        when(ecmDimensionDefMapper.selectList(any())).thenReturn(dimensionDefs);
//
//        Result<List<SysUserDTO>> userResult = new Result<>();
//        userResult.setCode(500);
//        userResult.setMsg("Error");
//        when(userApi.getUserListByUsernames(any())).thenReturn(userResult);
//
//        // Act
//        List<EcmDimensionDefDTO> result = modelPermissionsService.getDimensionList();
//
//        // Assert
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        // Should still work even if user API fails
//    }

    @Test
    void testEditDimension_WithValidationError() {
        // Arrange
        dimensionDef.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.editDimension(dimensionDef);
        });

        verify(ecmDimensionDefMapper, never()).update(any(), any());
    }

    @Test
    void testDelDimension_WithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delDimension(null);
        });

        verify(ecmDimensionDefMapper, never()).deleteById(any());
    }

    @Test
    void testDelLotDimension_WithEmptyArray() {
        // Arrange
        Long[] ids = new Long[]{};

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delLotDimension(ids);
        });

        verify(ecmDimensionDefMapper, never()).deleteBatchIds(any());
    }

    @Test
    void testDelLotDimension_WithNullArray() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delLotDimension(null);
        });

        verify(ecmDimensionDefMapper, never()).deleteBatchIds(any());
    }

    @Test
    void testGetRelateDimList_WithNullAppCode() {
        // Arrange
        docRightVO.setAppCode(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.getRelateDimList(docRightVO);
        });
    }

    @Test
    void testGetRelateDimList_WithNullRightVer() {
        // Arrange
        docRightVO.setRightVer(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.getRelateDimList(docRightVO);
        });
    }

    @Test
    void testGetLotDimDocRightList_WithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.getLotDimDocRightList(null, "testUser");
        });
    }

    @Test
    void testGetLotDimDocRightList_WithNullAppDocright() {
        // Arrange
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(null);

        // Act
        List<List<EcmDocrightDefDTO>> result = modelPermissionsService.getLotDimDocRightList(1L, "testUser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLotDimDocRightList_WithEmptyDocrightDefs() {
        // Arrange
        EcmAppDocright appDocright = new EcmAppDocright();
        appDocright.setId(1L);
        appDocright.setAppCode("APP001");
        appDocright.setRightVer(1);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(appDocright);

        when(ecmDocrightDefMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<EcmAppDimension> appDimensions = new ArrayList<>();
        EcmAppDimension appDimension = new EcmAppDimension();
        appDimension.setDimId(1L);
        appDimensions.add(appDimension);
        when(ecmAppDimensionMapper.selectList(any())).thenReturn(appDimensions);

        List<EcmAppDocRel> appDocRels = new ArrayList<>();
        EcmAppDocRel docRel = new EcmAppDocRel();
        docRel.setDocCode("DOC001");
        appDocRels.add(docRel);
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(appDocRels);

        List<EcmDocDefRelVer> docDefRelVers = new ArrayList<>();
        EcmDocDefRelVer docDefRelVer = new EcmDocDefRelVer();
        docDefRelVer.setDocCode("DOC001");
        docDefRelVer.setDocName("文档");
        docDefRelVers.add(docDefRelVer);
        when(ecmDocDefRelVerMapper.selectList(any())).thenReturn(docDefRelVers);

        // Act
        List<List<EcmDocrightDefDTO>> result = modelPermissionsService.getLotDimDocRightList(1L, "testUser");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testSaveLotDimDocRight_WithNullLotDimDocRightList() {
        // Arrange
        docRightVO.setLotDimDocRightList(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.saveLotDimDocRight(docRightVO, accountToken, null);
        });
    }

    @Test
    void testSaveLotDimDocRight_WithEmptyLotDimDocRightList() {
        // Arrange
        docRightVO.setLotDimDocRightList(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.saveLotDimDocRight(docRightVO, accountToken, null);
        });
    }

    @Test
    void testDetailsVer_WithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.detailsVer(null);
        });
    }

    @Test
    void testDetailsVer_WithEmptyResult() {
        // Arrange
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act
        EcmAppDocrightDTO result = modelPermissionsService.detailsVer(1L);

        // Assert
        assertNull(result);
    }

    @Test
    void testDelVer_WithNullRightVer() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delVer(null, "APP001");
        });
    }

    @Test
    void testDelVer_WithNullAppCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.delVer(1, null);
        });
    }

    @Test
    void testRelevanceInformation_WithNullAppCode() {
        // Arrange
        EcmDocTreeVO ecmDocTreeVO = new EcmDocTreeVO();
        ecmDocTreeVO.setAppCode(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.relevanceInformation(ecmDocTreeVO);
        });
    }

    @Test
    void testRelevanceInformation_WithEmptyList() {
        // Arrange
        EcmDocTreeVO ecmDocTreeVO = new EcmDocTreeVO();
        ecmDocTreeVO.setAppCode("APP001");
        ecmDocTreeVO.setList(null);

        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Act
        List<EcmAppDocRel> result = modelPermissionsService.relevanceInformation(ecmDocTreeVO);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRoleList_WithApiFailure() {
        // Arrange
        SysRoleDTO sysRoleDTO = new SysRoleDTO();

        Result result = new Result();
        result.setCode(500);
        result.setMsg("API Error");
        when(roleApi.searchListInUsePage(any())).thenReturn(result);

        // Act
        List<Map> result1 = modelPermissionsService.getRoleList(sysRoleDTO, "APP001", 1);

        // Assert
        assertNotNull(result1);
        assertTrue(result1.isEmpty());
    }

    @Test
    void testGetRoleList_WithNullData() {
        // Arrange
        SysRoleDTO sysRoleDTO = new SysRoleDTO();

        Result result = new Result();
        result.setCode(200);
        result.setData(null);
        when(roleApi.searchListInUsePage(any())).thenReturn(result);

        // Act
        List<Map> result1 = modelPermissionsService.getRoleList(sysRoleDTO, "APP001", 1);

        // Assert
        assertNotNull(result1);
        assertTrue(result1.isEmpty());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testSaveRoleAndLotDimDocRight_WithNullRightVer() {
        // Arrange
        DocRightRoleAndLotVO docRightRoleAndLotVO = new DocRightRoleAndLotVO();
        docRightRoleAndLotVO.setAppCode("APP001");
        docRightRoleAndLotVO.setRightVer(null);

        when(ecmAppDocrightMapper.selectCount(any())).thenReturn(0L);
        when(ecmAppDocrightMapper.insert(any())).thenAnswer(invocation -> {
            EcmAppDocright appDocright = invocation.getArgument(0);
            appDocright.setId(1L);
            return 1;
        });
        when(snowflakeUtil.nextId()).thenReturn(1L);

        // Mock other required dependencies
        when(ecmAppDocRelMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmAppDocRelMapper.delete(any())).thenReturn(1);
        when(ecmDocDefMapper.selectBatchIds(any())).thenReturn(new ArrayList<>());

        // Mock SqlSession for MybatisBatch
        SqlSession sqlSession = mock(SqlSession.class);
        when(sqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(sqlSession);
        when(sqlSession.getMapper(EcmAppDocRelMapper.class)).thenReturn(ecmAppDocRelMapper);
        // Mock required SqlSession methods
        when(sqlSession.flushStatements()).thenReturn(new ArrayList<>());
        doNothing().when(sqlSession).commit();
        doNothing().when(sqlSession).close();

        // Act
        modelPermissionsService.saveRoleAndLotDimDocRight(docRightRoleAndLotVO, accountToken);

        // Assert
        verify(ecmAppDocrightMapper, atLeastOnce()).insert(any());
    }

    @Test
    void testGetRightVerList_WithNullAppCode() {
        // Arrange
        addVerVo.setAppCode(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.getRightVerList(addVerVo);
        });
    }

    @Test
    void testGetRightVerList_WithEmptyResults() {
        // Arrange
        addVerVo.setAppCode("APP001");
        addVerVo.setPageNum(1);
        addVerVo.setPageSize(10);

        when(ecmAppDocrightMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act
        PageInfo result = modelPermissionsService.getRightVerList(addVerVo);

        // Assert
        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void testGetRoleDocRightList_WithNullId() {
        // Arrange
        docRightVO.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.getRoleDocRightList(docRightVO);
        });
    }

    @Test
    void testGetRoleDocRightList_WithNullAppDocright() {
        // Arrange
        docRightVO.setId(1L);
        when(ecmAppDocrightMapper.selectById(anyLong())).thenReturn(null);

        // Act
        List<EcmDocrightDefDTO> result = modelPermissionsService.getRoleDocRightList(docRightVO);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveRoleDocRight_WithNullRoleId() {
        // Arrange
        docRightVO.setRoleId(null);
        docRightVO.setDocRightList(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.saveRoleDocRight(docRightVO, accountToken, null);
        });
    }

    @Test
    void testSaveRoleDocRight_WithNullDocRightList() {
        // Arrange
        docRightVO.setRoleId(1L);
        docRightVO.setDocRightList(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.saveRoleDocRight(docRightVO, accountToken, null);
        });
    }

    @Test
    void testIsUse_WithNullId() {
        // Arrange
        addVerVo.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.isUse(addVerVo);
        });
    }

    @Test
    void testIsUse_WithNullDimType() {
        // Arrange
        addVerVo.setId(1L);
        addVerVo.setDimType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.isUse(addVerVo);
        });
    }

    @Test
    void testIsUse_WithNullIsUse() {
        // Arrange
        addVerVo.setId(1L);
        addVerVo.setDimType(1);
        addVerVo.setIsUse(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.isUse(addVerVo);
        });
    }

    @Test
    void testSetRightNew_WithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.setRightNew(null, "APP001", "testUser");
        });
    }

    @Test
    void testSetRightNew_WithNullAppCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.setRightNew(1L, null, "testUser");
        });
    }

    @Test
    void testAddVer_WithNullRightVer() {
        // Arrange
        addVerVo.setRightVer(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }

    @Test
    void testAddVer_WithNullAddVerType() {
        // Arrange
        addVerVo.setAddVerType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }

    @Test
    void testAddVer_WithNullAppCode() {
        // Arrange
        addVerVo.setAppCode(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelPermissionsService.addVer(addVerVo);
        });
    }
}
