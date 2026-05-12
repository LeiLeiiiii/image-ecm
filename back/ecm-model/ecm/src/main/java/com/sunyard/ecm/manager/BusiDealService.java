package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.AppTypeRuleDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDocRelInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiBatchScanDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiScanInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiSingleScanDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefRuleDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.service.ModelPermissionsService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.vo.EcmBusiDealVO;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lw
 * @describe 采集页面业务处理实现类
 * @since 2023-7-31
 */
@Slf4j
@Service
public class BusiDealService {
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private InstApi instApi;
    @Resource
    private ModelPermissionsService modelPermissionsService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private CommonService commonService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private OperateFullQueryService operateFullQueryService;

    /**
     * 扫描或者拍摄返回参数-批扫
     */
    @Transactional
    @Lock4j(keys = "#ecmBusiBatchScanDTO.appCode", acquireTimeout = 600000)
    public List<EcmBusiDealVO> batchScan(EcmBusiBatchScanDTO ecmBusiBatchScanDTO,
                                         AccountTokenExtendDTO token) {
        List<EcmBusiDealVO> ecmBusiDealVO = null;
        //        lock.lock();
        try {
            ecmBusiDealVO = new ArrayList<>();
            List<EcmBusiInfoRedisDTO> dataList = new ArrayList<>();
            //根据页面标识获取当前页面业务类型
            String pageFlag = ecmBusiBatchScanDTO.getPageFlag();
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();

            //新增时进入扫描
            if (StrUtil.isNotBlank(pageFlag)) {
                List<Long> busiIdList = ecmBusiBatchScanDTO.getBusiIdList();
                if (CollectionUtil.isNotEmpty(busiIdList)) {
                    for (Long busiId : busiIdList) {
                        //获取当前业务的业务结构树
                        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTOs = busiCacheService
                                .getEcmBusiInfoRedisDTO(token, busiId);
                        dataList.add(ecmBusiInfoRedisDTOs);

                    }
                } else {
                    UserBusiRedisDTO userBusiRedisDTO = busiCacheService
                            .getUserPageRedis(token.getFlagId(), token);
                    if (userBusiRedisDTO != null) {
                        //新增只会新增一笔业务类型
                        if (token.isOut()) {
                            //对外接口
                            ecmBusiInfoRedisDTO.setAppCode(ecmBusiBatchScanDTO.getAppCode());
                        } else {
                            //非对外接口
                            ecmBusiInfoRedisDTO.setAppCode(userBusiRedisDTO.getAppType().get(0));
                        }
                        dataList.add(ecmBusiInfoRedisDTO);
                    }
                }
                //匹配扫描数据(批扫)
                ecmBusiDealVO = matchScanData(dataList, ecmBusiBatchScanDTO, token,
                        IcmsConstants.TWO);
            }
        } finally {
            // 释放锁
            //            lock.unlock();
        }
        return ecmBusiDealVO;
    }

    /**
     * 返回扫描数据
     */
    @Transactional
    public List<EcmBusiDealVO> matchScanData(List<EcmBusiInfoRedisDTO> appTypeDataList,
                                             EcmBusiBatchScanDTO ecmBusiScanDTO,
                                             AccountTokenExtendDTO token, Integer modelType) {
        List<EcmBusiDealVO> ecmBusiDealVOList = new ArrayList<>();
        List<EcmBusiScanInfoDTO> busiScanInfoDTOList = ecmBusiScanDTO.getBusiScanInfoDTOList();
        if (CollectionUtil.isEmpty(busiScanInfoDTOList)) {
            return Collections.emptyList();
        }

        //查询所有业务类型
        List<AppTypeRuleDTO> appTypeRuleDTOList = new ArrayList<>();
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(null);
        for (EcmAppDef ecmAppDef : ecmAppDefs) {
            if (StrUtil.isNotBlank(ecmAppDef.getAppTypeSign())
                    && !ecmAppDef.getTypeSignStart().equals(null)
                    && !ecmAppDef.getTypeSignEnd().equals(null)) {
                appTypeRuleDTOList.add(BeanUtil.copyProperties(ecmAppDef, AppTypeRuleDTO.class));
            }
        }

        //1)获取页面现有的业务类型
        for (EcmBusiScanInfoDTO ecmBusiScanInfoDTO : busiScanInfoDTOList) {
            EcmBusiDealVO ecmBusiDealVO = new EcmBusiDealVO();
            ecmBusiDealVO.setFileName(ecmBusiScanInfoDTO.getFilename());
            //****匹配业务类型****，未匹配到则在第一笔业务下生成一个临时任务
            ecmBusiDealVO = matchAppType(appTypeDataList, ecmBusiScanInfoDTO, ecmBusiScanDTO, token,
                    ecmBusiDealVO, appTypeRuleDTOList);
            ecmBusiDealVOList.add(ecmBusiDealVO);
        }

        return ecmBusiDealVOList;
    }

    /**
     * 匹配业务
     */
    @Transactional(rollbackFor = Exception.class)
    public EcmBusiDealVO matchAppType(List<EcmBusiInfoRedisDTO> appTypeDataList,
                                      EcmBusiScanInfoDTO ecmBusiScanInfoDTO,
                                      EcmBusiBatchScanDTO ecmBusiScanDTO,
                                      AccountTokenExtendDTO token, EcmBusiDealVO ecmBusiDealVO,
                                      List<AppTypeRuleDTO> appTypeRuleDTOList) {
        //插件获取参数
        String scanAppCode = null;
        String scanDocCode = null;
        String scanBusiNo = null;
        //根据规则匹配业务类型及业务编号
        Map<String, String> appInfoMap = matchAppTypeAndBusiNo(appTypeRuleDTOList,
                ecmBusiScanInfoDTO.getBusiNo());
        if (MapUtil.isNotEmpty(appInfoMap)) {
            scanAppCode = appInfoMap.get("scanAppCode");
            scanBusiNo = appInfoMap.get("scanBusiNo");
            scanDocCode = appInfoMap.get("scanDocCode");
        }
        log.info("匹配参数：{}", appInfoMap);
        //页面选中参数
        String pageFlag = ecmBusiScanDTO.getPageFlag();
        String pageAppTypeId = ecmBusiScanDTO.getAppCode();
        String pageDocId = ecmBusiScanDTO.getDocId();
        Long pageBusiId = ecmBusiScanDTO.getBusiId();
        //获取所有页面业务类型的AppCode
        List<String> appTypeIds = appTypeDataList.stream().map(EcmBusiInfoRedisDTO::getAppCode)
                .collect(Collectors.toList());
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
        List<String> appCodeList = ecmAppDefs.stream().map(EcmAppDef::getAppCode)
                .collect(Collectors.toList());
        //匹配业务类型
        if (appCodeList.contains(scanAppCode)) {
            String finalScanAppCode = scanAppCode;
            Optional<EcmAppDef> appDef = ecmAppDefs.stream()
                    .filter(appType -> appType.getAppCode().equals(finalScanAppCode)).findFirst();
            if (appDef.isPresent()) {
                EcmAppDef ecmAppDef = appDef.get();
                Optional<EcmBusiInfoRedisDTO> first = appTypeDataList.stream()
                        .filter(f -> f.getAppCode().equals(ecmAppDef.getAppCode())).findFirst();
                if (first.isPresent()) {
                    //匹配业务
                    EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = first.get();
                    String existBusiNo = ecmBusiInfoRedisDTO.getBusiNo();
                    if (StrUtil.isNotBlank(scanBusiNo)) {
                        //扫描传入的业务编号匹配到页面业务编号
                        if (StrUtil.equals(existBusiNo, scanBusiNo)) {
                            ecmBusiDealVO.setBusiId(ecmBusiInfoRedisDTO.getBusiId());
                            String docId = matchDocList(scanDocCode,
                                    ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS(), pageDocId);
                            if (docId != null) {
                                EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectById(docId);
                                if (IcmsConstants.ZERO.equals(ecmBusiDoc.getDocMark())) {
                                    ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.THREE));
                                } else {
                                    ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FOUR));
                                }
                                ecmBusiDoc.setDocCode(ecmBusiDoc.getDocCode());
                                ecmBusiDealVO.setDocId(docId);
                            } else {
                                //没有匹配到资料且没有选中，否则放到未归类
                                ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                                ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                            }
                            ecmBusiDealVO.setBusiNo(existBusiNo);
                        } else {
                            //没匹配到判断是否选中业务类型或者资料类型
                            if (pageBusiId != null) {
                                ecmBusiDealVO.setBusiId(pageBusiId);
                                ecmBusiDealVO.setBusiNo(existBusiNo);
                                if (StrUtil.isNotBlank(pageDocId)) {
                                    Map<String, Object> docMap = getDocIdByDocCode(pageBusiId,
                                            pageDocId,token);
                                    if (CollectionUtil.isNotEmpty(docMap)) {
                                        ecmBusiDealVO.setDocId(docMap.get("docId").toString());
                                        ecmBusiDealVO.setType(ecmBusiScanDTO.getType());
                                        ecmBusiDealVO.setDocCode(docMap.get("docCode").toString());
                                    }
                                } else {
                                    ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                                    ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                                    ecmBusiDealVO.setDocCode(null);
                                }
                            } else {
                                //生成一笔业务
                                //校验业务是否存在
                                Map<String, String> map = checkBusiNo(ecmAppDef.getAppCode(),
                                        scanBusiNo, scanDocCode);
                                if (CollectionUtil.isNotEmpty(map)) {
                                    String existBusiId = map.get("busiId");
                                    //给权限
                                    EcmBusiInfoRedisDTO ecmBusiInfoExtend = busiCacheService.getEcmBusiInfoRedisDTO(token, Long.parseLong(existBusiId));
                                    ecmBusiInfoExtend.setPageFlag(token.getFlagId());
                                    operateCaptureService.updateUserBusiToRedis(ecmBusiInfoExtend, token, null);
                                    String existDocId = map.get("docId");
                                    String docMark = map.get("docMark");
                                    ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                                    ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                                    ecmBusiDealVO.setDocCode(null);
                                    //业务不存在新建业务
                                    if (StrUtil.isBlank(existBusiId)) {
                                        Long busiId = createBusiInfo(pageAppTypeId, pageFlag, token,
                                                scanBusiNo);
                                        ecmBusiDealVO.setBusiId(busiId);
                                    } else if (StrUtil.isBlank(existDocId)) {
                                        //业务存在，资料没匹配到
                                        ecmBusiDealVO.setBusiId(Long.valueOf(existBusiId));
                                    } else {
                                        //业务资料均匹配到
                                        ecmBusiDealVO.setBusiId(Long.valueOf(existBusiId));
                                        ecmBusiDealVO.setDocId(existDocId);
                                        ecmBusiDealVO.setDocCode(map.get("docCode"));
                                        if (StrUtil.isBlank(docMark)) {
                                            ecmBusiDealVO
                                                    .setType(Long.valueOf(IcmsConstants.THREE));
                                        } else {
                                            ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FOUR));
                                        }
                                    }
                                    ecmBusiDealVO.setBusiNo(scanBusiNo);
                                } else {
                                    if (StrUtil.isEmpty(scanDocCode)) {
                                        //属于未归类节点
                                        ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                                        ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                                    } else {
                                        ecmBusiDealVO.setDocId(scanDocCode);
                                        ecmBusiDealVO.setDocCode(scanDocCode);
                                        ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.THREE));
                                    }
                                    Long busiId = createBusiInfo(pageAppTypeId, pageFlag, token,
                                            scanBusiNo);
                                    ecmBusiDealVO.setBusiId(busiId);
                                    ecmBusiDealVO.setBusiNo(scanBusiNo);
                                }
                            }
                        }
                    }
                }
            }

        } else {
            //没有匹配到业务类型，则生成一笔临时业务到当前操作业务类型下
            if (pageBusiId != null && StrUtil.isBlank(pageDocId)) {
                //根据busiId获取busiNo
                EcmBusiInfoRedisDTO ecmBusiInfoSource = busiCacheService
                        .getEcmBusiInfoRedisDTO(token, pageBusiId);
                //页面选中业务节点
                ecmBusiDealVO.setBusiNo(ecmBusiInfoSource.getBusiNo());
                ecmBusiDealVO.setBusiId(pageBusiId);
                ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                ecmBusiDealVO.setDocCode(null);
            } else if (StrUtil.isNotBlank(pageDocId)) {
                //根据busiId获取busiNo
                LambdaQueryWrapper<EcmBusiInfo> infoWrapper = new LambdaQueryWrapper<>();
                infoWrapper.eq(pageBusiId != null, EcmBusiInfo::getBusiId, pageBusiId);
                EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(infoWrapper);
                //页面选中资料节点
                ecmBusiDealVO.setBusiNo(ecmBusiInfo.getBusiNo());
                ecmBusiDealVO.setBusiId(pageBusiId);
                ecmBusiDealVO.setDocId(pageDocId);
                ecmBusiDealVO.setType(ecmBusiScanDTO.getType());
                ecmBusiDealVO.setDocCode(pageDocId);
            } else {
                //新建一个业务类型空壳的情况
                Long busiId = null;
                String busiNo = null;
                //识别出的业务类型code和业务编号都为空
                if (StrUtil.isBlank(scanBusiNo)) {
                    log.info("当前线程名称：{},校验数据开始时间：{}", Thread.currentThread().getName(),
                            LocalDateTime.now());
                    EcmBusiInfo newEcmBusiInfo = checkCurrentAppType(pageAppTypeId, pageFlag);
                    if (newEcmBusiInfo == null) {
                        busiNo = IcmsConstants.AUTO_BUSI_PREFIX + pageFlag
                                + generateSixDigitRandomNumber();
                        busiId = createBusiInfo(pageAppTypeId, pageFlag, token, busiNo);
                        ecmBusiDealVO.setBusiId(busiId);
                        log.info("当前线程名称：{},执行完新增数据时间：{}", Thread.currentThread().getName(),
                                LocalDateTime.now());
                    } else {
                        busiId = newEcmBusiInfo.getBusiId();
                        busiNo = newEcmBusiInfo.getBusiNo();
                    }
                } else {
                    busiNo = scanBusiNo;
                    busiId = createBusiInfo(pageAppTypeId, pageFlag, token, scanBusiNo);
                }
                ecmBusiDealVO.setBusiId(busiId);
                ecmBusiDealVO.setBusiNo(busiNo);
                ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                ecmBusiDealVO.setDocCode(null);
            }
        }

        return ecmBusiDealVO;
    }

    /**
     * 根据资料类型规则匹配资料
     */
    private String matchDocType(String scanAppCode, String busiNo) {
        String scanDocCode = null;
        //资料类型规则匹配
        LambdaQueryWrapper<EcmAppDocright> rightWrapper = new LambdaQueryWrapper<>();
        rightWrapper.eq(EcmAppDocright::getAppCode, scanAppCode);
        rightWrapper.eq(EcmAppDocright::getRightNew, StateConstants.YES);
        EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectOne(rightWrapper);
        List<EcmAppDocRel> ecmAppDocRels = new ArrayList<>();
        List<EcmDocDefRelVer> ecmDocDefs = new ArrayList<>();
        if (ecmAppDocright != null) {
            //获取当前版本的资料
            //            LambdaQueryWrapper<EcmAppDocRelVer> relVerWrapper = new LambdaQueryWrapper<>();
            //            relVerWrapper.eq(EcmAppDocRelVer::getRightVer,ecmAppDocright.getRightVer());
            //            relVerWrapper.eq(EcmAppDocRelVer::getType,StateConstants.YES);
            //            relVerWrapper.eq(EcmAppDocRelVer::getAppCode,scanAppCode);
            //            ecmAppDocRelVers = ecmAppDocRelVerMapper.selectList(relVerWrapper);
            ecmAppDocRels = ecmAppDocRelMapper.selectList(new QueryWrapper<EcmAppDocRel>()
                    .eq("app_code", scanAppCode).eq("type", IcmsConstants.ONE));
            if (CollectionUtil.isNotEmpty(ecmAppDocRels)) {
                List<String> docCodeList = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode)
                        .collect(Collectors.toList());
                ecmDocDefs = ecmDocDefRelVerMapper.selectList(new QueryWrapper<EcmDocDefRelVer>()
                        .in("doc_code", docCodeList).eq("app_code", scanAppCode)
                        .eq("right_ver", ecmAppDocright.getRightVer()));
            }
        }

        if (CollectionUtil.isNotEmpty(ecmDocDefs)) {
            List<String> docCodeList = ecmDocDefs.stream().map(EcmDocDefRelVer::getDocCode)
                    .collect(Collectors.toList());
            List<EcmDocDef> ecmDocDefList = ecmDocDefMapper.selectBatchIds(docCodeList);
            List<EcmDocDefRuleDTO> ecmDocDefRuleDTOS = BeanUtil.copyToList(ecmDocDefList,
                    EcmDocDefRuleDTO.class);
            List<EcmDocDefRuleDTO> resultList = new ArrayList<>();
            for (EcmDocDefRuleDTO ecmDocDefRuleDTO : ecmDocDefRuleDTOS) {
                String docTypeSign = ecmDocDefRuleDTO.getDocTypeSign();
                Integer typeSignStart = ecmDocDefRuleDTO.getTypeSignStart();
                Integer typeSignEnd = ecmDocDefRuleDTO.getTypeSignEnd();
                if (StrUtil.isNotBlank(docTypeSign) && typeSignStart != null
                        && typeSignEnd != null) {
                    //截取业务编号
                    //判断输入截取的起始位置是否超出字符串
                    boolean typeFlag = splitRule(typeSignStart, typeSignEnd, busiNo);
                    if (typeFlag) {
                        typeSignEnd = busiNo.length();
                    }
                    //截取后的业务编号
                    String subStrDocCode = busiNo.substring(typeSignStart - 1, typeSignEnd);
                    ecmDocDefRuleDTO.setSubStrDocCodeSign(subStrDocCode);
                    ecmDocDefRuleDTO.setSubStrDocCodeLength(subStrDocCode.length());
                    resultList.add(ecmDocDefRuleDTO);
                }
            }
            //过滤匹配规则的业务类型
            List<EcmDocDefRuleDTO> matchList = resultList.stream()
                    .filter(f -> f.getDocTypeSign().equals(f.getSubStrDocCodeSign()))
                    .collect(Collectors.toList());
            if (CollectionUtil.isEmpty(matchList)) {
                return null;
            }
            matchList.sort(
                    Comparator.comparing(EcmDocDefRuleDTO::getSubStrDocCodeLength).reversed());
            EcmDocDefRuleDTO ecmDocDefRuleDTO = matchList.get(0);
            scanDocCode = ecmDocDefRuleDTO.getDocCode();
        }
        return scanDocCode;
    }

    /**
     * 根据规则匹配业务类型及业务编号
     */
    private Map<String, String> matchAppTypeAndBusiNo(List<AppTypeRuleDTO> appTypeRuleDTOList,
                                                      String busiNo) {
        String scanAppCode = null;
        String scanDocCode = null;
        String scanBusiNo = null;
        Map<String, String> map = new HashMap<>();
        if (StrUtil.isBlank(busiNo) || CollectionUtil.isEmpty(appTypeRuleDTOList)) {
            return null;
        }
        List<AppTypeRuleDTO> resultList = new ArrayList<>();
        //业务类型规则匹配
        for (AppTypeRuleDTO appTypeRuleDTO : appTypeRuleDTOList) {
            String appTypeSign = appTypeRuleDTO.getAppTypeSign();
            Integer typeSignStart = appTypeRuleDTO.getTypeSignStart();
            Integer typeSignEnd = appTypeRuleDTO.getTypeSignEnd();
            Integer busiNoStart = appTypeRuleDTO.getBusiNoStart();
            Integer busiNoEnd = appTypeRuleDTO.getBusiNoEnd();
            if (StrUtil.isNotBlank(appTypeSign) && typeSignStart != null && typeSignEnd != null) {
                //截取业务类型
                //判断输入截取的起始位置是否超出字符串
                boolean typeflag = splitRule(typeSignStart, typeSignEnd, busiNo);
                if (typeflag) {
                    typeSignEnd = busiNo.length();
                }
                String subStrAppTypeSign = busiNo.substring(typeSignStart - 1, typeSignEnd);
                //截取业务编号
                //判断输入截取的起始位置是否超出字符串
                boolean busiNoflag = splitRule(busiNoStart, busiNoEnd, busiNo);
                if (busiNoflag) {
                    busiNoEnd = busiNo.length();
                }
                String subStrBusiNo = busiNo.substring(busiNoStart - 1, busiNoEnd);
                appTypeRuleDTO.setSubStrAppTypeSign(subStrAppTypeSign);
                appTypeRuleDTO.setSubStrBusiNo(subStrBusiNo);
                appTypeRuleDTO.setSubStrAppCodeLength(subStrAppTypeSign.length());
                resultList.add(appTypeRuleDTO);
            }
        }

        //过滤匹配规则的业务类型
        List<AppTypeRuleDTO> matchList = resultList.stream()
                .filter(f -> f.getAppTypeSign().equals(f.getSubStrAppTypeSign()))
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(matchList)) {
            return null;
        }
        matchList.sort(Comparator.comparing(AppTypeRuleDTO::getSubStrAppCodeLength).reversed());
        //获取匹配度最高（匹配位数最多的数据）
        AppTypeRuleDTO appTypeRuleDTO = matchList.get(0);
        scanAppCode = appTypeRuleDTO.getAppCode();
        scanBusiNo = appTypeRuleDTO.getSubStrBusiNo();
        //匹配资料类型
        scanDocCode = matchDocType(scanAppCode, busiNo);
        map.put("scanAppCode", scanAppCode);
        map.put("scanBusiNo", scanBusiNo);
        map.put("scanDocCode", scanDocCode);
        return map;
    }

    /**
     * 截取规则（开始位置大于条码长度，不进行匹配，结束位置大于条码长度且不超过20位，结束位置取条码最大长度）
     */
    private boolean splitRule(Integer start, Integer end, String busiNo) {
        boolean flag = false;
        //截取业务编号
        //判断输入截取的起始位置是否超出字符串
        if (start >= busiNo.length()) {
            flag = false;
        }
        //结束位置超过字符串最大长度，则取最大长度(最多截取20位)
        if (end > busiNo.length() && busiNo.length() <= 20) {
            flag = true;
        }
        return flag;
    }

    /**
     * 校验当前业务类型下是否已经存在自动生成的业务
     */
    private EcmBusiInfo checkCurrentAppType(String pageAppTypeId, String pageFlag) {
        EcmBusiInfo ecmBusiInfo = null;
        LambdaQueryWrapper<EcmBusiInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmBusiInfo::getAppCode, pageAppTypeId);
        wrapper.like(EcmBusiInfo::getBusiNo, IcmsConstants.AUTO_BUSI_PREFIX + pageFlag);
        wrapper.eq(EcmBusiInfo::getIsDeleted, IcmsConstants.ZERO);
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(ecmBusiInfos)) {
            ecmBusiInfo = ecmBusiInfos.get(0);
        }
        return ecmBusiInfo;
    }

    /**
     * 校验是否已经添加过该业务
     */
    private Map<String, String> checkBusiNo(String appCode, String scanBusiNo, String scanDocCode) {
        Map<String, String> map = new HashMap<>();
        LambdaQueryWrapper<EcmBusiInfo> busiWrapper = new LambdaQueryWrapper<>();
        busiWrapper.eq(EcmBusiInfo::getAppCode, appCode);
        busiWrapper.eq(EcmBusiInfo::getBusiNo, scanBusiNo);
        busiWrapper.eq(EcmBusiInfo::getIsDeleted, StateConstants.NO);
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(busiWrapper);
        if (ecmBusiInfo != null) {
            map.put("busiId", ecmBusiInfo.getBusiId().toString());
            //判断业务树类型
            if (ecmBusiInfo.getTreeType().equals(IcmsConstants.STATIC_TREE)) {
                //                LambdaQueryWrapper<EcmAppDocRelVer> staticTreeWrapper = new LambdaQueryWrapper<>();
                //                staticTreeWrapper.eq(EcmAppDocRelVer::getAppCode,appCode);
                //                staticTreeWrapper.eq(EcmAppDocRelVer::getRightVer,ecmBusiInfo.getRightVer());
                //                staticTreeWrapper.eq(EcmAppDocRelVer::getDocCode,scanDocCode);
                //                EcmAppDocRelVer ecmAppDocRelVer = ecmAppDocRelVerMapper.selectOne(staticTreeWrapper);
                EcmDocDefRelVer ecmDocDefRelVer = ecmDocDefRelVerMapper
                        .selectOne(new QueryWrapper<EcmDocDefRelVer>().eq("app_code", appCode)
                                .eq("doc_code", scanDocCode)
                                .eq("right_ver", ecmBusiInfo.getRightVer()));
                if (ecmDocDefRelVer != null) {
                    //静态树docCode = docId
                    map.put("docId", scanDocCode);
                    map.put("docCode", scanDocCode);
                }
            } else {
                LambdaQueryWrapper<EcmBusiDoc> dynamicTreeWrapper = new LambdaQueryWrapper<>();
                dynamicTreeWrapper.eq(EcmBusiDoc::getBusiId, ecmBusiInfo.getBusiId());
                dynamicTreeWrapper.eq(EcmBusiDoc::getDocCode, scanDocCode);
                EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectOne(dynamicTreeWrapper);
                if (ecmBusiDoc != null) {
                    //静态树docCode != docId
                    map.put("docId", ecmBusiDoc.getDocId().toString());
                    map.put("docCode", scanDocCode);
                    map.put("docMark", ecmBusiDoc.getDocMark().toString());
                }
            }
        }
        return map;
    }

    /**
     * 匹配资料类型
     */
    private String matchDocList(String scanDocCode, List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS,
                                String pageDocId) {
        String docId = pageDocId;
        if (CollectionUtil.isEmpty(ecmBusiDocRedisDTOS)) {
            return docId;
        }
        Optional<EcmBusiDocRedisDTO> docInfo = ecmBusiDocRedisDTOS.stream()
                .filter(f -> f.getDocCode().equals(scanDocCode)).findFirst();
        if (docInfo.isPresent()) {
            EcmBusiDocRedisDTO ecmBusiDocRedisDTO = docInfo.get();
            docId = ecmBusiDocRedisDTO.getDocId().toString();
        }
        return docId;
    }

    /**
     * 匹配扫描传入数据
     */
    @Transactional(rollbackFor = Exception.class)
    public List<EcmBusiDealVO> matchScanDataBak(List<EcmBusiInfoRedisDTO> appTypeDataList,
                                                EcmBusiBatchScanDTO ecmBusiScanDTO,
                                                AccountTokenExtendDTO token, Integer modelType) {
        List<EcmBusiDealVO> ecmBusiDealVOList = new ArrayList<>();
        List<EcmBusiScanInfoDTO> busiScanInfoDTOList = ecmBusiScanDTO.getBusiScanInfoDTOList();

        if (CollectionUtil.isEmpty(busiScanInfoDTOList)) {
            AssertUtils.isTrue(CollectionUtil.isEmpty(busiScanInfoDTOList), "未传入扫描参数");
        }
        List<String> appTypeIds = appTypeDataList.stream().map(EcmBusiInfoRedisDTO::getAppCode)
                .collect(Collectors.toList());
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
        List<String> appCodeList = ecmAppDefs.stream().map(EcmAppDef::getAppCode)
                .collect(Collectors.toList());
        for (EcmBusiScanInfoDTO ecmBusiScanInfoDTO : busiScanInfoDTOList) {
            EcmBusiDealVO ecmBusiDealVO = new EcmBusiDealVO();
            ecmBusiDealVO.setFileName(ecmBusiScanInfoDTO.getFilename());
            //匹配到业务类型
            if (CollectionUtil.isNotEmpty(appCodeList)
                    && appCodeList.contains(ecmBusiScanInfoDTO.getAppCode())) {
                //获取业务对应的资料类型
                List<EcmAppDocRelInfoDTO> ecmAppDocRelInfoDTOS = matchAppTypeByAppCode(
                        ecmBusiScanInfoDTO.getAppCode());
                //匹配到业务类型
                if (CollectionUtil.isNotEmpty(ecmAppDocRelInfoDTOS)) {
                    String appCode = ecmAppDocRelInfoDTOS.get(0).getAppCode();
                    List<String> docCodeList = ecmAppDocRelInfoDTOS.stream()
                            .map(EcmAppDocRelInfoDTO::getDocCode).collect(Collectors.toList());
                    //匹配资料
                    if (CollectionUtil.isNotEmpty(docCodeList)
                            && docCodeList.contains(ecmBusiScanInfoDTO.getDocCode())) {
                        //根据业务类型和资料类型获取业务
                        Optional<EcmAppDocRelInfoDTO> docType = ecmAppDocRelInfoDTOS.stream()
                                .filter(f -> f.getDocCode().equals(ecmBusiScanInfoDTO.getDocCode()))
                                .findFirst();
                        if (docType.isPresent()) {
                            EcmAppDocRelInfoDTO docTypeInfo = docType.get();
                            //匹配到资料，则返回业务id(busiId)和资料类型id(docId)
                            List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper
                                    .selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                                            .eq(EcmBusiInfo::getAppCode, appCode)
                                            .eq(EcmBusiInfo::getBusiNo,
                                                    ecmBusiScanInfoDTO.getBusiNo())
                                            .eq(EcmBusiInfo::getIsDeleted, IcmsConstants.ZERO));
                            //没匹配到对应业务，生成临时业务，资料放到未归类
                            if (CollectionUtil.isNotEmpty(ecmBusiInfos)) {
                                //匹配到
                                Long busiId = createBusiInfo(appCode, ecmBusiScanDTO.getPageFlag(),
                                        token, ecmBusiScanInfoDTO.getBusiNo());
                                ecmBusiDealVO.setBusiId(busiId);
                                ecmBusiDealVO.setDocId(docTypeInfo.getId().toString());
                                ecmBusiDealVOList.add(ecmBusiDealVO);
                                continue;
                            }
                        }
                    } else {
                        //匹配到业务类型，没有匹配到资料，则在该业务类型下新建一个临时业务
                        Long busiId = createBusiInfo(appCode, ecmBusiScanDTO.getPageFlag(), token,
                                null);
                        ecmBusiDealVO.setBusiId(busiId);
                        ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                        ecmBusiDealVOList.add(ecmBusiDealVO);
                    }
                }
            } else {
                //只有批扫类型才自动生成业务
                if (IcmsConstants.TWO.equals(modelType)) {
                    String appCode = ecmAppDefs.get(0).getAppCode();
                    //匹配到业务类型，没有匹配到资料，则在该业务类型下新建一个临时业务
                    Long busiId = createBusiInfo(appCode, ecmBusiScanDTO.getPageFlag(), token,
                            null);
                    ecmBusiDealVO.setBusiId(busiId);
                    ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                    ecmBusiDealVOList.add(ecmBusiDealVO);
                }
            }
        }
        return ecmBusiDealVOList;
    }

    /**
     * 匹配业务类型数据
     */
    private List<EcmAppDocRelInfoDTO> matchAppTypeByAppCode(String appCode) {
        List<EcmAppDocRelInfoDTO> ecmAppDocRelList = new ArrayList<>();
        //根据业务类型code获取业务类型id
        LambdaQueryWrapper<EcmAppDef> ecmAppDefWrapper = new LambdaQueryWrapper<>();
        ecmAppDefWrapper.eq(EcmAppDef::getAppCode, appCode);
        //业务code唯一
        EcmAppDef ecmAppDefs = ecmAppDefMapper.selectOne(ecmAppDefWrapper);
        if (ecmAppDefs != null) {
            String appCode1 = ecmAppDefs.getAppCode();
            //根据业务类型id获取业务类型对应资料
            Long busiId = null;
            ecmAppDocRelList = ecmAppDocRelMapper.selectListByAppTypeId(appCode1, busiId);

        }
        return ecmAppDocRelList;
    }

    /**
     * 保存数据(包括匹配到或者未匹配到两种)
     */
    @Transactional
    public Long createBusiInfo(String appCode, String pageFlag, AccountTokenExtendDTO token,
                               String busiNo) {
        String userId = token.getUsername();
        EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
        ecmBusiInfoExtend.setPageFlag(pageFlag);
        ecmBusiInfoExtend.setCreateUser(userId);
        ecmBusiInfoExtend.setAppCode(appCode);
        ecmBusiInfoExtend.setBusiNo(busiNo);
        ecmBusiInfoExtend.setCreateUserName(token.getName());
        ecmBusiInfoExtend.setStatus(BusiInfoConstants.BUSI_STATUS_ZERO);
        //保存业务及业务属性
        saveBusiAndAttr(appCode, ecmBusiInfoExtend, busiNo, token);
        //        添加业务类型关联的资料类型静态树
        //        addDocTypeTree(ecmBusiInfoExtend);
        //redis添加采集页面业务相关信息
        busiCacheService.addBusiExtendInfoToRedis(ecmBusiInfoExtend, token, null);
        //redis更新用户-业务相关信息
        operateCaptureService.updateUserBusiToRedis(ecmBusiInfoExtend, token, null);
        //添加操作记录表
        busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(), IcmsConstants.ADD_BUSI,
                token, "扫描业务");
        //添加到es
        operateFullQueryService.addEsBusiInfo(ecmBusiInfoExtend, token.getId());
        return ecmBusiInfoExtend.getBusiId();
    }

    /**
     * 保存业务和业务属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBusiAndAttr(String appCode, EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                String busiNo, AccountTokenExtendDTO token) {
        //1、保存业务信息
        //根据业务类型获取业务属性
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
        AssertUtils.isTrue(CollectionUtil.isEmpty(ecmAppAttrs), "业务类型未关联属性");
        List<EcmAppAttrDTO> ecmAppAttrDTOS = BeanUtil.copyToList(ecmAppAttrs, EcmAppAttrDTO.class);
        ecmAppAttrDTOS.stream().forEach(attr -> {
            if (IcmsConstants.ONE.equals(attr.getIsKey())) {
                attr.setAppAttrValue(busiNo);
            }
        });
        ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);

        //获取当前业务类型所对应的权限版本
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper.selectList(
                new QueryWrapper<EcmAppDocright>().eq("app_code", ecmBusiInfoExtend.getAppCode())
                        .eq("right_new", StateConstants.COMMON_ONE_STR));
        if (CollectionUtils.isEmpty(appDocrights)) {
            AssertUtils.isTrue(true, "无法新增业务，该业务类型未配置业务资料权限版本");
        }
        //创建人
        ecmBusiInfoExtend.setCreateUser(ecmBusiInfoExtend.getCreateUser());
        ecmBusiInfoExtend.setCreateUserName(ecmBusiInfoExtend.getCreateUserName());
        //静态树
        ecmBusiInfoExtend.setTreeType(StateConstants.ZERO);
        //资料权限版本
        ecmBusiInfoExtend.setRightVer(appDocrights.get(0).getRightVer());
        EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
        BeanUtils.copyProperties(ecmBusiInfoExtend, ecmBusiInfo);
        ecmBusiInfo.setBusiNo(busiNo);
        //添加机构号
        if (token.isOut()) {
            ecmBusiInfo.setOrgCode(token.getOrgCode());
            ecmBusiInfo.setOrgName(token.getOrgName());
        } else {
            addInstNo(ecmBusiInfo);
        }
        //        commonService.saveBusiInfo(ecmBusiInfo);
        ecmBusiInfoMapper.insert(ecmBusiInfo);
        ecmBusiInfoExtend.setBusiId(ecmBusiInfo.getBusiId());
        ecmBusiInfoExtend.setCreateTime(ecmBusiInfo.getCreateTime());
        ecmBusiInfoExtend.setOrgCode(ecmBusiInfo.getOrgCode());
        List<EcmAppAttrDTO> appAttrs = ecmBusiInfoExtend.getAttrList();
        for (EcmAppAttrDTO extend : appAttrs) {
            if (!ObjectUtils.isEmpty(extend.getAppAttrValue())) {
                EcmBusiMetadata ecmBusiMetadata = new EcmBusiMetadata();
                ecmBusiMetadata.setBusiId(ecmBusiInfoExtend.getBusiId());
                ecmBusiMetadata.setAppAttrId(extend.getAppAttrId());
                ecmBusiMetadata.setAppAttrVal(extend.getAppAttrValue());
                //todo 批量插入
                ecmBusiMetadataMapper.insert(ecmBusiMetadata);
            }
        }
    }

    private void addInstNo(EcmBusiInfo ecmBusiInfo) {
        //根据用户id获取机构id
        List<String> userIds = new ArrayList<>();
        Long instId = null;
        if (!ObjectUtils.isEmpty(ecmBusiInfo.getCreateUser())) {
            userIds.add(ecmBusiInfo.getCreateUser());
        }
        Map<String, List<SysUserDTO>> groupedByUserId = modelPermissionsService
                .getUserListByUserIds(userIds);
        //添加创建人名称
        if (!ObjectUtils.isEmpty(ecmBusiInfo.getCreateUser())) {
            if (!org.springframework.util.CollectionUtils
                    .isEmpty(groupedByUserId.get(ecmBusiInfo.getCreateUser()))) {
                instId = groupedByUserId.get(ecmBusiInfo.getCreateUser()).get(0).getInstId();
            }
        }
        //根据机构id获取机构号
        SysInstDTO sysInstDTO = instApi.getInstByInstId(instId).getData();
        AssertUtils.isNull(sysInstDTO, "参数错误");
        ecmBusiInfo.setOrgCode(sysInstDTO.getInstNo());
        ecmBusiInfo.setOrgName(sysInstDTO.getName());
    }

    /**
     * 逻辑描述：选中业务节点，则匹配扫描传入的
     */
    public List<EcmBusiDealVO> singleScan(EcmBusiSingleScanDTO ecmBusiSingleScanDTO,
                                          AccountTokenExtendDTO token) {
        List<EcmBusiScanInfoDTO> busiScanInfoDTOList = ecmBusiSingleScanDTO
                .getBusiScanInfoDTOList();
        if (CollectionUtil.isEmpty(busiScanInfoDTOList)) {
            return Collections.emptyList();
        }
        List<EcmBusiDealVO> ecmBusiDealListVO = new ArrayList<>();
        AssertUtils.isTrue((ecmBusiSingleScanDTO.getBusiId() == null
                && ecmBusiSingleScanDTO.getDocId() == null), "未选择节点");
        String appCode = ecmBusiSingleScanDTO.getAppCode();
        Long busiId = ecmBusiSingleScanDTO.getBusiId();
        String docCode = ecmBusiSingleScanDTO.getDocCode();
        Long type = ecmBusiSingleScanDTO.getType();
        EcmBusiInfoRedisDTO ecmBusiInfo = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        //文件新增：0待提交状态、4处理失败状态、5已完结状态可进行新增
        if(!token.isOut()){
            Integer status = ecmBusiInfo.getStatus();
            if(!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status)&&!BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)&&
                    !BusiInfoConstants.BUSI_STATUS_FIVE.equals(status)){
                AssertUtils.isTrue(true,"当前业务状态暂无新增文件权限");
            }
        }
        if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfo.getTreeType())) {
            //根据业务id获取关联的资料类型
            List<EcmAppDocRelInfoDTO> ecmAppDocRelList = ecmAppDocRelMapper
                    .selectListByAppTypeId(appCode, busiId);
            List<String> docCodeList = null;
            if (CollectionUtil.isNotEmpty(ecmAppDocRelList)) {
                docCodeList = ecmAppDocRelList.stream()
                        .map(EcmAppDocRelInfoDTO::getDocCode).collect(Collectors.toList());
            }

            for (EcmBusiScanInfoDTO busiScanInfoDTO : busiScanInfoDTOList) {
                EcmBusiDealVO ecmBusiDealVO = new EcmBusiDealVO();
                ecmBusiDealVO.setFileName(busiScanInfoDTO.getFilename());
                //选中业务节点
                if (ecmBusiSingleScanDTO.getBusiId() != null
                        && StrUtil.isBlank(ecmBusiSingleScanDTO.getDocId())) {
                    if (docCodeList != null && docCodeList.contains(busiScanInfoDTO.getDocCode())) {
                        Optional<EcmAppDocRelInfoDTO> docType = ecmAppDocRelList.stream()
                                .filter(f -> f.getDocCode()
                                        .equals(busiScanInfoDTO.getDocCode()))
                                .findFirst();
                        if (docType.isPresent()) {
                            EcmAppDocRelInfoDTO docTypeInfo = docType.get();
                            //选中业务节点，匹配业务节点对应的资料节点，匹配上则放到对应资料节点，否则放到未归类节点
                            matchDocDataByDocCode(busiScanInfoDTO, appCode, ecmBusiDealVO,
                                    busiId, docTypeInfo, ecmBusiInfo.getBusiNo());
                            ecmBusiDealListVO.add(ecmBusiDealVO);
                        }
                    } else {
                        //未匹配到放到未归类
                        ecmBusiDealVO.setBusiNo(ecmBusiInfo.getBusiNo());
                        ecmBusiDealVO.setBusiId(busiId);
                        ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                        ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                        ecmBusiDealVO.setDocCode(null);
                        ecmBusiDealListVO.add(ecmBusiDealVO);
                    }
                    //选中资料叶子节点
                } else {
                    //直接放到资料节点下
                    ecmBusiDealVO.setBusiNo(ecmBusiInfo.getBusiNo());
                    ecmBusiDealVO.setBusiId(busiId);
                    ecmBusiDealVO.setDocId(ecmBusiSingleScanDTO.getDocId());
                    ecmBusiDealVO.setType(type);
                    ecmBusiDealVO.setDocCode(docCode);
                    ecmBusiDealListVO.add(ecmBusiDealVO);
                }

            }
        } else {
            //当业务ID无关联的资料类型说明为静态树
            for (EcmBusiScanInfoDTO busiScanInfoDTO : busiScanInfoDTOList) {
                EcmBusiDealVO ecmBusiDealVO = new EcmBusiDealVO();
                ecmBusiDealVO.setFileName(busiScanInfoDTO.getFilename());
                if (ecmBusiSingleScanDTO.getBusiId() != null
                        && StrUtil.isBlank(ecmBusiSingleScanDTO.getDocId())) {
                    //未匹配到放到未归类
                    ecmBusiDealVO.setBusiNo(ecmBusiInfo.getBusiNo());
                    ecmBusiDealVO.setBusiId(busiId);
                    ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                    ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
                    ecmBusiDealVO.setDocCode(null);
                    ecmBusiDealListVO.add(ecmBusiDealVO);
                } else {
                    //直接放到资料节点下
                    ecmBusiDealVO.setBusiNo(ecmBusiInfo.getBusiNo());
                    ecmBusiDealVO.setBusiId(busiId);
                    ecmBusiDealVO.setDocId(ecmBusiSingleScanDTO.getDocId());
                    ecmBusiDealVO.setType(type);
                    ecmBusiDealVO.setDocCode(docCode);
                    ecmBusiDealListVO.add(ecmBusiDealVO);
                }
            }
        }

        return ecmBusiDealListVO;
    }

    /**
     * 根据业务id或者资料id匹配
     */
    private void matchDocDataByDocCode(EcmBusiScanInfoDTO busiScanInfoDTO, String appCode,
                                       EcmBusiDealVO ecmBusiDealVO, Long busiId,
                                       EcmAppDocRelInfoDTO docTypeInfo, String busiNo) {
        //匹配到资料，则返回业务id(busiId)和资料类型id(docId)
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(
                new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getAppCode, appCode)
                        .eq(EcmBusiInfo::getBusiNo, busiScanInfoDTO.getBusiNo())
                        .eq(EcmBusiInfo::getIsDeleted, IcmsConstants.ZERO));
        //没匹配到对应业务资料放到未归类
        if (CollectionUtil.isNotEmpty(ecmBusiInfos)) {
            Long docId = docTypeInfo.getDocId();
            EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectById(docId);
            //匹配到
            ecmBusiDealVO.setBusiNo(busiNo);
            ecmBusiDealVO.setBusiId(busiId);
            ecmBusiDealVO.setDocId(docId.toString());
            ecmBusiDealVO.setDocCode(ecmBusiDoc.getDocCode());
            ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.THREE));
        } else {
            //未匹配到
            ecmBusiDealVO.setBusiNo(busiNo);
            ecmBusiDealVO.setBusiId(busiId);
            ecmBusiDealVO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmBusiDealVO.setType(Long.valueOf(IcmsConstants.FIVE));
            ecmBusiDealVO.setDocCode(null);
        }
    }

    /**
     * 生成6位随机数
     */
    public static String generateSixDigitRandomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // Generate 6 random digits
        for (int i = 0; i < 6; i++) {
            // Generate a random digit between 0 and 9
            int randomDigit = secureRandom.nextInt(10);
            sb.append(randomDigit);
        }

        return sb.toString();
    }

    /**
     * 获取docCode
     */
    public Map<String, Object> getDocIdByDocCode(Long busiId, String docCode,AccountTokenExtendDTO token) {
        Map<String, Object> map = new HashMap<>();
        EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                .getEcmBusiInfoRedisDTO(token, busiId);
        BeanUtils.copyProperties(ecmBusiInfoRedisDTO,ecmBusiInfo);
        if (ecmBusiInfo == null) {
            return null;
        }
        //判断业务树类型
        //静态树
        if (ecmBusiInfo.getTreeType().equals(IcmsConstants.STATIC_TREE)) {
            LambdaQueryWrapper<EcmDocDef> staticTreeWrapper = new LambdaQueryWrapper<>();
            staticTreeWrapper.eq(EcmDocDef::getDocCode, docCode);
            EcmDocDef ecmAppDocRelVer = ecmDocDefMapper.selectOne(staticTreeWrapper);
            map.put("docCode", ecmAppDocRelVer.getDocCode());
            map.put("docId", ecmAppDocRelVer.getDocCode());
            //动态树
        } else {
            LambdaQueryWrapper<EcmBusiDoc> dynamicWrapper = new LambdaQueryWrapper<>();
            dynamicWrapper.eq(EcmBusiDoc::getDocCode, docCode);
            EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectOne(dynamicWrapper);
            map.put("docCode", ecmBusiDoc.getDocCode());
            map.put("docId", ecmBusiDoc.getDocId());
        }
        return map;
    }

}
