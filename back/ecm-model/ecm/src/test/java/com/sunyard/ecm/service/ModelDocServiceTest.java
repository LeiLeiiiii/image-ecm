package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrMulDTO;
import com.sunyard.ecm.dto.ecm.EcmMoveDtdAttrDTO;
import com.sunyard.ecm.mapper.EcmDtdAttrMapper;
import com.sunyard.ecm.mapper.EcmDtdDefMapper;
import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.vo.DeleteDocumentAttrVO;
import com.sunyard.ecm.vo.EcmDtdDefVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.UserApi;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelDocServiceTest {

    @Mock
    private SnowflakeUtils snowflakeUtil;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @Mock
    private EcmDtdDefMapper ecmDtdDefMapper;

    @Mock
    private EcmDtdAttrMapper ecmDtdAttrMapper;

    @Mock
    private UserApi userApi;

    @Mock
    private ModelPermissionsService modelPermissionsService;

    @InjectMocks
    private ModelDocService modelDocService;

    private EcmDtdDefVO ecmDtdDefVo;
    private EcmDtdDef ecmDtdDef;
    private List<EcmDtdAttr> ecmDtdAttrList;
    private String userId = "testUser";
    private Long dtdTypeId = 12345L;

    @BeforeEach
    void setUp() {
        ecmDtdDefVo = new EcmDtdDefVO();
        ecmDtdDefVo.setDtdCode("TEST_CODE");
        ecmDtdDefVo.setDtdName("TEST_NAME");
        ecmDtdDefVo.setDtdTypeId(123L);

        ecmDtdDef = new EcmDtdDef();
        BeanUtils.copyProperties(ecmDtdDefVo, ecmDtdDef);

        ecmDtdAttrList = new ArrayList<>();
        EcmDtdAttr attr1 = new EcmDtdAttr();
        attr1.setAttrCode("ATTR1");
        attr1.setAttrName("Attribute 1");
        attr1.setDtdAttrId(2134l);
        attr1.setAttrSort(2);
        ecmDtdAttrList.add(attr1);

        ecmDtdDefVo.setEcmDtdAttrList(ecmDtdAttrList);
    }

    @Test
    void addDocumentType_Success() {
        when(snowflakeUtil.nextId()).thenReturn(dtdTypeId);
        when(ecmDtdDefMapper.selectList(null)).thenReturn(new ArrayList<>());
//        SqlSession mockSession = mock(org.apache.ibatis.session.SqlSession.class);
//        when(mockSession.getMapper(EcmDtdAttrMapper.class)).thenReturn(ecmDtdAttrMapper);
//        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mockSession);

        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mock(org.apache.ibatis.session.SqlSession.class));

        Result result = modelDocService.addDocumentType(ecmDtdDefVo, userId);

        assertTrue(result.isSucc());
        verify(ecmDtdDefMapper).insert(any(EcmDtdDef.class));
    }

    @Test
    void addDocumentType_DuplicateCode_Failure() {
        List<EcmDtdDef> existingDefs = new ArrayList<>();
        EcmDtdDef existingDef = new EcmDtdDef();
        existingDef.setDtdCode("TEST_CODE");
        existingDefs.add(existingDef);

        when(ecmDtdDefMapper.selectList(null)).thenReturn(existingDefs);

        Result result = modelDocService.addDocumentType(ecmDtdDefVo, userId);

        assertFalse(result.isSucc());
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
    }

    @Test
    void editDocumentType_Success() {
        when(snowflakeUtil.nextId()).thenReturn(123L);
        when(ecmDtdDefMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(ecmDtdAttrMapper.selectList(any())).thenReturn(new ArrayList<>());

        when(sqlSessionFactory.openSession(eq(ExecutorType.BATCH), eq(false))).thenReturn(mock(org.apache.ibatis.session.SqlSession.class));

        Result result = modelDocService.editDocumentType(ecmDtdDefVo, userId);

        assertTrue(result.isSucc());
        verify(ecmDtdDefMapper).updateById(any(EcmDtdDef.class));
        verify(ecmDtdAttrMapper).delete(any());
    }

    @Test
    void deleteDocumentType_Success() {
        Result result = modelDocService.deleteDocumentType(dtdTypeId, "TEST_NAME");

        assertTrue(result.isSucc());
        verify(ecmDtdDefMapper).deleteById(dtdTypeId);
        verify(ecmDtdAttrMapper).delete(any());
    }

    @Test
    void addDocumentAttr_Success() {
        EcmDtdAttr attr = new EcmDtdAttr();
        attr.setAttrCode("ATTR1");
        attr.setDtdTypeId(dtdTypeId);
        attr.setAttrName("Attribute 1");

        when(ecmDtdAttrMapper.selectList(any())).thenReturn(new ArrayList<>());

        Result result = modelDocService.addDocumentAttr(attr, userId);

        assertTrue(result.isSucc());
        verify(ecmDtdAttrMapper).insert(attr);
    }

    @Test
    void addDocumentAttr_DuplicateCode_Failure() {
        EcmDtdAttr attr = new EcmDtdAttr();
        attr.setAttrCode("ATTR1");
        attr.setDtdTypeId(dtdTypeId);
        attr.setAttrName("Attribute 1");

        List<EcmDtdAttr> existingAttrs = new ArrayList<>();
        EcmDtdAttr existingAttr = new EcmDtdAttr();
        existingAttr.setAttrCode("ATTR1");
        existingAttrs.add(existingAttr);

        when(ecmDtdAttrMapper.selectList(any())).thenReturn(existingAttrs);

        Result result = modelDocService.addDocumentAttr(attr, userId);

        assertFalse(result.isSucc());
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
    }

    @Test
    void editDocumentAttr_Success() {
        EcmDtdAttr attr = new EcmDtdAttr();
        attr.setAttrCode("ATTR1");
        attr.setDtdTypeId(dtdTypeId);
        attr.setAttrName("Attribute 1");

        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(userId);

        Result result = modelDocService.editDocumentAttr(attr, token);

        assertTrue(result.isSucc());
        verify(ecmDtdAttrMapper).updateById(attr);
    }

    @Test
    void deleteDocumentAttr_Success() {
        DeleteDocumentAttrVO vo = new DeleteDocumentAttrVO();
        vo.setDtdAttrId(Collections.singletonList(123L));
        vo.setDtdTypeId(dtdTypeId);

        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(userId);

        Result result = modelDocService.deleteDocumentAttr(vo, token);

        assertTrue(result.isSucc());
        verify(ecmDtdAttrMapper).delete(any());
    }

    @Test
    void searchDocumentAttr_Success() {
        EcmDtdAttr expectedAttr = new EcmDtdAttr();
        expectedAttr.setDtdAttrId(123L);

        when(ecmDtdAttrMapper.selectById(123L)).thenReturn(expectedAttr);

        Result result = modelDocService.searchDocumentAttr(123L);

        assertTrue(result.isSucc());
        assertEquals(expectedAttr, result.getData());
    }

    @Test
    void searchDocumentType_Success() {
        List<EcmDtdDef> expectedDefs = new ArrayList<>();
        expectedDefs.add(ecmDtdDef);

        when(ecmDtdDefMapper.selectList(any())).thenReturn(expectedDefs);

        Result result = modelDocService.searchDocumentType();

        assertTrue(result.isSucc());
        assertEquals(expectedDefs, result.getData());
    }

    @Test
    void searchOneDocumentType_Success() {
        when(ecmDtdDefMapper.selectById(dtdTypeId)).thenReturn(ecmDtdDef);
        when(ecmDtdAttrMapper.selectList(any())).thenReturn(ecmDtdAttrList);
        when(userApi.getUserListByUsernames(any())).thenReturn(Result.success(new ArrayList<>()));

        Result<EcmDtdDefVO> result = modelDocService.searchOneDocumentType(dtdTypeId);

        assertTrue(result.isSucc());
        assertNotNull(result.getData());
    }

    @Test
    void searchDocumentAttrList_Success() {
        PageForm pageForm = new PageForm();
        pageForm.setPageNum(1);
        pageForm.setPageSize(10);

        List<EcmDtdAttr> expectedAttrs = new ArrayList<>();
        expectedAttrs.add(ecmDtdAttrList.get(0));

        when(ecmDtdAttrMapper.selectList(any())).thenReturn(expectedAttrs);
        when(modelPermissionsService.getUserListByUserIds(any())).thenReturn(new HashMap<>());

        Result<PageInfo<EcmDtdAttrInfoDTO>> result = modelDocService.searchDocumentAttrList(pageForm, dtdTypeId);

        assertTrue(result.isSucc());
        assertNotNull(result.getData());
    }

    @Test
    void multiplexDocumentArrtList_Success() {
        when(ecmDtdAttrMapper.selectList(any())).thenReturn(ecmDtdAttrList);

        Result result = modelDocService.multiplexDocumentArrtList(dtdTypeId);

        assertTrue(result.isSucc());
        assertEquals(ecmDtdAttrList, result.getData());
    }

    @Test
    void multiplexAddDocumentAttr_Success() {
        EcmDtdAttrMulDTO mulDTO = new EcmDtdAttrMulDTO();
        mulDTO.setTypeId(dtdTypeId);
        mulDTO.setAttrIdList(Collections.singletonList(123L));

        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(userId);

        when(ecmDtdAttrMapper.selectList(any())).thenReturn(ecmDtdAttrList);
        when(ecmDtdAttrMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        Result result = modelDocService.multiplexAddDocumentAttr(mulDTO, token);

        assertTrue(result.isSucc());
    }

    @Test
    void moveDocumentArrtList_Success() {
        EcmMoveDtdAttrDTO moveDTO = new EcmMoveDtdAttrDTO();
        moveDTO.setDtdAttrId(23l);
        moveDTO.setTargetDtdAttrId(456L);
        moveDTO.setAttrSort(1);
        moveDTO.setTargetAttrSort(2);
        moveDTO.setDtdTypeId(dtdTypeId);

        when(ecmDtdAttrMapper.selectList(any())).thenReturn(ecmDtdAttrList);
//        when(ecmDtdAttrMapper.update(any())).thenReturn(1);
        when(ecmDtdAttrMapper.update(any(), any())).thenReturn(1);
        Result result = modelDocService.moveDocumentArrtList(moveDTO, userId);

        assertTrue(result.isSucc());
    }
}