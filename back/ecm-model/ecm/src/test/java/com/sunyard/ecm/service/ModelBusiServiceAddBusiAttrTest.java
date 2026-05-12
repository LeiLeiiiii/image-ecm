package com.sunyard.ecm.service;

import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.vo.DeleteBusiAttrVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"fileIndex=ecm_file_dev"})
public class ModelBusiServiceAddBusiAttrTest{

    @Mock
    private EcmAppAttrMapper ecmAppAttrMapper;

    @Mock
    private EcmAppDefMapper ecmAppDefMapper;

    @InjectMocks
    private ModelBusiService modelBusiService;

    private EcmAppAttr ecmAppAttr;

    private AccountTokenExtendDTO token;

    private DeleteBusiAttrVO deleteBusiAttrVO;
    private List<Long> appAttrIds;

    private String appCode = "TEST_APP";
    @BeforeEach
    void setUp() {
//
        ecmAppAttr = new EcmAppAttr();

        ecmAppAttr.setAttrCode("TEST_ATTR");
        ecmAppAttr.setAttrName("Test Attribute");
        ecmAppAttr.setAppCode("TEST_APP");
        ecmAppAttr.setInputType(1);
        ecmAppAttr.setIsKey(0);

        token = new AccountTokenExtendDTO();
        token.setUsername("testUser");

        appAttrIds = Arrays.asList(1L, 2L, 3L);
        deleteBusiAttrVO = new DeleteBusiAttrVO();
        deleteBusiAttrVO.setAppCode("TEST_APP_CODE");
        deleteBusiAttrVO.setAppAttrId(appAttrIds);
    }

    @Test
    void addBusiAttrWhenInputType3AndListValueNullShouldThrowException() {
        ecmAppAttr.setInputType(3);
        ecmAppAttr.setListValue(null);

        assertThrows(IllegalArgumentException.class,
            () -> modelBusiService.addBusiAttr(ecmAppAttr, token));
    }

    @Test
    void addBusiAttrWhenAttrCodeNullShouldThrowException() {
        ecmAppAttr.setAttrCode(null);

        assertThrows(IllegalArgumentException.class,
            () -> modelBusiService.addBusiAttr(ecmAppAttr, token));
    }

    @Test
    void addBusiAttrWhenAttrNameNullShouldThrowException() {
        ecmAppAttr.setAttrName(null);

        assertThrows(IllegalArgumentException.class,
            () -> modelBusiService.addBusiAttr(ecmAppAttr, token));
    }

    @Test
    void addBusiAttrWhenAppCodeNullShouldThrowException() {
        ecmAppAttr.setAppCode(null);

        assertThrows(IllegalArgumentException.class,
            () -> modelBusiService.addBusiAttr(ecmAppAttr, token));
    }

    @Test
    void addBusiAttrWhenSortHandlingShouldSetCorrectSort() {
        EcmAppAttr existingAttr1 = new EcmAppAttr();
        existingAttr1.setAttrSort(1);
        existingAttr1.setAppAttrId(1L);
        EcmAppAttr existingAttr2 = new EcmAppAttr();
        existingAttr2.setAttrSort(3);
        existingAttr2.setAppAttrId(2L);

        when(ecmAppAttrMapper.selectList(any()))
                .thenReturn(Arrays.asList(existingAttr1, existingAttr2));
        when(ecmAppDefMapper.update(any(), any())).thenReturn(1);
        when(ecmAppAttrMapper.insert(any())).thenReturn(1);

        Result result = modelBusiService.addBusiAttr(ecmAppAttr, token);

        assertTrue(result.isSucc());
        assertEquals(4, ecmAppAttr.getAttrSort());
    }

    @Test
    void addBusiAttrSuccess() {
        // Mock existing attributes
        when(ecmAppAttrMapper.selectList(any()))
                .thenReturn(Collections.emptyList());

        // Mock update
        when(ecmAppDefMapper.update(any(), any())).thenReturn(1);

        // Mock insert
        when(ecmAppAttrMapper.insert(any())).thenReturn(1);

        Result result = modelBusiService.addBusiAttr(ecmAppAttr, token);

        assertTrue(result.isSucc());
        assertEquals(ecmAppAttr, result.getData());
        verify(ecmAppAttrMapper, times(1)).insert(any());
    }

    @Test
    void addBusiAttrWhenAttrCodeExistsShouldReturnError() {
        EcmAppAttr existingAttr = new EcmAppAttr();
        existingAttr.setAttrCode("TEST_ATTR");

        when(ecmAppAttrMapper.selectList(any()))
                .thenReturn(Collections.singletonList(existingAttr));

        Result result = modelBusiService.addBusiAttr(ecmAppAttr, token);

        assertFalse(result.isSucc());
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("业务属性代码为TEST_ATTR的业务属性代码重复！", result.getMsg());
    }


    @Test
    void editBusiAttrWhenAllFieldsValidShouldSuccess() {
        when(ecmAppAttrMapper.updateById(any())).thenReturn(1);
        when(ecmAppDefMapper.update(any(), any())).thenReturn(1);

        Result result = modelBusiService.editBusiAttr(ecmAppAttr, token);

        assertTrue(result.isSucc());
        verify(ecmAppAttrMapper).updateById(ecmAppAttr);
        verify(ecmAppDefMapper).update(any(),any());
    }

    @Test
    void editBusiAttrWhenAttrCodeNullShouldThrowException() {
        ecmAppAttr.setAttrCode(null);

        assertThrows(IllegalArgumentException.class,
                () -> modelBusiService.editBusiAttr(ecmAppAttr, token));
    }

    @Test
    void editBusiAttrWhenAttrNameNullShouldThrowException() {
        ecmAppAttr.setAttrName(null);

        assertThrows(IllegalArgumentException.class,
                () -> modelBusiService.editBusiAttr(ecmAppAttr, token));
    }

    @Test
    void editBusiAttrWhenAppCodeNullShouldThrowException() {
        ecmAppAttr.setAppCode(null);

        assertThrows(IllegalArgumentException.class,
                () -> modelBusiService.editBusiAttr(ecmAppAttr, token));
    }

    @Test
    void editBusiAttrWhenInputType3AndListValueNullShouldThrowException() {
        ecmAppAttr.setInputType(3);
        ecmAppAttr.setListValue(null);

        assertThrows(IllegalArgumentException.class,
                () -> modelBusiService.editBusiAttr(ecmAppAttr, token));
    }



    @Test
    void deleteBusiAttr_Success() {
        // Act
        Result result = modelBusiService.deleteBusiAttr(deleteBusiAttrVO, token);

        // Assert
        assertTrue(result.isSucc());
        assertEquals(true, result.getData());
        verify(ecmAppAttrMapper, times(1)).deleteBatchIds(appAttrIds);
        verify(ecmAppDefMapper).update(any(),any());
    }

    @Test
    void deleteBusiAttr_WhenAppCodeIsNull_ShouldThrowException() {
        // Arrange
        deleteBusiAttrVO.setAppCode(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            modelBusiService.deleteBusiAttr(deleteBusiAttrVO, token);
        });
        assertEquals("业务类型id不能为空", exception.getMessage());
    }

    @Test
    void deleteBusiAttr_WhenAppAttrIdIsNull_ShouldThrowException() {
        // Arrange
        deleteBusiAttrVO.setAppAttrId(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            modelBusiService.deleteBusiAttr(deleteBusiAttrVO, token);
        });
        assertEquals("业务属性id不能为空", exception.getMessage());
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

    @Test
    void searchOneBusiAttrShouldReturnAppAttrWhenIdExists() {
        // Arrange
        Long appAttrId = 1L;
        EcmAppAttr mockAppAttr = createEcmAppAttr(appAttrId, "ATTR1", 1, "0", "user1", "user2");

        when(ecmAppAttrMapper.selectById(appAttrId)).thenReturn(mockAppAttr);

        // Act
        EcmAppAttr result = modelBusiService.searchOneBusiAttr(appAttrId);

        // Assert
        assertNotNull(result);
        assertEquals(appAttrId, result.getAppAttrId());
        assertEquals("ATTR1", result.getAttrCode());
        assertEquals("user1", result.getCreateUser());
        assertEquals("user2", result.getUpdateUser());

        verify(ecmAppAttrMapper).selectById(appAttrId);
    }

    @Test
    void searchOneBusiAttrShouldThrowExceptionWhenIdIsNull() {
        // Arrange
        Long appAttrId = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelBusiService.searchOneBusiAttr(appAttrId);
        });

        verify(ecmAppAttrMapper, never()).selectById(any());
    }

    @Test
    void searchOneBusiAttrShouldReturnNullWhenIdNotExists() {
        // Arrange
        Long appAttrId = 999L;

        when(ecmAppAttrMapper.selectById(appAttrId)).thenReturn(null);

        // Act
        EcmAppAttr result = modelBusiService.searchOneBusiAttr(appAttrId);

        // Assert
        assertNull(result);
        verify(ecmAppAttrMapper).selectById(appAttrId);
    }
}