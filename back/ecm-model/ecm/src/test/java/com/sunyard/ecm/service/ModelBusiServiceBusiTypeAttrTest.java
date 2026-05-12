package com.sunyard.ecm.service;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.dto.ecm.EcmAppAttrDTO;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.module.system.api.dto.SysUserDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"fileIndex=ecm_file_dev"})
public class ModelBusiServiceBusiTypeAttrTest{

    @Mock
    private EcmAppAttrMapper ecmAppAttrMapper;

    @Mock
    private ModelPermissionsService modelPermissionsService;

    @InjectMocks
    private ModelBusiService modelBusiService;

    private PageForm pageForm;

    private String appCode = "TEST_APP";
    private List<EcmAppAttr> mockAppAttrs;
    @BeforeEach
    void setUp() {
        pageForm = new PageForm();
        pageForm.setPageNum(1);
        pageForm.setPageSize(10);

        EcmAppAttr attr1 = new EcmAppAttr();
        attr1.setAppAttrId(1L);
        attr1.setAppCode("TEST_APP");
        attr1.setAttrName("主属性");
        attr1.setAttrCode("main_attr");
        attr1.setIsKey(1);
        attr1.setAttrSort(1);

        EcmAppAttr attr2 = new EcmAppAttr();
        attr2.setAppAttrId(2L);
        attr2.setAppCode("TEST_APP");
        attr2.setAttrName("普通属性1");
        attr2.setAttrCode("normal_attr1");
        attr2.setIsKey(0);
        attr2.setAttrSort(3);

        EcmAppAttr attr3 = new EcmAppAttr();
        attr3.setAppAttrId(3L);
        attr3.setAppCode("TEST_APP");
        attr3.setAttrName("普通属性2");
        attr3.setAttrCode("normal_attr2");
        attr3.setIsKey(0);
        attr3.setAttrSort(2);

        mockAppAttrs = Arrays.asList(attr1, attr2, attr3);
    }

    @Test
    void searchBusiTypeAttrListShouldReturnPageInfoWithSortedAttributes() {
        // Arrange
        List<EcmAppAttr> mockAppAttrs = new ArrayList<>();
        EcmAppAttr attr1 = createEcmAppAttr(1L, "ATTR1", 2, "0", "user1", "user2");
        EcmAppAttr attr2 = createEcmAppAttr(2L, "ATTR2", 1, "1", "user1", "user3");
        mockAppAttrs.add(attr1);
        mockAppAttrs.add(attr2);
    
        Map<String, List<SysUserDTO>> userMap = new HashMap<>();
        userMap.put("user1", Arrays.asList(createSysUserDTO("user1", "User One")));
        userMap.put("user2", Arrays.asList(createSysUserDTO("user2", "User Two")));
        userMap.put("user3", Arrays.asList(createSysUserDTO("user3", "User Three")));
    
        when(ecmAppAttrMapper.selectList(any())).thenReturn(mockAppAttrs);
        when(modelPermissionsService.getUserListByUserIds(anyList())).thenReturn(userMap);
    
        // Act
        PageInfo<EcmAppAttrDTO> result = modelBusiService.searchBusiTypeAttrList(pageForm, appCode);
    
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getList().size());
    
        List<EcmAppAttrDTO> resultList = result.getList();
        // Verify key attribute is first
        assertEquals("1", resultList.get(0).getIsKey().toString());
        // Verify sorting
        assertTrue(resultList.get(0).getAttrSort() < resultList.get(1).getAttrSort());
        // Verify user names are set
        assertEquals("User One", resultList.get(0).getCreateUserName());
        assertEquals("User Three", resultList.get(0).getUpdateUserName());
        assertEquals("User One", resultList.get(1).getCreateUserName());
        assertEquals("User Two", resultList.get(1).getUpdateUserName());
    
        verify(ecmAppAttrMapper).selectList(any());
        verify(modelPermissionsService).getUserListByUserIds(anyList());
    }

    @Test
    void searchBusiTypeAttrListShouldHandleEmptyUserMap() {
        // Arrange
        List<EcmAppAttr> mockAppAttrs = new ArrayList<>();
        EcmAppAttr attr = createEcmAppAttr(1L, "ATTR1", 1, "0", "user1", "user2");
        mockAppAttrs.add(attr);
    
        when(ecmAppAttrMapper.selectList(any())).thenReturn(mockAppAttrs);
        when(modelPermissionsService.getUserListByUserIds(anyList())).thenReturn(null);
    
        // Act
        PageInfo<EcmAppAttrDTO> result = modelBusiService.searchBusiTypeAttrList(pageForm, appCode);
    
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertNull(result.getList().get(0).getCreateUserName());
        assertNull(result.getList().get(0).getUpdateUserName());
    }

    @Test
    void searchBusiTypeAttrListShouldHandleEmptyAttributes() {
        // Arrange
        when(ecmAppAttrMapper.selectList(any())).thenReturn(new ArrayList<>());
    
        // Act
        PageInfo<EcmAppAttrDTO> result = modelBusiService.searchBusiTypeAttrList(pageForm, appCode);
    
        // Assert
        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
    }

    private EcmAppAttr createEcmAppAttr(Long id, String attrCode, Integer sort, String isKey, 
                                      String createUser, String updateUser) {
        EcmAppAttr attr = new EcmAppAttr();
        attr.setAppAttrId(id);
        attr.setAttrCode(attrCode);
        attr.setAttrSort(sort);
        attr.setIsKey(Integer.valueOf(isKey));
        attr.setCreateUser(createUser);
        attr.setUpdateUser(updateUser);
        attr.setAppCode(appCode);
        return attr;
    }

    private SysUserDTO createSysUserDTO(String userId, String name) {
        SysUserDTO user = new SysUserDTO();
        user.setCode(userId);
        user.setName(name);
        return user;
    }

    @Test
    void multiplexBusiAttrAllList_shouldReturnEmptyList_whenNoAttributesFound() {
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<EcmAppAttrDTO> result = modelBusiService.multiplexBusiAttrAllList("TEST_APP");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void multiplexBusiAttrAllList_shouldReturnSortedListWithKeyAttributeFirst() {
        when(ecmAppAttrMapper.selectList(any())).thenReturn(mockAppAttrs);

        List<EcmAppAttrDTO> result = modelBusiService.multiplexBusiAttrAllList("TEST_APP");

        assertNotNull(result);
        assertEquals(3, result.size());

        // 验证主属性排在第一位
        assertEquals("主属性", result.get(0).getAttrName());
        assertEquals(1, result.get(0).getIsKey());

        // 验证普通属性按sort排序
        assertEquals("普通属性2", result.get(1).getAttrName());
        assertEquals(2, result.get(1).getAttrSort());
        assertEquals("普通属性1", result.get(2).getAttrName());
        assertEquals(3, result.get(2).getAttrSort());

        // 验证DTO转换
        assertEquals("主属性", result.get(0).getLabel());
        assertEquals("普通属性2", result.get(1).getLabel());
        assertEquals("普通属性1", result.get(2).getLabel());
    }

    @Test
    void multiplexBusiAttrAllList_shouldReturnListWithoutKeyAttribute_whenNoKeyAttributeExists() {
        // 移除主属性
        mockAppAttrs.forEach(attr -> attr.setIsKey(0));
        when(ecmAppAttrMapper.selectList(any())).thenReturn(mockAppAttrs);

        List<EcmAppAttrDTO> result = modelBusiService.multiplexBusiAttrAllList("TEST_APP");

        assertNotNull(result);
        assertEquals(3, result.size());

        // 验证按sort排序
        assertEquals("主属性", result.get(0).getAttrName()); // sort=1
        assertEquals("普通属性2", result.get(1).getAttrName()); // sort=2
        assertEquals("普通属性1", result.get(2).getAttrName()); // sort=3
    }

    @Test
    void multiplexBusiAttrAllList_shouldHandleSingleAttribute() {
        EcmAppAttr singleAttr = new EcmAppAttr();
        singleAttr.setAppAttrId(1L);
        singleAttr.setAppCode("TEST_APP");
        singleAttr.setAttrName("单属性");
        singleAttr.setAttrCode("single_attr");
        singleAttr.setIsKey(1);
        singleAttr.setAttrSort(1);

        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(singleAttr));

        List<EcmAppAttrDTO> result = modelBusiService.multiplexBusiAttrAllList("TEST_APP");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("单属性", result.get(0).getAttrName());
        assertEquals("单属性", result.get(0).getLabel());
        assertEquals(1, result.get(0).getIsKey());
    }

}