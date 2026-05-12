package com.sunyard.ecm.service.sdk;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.PatternConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.AddBusiDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.EcmBusiAttrDTO;
import com.sunyard.ecm.dto.EcmUploadAllDTO;
import com.sunyard.ecm.dto.EcmUploadFileDTO;
import com.sunyard.ecm.dto.EcmVTreeDataDTO;
import com.sunyard.ecm.dto.FileDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author： rao
 * @Description：第三方文件上传类
 * @create： 2025/4/21
 */
@Slf4j
@Service
public class ApiUploadService  {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private CommonService commonService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private CaptureSubmitService captureSubmitService;

    /**
     * 文件上传
     */
    public Result checkFile(EcmUploadAllDTO dto) {
        //1、校验用户
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmBaseInfoDTO ecmBaseInfoDTO = dto.getEcmRootDataDTO().getEcmBaseInfoDTO();
        token.setRoleCodeList(ecmBaseInfoDTO.getRoleCode());
        token.setUsername(ecmBaseInfoDTO.getUserCode());
        token.setName(ecmBaseInfoDTO.getUserName());
        token.setOrgCode(ecmBaseInfoDTO.getOrgCode());
        token.setOrgName(ecmBaseInfoDTO.getOrgName());
        token.setOut(true);
        long l1 = System.currentTimeMillis();
        token = busiCacheService.checkUser(ecmBaseInfoDTO, token);

        ecmBaseInfoDTO.setUserName(token.getName());
        ecmBaseInfoDTO.setOrgName(token.getOrgName());
        AddBusiDTO ecmRootDataDTO2 = dto.getEcmRootDataDTO();
        ecmRootDataDTO2.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        dto.setEcmRootDataDTO(ecmRootDataDTO2);
        long l2 = System.currentTimeMillis();
        log.debug("校验用户时间：" + (l2 - l1));

        //2、校验业务
        String appCode = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getAppCode();
        if (Strings.isBlank(appCode)) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务不存在!");
        }
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .in(EcmAppAttr::getAppCode, appCode)
                .eq(EcmAppAttr::getIsKey, StateConstants.YES)
        );
        if (CollectionUtils.isEmpty(ecmAppAttrs)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务类型appCode的主键：" + appCode + "不存在");
        }
        EcmAppAttr ecmAppAttr = ecmAppAttrs.get(0);
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getEcmBusiAttrDTOList();
        List<EcmBusiAttrDTO> collect2 = ecmBusiAttrDTOList.stream().filter(s -> ecmAppAttr.getAttrCode().equals(s.getAttrCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect2) || StrUtil.isBlank(collect2.get(0).getAppAttrValue())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引不能为空!");
        }
        String busNo = collect2.get(0).getAppAttrValue();


        //业务busno
        dto.getEcmRootDataDTO().getEcmBusExtendDTOS().setBusiNo(busNo);

        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        if (ecmAppDef == null) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务不存在!");
        }

        Integer treeType = null;
        //判断业务树类型
        List<EcmVTreeDataDTO> ecmVTreeDataDTOS = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getEcmVTreeDataDTOS();
        //静态树
        if (CollectionUtil.isEmpty(ecmVTreeDataDTOS)) {
            ecmBaseInfoDTO.setTypeTree(IcmsConstants.STATIC_TREE.toString());
            treeType = IcmsConstants.STATIC_TREE;
        } else {
            ecmBaseInfoDTO.setTypeTree(IcmsConstants.DYNAMIC_TREE.toString());
            //取出动态树资料
            treeType = IcmsConstants.DYNAMIC_TREE;
        }

        //3、业务处理-业务不存在新建，业务存在判断动态树是否需要新建
        EcmBusiInfoDTO ecmBusiInfoDTO = new EcmBusiInfoDTO();
        //业务不存在，但是业务类型一定存在
        ecmBusiInfoDTO.setAppTypeName(ecmAppDef.getAppName());
        ecmBusiInfoDTO.setEquipmentId(ecmAppDef.getEquipmentId());
        ecmBusiInfoDTO.setAppCode(ecmAppDef.getAppCode());
        ecmBusiInfoDTO.setAppTypeName(ecmAppDef.getAppName());
        ecmBusiInfoDTO.setEquipmentId(ecmAppDef.getEquipmentId());
        ecmBusiInfoDTO.setBusiBatchNo(busNo);
        ecmBusiInfoDTO.setBusiNo(busNo);
        ecmBusiInfoDTO.setTreeType(treeType);
        ecmBusiInfoDTO = saveBusiOrNot(dto, ecmBusiInfoDTO, treeType, token, ecmVTreeDataDTOS, ecmAppAttr, busNo, appCode);

        //4、校验文件
        EcmBusiFileInfoDTO ecmBusiFileInfoDTO = new EcmBusiFileInfoDTO();
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        List<FileDTO> matchFileList = new ArrayList<>();
        List<FileDTO> repeatFile = new ArrayList<>();
        BeanUtil.copyProperties(ecmBusiInfoDTO, ecmFileInfoDTO);
        ecmFileInfoDTO.setTreeType(treeType.toString());
        List<EcmUploadFileDTO> splitDTO = dto.getSplitDTO();
        if(dto.getDocNo()==null){
            dto.setDocNo(IcmsConstants.UNCLASSIFIED_ID);
        }
        splitDTO.forEach(s->{if(s.getDocNo()==null){s.setDocNo(IcmsConstants.UNCLASSIFIED_ID);}});
        EcmUploadFileDTO ecmUploadFileDTO = splitDTO.stream().filter(f -> f.getDocNo().contains(dto.getDocNo())).findFirst().get();
        Map<String, Object> map = fileToUnclassified(treeType, ecmUploadFileDTO, ecmFileInfoDTO, ecmVTreeDataDTOS);

        if (MapUtil.isNotEmpty(map)) {
            Boolean unClassifyFlag = (Boolean) map.get("unClassifyFlag");
            //未匹配到，文件放到未归类
            if (unClassifyFlag) {
                matchFileList.addAll(ecmUploadFileDTO.getFileAndSortDTOS());
                ecmBusiFileInfoDTO.setMatchFileList(matchFileList);
            } else {
                //默认有新增权限，过滤重复文件
                //根据资料id获取已经上传过的文件md5
                List<EcmFileInfo> ecmFileInfos = checkRepeatFile(ecmBusiInfoDTO.getBusiId(), ecmUploadFileDTO.getDocNo());
                //需要过滤掉已经上传过的文件md5
                List<String> md5List = ecmFileInfos.stream().map(EcmFileInfo::getFileMd5).collect(Collectors.toList());
                List<FileDTO> fileAndSortDTOS = ecmUploadFileDTO.getFileAndSortDTOS();
                List<FileDTO> repeatFileList = fileAndSortDTOS.stream().filter(f -> md5List.contains(f.getFileMd5())).collect(Collectors.toList());
                repeatFile.addAll(repeatFileList);
                //没有重复的文件
                List<FileDTO> noRepeatFileList = fileAndSortDTOS.stream().filter(f -> !md5List.contains(f.getFileMd5())).collect(Collectors.toList());
                //校验文件格式（去掉格式校验）
                List<FileDTO> matchFile = noRepeatFileList;
                matchFileList.addAll(matchFile);
            }
            ecmBusiFileInfoDTO.setMatchFileList(matchFileList);
            ecmBusiFileInfoDTO.setRepeatFileMd5List(repeatFile);
            ecmBusiFileInfoDTO.setEcmFileInfoDTO(ecmFileInfoDTO);
        }
        return Result.success(JSONObject.toJSONString(ecmBusiFileInfoDTO), ResultCode.SUCCESS.getCode(), null);
    }

    /**
     * 校验重复文件
     *
     * @param busiId
     */
    public List<EcmFileInfo> checkRepeatFile(Long busiId, String docCode) {
        //根据busiId和docNo获取该类型已经上传过的文件
        LambdaQueryWrapper<EcmFileInfo> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(EcmFileInfo::getBusiId, busiId);
        fileWrapper.eq(EcmFileInfo::getDocCode, docCode);
        fileWrapper.eq(EcmFileInfo::getState,IcmsConstants.ZERO);
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(fileWrapper);
        return ecmFileInfos;
    }


    public Map<String, Object> fileToUnclassified(Integer treeType, EcmUploadFileDTO fileDTO, EcmFileInfoDTO ecmFileInfoDTO, List<EcmVTreeDataDTO> dynamicTreeData) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(fileDTO.getDocNo())) {
            String docNo = fileDTO.getDocNo();
            if (treeType.equals(IcmsConstants.STATIC_TREE)) {
                ecmFileInfoDTO.setDocCode(docNo);
                ecmFileInfoDTO.setDocId(fileDTO.getDocNo());
                //获取关联资料
                LambdaQueryWrapper<EcmAppDocRel> relWrapper = new LambdaQueryWrapper<>();
                relWrapper.eq(EcmAppDocRel::getAppCode, ecmFileInfoDTO.getAppCode());
                relWrapper.eq(EcmAppDocRel::getDocCode, docNo);
                relWrapper.eq(EcmAppDocRel::getType, IcmsConstants.ONE);
                EcmAppDocRel ecmAppDocRels = ecmAppDocRelMapper.selectOne(relWrapper);
                if (ecmAppDocRels == null) {
                    ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                    ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                    map.put("unClassifyFlag", true);
                } else {
                    //静态树文件类型允许所有文件类型
                    List<String> existFileTypeList = getMatchFileTypeList();
                    //默认有新增权限
                    map.put("unClassifyFlag", false);
                    map.put("existFileTypeList", existFileTypeList);
                }

            } else {
                if (CollectionUtil.isNotEmpty(dynamicTreeData)) {
                    //平铺树
                    List<EcmVTreeDataDTO> ecmVTreeDataDTOS1 = flattenTree(dynamicTreeData);
                    Optional<EcmVTreeDataDTO> first = ecmVTreeDataDTOS1.stream().filter(f -> f.getDocCode().equals(docNo)).findFirst();
                    if (first.isPresent()) {
                        //取出动态树docId
                        LambdaQueryWrapper<EcmBusiDoc> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(EcmBusiDoc::getBusiId, ecmFileInfoDTO.getBusiId());
                        wrapper.eq(EcmBusiDoc::getDocCode, docNo);
                        EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectOne(wrapper);
                        if (ecmBusiDoc != null) {
                            ecmFileInfoDTO.setDocCode(docNo);
                            ecmFileInfoDTO.setDocId(ecmBusiDoc.getDocId().toString());
                            map.put("unClassifyFlag", false);
                            List<String> existFileTypeList = getMatchFileTypeList();
                            map.put("existFileTypeList", existFileTypeList);
                        }
                    } else {
                        ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                        ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                        map.put("unClassifyFlag", true);
                    }
                }
            }
        } else {
            ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
            map.put("unClassifyFlag", true);
        }
        return map;
    }


    /**
     * 平铺动态树结构
     *
     * @param list
     * @return
     */
    public List<EcmVTreeDataDTO> flattenTree(List<EcmVTreeDataDTO> list) {
        List<EcmVTreeDataDTO> flatList = new ArrayList<>();
        for (EcmVTreeDataDTO node : list) {
            flatList.add(node);
            if (CollectionUtil.isNotEmpty(node.getEcmVTreeDataDTOS())) {
                flatList.addAll(flattenTree(node.getEcmVTreeDataDTOS()));
            }
        }
        return flatList;
    }

    public List<String> getMatchFileTypeList() {
        List<String> objects = new ArrayList<>();

        //获取文件配置的所有类型
        Result<Map<String, List<SysDictionaryDTO>>> dictionaryAll = dictionaryApi.getDictionaryAll(IcmsConstants.FILE_TYPE_DIC, null);
        if (dictionaryAll.isSucc()) {
            Map<String, List<SysDictionaryDTO>> data = dictionaryAll.getData();
            List<SysDictionaryDTO> sysDictionaryDTOS = data.get(IcmsConstants.FILE_TYPE_DIC);

            List<String> collect = sysDictionaryDTOS.stream().map(SysDictionaryDTO::getValue).collect(Collectors.toList());
            Set<String> resultSet = new HashSet<>();
            for (String item : collect) {
                if (StringUtils.isEmpty(item)) {
                    continue;
                }
                JSONObject jsonObject = JSONObject.parseObject(item);
                String limitFormat = jsonObject.getString("limit_format");
                String[] items = limitFormat.split(";");
                for (String subItem : items) {
                    resultSet.add(subItem);
                }
            }
            return new ArrayList<>(resultSet);
        }
        return objects;
    }

    /**
     * 业务校验 是否需要新建
     * @param dto
     * @param ecmBusiInfoDTO
     * @param treeType
     * @param token
     * @param ecmVTreeDataDTOS
     * @param ecmAppAttr
     * @param busNo
     * @param appCode
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public EcmBusiInfoDTO saveBusiOrNot(EcmUploadAllDTO dto, EcmBusiInfoDTO ecmBusiInfoDTO, Integer treeType, AccountTokenExtendDTO token, List<EcmVTreeDataDTO> ecmVTreeDataDTOS, EcmAppAttr ecmAppAttr, String busNo, String appCode) {
        LambdaQueryWrapper<EcmBusiInfo> busiWrapper = new LambdaQueryWrapper<>();
        busiWrapper.eq(EcmBusiInfo::getAppCode, appCode);
        busiWrapper.eq(EcmBusiInfo::getBusiNo, busNo);
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(busiWrapper);
        if (ecmBusiInfo != null) {
            ecmBusiInfoDTO.setBusiId(ecmBusiInfo.getBusiId());
            if (IcmsConstants.DYNAMIC_TREE.equals(treeType)){
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmBusiInfo.getBusiId());
                    // 更新动态树资料
                updateDynamicDoc(ecmBusiInfoRedisDTO1, ecmVTreeDataDTOS, token);
            }
        } else {
            //校验入参是否能新建
            Integer rightVer = checkSaveBusi(treeType, ecmAppAttr, busNo, ecmVTreeDataDTOS, appCode);
            ecmBusiInfoDTO.setTreeType(treeType);
            ecmBusiInfoDTO.setCreateTime(new Date());
            ecmBusiInfoDTO.setOrgCode(token.getOrgCode());
            ecmBusiInfoDTO.setOrgName(token.getOrgName());
            ecmBusiInfoDTO.setCreateUser(token.getUsername());
            ecmBusiInfoDTO.setCreateUserName(token.getName());
            ecmBusiInfoDTO.setRightVer(rightVer);
            ecmBusiInfoDTO.setBusiId(snowflakeUtil.nextId());
            //根据业务类型Code获取业务类型id
            EcmBusiInfo ecmBusiInfo1 = new EcmBusiInfo();
            BeanUtils.copyProperties(ecmBusiInfoDTO, ecmBusiInfo1);
            ecmBusiInfoMapper.insert(ecmBusiInfo1);
            //业务属性
            List<EcmAppAttrDTO> ecmAppAttrDTOS = saveOrUpdateEcmBusiMetadata(dto, appCode, ecmBusiInfo1);
            EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
            BeanUtils.copyProperties(ecmBusiInfoDTO, ecmBusiInfoExtend);
            ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);
            operateFullQueryService.addEsBusiInfo(ecmBusiInfoExtend, token.getId());
            //新建逻辑
            if (IcmsConstants.DYNAMIC_TREE.equals(treeType)){
                if (CollectionUtil.isNotEmpty(ecmVTreeDataDTOS)) {
                    Long busiId = ecmBusiInfo1.getBusiId();
                    //外部动态树根节点的父节点设置为业务ID
                    saveVTreeDoc(ecmVTreeDataDTOS, busiId, busiId, token,  ecmBusiInfoExtend);
                }
            }
        }
        return ecmBusiInfoDTO;
    }


    /**
     * 修改动态树节点
     *
     * @param ecmBusiInfoExtend
     * @param ecmVTreeDataDTOS
     */
    public void updateDynamicDoc(EcmBusiInfoRedisDTO ecmBusiInfoExtend, List<EcmVTreeDataDTO> ecmVTreeDataDTOS, AccountTokenExtendDTO token) {
        if (CollectionUtil.isEmpty(ecmVTreeDataDTOS)) {
            return;
        }
        //根据业务类型和资料代码查询资料是否更新
        LambdaQueryWrapper<EcmBusiDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmBusiDoc::getBusiId, ecmBusiInfoExtend.getBusiId());
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(wrapper);
        List<String> collect = ecmBusiDocs.stream().map(EcmBusiDoc::getDocCode).collect(Collectors.toList());

        //调用新增资料节点方法
        saveEcmBusiDoc(ecmBusiInfoExtend, ecmVTreeDataDTOS, collect, token);
    }

    private void saveVTreeDocHave(List<EcmVTreeDataDTO> ecmVTreeDataDTOS, Long busiId, Long parentId, AccountTokenExtendDTO token, List<String> ecmVTreeDataDTOS1, EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        for (EcmVTreeDataDTO vTreeDataDTO : ecmVTreeDataDTOS) {
            if (CollectionUtil.isEmpty(vTreeDataDTO.getEcmVTreeDataDTOS())) {
                if (!ecmVTreeDataDTOS1.contains(vTreeDataDTO.getDocCode())) {
                    buildDocTree(busiId, vTreeDataDTO, parentId);
                }
            } else {
                Long aLong = null;
                if (!ecmVTreeDataDTOS1.contains(vTreeDataDTO.getDocCode())) {
                    aLong = buildDocTree(busiId, vTreeDataDTO, parentId);
                }
                saveVTreeDocHave(vTreeDataDTO.getEcmVTreeDataDTOS(), busiId, aLong, token, ecmVTreeDataDTOS1, ecmBusiInfoExtend);
            }
        }
    }

    /**
     * 保存动态文档树及树形父子关系
     */
    private void saveEcmBusiDoc(EcmBusiInfoRedisDTO ecmBusiInfoExtend, List<EcmVTreeDataDTO> ecmVTreeDataDTOS, List<String> ecmVTreeDataDTOS1, AccountTokenExtendDTO token) {
        if (CollectionUtils.isEmpty(ecmVTreeDataDTOS)) {
            return;
        }
        Long busiId = ecmBusiInfoExtend.getBusiId();
        //外部动态树根节点的父节点设置为业务ID
        saveVTreeDocHave(ecmVTreeDataDTOS, busiId, busiId, token, ecmVTreeDataDTOS1, ecmBusiInfoExtend);
        //构建资料类型动态树（动态树资料节点入库后）
        LambdaQueryWrapper<EcmBusiDoc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiDoc::getBusiId, busiId);
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(queryWrapper);
        EcmBusiDocRedisDTO dto = new EcmBusiDocRedisDTO();
        dto.setDocId(busiId);
        //组装动态树
        commonService.buildDynTree(dto, ecmBusiDocs);
        ecmBusiInfoExtend.setEcmBusiDocRedisDTOS(dto.getChildren());
    }


    private void saveVTreeDoc(List<EcmVTreeDataDTO> ecmVTreeDataDTOS, Long busiId, Long parentId, AccountTokenExtendDTO token, EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        for (EcmVTreeDataDTO vTreeDataDTO : ecmVTreeDataDTOS) {
            if (CollectionUtil.isEmpty(vTreeDataDTO.getEcmVTreeDataDTOS())) {
                buildDocTree(busiId, vTreeDataDTO, parentId);
            } else {
                Long aLong = buildDocTree(busiId, vTreeDataDTO, parentId);
                saveVTreeDoc(vTreeDataDTO.getEcmVTreeDataDTOS(), busiId, aLong, token, ecmBusiInfoExtend);
            }
        }

    }

    /**
     * 保存动态树节点
     *
     * @param busiId
     * @param treeDataDTO
     * @param parentId
     * @return
     */
    private Long buildDocTree(Long busiId, EcmVTreeDataDTO treeDataDTO, Long parentId) {
        EcmBusiDoc ecmBusiDoc = new EcmBusiDoc();
        String docCode = treeDataDTO.getDocCode();
        String docName = treeDataDTO.getDocName();
        ecmBusiDoc.setBusiId(busiId);
        //外部动态树
        ecmBusiDoc.setDocCode(docCode);
        ecmBusiDoc.setDocName(docName);
        //资料顺序外部数据为定义，暂时设定默认值
        ecmBusiDoc.setDocSort(1.0f);
        ecmBusiDoc.setDocMark(StateConstants.ZERO);
        ecmBusiDoc.setParentId(parentId);

        //动态树资料节点表
        ecmBusiDocMapper.insert(ecmBusiDoc);

        //刷新redis
        return ecmBusiDoc.getDocId();
    }

    /**
     * 保存业务属性
     * @param dto
     * @param appCode
     * @param ecmBusiInfo1
     * @return
     */
    public List<EcmAppAttrDTO> saveOrUpdateEcmBusiMetadata(EcmUploadAllDTO dto, String appCode, EcmBusiInfo ecmBusiInfo1) {
        List<EcmBusiAttrDTO> attrDTOList = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getEcmBusiAttrDTOList();
        List<String> collect = attrDTOList.stream().map(EcmBusiAttrDTO::getAttrCode).collect(Collectors.toList());
        List<EcmAppAttr> ecmAppAttrs1 = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, appCode)
                .in(EcmAppAttr::getAttrCode, collect));

        Map<String, List<EcmAppAttr>> collect1 =
                ecmAppAttrs1.stream().collect(Collectors.groupingBy(EcmAppAttr::getAttrCode));
        List<EcmAppAttrDTO> ecmAppAttrDTOS = new ArrayList<>();
        for (EcmBusiAttrDTO ecmBusiAttrDTO : attrDTOList) {
            List<EcmAppAttr> ecmAppAttrs2 = collect1.get(ecmBusiAttrDTO.getAttrCode());
            if(!CollectionUtils.isEmpty(ecmAppAttrs2)){
                EcmAppAttr ecmAppAttr1 = ecmAppAttrs2.get(0);
                EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
                BeanUtils.copyProperties(ecmAppAttr1, ecmAppAttrDTO);
                BeanUtils.copyProperties(ecmBusiAttrDTO, ecmAppAttrDTO);
                ecmAppAttrDTO.setAppAttrId(ecmAppAttr1.getAppAttrId());
                ecmAppAttrDTO.setAttrName(ecmAppAttr1.getAttrName());
                ecmBusiAttrDTO.setAttrName(ecmAppAttr1.getAttrName());
                ecmBusiAttrDTO.setAppAttrId(ecmAppAttr1.getAppAttrId());
                ecmAppAttrDTOS.add(ecmAppAttrDTO);
            }else{
                throw new SunyardException(ResultCode.PARAM_ERROR, "无指定业务属性");
            }
        }

        //新增业务属性
        for(EcmBusiAttrDTO ecmBusiAttrDTO :attrDTOList){
            EcmBusiMetadata ecmBusiMetadata = new EcmBusiMetadata();
            ecmBusiMetadata.setBusiId(ecmBusiInfo1.getBusiId());
            ecmBusiMetadata.setAppAttrId(ecmBusiAttrDTO.getAppAttrId());
            ecmBusiMetadata.setAppAttrVal(ecmBusiAttrDTO.getAppAttrValue());
            //todo 改批量插入
            ecmBusiMetadataMapper.insert(ecmBusiMetadata);
        }
        return ecmAppAttrDTOS;
    }

    private Integer checkSaveBusi(Integer treeType, EcmAppAttr ecmAppAttr, String busNo, List<EcmVTreeDataDTO> ecmVTreeDataDTOS, String appCode) {
        Integer rightVer =null;
        if (IcmsConstants.STATIC_TREE.equals(treeType)) {
            //新建业务
            String regex = ecmAppAttr.getRegex();
            //校验业务索引
            if (!StringUtils.isEmpty(regex)) {
                //校验
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(busNo);
                if (!m.matches()) {
                    throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引的定义不符合规则!");
                }
            } else {
                //校验
                Pattern p = Pattern.compile(PatternConstants.NUMBER_AND_ZM);
                Matcher m = p.matcher(busNo);
                if (!m.matches()) {
                    throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引的定义不符合规则!");
                }
            }
            //获取当前业务类型所对应的权限版本
            List<EcmAppDocright> appDocrights = ecmAppDocrightMapper.selectList(new LambdaQueryWrapper<EcmAppDocright>()
                    .eq(EcmAppDocright::getAppCode, appCode)
                    .eq(EcmAppDocright::getRightNew, StateConstants.COMMON_ONE));
            if (CollectionUtils.isEmpty(appDocrights)) {
                AssertUtils.isTrue(true, "无法新增业务，该业务类型未配置业务资料权限版本");
            }
            rightVer = appDocrights.get(0).getRightVer();
        } else if (IcmsConstants.DYNAMIC_TREE.equals(treeType)) {
            //校验动态树
//            HashMap<String, List<String>> objectObjectHashMap = new HashMap<>();
//            handleCheckTree(ecmVTreeDataDTOS, objectObjectHashMap);
//            List<String> list = objectObjectHashMap.get("child");
//            List<String> parents = objectObjectHashMap.get("parents");
//            list.addAll(parents);
//            Set<String> collect3 = list.stream().collect(Collectors.toSet());
//            if (list.size() != collect3.size()) {
//                throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误:存在相同的资料节点代码!");
//            }
            //校验动态树
            openApiService.validateTree(ecmVTreeDataDTOS);
            rightVer = IcmsConstants.ZERO;

        }
        return rightVer;
    }


    /**
     * 动态树校验
     * @param ecmVTreeDataDTOS
     * @param objectObjectHashMap
     */
    public void handleCheckTree(List<EcmVTreeDataDTO> ecmVTreeDataDTOS, HashMap<String, List<String>> objectObjectHashMap) {
        ecmVTreeDataDTOS.forEach(s -> {
            if (Objects.equals(s.getChildFlag(), StateConstants.YES.toString())) {
                //是否是子节点
                //如果为子节点，则后一层级不应该有值
                if (!CollectionUtils.isEmpty(s.getEcmVTreeDataDTOS())) {
                    throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误:子节点未最后一层级!");
                }
                List<String> list = objectObjectHashMap.get("child");
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(s.getDocCode());
                objectObjectHashMap.put("child", list);
            } else {
                if (CollectionUtils.isEmpty(s.getEcmVTreeDataDTOS())) {
                    throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误：请传递完整的树结构!");
                } else {
                    List<String> list = objectObjectHashMap.get("parents");
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(s.getDocCode());
                    objectObjectHashMap.put("parents", list);
                    handleCheckTree(s.getEcmVTreeDataDTOS(), objectObjectHashMap);
                }
            }
        });
    }
}


