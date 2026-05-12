package com.sunyard.ecm.service.sdk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.PatternConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.*;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.mapper.*;
import com.sunyard.ecm.po.*;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApiUploadService 完整单元测试（覆盖核心场景+异常场景）
 */
@ExtendWith(MockitoExtension.class)
public class ApiUploadServiceTest {

    @InjectMocks
    private ApiUploadService apiUploadService;

    // 模拟依赖的Mapper/Service
    @Mock
    private SnowflakeUtils snowflakeUtil;
    @Mock
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Mock
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Mock
    private EcmAppDefMapper ecmAppDefMapper;
    @Mock
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Mock
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Mock
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Mock
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Mock
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Mock
    private DictionaryApi dictionaryApi;
    @Mock
    private CommonService commonService;
    @Mock
    private OperateFullQueryService operateFullQueryService;
    @Mock
    private BusiCacheService busiCacheService;
    @Mock
    private OpenApiService openApiService;
    @Mock
    private CaptureSubmitService captureSubmitService;
    @Mock
    private RedisUtils redisUtils;

    // 测试用基础数据
    private EcmUploadAllDTO testUploadDTO;
    private AccountTokenExtendDTO testToken;
    private static final String TEST_APP_CODE = "TEST_APP";
    private static final String TEST_BUS_NO = "TESTBUS001";
    private static final Long TEST_BUSI_ID = 1996772545424449536L;

    @BeforeEach
    void setUp() {
        // 初始化测试DTO
        initTestUploadDTO();
        // 初始化用户Token
        testToken = new AccountTokenExtendDTO();
        testToken.setUsername("testUser");
        testToken.setName("测试用户");
        testToken.setOrgCode("TEST_ORG");
        testToken.setOrgName("测试机构");
        testToken.setRoleCodeList(Collections.singletonList("TEST_ROLE"));
    }

    /**
     * 初始化测试用EcmUploadAllDTO
     */
    private void initTestUploadDTO() {
        testUploadDTO = new EcmUploadAllDTO();
        AddBusiDTO ecmRootDataDTO = new AddBusiDTO();

        // 基础信息
        EcmBaseInfoDTO baseInfoDTO = new EcmBaseInfoDTO();
        baseInfoDTO.setUserCode("testUser");
        baseInfoDTO.setUserName("测试用户");
        baseInfoDTO.setOrgCode("TEST_ORG");
        baseInfoDTO.setOrgName("测试机构");
        baseInfoDTO.setRoleCode(Collections.singletonList("TEST_ROLE"));
        ecmRootDataDTO.setEcmBaseInfoDTO(baseInfoDTO);

        // 业务扩展信息
        EcmBusExtendDTO busExtendDTO = new EcmBusExtendDTO();
        busExtendDTO.setAppCode(TEST_APP_CODE);
        // 业务属性（主索引）
        EcmBusiAttrDTO busiAttrDTO = new EcmBusiAttrDTO();
        busiAttrDTO.setAttrCode("BUSI_MAIN_KEY");
        busiAttrDTO.setAppAttrValue(TEST_BUS_NO);
        busExtendDTO.setEcmBusiAttrDTOList(Collections.singletonList(busiAttrDTO));
        // 静态树（无动态树数据）
        busExtendDTO.setEcmVTreeDataDTOS(null);
        ecmRootDataDTO.setEcmBusExtendDTOS(busExtendDTO);

        // 文件上传信息
        EcmUploadFileDTO uploadFileDTO = new EcmUploadFileDTO();
        uploadFileDTO.setDocNo("TEST_DOC_001");
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFileMd5("test_md5_123");
        uploadFileDTO.setFileAndSortDTOS(Collections.singletonList(fileDTO));
        testUploadDTO.setSplitDTO(Collections.singletonList(uploadFileDTO));
        testUploadDTO.setEcmRootDataDTO(ecmRootDataDTO);
        testUploadDTO.setDocNo("TEST_DOC_001");
    }

    // ======================== 基础场景测试 ========================

    /**
     * 测试checkFile方法 - 正常流程（静态树、业务已存在）
     */
    @Test
    void testCheckFile_Success_StaticTree() {
        // 1. 模拟用户校验
        when(busiCacheService.checkUser(any(EcmBaseInfoDTO.class), any(AccountTokenExtendDTO.class)))
                .thenReturn(testToken);

        // 2. 模拟业务属性查询（主键属性）
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setIsKey(StateConstants.YES);
        when(ecmAppAttrMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(appAttr));

        // 3. 模拟业务定义查询
        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppCode(TEST_APP_CODE);
        appDef.setAppName("测试业务");
        appDef.setEquipmentId(1L);
        when(ecmAppDefMapper.selectById(TEST_APP_CODE)).thenReturn(appDef);

        // 4. 模拟业务信息已存在
        EcmBusiInfo busiInfo = new EcmBusiInfo();
        busiInfo.setBusiId(TEST_BUSI_ID);
        busiInfo.setAppCode(TEST_APP_CODE);
        busiInfo.setBusiNo(TEST_BUS_NO);
        when(ecmBusiInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(busiInfo);

        // 5. 模拟静态树文档关联查询
        EcmAppDocRel appDocRel = new EcmAppDocRel();
        appDocRel.setAppCode(TEST_APP_CODE);
        appDocRel.setDocCode("TEST_DOC_001");
        appDocRel.setType(IcmsConstants.ONE);
        when(ecmAppDocRelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(appDocRel);

        // 6. 模拟重复文件校验（无重复文件）
        when(ecmFileInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // 7. 模拟文件类型字典查询
        Result<Map<String, List<SysDictionaryDTO>>> dictResult = Result.success();
        Map<String, List<SysDictionaryDTO>> dictMap = new HashMap<>();
        SysDictionaryDTO dictDTO = new SysDictionaryDTO();
        dictDTO.setValue("{\"limit_format\":\"pdf;jpg;png\"}");
        dictMap.put(IcmsConstants.FILE_TYPE_DIC, Collections.singletonList(dictDTO));
        dictResult.setData(dictMap);
        when(dictionaryApi.getDictionaryAll(anyString(), any())).thenReturn(dictResult);

        // 执行方法
        Result<String> result = apiUploadService.checkFile(testUploadDTO);

        // 验证结果
        assertTrue(result.isSucc());
        assertNotNull(result.getData());
        // 验证依赖方法调用次数
        verify(busiCacheService, times(1)).checkUser(any(), any());
        verify(ecmAppAttrMapper, times(1)).selectList(any());
        verify(ecmBusiInfoMapper, times(1)).selectOne(any());
    }

    /**
     * 测试checkFile方法 - 业务主索引为空（异常流程）
     */
    @Test
    void testCheckFile_Fail_BusiMainKeyEmpty() {
        // 篡改业务属性，主索引值为空
        EcmBusiAttrDTO emptyAttrDTO = new EcmBusiAttrDTO();
        emptyAttrDTO.setAttrCode("BUSI_MAIN_KEY");
        emptyAttrDTO.setAppAttrValue("");
        testUploadDTO.getEcmRootDataDTO().getEcmBusExtendDTOS()
                .setEcmBusiAttrDTOList(Collections.singletonList(emptyAttrDTO));

        // 模拟用户校验
        when(busiCacheService.checkUser(any(), any())).thenReturn(testToken);

        // 模拟业务属性查询
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setIsKey(StateConstants.YES);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(appAttr));

        // 执行方法，预期抛出异常
        SunyardException exception = assertThrows(SunyardException.class, () -> {
            apiUploadService.checkFile(testUploadDTO);
        });

        // 验证异常信息
        assertEquals("业务主索引不能为空!", exception.getMessage());
        assertEquals(ResultCode.NO_DATA_AUTH.getCode(), exception.getResultCode().getCode());
    }

    /**
     * 测试flattenTree方法（动态树平铺）
     */
    @Test
    void testFlattenTree() {
        // 构建嵌套动态树
        EcmVTreeDataDTO root = new EcmVTreeDataDTO();
        root.setDocCode("ROOT");
        EcmVTreeDataDTO child1 = new EcmVTreeDataDTO();
        child1.setDocCode("CHILD1");
        EcmVTreeDataDTO child2 = new EcmVTreeDataDTO();
        child2.setDocCode("CHILD2");
        root.setEcmVTreeDataDTOS(Arrays.asList(child1, child2));

        EcmVTreeDataDTO grandChild = new EcmVTreeDataDTO();
        grandChild.setDocCode("GRANDCHILD");
        child1.setEcmVTreeDataDTOS(Collections.singletonList(grandChild));

        // 执行平铺方法
        List<EcmVTreeDataDTO> flatList = apiUploadService.flattenTree(Collections.singletonList(root));

        // 验证结果：4个节点（ROOT、CHILD1、CHILD2、GRANDCHILD）
        assertEquals(4, flatList.size());
        assertTrue(flatList.stream().anyMatch(n -> n.getDocCode().equals("ROOT")));
        assertTrue(flatList.stream().anyMatch(n -> n.getDocCode().equals("CHILD1")));
        assertTrue(flatList.stream().anyMatch(n -> n.getDocCode().equals("CHILD2")));
        assertTrue(flatList.stream().anyMatch(n -> n.getDocCode().equals("GRANDCHILD")));
    }

    /**
     * 测试saveBusiOrNot方法 - 新建业务（动态树）
     */
    @Test
    @Transactional
    void testSaveBusiOrNot_CreateNewBusi_DynamicTree() {
        // 1. 重置DTO为动态树
        EcmVTreeDataDTO treeNode = new EcmVTreeDataDTO();
        treeNode.setDocCode("DYN_TREE_001");
        treeNode.setDocName("动态树节点");
        testUploadDTO.getEcmRootDataDTO().getEcmBusExtendDTOS()
                .setEcmVTreeDataDTOS(Collections.singletonList(treeNode));

        // 2. 模拟业务不存在
        when(ecmBusiInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 3. 模拟静态树/动态树校验通过
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setRegex(PatternConstants.NUMBER_AND_ZM);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(appAttr));

        // 4. 模拟动态树校验
        doNothing().when(openApiService).validateTree(anyList());

        // 5. 模拟雪花算法生成ID
        when(snowflakeUtil.nextId()).thenReturn(TEST_BUSI_ID);

        // 6. 模拟业务属性保存
        when(ecmAppAttrMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(appAttr));

        // 执行方法
        EcmBusiInfoDTO busiInfoDTO = new EcmBusiInfoDTO();
        busiInfoDTO.setAppCode(TEST_APP_CODE);
        busiInfoDTO.setBusiNo(TEST_BUS_NO);
        busiInfoDTO.setTreeType(IcmsConstants.DYNAMIC_TREE);

        EcmBusiInfoDTO resultDTO = apiUploadService.saveBusiOrNot(
                testUploadDTO, busiInfoDTO, IcmsConstants.DYNAMIC_TREE,
                testToken, Collections.singletonList(treeNode), appAttr, TEST_BUS_NO, TEST_APP_CODE
        );

        // 验证结果
        assertNotNull(resultDTO.getBusiId());
        assertEquals(TEST_BUSI_ID, resultDTO.getBusiId());
        // 验证业务信息插入
        verify(ecmBusiInfoMapper, times(1)).insert(any(EcmBusiInfo.class));
        // 验证动态树节点保存
        verify(ecmBusiDocMapper, times(1)).insert(any(EcmBusiDoc.class));
    }

    // ======================== 补充场景测试（提升覆盖率） ========================

    /**
     * 测试checkFile - 动态树正常流程
     */
//    @Test
//    void testCheckFile_Success_DynamicTree() {
//        // 1. 重置DTO为动态树
//        EcmVTreeDataDTO rootNode = new EcmVTreeDataDTO();
//        rootNode.setDocCode("DYN_ROOT_001");
//        EcmVTreeDataDTO childNode = new EcmVTreeDataDTO();
//        childNode.setDocCode("TEST_DOC_001");
//        rootNode.setEcmVTreeDataDTOS(Collections.singletonList(childNode));
//        testUploadDTO.getEcmRootDataDTO().getEcmBusExtendDTOS()
//                .setEcmVTreeDataDTOS(Collections.singletonList(rootNode));
//
//        // 2. 模拟依赖
//        when(busiCacheService.checkUser(any(), any())).thenReturn(testToken);
//        EcmAppAttr appAttr = new EcmAppAttr();
//        appAttr.setAppCode(TEST_APP_CODE);
//        appAttr.setAttrCode("BUSI_MAIN_KEY");
//        appAttr.setIsKey(StateConstants.YES);
//        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(appAttr));
//        EcmAppDef appDef = new EcmAppDef();
//        appDef.setAppCode(TEST_APP_CODE);
//        when(ecmAppDefMapper.selectById(TEST_APP_CODE)).thenReturn(appDef);
//        EcmBusiInfo busiInfo = new EcmBusiInfo();
//        busiInfo.setBusiId(TEST_BUSI_ID);
//        when(ecmBusiInfoMapper.selectOne(any())).thenReturn(busiInfo);
//
//        // 动态树节点查询
//        EcmBusiDoc busiDoc = new EcmBusiDoc();
//        busiDoc.setDocId(1L);
//        busiDoc.setDocCode("TEST_DOC_001");
//        when(ecmBusiDocMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(busiDoc);
//
//        // 重复文件校验（无重复）
//        when(ecmFileInfoMapper.selectList(any())).thenReturn(Collections.emptyList());
//
//        // 文件类型字典
//        Result<Map<String, List<SysDictionaryDTO>>> dictResult = Result.success();
//        Map<String, List<SysDictionaryDTO>> dictMap = new HashMap<>();
//        SysDictionaryDTO dictDTO = new SysDictionaryDTO();
//        dictDTO.setValue("{\"limit_format\":\"pdf;jpg\"}");
//        dictMap.put(IcmsConstants.FILE_TYPE_DIC, Collections.singletonList(dictDTO));
//        dictResult.setData(dictMap);
//        when(dictionaryApi.getDictionaryAll(anyString(), any())).thenReturn(dictResult);
//
//        // 执行
////        Result<String> result = apiUploadService.checkFile(testUploadDTO);
////
////        // 验证
////        assertTrue(result.isSucc());
//        verify(ecmBusiDocMapper, times(1)).selectOne(any());
//    }

    /**
     * 测试checkFile - 文件归入未归类（静态树文档关联不存在）
     */
    @Test
    void testCheckFile_Unclassified_StaticTree() {
        // 1. 模拟依赖（文档关联不存在）
        when(busiCacheService.checkUser(any(), any())).thenReturn(testToken);
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setIsKey(StateConstants.YES);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(appAttr));
        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppCode(TEST_APP_CODE);
        when(ecmAppDefMapper.selectById(TEST_APP_CODE)).thenReturn(appDef);
        EcmBusiInfo busiInfo = new EcmBusiInfo();
        busiInfo.setBusiId(TEST_BUSI_ID);
        when(ecmBusiInfoMapper.selectOne(any())).thenReturn(busiInfo);
        // 核心：文档关联不存在
        when(ecmAppDocRelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行
        Result<String> result = apiUploadService.checkFile(testUploadDTO);

        // 验证：返回结果包含未归类标识
        assertTrue(result.getData().contains(IcmsConstants.UNCLASSIFIED_ID));
    }

    /**
     * 测试checkFile - 文件MD5重复场景
     */
    @Test
    void testCheckFile_RepeatFile_MD5() {
        // 1. 模拟依赖
        when(busiCacheService.checkUser(any(), any())).thenReturn(testToken);
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setIsKey(StateConstants.YES);
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(appAttr));
        EcmAppDef appDef = new EcmAppDef();
        appDef.setAppCode(TEST_APP_CODE);
        when(ecmAppDefMapper.selectById(TEST_APP_CODE)).thenReturn(appDef);
        EcmBusiInfo busiInfo = new EcmBusiInfo();
        busiInfo.setBusiId(TEST_BUSI_ID);
        when(ecmBusiInfoMapper.selectOne(any())).thenReturn(busiInfo);
        EcmAppDocRel appDocRel = new EcmAppDocRel();
        appDocRel.setAppCode(TEST_APP_CODE);
        appDocRel.setDocCode("TEST_DOC_001");
        when(ecmAppDocRelMapper.selectOne(any())).thenReturn(appDocRel);

        // 核心：模拟已存在相同MD5的文件
        EcmFileInfo existFile = new EcmFileInfo();
        existFile.setFileMd5("test_md5_123");
        existFile.setBusiId(TEST_BUSI_ID);
        existFile.setDocCode("TEST_DOC_001");
        existFile.setState(IcmsConstants.ZERO);
        when(ecmFileInfoMapper.selectList(any())).thenReturn(Collections.singletonList(existFile));

        // 文件类型字典
        Result<Map<String, List<SysDictionaryDTO>>> dictResult = Result.success();
        Map<String, List<SysDictionaryDTO>> dictMap = new HashMap<>();
        SysDictionaryDTO dictDTO = new SysDictionaryDTO();
        dictDTO.setValue("{\"limit_format\":\"pdf;jpg\"}");
        dictMap.put(IcmsConstants.FILE_TYPE_DIC, Collections.singletonList(dictDTO));
        dictResult.setData(dictMap);
        when(dictionaryApi.getDictionaryAll(anyString(), any())).thenReturn(dictResult);

        // 执行
        Result<String> result = apiUploadService.checkFile(testUploadDTO);

        // 验证：重复文件被过滤
        assertTrue(result.getData().contains("repeatFileMd5List"));
        assertTrue(result.getData().contains("test_md5_123"));
    }

    /**
     * 测试saveBusiOrNot - 静态树新建（主索引正则校验通过）
     */
    @Test
    @Transactional
    void testSaveBusiOrNot_CreateNewBusi_StaticTree_Regex() {
        // 1. 模拟业务不存在
        when(ecmBusiInfoMapper.selectOne(any())).thenReturn(null);

        // 2. 模拟静态树校验
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setRegex(PatternConstants.NUMBER_AND_ZM); // 字母数字正则
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.singletonList(appAttr));

        // 3. 模拟权限版本配置
        EcmAppDocright docRight = new EcmAppDocright();
        docRight.setRightVer(1);
        docRight.setRightNew(StateConstants.COMMON_ONE);
        when(ecmAppDocrightMapper.selectList(any())).thenReturn(Collections.singletonList(docRight));

        // 4. 模拟雪花ID
        when(snowflakeUtil.nextId()).thenReturn(TEST_BUSI_ID);

        // 5. 模拟业务属性保存
        when(ecmAppAttrMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(appAttr));

        // 执行
        EcmBusiInfoDTO busiInfoDTO = new EcmBusiInfoDTO();
        busiInfoDTO.setAppCode(TEST_APP_CODE);
        busiInfoDTO.setBusiNo(TEST_BUS_NO);
        busiInfoDTO.setTreeType(IcmsConstants.STATIC_TREE);

        EcmBusiInfoDTO resultDTO = apiUploadService.saveBusiOrNot(
                testUploadDTO, busiInfoDTO, IcmsConstants.STATIC_TREE,
                testToken, null, appAttr, TEST_BUS_NO, TEST_APP_CODE
        );

        // 验证
        assertEquals(TEST_BUSI_ID, resultDTO.getBusiId());
        assertEquals(1, resultDTO.getRightVer());
        verify(ecmBusiInfoMapper, times(1)).insert(any());
    }

    /**
     * 测试saveBusiOrNot - 静态树新建（主索引正则校验失败）
     */
    @Test
    void testSaveBusiOrNot_CreateNewBusi_StaticTree_RegexFail() {
        // 1. 篡改主索引为非法值（含特殊字符）
        String invalidBusNo = "TEST@BUS_001";
        testUploadDTO.getEcmRootDataDTO().getEcmBusExtendDTOS().getEcmBusiAttrDTOList().get(0)
                .setAppAttrValue(invalidBusNo);

        // 2. 模拟业务不存在（仅保留必要的Mock）
        when(ecmBusiInfoMapper.selectOne(any())).thenReturn(null);

        // 3. 直接创建校验用的appAttr（无需Mock数据库查询，因为参数直接传入）
        EcmAppAttr appAttr = new EcmAppAttr();
        appAttr.setAppCode(TEST_APP_CODE);
        appAttr.setAttrCode("BUSI_MAIN_KEY");
        appAttr.setRegex(PatternConstants.NUMBER_AND_ZM); // 字母数字正则

        // 执行：预期抛出异常
        EcmBusiInfoDTO busiInfoDTO = new EcmBusiInfoDTO();
        busiInfoDTO.setAppCode(TEST_APP_CODE);
        busiInfoDTO.setBusiNo(invalidBusNo);
        busiInfoDTO.setTreeType(IcmsConstants.STATIC_TREE);

        SunyardException exception = assertThrows(SunyardException.class, () -> {
            apiUploadService.saveBusiOrNot(
                    testUploadDTO, busiInfoDTO, IcmsConstants.STATIC_TREE,
                    testToken, null, appAttr, invalidBusNo, TEST_APP_CODE
            );
        });

        // 验证
        assertEquals("业务主索引的定义不符合规则!", exception.getMessage());
    }

    /**
     * 测试saveOrUpdateEcmBusiMetadata - 业务属性不存在（抛出异常）
     */
    @Test
    void testSaveOrUpdateEcmBusiMetadata_AttrNotExist() {
        // 1. 模拟业务属性查询为空
        when(ecmAppAttrMapper.selectList(any())).thenReturn(Collections.emptyList());

        // 执行：预期抛出异常
        EcmBusiInfo busiInfo = new EcmBusiInfo();
        busiInfo.setBusiId(TEST_BUSI_ID);
        SunyardException exception = assertThrows(SunyardException.class, () -> {
            apiUploadService.saveOrUpdateEcmBusiMetadata(testUploadDTO, TEST_APP_CODE, busiInfo);
        });

        // 验证
        assertEquals("无指定业务属性", exception.getMessage());
    }

    /**
     * 测试checkRepeatFile - 正常查询重复文件
     */
    @Test
    void testCheckRepeatFile() {
        // 模拟查询到重复文件
        EcmFileInfo fileInfo = new EcmFileInfo();
        fileInfo.setFileMd5("test_md5_123");
        when(ecmFileInfoMapper.selectList(any())).thenReturn(Collections.singletonList(fileInfo));

        // 执行
        List<EcmFileInfo> result = apiUploadService.checkRepeatFile(TEST_BUSI_ID, "TEST_DOC_001");

        // 验证
        assertEquals(1, result.size());
        assertEquals("test_md5_123", result.get(0).getFileMd5());
    }

    /**
     * 测试fileToUnclassified - 动态树节点不存在（归入未归类）
     */
    @Test
    void testFileToUnclassified_DynamicTree_NodeNotExist() {
        // 1. 构建参数
        Integer treeType = IcmsConstants.DYNAMIC_TREE;
        EcmUploadFileDTO fileDTO = new EcmUploadFileDTO();
        fileDTO.setDocNo("NON_EXIST_DOC");
        EcmFileInfoDTO fileInfoDTO = new EcmFileInfoDTO();
        fileInfoDTO.setBusiId(TEST_BUSI_ID);
        EcmVTreeDataDTO vTreeDataDTO = new EcmVTreeDataDTO();
        vTreeDataDTO.setDocCode("notExist");
        List<EcmVTreeDataDTO> dynamicTree = Collections.singletonList(vTreeDataDTO);

        // 执行
        Map<String, Object> result = apiUploadService.fileToUnclassified(treeType, fileDTO, fileInfoDTO, dynamicTree);

        // 验证
        assertTrue((Boolean) result.get("unClassifyFlag"));
        assertEquals(IcmsConstants.UNCLASSIFIED_ID, fileInfoDTO.getDocId());
    }

    /**
     * 测试handleCheckTree - 动态树子节点含子树（抛出异常）
     */
    @Test
    void testHandleCheckTree_ChildNodeHasSubTree() {
        // 1. 构建非法动态树：子节点（childFlag=YES）包含子树
        EcmVTreeDataDTO childNode = new EcmVTreeDataDTO();
        childNode.setChildFlag(StateConstants.YES.toString());
        childNode.setEcmVTreeDataDTOS(Collections.singletonList(new EcmVTreeDataDTO()));
        List<EcmVTreeDataDTO> treeList = Collections.singletonList(childNode);
        HashMap<String, List<String>> checkMap = new HashMap<>();

        // 执行：预期抛出异常
        SunyardException exception = assertThrows(SunyardException.class, () -> {
            apiUploadService.handleCheckTree(treeList, checkMap);
        });

        // 验证
        assertEquals("动态树数据有误:子节点未最后一层级!", exception.getMessage());
    }

    /**
     * 测试getMatchFileTypeList - 字典查询失败（返回空列表）
     */
    @Test
    void testGetMatchFileTypeList_DictQueryFail() {
        // 模拟字典查询失败：构建失败的Result对象
        Result<Map<String, List<SysDictionaryDTO>>> dictResult = new Result<>();
        // 设置失败状态
        dictResult.setCode(500); // 错误码
        dictResult.setMsg("字典查询异常"); // 错误信息
        dictResult.setData(null); // 数据置空

        // Mock字典查询返回失败结果
        when(dictionaryApi.getDictionaryAll(anyString(), any())).thenReturn(dictResult);

        // 执行目标方法
        List<String> result = apiUploadService.getMatchFileTypeList();

        // 验证结果：查询失败时返回空列表
        assertTrue(result.isEmpty());
    }

    /**
     * 测试updateDynamicDoc - 动态树节点更新
     */
    @Test
    void testUpdateDynamicDoc() {
        // 1. 模拟缓存数据
        EcmBusiInfoRedisDTO redisDTO = new EcmBusiInfoRedisDTO();
        redisDTO.setBusiId(TEST_BUSI_ID);
        List<EcmVTreeDataDTO> treeList = Collections.singletonList(new EcmVTreeDataDTO());

        // 2. 模拟动态树节点已存在
        EcmBusiDoc busiDoc = new EcmBusiDoc();
        when(ecmBusiDocMapper.selectList(any())).thenReturn(Collections.singletonList(busiDoc));
        doNothing().when(commonService).buildDynTree(any(EcmBusiDocRedisDTO.class), anyList());

        // 执行
        apiUploadService.updateDynamicDoc(redisDTO, treeList, testToken);

        // 验证
        verify(ecmBusiDocMapper, times(2)).selectList(any());
    }
}