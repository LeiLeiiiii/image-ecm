package com.sunyard.ecm.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoPhoneDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmMobileParamsDTO;
import com.sunyard.ecm.dto.mobile.EcmMobileBusiListDTO;
import com.sunyard.ecm.dto.mobile.EcmMobileCaptureBusiDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.util.CommonUtils;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.SysStrategyVO;
import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.api.MenuApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lw
 * @describe 移动端采集接口
 * @since 2023-8-14
 */
@Slf4j
@Service
public class MobileCaptureService {
    @Value("${nodeType:1}")
    private String nodeType;
    @Value("${fileSizeMaxMobile:3000}")
    private Integer fileSizeMaxMobile;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private MenuApi menuApi;
    @Resource
    private CommonService commonService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private OperateCaptureService operateCaptureService;

    /**
     * 扫码移动端查询业务类型列表
     */
    public List<EcmBusiInfoPhoneDTO> getMobileBusiList(EcmMobileBusiListDTO ecmMobileCaptureBusiDTO) {
        String pageBusiListKey = ecmMobileCaptureBusiDTO.getPageBusiListKey();
        AssertUtils.isNull(pageBusiListKey, "参数有误");
        String busiList = redisUtils.get(pageBusiListKey);
        List<Long> busiIds = new ArrayList<>();
        if (StrUtil.isNotBlank(busiList)) {
            busiIds = JSONObject.parseArray(busiList, Long.class);

        }
        List<EcmBusiInfoPhoneDTO> busiInfoList = getBusiList(busiIds, null);
        return busiInfoList;
    }

    /**
     * 扫码移动端查询业务类型列表
     */
    public List<EcmBusiInfoPhoneDTO> getBusinessList(EcmMobileCaptureBusiDTO ecmMobileCaptureBusiDTO) {
        List<Long> busiIds = ecmMobileCaptureBusiDTO.getBusiIds();
        AssertUtils.isNull(busiIds, "传入的业务ID集合不能为空");
        String busiNo = ecmMobileCaptureBusiDTO.getBusiNo();
        List<EcmBusiInfoPhoneDTO> busiInfoList = getBusiList(busiIds, busiNo);
        return busiInfoList;
    }

    /**
     * 获取移动端业务列表
     */
    private List<EcmBusiInfoPhoneDTO> getBusiList(List<Long> busiIds, String busiNo) {
        //查询对应业务
        LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(EcmBusiInfo::getBusiId, busiIds)
                .like(StrUtil.isNotBlank(busiNo), EcmBusiInfo::getBusiNo, busiNo)
                .orderByDesc(EcmBusiInfo::getCreateTime);

        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(queryWrapper);
        List<EcmBusiInfoPhoneDTO> ecmBusiInfoPhoneDTOS = new ArrayList<>();
        List<String> appTypeIds = new ArrayList<>();
        ecmBusiInfos.forEach(e -> {
            appTypeIds.add(e.getAppCode());
            EcmBusiInfoPhoneDTO ecmBusiInfoPhoneDTO = new EcmBusiInfoPhoneDTO();
            BeanUtils.copyProperties(e, ecmBusiInfoPhoneDTO);
            ecmBusiInfoPhoneDTOS.add(ecmBusiInfoPhoneDTO);
        });

        if (CollectionUtil.isNotEmpty(appTypeIds)) {
            //业务类型名称赋值
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
            Map<String, List<EcmAppDef>> groupedByApp = ecmAppDefs.stream().collect(Collectors.groupingBy(EcmAppDef::getAppCode));
            ecmBusiInfoPhoneDTOS.forEach(e -> {
                if (!ObjectUtils.isEmpty(groupedByApp.get(e.getAppCode()))) {
                    EcmAppDef ecmAppDef = groupedByApp.get(e.getAppCode()).get(0);
                    e.setAppTypeName(ecmAppDef.getAppName());
                    //设置业务压缩参数
                    e.setQulity(ecmAppDef.getQulity());
                    e.setResiz(ecmAppDef.getResize());
                    e.setIsQulity(ecmAppDef.getIsResize());
                }
            });
        }
        return ecmBusiInfoPhoneDTOS;
    }

    /**
     * 移动端资料类型列表
     */
    public List<EcmBusiStructureTreeDTO> getInformation(AccountTokenExtendDTO tokenExtend, Long busiId, String infoTypeName) {
        return getInformation(tokenExtend,busiId,infoTypeName,null);
    }

    /**
     * 移动端资料类型列表
     */
    public List<EcmBusiStructureTreeDTO> getInformation(AccountTokenExtendDTO tokenExtend, Long busiId, String infoTypeName, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        //业务对应的资料静态树
        List<EcmBusiStructureTreeDTO> result = new ArrayList<>();
        List<EcmBusiDocRedisDTO> docList = new ArrayList<>();
        long l = System.currentTimeMillis();

        if(ecmBusiInfoRedisDTO==null){
            ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(tokenExtend, busiId);
        }
        List<EcmBusiDocRedisDTO> existRedisData = ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS();
        if (CollectionUtil.isEmpty(existRedisData)) {
            //静态树
            existRedisData = commonService.getDocList(ecmBusiInfoRedisDTO.getAppCode());
        }

        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            docList = flattenTreeByStatic(existRedisData, "");
        } else if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            docList = flattenTreeByDynamic(existRedisData, "");
        }


        if (StrUtil.isNotBlank(infoTypeName)) {
            docList = docList.stream().filter(f -> f.getDocName().contains(infoTypeName)).collect(Collectors.toList());
        }
        List<FileInfoRedisDTO> ecmFileInfos = busiCacheService.getFileInfoRedis(busiId);
        //获取资料节点下的文件数
        Map<String, List<FileInfoRedisDTO>> map = new HashMap<>();
        if (CollectionUtil.isNotEmpty(ecmFileInfos)) {
            map = ecmFileInfos.stream().filter(s -> IcmsConstants.ZERO.equals(s.getState())).collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
        }
        //获取资料
        result = checkBusiDocRight(tokenExtend, ecmBusiInfoRedisDTO, docList, map, ecmFileInfos);

//        //获取资料节点的最子级节点
        //添加未归类节点
        addUnclassifiedDocNode(result, ecmBusiInfoRedisDTO, ecmFileInfos);
        Integer capturePage = IcmsConstants.CAPTURE_PAGE;
        if (tokenExtend.isOut()) {
            UserBusiRedisDTO userBusiData = busiCacheService.getUserPageRedis(tokenExtend.getFlagId(), tokenExtend);
            if (userBusiData.getDocCodeShow() != null) {
                Map<Long, List<String>> docCodeShow = userBusiData.getDocCodeShow();
                List<String> strings = docCodeShow.get(ecmBusiInfoRedisDTO.getBusiId());
                if (CollectionUtil.isNotEmpty(strings)) {
                    result = CommonUtils.isshowNode(strings, result);
                }
            }
            if(userBusiData.getIsShow()!=null){
                capturePage = userBusiData.getIsShow();
            }
        }


        //隐藏节点的特殊处理
        if (IcmsConstants.NODETYPPE_NOSHOW.equals(nodeType)) {
//                        //隐藏带锁的节点
            result = CommonUtils.removeTreeLock(result);
        }

        //添加已删除节点
        List<FileInfoRedisDTO> delListFile = null;
        if (!CollectionUtils.isEmpty(ecmFileInfos)) {
            delListFile = ecmFileInfos.stream().filter(p -> IcmsConstants.ONE.equals(p.getState())).collect(Collectors.toList());
        }
        operateCaptureService.addDeletedDocNode(result, ecmBusiInfoRedisDTO, ecmBusiInfoRedisDTO.getTreeType(), ecmBusiInfoRedisDTO.getAppCode(), tokenExtend, capturePage, delListFile);

        return result;
    }


    private void addUnclassifiedDocNode(List<EcmBusiStructureTreeDTO> result, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO, List<FileInfoRedisDTO> fileInfoRedisEntities) {
        //获取所有文件信息
        EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
        //添加是否偏离矫正
        Boolean isFlat = searchIsFlat(StrategyConstantsEnum.OCR_STRATEGY.toString(), ecmBusiInfoRedisDTO.getAppCode());
        node.setIsFlat(isFlat);
        node.setEquipmentId(ecmBusiInfoRedisDTO.getEquipmentId());
        node.setAppTypeName(ecmBusiInfoRedisDTO.getAppTypeName());
        node.setAppCode(ecmBusiInfoRedisDTO.getAppCode());
        node.setId(IcmsConstants.UNCLASSIFIED_ID);
        node.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
        node.setName(IcmsConstants.UNCLASSIFIED);
        node.setType(IcmsConstants.FIVE);
        node.setNodeType(StateConstants.COMMON_ONE);
        node.setPid(ecmBusiInfoRedisDTO.getBusiId().toString());
        node.setPName(ecmBusiInfoRedisDTO.getBusiNo());
        node.setBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
        node.setBusiId(ecmBusiInfoRedisDTO.getBusiId());
        node.setTreeType(ecmBusiInfoRedisDTO.getTreeType());
        node.setIsResize(ecmBusiInfoRedisDTO.getIsQulity());
        node.setResize(ecmBusiInfoRedisDTO.getResiz());
        node.setQulity(ecmBusiInfoRedisDTO.getQulity());
        node.setDocRight(operateCaptureService.addUnclassifyNodeDocRight());
        node.setFileCount(0);
        if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            //获取未归类文件信息
            List<FileInfoRedisDTO> collectByUnclassified = fileInfoRedisEntities.stream()
                    .filter(p -> IcmsConstants.ZERO.equals(p.getState()) && IcmsConstants.UNCLASSIFIED_ID.equals(p.getDocCode()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collectByUnclassified)) {
                //统计资料节点下文件数量
                node.setFileCount(collectByUnclassified.size());
                //添加md5list
                List<String> md5List = collectByUnclassified.stream()
                        .map(EcmFileInfoDTO::getFileMd5)
                        .distinct()
                        .collect(Collectors.toList());
                node.setMd5List(md5List);
            }
        }
        result.add(StateConstants.ZERO,node);

    }

    /**
     * 判断业务资料是否全部没有权限
     */
    private List<EcmBusiStructureTreeDTO> checkBusiDocRight(AccountTokenExtendDTO tokenExtend,
                                                            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO,
                                                            List<EcmBusiDocRedisDTO> docList,
                                                            Map<String, List<FileInfoRedisDTO>> map, List<FileInfoRedisDTO> fileInfoRedis) {
        List<EcmBusiStructureTreeDTO> resultList = new ArrayList<>();
        List<EcmBusiDocRedisDTO> resultDocList = new ArrayList<>();
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(ecmBusiInfoRedisDTO.getAppCode());
        EcmAppDef redisZip = busiCacheService.getRedisZip(ecmBusiInfoRedisDTO.getAppCode());
        //是否加密
        Result<SysParamDTO> result = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = result.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        Integer isEncrypt = IcmsConstants.NO_ENCRYPT;
        if (sysStrategyVO.getEncryptStatus()) {
            isEncrypt = IcmsConstants.YES_ENCRYPT;
        }
        //获取最新权限
        List<EcmDocrightDefDTO> currentDocRight = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, tokenExtend);
//        ecmBusiInfoRedisDTO.setDocRightList(currentDocRight);
        Map<String, List<EcmDocrightDefDTO>> collect = currentDocRight.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
        //获取标记节点
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiInfoRedisDTO.getEcmBusiDocs();
        Map<String, List<EcmBusiDoc>> docCodeListMap = null;
        if (!ObjectUtils.isEmpty(ecmBusiDocs)) {
            //根据docCode分组
            docCodeListMap = ecmBusiDocs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getDocCode));
        }

        for (EcmBusiDocRedisDTO ecmBusiDocRedisDTO : docList) {
            if (CollectionUtil.isNotEmpty(ecmBusiDocRedisDTO.getChildren())) {
                continue;
            }
            resultDocList.add(ecmBusiDocRedisDTO);
        }

        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(collect.keySet());
        Map<String, List<EcmDocDef>> collect2 = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        Boolean isFlat = searchIsFlat(StrategyConstantsEnum.OCR_STRATEGY.toString(), ecmBusiInfoRedisDTO.getAppCode());

        for (EcmBusiDocRedisDTO ecmBusiDocRedisDTO : resultDocList) {
            //跟节点,添加数据
            EcmBusiStructureTreeDTO structureTreeDTO = new EcmBusiStructureTreeDTO();
            BeanUtils.copyProperties(ecmBusiDocRedisDTO, structureTreeDTO);
            structureTreeDTO.setId(ecmBusiDocRedisDTO.getDocId() != null ? ecmBusiDocRedisDTO.getDocId().toString() : ecmBusiDocRedisDTO.getDocCode());
            structureTreeDTO.setName(ecmBusiDocRedisDTO.getDocName());
            structureTreeDTO.setFileCount(map.get(ecmBusiDocRedisDTO.getDocCode()) == null ? 0 : map.get(ecmBusiDocRedisDTO.getDocCode()).size());
            List<EcmDocDef> ecmDocDefs1 = collect2.get(ecmBusiDocRedisDTO.getDocCode());
            if (CollectionUtil.isNotEmpty(ecmDocDefs1)) {
                EcmDocDef ecmDocDef = ecmDocDefs1.get(0);
                structureTreeDTO.setImgLimit(ecmDocDef.getImgLimit());
                structureTreeDTO.setAudioLimit(ecmDocDef.getAudioLimit());
                structureTreeDTO.setOfficeLimit(ecmDocDef.getOfficeLimit());
                structureTreeDTO.setVideoLimit(ecmDocDef.getVideoLimit());
                structureTreeDTO.setOtherLimit(ecmDocDef.getOtherLimit());
                //如果是对外接口把最大最小值改为多维度权限配置
                EcmDocrightDefDTO ecmDocrightDefDTO = Optional.ofNullable(collect.get(ecmDocDef.getDocCode()))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .orElse(null);
                //先取权限,取不到并且未启用取资料定义表
                if (!ObjectUtils.isEmpty(ecmDocrightDefDTO)&&IcmsConstants.ONE.toString().equals(ecmDocrightDefDTO.getEnableLenLimit())) {
                    //优先用 ecmDocrightDefDTO 的 maxLen：不为 null 则用，为 null 则取 ecmDocDef 的 maxFiles
                    structureTreeDTO.setMaxLen(ecmDocrightDefDTO.getMaxLen() != null
                            ? ecmDocrightDefDTO.getMaxLen()
                            : ecmDocDef.getMaxFiles());

                    //优先用 ecmDocrightDefDTO 的 minLen：不为 null 则用，为 null 则取 ecmDocDef 的 minFiles
                    structureTreeDTO.setMinLen(ecmDocrightDefDTO.getMinLen() != null
                            ? ecmDocrightDefDTO.getMinLen()
                            : ecmDocDef.getMinFiles());
                } else {
                    //若 ecmDocrightDefDTO 为空，直接取 ecmDocDef 的配置
                    structureTreeDTO.setMaxLen(ecmDocDef.getMaxFiles());
                    structureTreeDTO.setMinLen(ecmDocDef.getMinFiles());
                }
            }
            //资料权限
            structureTreeDTO.setAppCode(ecmBusiInfoRedisDTO.getAppCode());
            structureTreeDTO.setBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
            structureTreeDTO.setIsFlat(isFlat);
            //需采集资料文件数量
            structureTreeDTO.setNeedCaptureFileCount(ecmBusiDocRedisDTO.getMinLen());
            structureTreeDTO.setEquipmentId(ecmAppDef.getEquipmentId());
            structureTreeDTO.setBusiId(ecmBusiInfoRedisDTO.getBusiId());
            //添加md5list
            if (!CollectionUtils.isEmpty(fileInfoRedis)) {
                List<String> md5List = fileInfoRedis.stream()
                        .filter(p -> IcmsConstants.ZERO.equals(p.getState())
                                && p.getDocCode().equals(ecmBusiDocRedisDTO.getDocCode()))
                        .map(EcmFileInfoDTO::getFileMd5)
                        .distinct()
                        .collect(Collectors.toList());
                structureTreeDTO.setMd5List(md5List);
            }
            if (!ObjectUtils.isEmpty(ecmBusiInfoRedisDTO)) {
                //添加业务类型名称
                structureTreeDTO.setAppTypeName(ecmBusiInfoRedisDTO.getAppTypeName());
            }
            //添加是否压缩
            structureTreeDTO.setIsResize(redisZip.getIsResize());
            structureTreeDTO.setIsQulity(redisZip.getIsResize());
            //添加压缩比例
            structureTreeDTO.setResize(redisZip.getResize());
            //添加压缩质量
            structureTreeDTO.setQulity(redisZip.getQulity());
            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = collect.get(ecmBusiDocRedisDTO.getDocCode());
            structureTreeDTO.setDocRight(CollectionUtil.isNotEmpty(ecmDocrightDefDTOS) ? ecmDocrightDefDTOS.get(0) : null);
            //添加父节点层级关系
            structureTreeDTO.setFatherNodeName(removeFirstSlash(ecmBusiDocRedisDTO.getAllParentName()));
            structureTreeDTO.setType(IcmsConstants.THREE);
            structureTreeDTO.setTreeType(ecmBusiInfoRedisDTO.getTreeType());
            structureTreeDTO.setIsEncrypt(isEncrypt);

            if (!ObjectUtils.isEmpty(docCodeListMap)) {
                Map<Long, List<FileInfoRedisDTO>> collect1 = null;
                if (!org.apache.commons.collections4.CollectionUtils.isEmpty(fileInfoRedis)) {
                    collect1 = fileInfoRedis.stream().filter(s -> s.getMarkDocId() != null).collect(Collectors.groupingBy(FileInfoRedisDTO::getMarkDocId));
                }
                //添加标记节点信息
                operateCaptureService.addDocMarkNode(structureTreeDTO, docCodeListMap, collect1, ecmBusiInfoRedisDTO.getTreeType(), CollectionUtil.isNotEmpty(ecmDocrightDefDTOS) ? ecmDocrightDefDTOS.get(0) : null, isFlat, structureTreeDTO.getIsEncrypt(), ecmBusiInfoRedisDTO);
            }
            //添加权限，是否加锁
            List<String> docCodeList = operateCaptureService.checkAllDocRight(currentDocRight, IcmsConstants.OPERATIONFLAG_TREE);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(docCodeList)) {
                structureTreeDTO.setLock(docCodeList.contains(structureTreeDTO.getDocCode()));
            }
            resultList.add(structureTreeDTO);
        }

        return resultList;
    }

    private static String removeFirstSlash(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        int indexOfFirstSlash = input.indexOf('/');
        if (indexOfFirstSlash == -1) {
            return input;
        }

        return input.substring(0, indexOfFirstSlash) + input.substring(indexOfFirstSlash + 1);
    }

    /**
     * 静态树
     */
    public List<EcmBusiDocRedisDTO> flattenTreeByStatic(List<EcmBusiDocRedisDTO> tree, String fatherNodeNames) {
        List<EcmBusiDocRedisDTO> flatList = new ArrayList<>();
        for (EcmBusiDocRedisDTO node : tree) {
            String allParentName = "0".equals(node.getParent()) ? "" : fatherNodeNames + "/" + node.getParentName();
            node.setAllParentName(allParentName);
            flatList.add(node);
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                flatList.addAll(flattenTreeByStatic(node.getChildren(), "".equals(fatherNodeNames) ? node.getAllParentName() : fatherNodeNames + "/" + node.getAllParentName()));
            }
        }
        return flatList;
    }

    /**
     * 动态树
     */
    private List<EcmBusiDocRedisDTO> flattenTreeByDynamic(List<EcmBusiDocRedisDTO> tree, String fatherNodeNames) {
        List<EcmBusiDocRedisDTO> flatList = new ArrayList<>();
        for (EcmBusiDocRedisDTO node : tree) {
            String allParentName = String.valueOf(node.getBusiId()).equals(node.getParent()) ? "" : "/" + fatherNodeNames;
            node.setAllParentName(allParentName);
            flatList.add(node);
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                flatList.addAll(flattenTreeByDynamic(node.getChildren(), "".equals(fatherNodeNames) ? node.getDocName() : fatherNodeNames + "/" + node.getDocName()));
            }
        }
        return flatList;
    }

    /**
     * 平铺列表树结构
     */
    private Boolean searchIsFlat(String key, String appCode) {
        //获取配置信息
        Result<SysParamDTO> pageReturnResult = paramApi.searchValueByKey(key);
        if (!pageReturnResult.isSucc()) {
            throw new SunyardException(pageReturnResult.getMsg());
        }
        SysParamDTO data = pageReturnResult.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        return sysStrategyVO.getOcrFlatStatus() && sysStrategyVO.getOcrFlatIds().contains(appCode);
    }


    /**
     * 获取资料需要采集的文件数量
     */
    private Integer getNeedCaptureFileCount(EcmBusiDocRedisDTO ecmBusiDocRedisDTO, String appCode) {
        Integer needCaptureFileCount = IcmsConstants.ZERO;
        //查询当前版本资料权限
        LambdaQueryWrapper<EcmAppDocright> rightDefWrapper = new LambdaQueryWrapper<>();
        rightDefWrapper.eq(EcmAppDocright::getAppCode, ecmBusiDocRedisDTO.getDocCode());
        rightDefWrapper.eq(EcmAppDocright::getRightNew, IcmsConstants.ONE);
        EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectOne(rightDefWrapper);
        if (ecmAppDocright != null) {
            Integer rightVer = ecmAppDocright.getRightVer();
            LambdaQueryWrapper<EcmDocrightDef> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmDocrightDef::getAppCode, appCode);
            wrapper.eq(EcmDocrightDef::getDocCode, ecmBusiDocRedisDTO.getDocCode());
            wrapper.eq(EcmDocrightDef::getRightVer, rightVer);
            List<EcmDocrightDef> ecmDocrightDefList = ecmDocrightDefMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(ecmDocrightDefList)) {
                EcmDocrightDef ecmDocrightDef = ecmDocrightDefList.get(0);
                //   needCaptureFileCount = ecmDocrightDef.getMinPages();
            }
        }
        return needCaptureFileCount;
    }

    /**
     * 移动端获取资料文件列表
     */
    public PageInfo getDocFileList(Long busiId, String docId, Integer shouAll, Long markDocId, AccountTokenExtendDTO token) {
        EcmsCaptureVO ecmsCaptureVO = new EcmsCaptureVO();
        ecmsCaptureVO.setBusiId(busiId);
        ecmsCaptureVO.setDocId(docId);
        ecmsCaptureVO.setShowAll(shouAll);
        ecmsCaptureVO.setDocCode(docId);
        ecmsCaptureVO.setMarkDocId(markDocId);
        PageInfo pageInfo = operateCaptureService.searchEcmsFileList(ecmsCaptureVO, token);
        return pageInfo;
    }

    /**
     * 获取移动端页面地址
     */
    public Result getMobilePagePath(List<Long> busiIdList, AccountTokenExtendDTO token) {
        AssertUtils.isNull(CollectionUtil.isEmpty(busiIdList), "采集页面无业务数据");
        EcmMobileParamsDTO ecmMobileParamsDTO = new EcmMobileParamsDTO();
        Result<SysParamDTO> pagePathResult = paramApi.searchValueByKey(IcmsConstants.ECMS_MOBILE_CAPTURE_PATH);
        if (pagePathResult.isSucc()) {
            BeanUtils.copyProperties(pagePathResult.getData(), ecmMobileParamsDTO);
            Map<String, String> map = dealUrl(ecmMobileParamsDTO.getValue(), token);
            String timestamp = map.get("timestamp");
            String encryptUrl = map.get("encryptUrl");
            ecmMobileParamsDTO.setValue(encryptUrl);
            ecmMobileParamsDTO.setPageFlag(token.getFlagId());
            redisUtils.set(RedisConstants.PAGE_BUSI_LIST + timestamp, JSONObject.toJSONString(busiIdList), TimeOutConstants.ONE_DAY);
            ecmMobileParamsDTO.setPageBusiListKey(RedisConstants.PAGE_BUSI_LIST + timestamp);

        }
        return Result.success(ecmMobileParamsDTO);
    }

    /**
     * 获取影像采集按钮列表
     */
    public List<HashMap<String, String>> getRightButtonList(Long id, String imageCapture) {
        List<String> button = Arrays.asList("归类", "编辑", "删除", "重命名");
        List<HashMap<String, String>> collect = getHashMaps(id, imageCapture, button);
        return collect;
    }

    /**
     * 获取已删除界面钮列表
     */
    public List<HashMap<String, String>> getDeletedButtonList(Long id, String captureViewDeleted) {
        List<String> button = Arrays.asList("恢复");
        List<HashMap<String, String>> collect = getHashMaps(id, captureViewDeleted, button);
        return collect;
    }

    /**
     * 获取业务列表完整的
     */
    public List<EcmBusiInfoPhoneDTO> getBusinessListAll(EcmMobileBusiListDTO ecmMobileCaptureBusiDTO, AccountTokenExtendDTO token) {
        List<EcmBusiInfoPhoneDTO> mobileBusiListo = null;
        if(StringUtils.isEmpty(ecmMobileCaptureBusiDTO.getPageBusiListKey())){
            EcmMobileCaptureBusiDTO dto = new EcmMobileCaptureBusiDTO();
            dto.setBusiIds(ecmMobileCaptureBusiDTO.getBusiIds());
            mobileBusiListo = getBusinessList(dto);
        }else {
            mobileBusiListo = getMobileBusiList(ecmMobileCaptureBusiDTO);
        }
        for (EcmBusiInfoPhoneDTO dto : mobileBusiListo) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, dto.getBusiId());
            List<EcmBusiStructureTreeDTO> information = getInformation(token, dto.getBusiId(), ecmMobileCaptureBusiDTO.getInfoTypeName(),ecmBusiInfoRedisDTO);
            //添加属性信息
            addEcmAppAttrDTO(dto, ecmBusiInfoRedisDTO);
            EcmsCaptureVO ecmsCaptureVO = new EcmsCaptureVO();
            ecmsCaptureVO.setBusiId( dto.getBusiId());
//            List<FileInfoRedisDTO> resultFiles_del = ecmRedisService.getFileInfoRedis(dto.getBusiId());
//            if (CollUtil.isNotEmpty(resultFiles)) {
//                //资料节点名称为空返回空字符串
//                //文件格式统一转换小写
//                resultFiles.forEach(file -> {
//                    if (file.getDocName() == null) {
//                        file.setDocName("");
//                    }
//                    file.setFileHistories(null);
//                    if (!ObjectUtils.isEmpty(file.getFormat())) {
//                        file.setFormat(file.getFormat().toLowerCase());
//                    }
//                });
////                //添加权限列表
//                resultFiles = operateCaptureService.andDocRightList(ecmBusiInfoRedisDTO, resultFiles, token);
//            }
            PageInfo pageInfo = operateCaptureService.searchEcmsFileList(ecmsCaptureVO, token);
            List<FileInfoRedisDTO> resultFiles = pageInfo.getList();
            Map<String, List<FileInfoRedisDTO>> collect = resultFiles.stream()
                    // 1. 先筛选状态为 NO 的记录
                    .filter(s -> StateConstants.NO.equals(s.getState()))
                    // 2. 对每个对象的指定属性置空
                    .map(fileInfoRedisDTO -> {
                        fileInfoRedisDTO.setBucketName(null);
                        fileInfoRedisDTO.setFilePath(null);
                        fileInfoRedisDTO.setObjectKey(null);
                        fileInfoRedisDTO.setUrl(null);
                        return fileInfoRedisDTO; // 返回处理后的对象
                    })
                    // 3. 按 docCode 分组
                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
            for(EcmBusiStructureTreeDTO e: information){
                if(IcmsConstants.TREE_TYPE_DOCCODE.equals(e.getType())){
                    List<FileInfoRedisDTO> list = collect.get(e.getDocCode());
                    if(CollectionUtil.isNotEmpty(list)){
                        if(list.size()>fileSizeMaxMobile){
                            list = list.subList(0, fileSizeMaxMobile);
                        }
                        e.setResultFiles(list);
                    }else{
                        e.setResultFiles(new ArrayList<>());
                    }

                }else if(IcmsConstants.TREE_TYPE_DEL.equals(e.getType())){
                    List<FileInfoRedisDTO> resultFilesD = busiCacheService.getFileInfoRedis(dto.getBusiId());
                    List<FileInfoRedisDTO> collect1 = resultFilesD.stream()
                            .filter(s -> StateConstants.YES.equals(s.getState()))
                            .map(fileInfoRedisDTO -> {
                                fileInfoRedisDTO.setBucketName(null);
                                fileInfoRedisDTO.setFilePath(null);
                                fileInfoRedisDTO.setObjectKey(null);
                                fileInfoRedisDTO.setUrl(null);
                                return fileInfoRedisDTO;
                            })
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(collect1)) {
                        //资料节点名称为空返回空字符串
                        //文件格式统一转换小写
                        collect1.forEach(file -> {
                            if (file.getDocName() == null) {
                                file.setDocName("");
                            }
                            file.setFileHistories(null);
                            if (!ObjectUtils.isEmpty(file.getFormat())) {
                                file.setFormat(file.getFormat().toLowerCase());
                            }
                        });
                        collect1 = operateCaptureService.andDocRightList(ecmBusiInfoRedisDTO, collect1, token);
                    }
                    e.setResultFiles(collect1);
                }else if(IcmsConstants.TREE_TYPE_UNCLASSIFIED.equals(e.getType())){
                    e.setResultFiles(resultFiles.stream()
                            .filter(s -> IcmsConstants.UNCLASSIFIED_ID.toString().equals(s.getDocCode())
                                    && StateConstants.NO.equals(s.getState()))
                            .map(fileInfoRedisDTO -> {
                                fileInfoRedisDTO.setBucketName(null);
                                fileInfoRedisDTO.setFilePath(null);
                                fileInfoRedisDTO.setObjectKey(null);
                                fileInfoRedisDTO.setUrl(null);
                                return fileInfoRedisDTO;
                            })
                            .collect(Collectors.toList()));
                }
            }

            dto.setChildren(information);
        }
        return mobileBusiListo;
    }

    private void addEcmAppAttrDTO(EcmBusiInfoPhoneDTO dto, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        List<EcmAppAttrDTO> attrList = ecmBusiInfoRedisDTO.getAttrList();
        if(!CollectionUtils.isEmpty(attrList)){
            List<EcmAppAttrDTO> dtoList = new ArrayList<>();
            attrList.forEach(a->{
                EcmAppAttrDTO attrDTO = new EcmAppAttrDTO();
                attrDTO.setAttrName(a.getAttrName());
                attrDTO.setAppAttrValue(a.getAppAttrValue());
                dtoList.add(attrDTO);
            });
            dto.setAttrList(dtoList);
        }
    }


    private List<HashMap<String, String>> getHashMaps(Long id, String captureViewDeleted, List<String> button) {
        Result<List<HashMap<String, String>>> rightButtonListByMenuPerms = menuApi.getRightButtonListByMenuPerms(id, captureViewDeleted);
        List<HashMap<String, String>> data = rightButtonListByMenuPerms.getData();
//        if (!CollectionUtils.isEmpty(data)) {
//            return data.stream().filter(p -> button.contains(p.get("name"))).collect(Collectors.toList());
//        }
        return data;
    }

    /**
     * 获取影像采集按钮列表
     */
    public List<HashMap<String, String>> getRightButtonList(List<String> role, String imageCapture) {
        List<String> button = Arrays.asList("归类", "编辑", "删除", "重命名");
        List<HashMap<String, String>> collect = getHashMaps(role, imageCapture, button);
        return collect;
    }

    /**
     * 获取已删除界面钮列表
     */
    public List<HashMap<String, String>> getDeletedButtonList(List<String> role, String captureViewDeleted) {
        List<String> button = Arrays.asList("恢复");
        List<HashMap<String, String>> collect = getHashMaps(role, captureViewDeleted, button);
        return collect;
    }

    private List<HashMap<String, String>> getHashMaps(List<String> role, String captureViewDeleted, List<String> button) {
        Result<List<HashMap<String, String>>> rightButtonListByMenuPerms = menuApi.getRightButtonListByRoles(role, captureViewDeleted);
        List<HashMap<String, String>> data = rightButtonListByMenuPerms.getData();
//        if (!CollectionUtils.isEmpty(data)) {
//            return data.stream().filter(p -> button.contains(p.get("name"))).collect(Collectors.toList());
//        }
        return data;
    }

    /**
     * 页面路径加密处理（路径+时间戳）
     */
    private Map<String, String> dealUrl(String mobilePageUrl, AccountTokenExtendDTO token) {
        Map map = new HashMap();
        String encryptUrl = "";
        if (token.isOut()) {
            encryptUrl = mobilePageUrl;
        } else {
            Long timestamp = Instant.now().toEpochMilli();
            map.put("timestamp", timestamp.toString());
            encryptUrl = mobilePageUrl + "?" + "timestamp=" + timestamp;
        }

        Integer loginEncryption = 1;
        //对路径加密
        try {
            switch (loginEncryption) {
                case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                    encryptUrl = RsaUtils.encrypt(encryptUrl);
                    break;
                default:
                    encryptUrl = Sm2Util.encrypt(encryptUrl.getBytes());
            }
            if (encryptUrl.startsWith(LoginEncryptionConstant.ENCRYPTED_PREFIX)) {
                encryptUrl = encryptUrl.substring(LoginEncryptionConstant.ENCRYPTED_PREFIX.length());
            }
            map.put("encryptUrl", encryptUrl);
        } catch (Exception e) {
            log.error("路径解析有误");
        }
        return map;
    }
}
