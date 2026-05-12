package com.sunyard.ecm.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmBusiAttrDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.dto.EditBusiAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmFileBatchOperationMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmFileBatchOperation;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.FileInfoRedisEntityVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MergFileVO;
import com.sunyard.ecm.vo.MultiplexFileVO;
import com.sunyard.ecm.vo.RotateFileVO;
import com.sunyard.ecm.vo.SplitFileVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sunyard.ecm.config.ThreadPoolConfig.BUSI_LOG_TASK_EXECUTOR;

/**
 * @author zyl
 * @Description 复用归类异步存储实现类
 * @since 2023/11/22 9:53
 */
@Slf4j
@Service
public class AsyncBusiLogService {
    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private SysBusiLogMapper ecmBusiLogMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private BusiCacheService busiCacheService;

    /**
     * 异步保存归类日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveClassifyLog(EcmsCaptureVO vo, AccountTokenExtendDTO token, List<String> docNames) {
        log.info("###### 归类:{}, 缓存文件Id:{} ######", Thread.currentThread().getName(), vo.getBusiId());
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        //获取业务信息实体
        EcmBusiInfo ecmBusiInfoNew = new EcmBusiInfo();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                .getEcmBusiInfoRedisDTO(token, vo.getNewBusiId());
        BeanUtils.copyProperties(ecmBusiInfoRedisDTO,ecmBusiInfoNew);
        EcmBusiInfo ecmBusiInfoOld = new EcmBusiInfo();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService
                .getEcmBusiInfoRedisDTO(token, vo.getOldBusiId());
        BeanUtils.copyProperties(ecmBusiInfoRedisDTO1,ecmBusiInfoOld);
        //获取业务类型实体
        LambdaQueryWrapper<EcmAppDef> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(EcmAppDef::getAppCode, ecmBusiInfoNew.getAppCode());
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper1);
        //设置日志值
        ecmBusiLog.setBusiNo(ecmBusiInfoNew.getBusiNo());
        ecmBusiLog.setAppName(ecmAppDef.getAppName());
        ecmBusiLog.setAppCode(ecmAppDef.getAppCode());
        ecmBusiLog.setOrgCode(ecmBusiInfoNew.getOrgCode());
        if (token.isOut()) {
            ecmBusiLog.setOperatorId(token.getUsername());
            ecmBusiLog.setOperator(token.getName());
        } else {
            Result<SysUserDTO> userByUserId = userApi.getUserByUserId(token.getId());
            SysUserDTO user = userByUserId.getData();
            ecmBusiLog.setOperatorId(user.getUserId().toString());
            ecmBusiLog.setOperator(user.getName());
        }

        List<String> collect = docNames.stream().distinct().collect(Collectors.toList());
        String docName = String.join(",", collect);
        ecmBusiLog.setOperateContent(
                "归类文件: 从" +
                        ecmBusiInfoOld.getBusiNo() + "业务的" + docName + "节点，归类至" +
                        ecmBusiInfoNew.getBusiNo() + "业务的" + vo.getDocNode().getName() + "节点");
        ecmBusiLog.setOperatorType(BusiLogConstants.OPERATION_TYPE_TWO);
        ecmBusiLogMapper.insert(ecmBusiLog);
        List<Long> fileIds = vo.getFileIds();
        List<EcmFileBatchOperation> ecmFileBatchOperationList = new ArrayList<>();
        for (Long fileId : fileIds) {
            EcmFileBatchOperation batchOperation = new EcmFileBatchOperation();
            batchOperation.setEcmBusiLogId(ecmBusiLog.getId());
            batchOperation.setFileId(fileId);
            batchOperation.setId(snowflakeUtils.nextId());
            ecmFileBatchOperationList.add(batchOperation);
        }


        insertEcmFileBatchOperations(ecmFileBatchOperationList);
        //fileBatchOperationMapper.insertList(ecmFileBatchOperationList);
    }

    /**
     * 异步保存复用文件日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveRepeatLog(FileInfoRedisEntityVO ecmBusiDocExtend, AccountTokenExtendDTO token) {
        log.info("###### 文件重新缓存任务线程信息:{}, 缓存文件Id:{} ######", Thread.currentThread().getName(), ecmBusiDocExtend.getSourceBusiId());
        //获取目标业务主索引，资料节点，文件ID
        //A业务的 1，2节点 复用至 B业务
        List<MultiplexFileVO> multiplexFileVO = ecmBusiDocExtend.getMultiplexFileVO();
        List<String> repeatTarget = new ArrayList<>();
        List<EcmFileInfo> ecmFileInfos = new ArrayList<>();
        for (MultiplexFileVO fileVO : multiplexFileVO) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, fileVO.getTargetBusiId());
            String busiNo = ecmBusiInfoRedisDTO.getBusiNo();
            List<String> targetDocCodeList = fileVO.getTargetDocTypeId();
            List<String> docNames = new ArrayList<>();
            targetDocCodeList.forEach(d -> {
                if (IcmsConstants.UNCLASSIFIED_ID.equals(d)) {
                    docNames.add(IcmsConstants.UNCLASSIFIED);
                } else {
                    EcmDocDef ecmDocDef = ecmDocDefMapper.selectOne(new LambdaQueryWrapper<EcmDocDef>().eq(EcmDocDef::getDocCode, d));
                    docNames.add(ecmDocDef.getDocName());
                }
                LambdaQueryWrapper<EcmFileInfo> fileWrapper = new LambdaQueryWrapper<>();
                fileWrapper.eq(EcmFileInfo::getBusiId, fileVO.getTargetBusiId())
                        .eq(EcmFileInfo::getDocCode, d);
                ecmFileInfos.addAll(ecmFileInfoMapper.selectList(fileWrapper));
            });
            List<String> collect = docNames.stream().distinct().collect(Collectors.toList());
            String docNodeNames = String.join(",", collect);
            String repeatTargetInfo = busiNo + "业务的" + docNodeNames + "节点";
            repeatTarget.add(repeatTargetInfo);
        }
        Long sourceBusiId = ecmBusiDocExtend.getSourceBusiId();
        EcmBusiInfoRedisDTO ecmBusiInfoSource = busiCacheService
                .getEcmBusiInfoRedisDTO(token, sourceBusiId);
        String targetInfo = String.join(",", repeatTarget);
        String repeatContent = "复用文件：从" + targetInfo + "复用至" + ecmBusiInfoSource.getBusiNo() + "业务";
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        //获取业务类型实体
        LambdaQueryWrapper<EcmAppDef> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(EcmAppDef::getAppCode, ecmBusiInfoSource.getAppCode());
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper1);
        ecmBusiLog.setBusiNo(ecmBusiInfoSource.getBusiNo());
        ecmBusiLog.setAppName(ecmAppDef.getAppName());
        ecmBusiLog.setAppCode(ecmAppDef.getAppCode());
        ecmBusiLog.setOrgCode(ecmBusiInfoSource.getOrgCode());
        ecmBusiLog.setOperatorId(token.getUsername());
        ecmBusiLog.setOperator(token.getName());
        ecmBusiLog.setOperateContent(repeatContent);
        ecmBusiLog.setOperatorType(BusiLogConstants.OPERATION_TYPE_FOUR);
        ecmBusiLogMapper.insert(ecmBusiLog);
        List<EcmFileBatchOperation> ecmFileBatchOperationList = new ArrayList<>();
        ecmFileInfos.forEach(e -> {
            EcmFileBatchOperation batchOperation = new EcmFileBatchOperation();
            batchOperation.setFileId(e.getFileId());
            batchOperation.setEcmBusiLogId(ecmBusiLog.getId());
            batchOperation.setId(snowflakeUtils.nextId());
            ecmFileBatchOperationList.add(batchOperation);
        });
        insertEcmFileBatchOperations(ecmFileBatchOperationList);
    }

    /**
     * 保存查看业务日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void handleSaveLog(Map<String, Object> params, String operate, Class<?>[] exceptionTypes,Integer operatorType) {
        // 查看业务（EcmsCaptureVO）
        EcmRootDataDTO ecmRootDataDTO = (EcmRootDataDTO) params.get(BusiLogConstants.GETSORTDEL);
        List<EcmBusExtendDTO> ecmBusExtendDTOList = ecmRootDataDTO.getEcmBusExtendDTOS();
        if (CollUtil.isEmpty(ecmBusExtendDTOList)) {
            return;
        }
        if (IcmsConstants.SIGN_FLAG_ONE.equals(ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan())) {
            operate = "采集业务（对外接口）";
        }
        Set<String> appCodeSet = ecmBusExtendDTOList.stream()
                .map(EcmBusExtendDTO::getAppCode)
                .collect(Collectors.toSet());
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appCodeSet);
        Map<String, EcmAppDef> appCodeWithObj = ecmAppDefs.stream()
                .collect(Collectors.toMap(EcmAppDef::getAppCode, obj -> obj));
        List<EcmBusiLog> busExtendDTOList = new LinkedList<>();
        for (EcmBusExtendDTO dto : ecmBusExtendDTOList) {
            EcmBusiLog ecmBusiLog = new EcmBusiLog();
            ecmBusiLog.setBusiNo(dto.getBusiNo())
                    .setAppName(appCodeWithObj.get(dto.getAppCode()).getAppName())
                    .setAppCode(dto.getAppCode())
                    .setOrgCode(dto.getOrgCode())
                    .setOperatorId(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode())
                    .setOperator(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName())
                    .setOperateContent(operate + ":" + dto.getBusiNo())
                    .setOperatorType(operatorType)
                    .setErrorInfo(exceptionTypes.length == 0 ? null : Arrays.toString(exceptionTypes));
            busExtendDTOList.add(ecmBusiLog);
        }
        // 批量插入业务日志
        MybatisBatch<EcmBusiLog> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, busExtendDTOList);
        MybatisBatch.Method<EcmBusiLog> method = new MybatisBatch.Method<>(SysBusiLogMapper.class);
        mybatisBatch.execute(method.insert());
    }


    /**
     * 保存编辑日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveEditLog(EcmBusiLog ecmBusiLog, String operate, RotateFileVO vo, AccountToken token,Integer operatorType) {
        Long busiId = vo.getBusiId();
        // 获取业务信息实体
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operatorType)
                .setOperateContent(operate + ":" + vo.getEcmFileInfoExtendsNew().get(0).getNewFileName() + "等" + vo.getEcmFileInfoExtendsNew().size() + "个文件");
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 保存合并文件日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveMergeLog(EcmBusiLog ecmBusiLog, String operate, MergFileVO vo, AccountToken token,Integer operatorType) {
        Long busiId = vo.getBusiId();
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operatorType)
                //合并xxx等size个文件 至 新文件名
                .setOperateContent(operate + ": 合并" + vo.getNewFileNames().get(IcmsConstants.ZERO) + "等" + vo.getFileIdList().size() + "个文件，至" + vo.getEcmFileInfoDTO().getNewFileName());
        ecmBusiLogMapper.insert(ecmBusiLog);
        // 批量插入文件记录
        insertFileBatchOperation(vo.getFileIdList(), ecmBusiLog.getId());
    }

    /**
     * 保存拆分文件日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveSplitLog(EcmBusiLog ecmBusiLog, String operate, SplitFileVO vo, AccountToken token,Integer operatorType) {
        Long busiId = vo.getBusiId();
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operatorType)
                .setOperateContent(operate + ": " + vo.getEcmFileInfoDTO().getNewFileName());
        ecmBusiLogMapper.insert(ecmBusiLog);
        List<EcmFileInfoDTO> ecmFileInfoDTOS = vo.getEcmFileInfoDTOS();
        List<Long> fileIdList = ecmFileInfoDTOS.stream()
                .map(EcmFileInfoDTO::getFileId)
                .collect(Collectors.toList());
        // 批量插入文件操作日志
        insertFileBatchOperation(fileIdList, ecmBusiLog.getId());
    }

    /**
     * 保存还原文件日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveRestoreLog(EcmBusiLog ecmBusiLog, String operate, FileInfoVO vo, AccountToken token,Integer operatorType) {
        Long busiId = vo.getBusiId();
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        List<Long> fileIdList = vo.getFileIdList();
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operatorType)
                .setOperateContent(operate + ": " + fileIdList.get(0) + "等" + fileIdList.size() + "个文件");
        ecmBusiLogMapper.insert(ecmBusiLog);
        // 批量插入操作日志
        insertFileBatchOperation(fileIdList, ecmBusiLog.getId());
    }

    /**
     * 保存删除文件日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveDeleteLog(EcmBusiLog ecmBusiLog, String operate, FileInfoVO vo, AccountToken token,Integer operatorType) {
        Long busiId = vo.getBusiId();
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        List<Long> fileIdList = vo.getFileIdList();
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operatorType)
                .setOperateContent(operate + ": " + fileIdList.get(0) + "等" + fileIdList.size() + "个文件");
        ecmBusiLogMapper.insert(ecmBusiLog);
        // 批量插入文件操作日志
        insertFileBatchOperation(fileIdList, ecmBusiLog.getId());
    }

    /**
     * 保存上传文件日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveUploadLog(EcmBusiLog ecmBusiLog, String operate, EcmFileInfoDTO ecmFileInfoDTO,AccountToken token,Integer operatorType) {
        Long busiId = ecmFileInfoDTO.getBusiId();
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operatorType)
                .setOperateContent(operate + ":" + ecmFileInfoDTO.getNewFileName());
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 保存新增业务日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveAddLogByDTO(EcmBusiLog ecmBusiLog, String operate, EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountToken token,Integer operationType) {
        if (StrUtil.isEmpty(ecmBusiInfoExtend.getAppCode())
                || StrUtil.isEmpty(ecmBusiInfoExtend.getBusiNo())
                || StrUtil.isEmpty(ecmBusiInfoExtend.getAppTypeName())) {
            return;
        }
        // 设置日志信息
        ecmBusiLog.setAppCode(ecmBusiInfoExtend.getAppCode())
                .setAppName(ecmBusiInfoExtend.getAppTypeName())
                .setOrgCode(ecmBusiInfoExtend.getOrgCode())
                .setBusiNo(ecmBusiInfoExtend.getBusiNo())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperateContent(operate + ":" + ecmBusiInfoExtend.getBusiNo())
                .setOperatorType(operationType)
                .setId(null);
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 保存编辑业务日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveEditLogByDTO(EcmBusiLog ecmBusiLog, String operate, EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountToken token,Integer operationType) {
        Long busiId = ecmBusiInfoExtend.getBusiId();
        //获取业务信息实体
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(operationType)
                .setOperateContent(operate + ": " + ecmBusiInfoDTO.getBusiNo());
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 保存编辑业务日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveEditLogByDTO(EcmBusiLog ecmBusiLog, String operate, EcmBusExtendDTO busExtendDTO, AccountToken token,Integer operationType) {
        String busiNo = busExtendDTO.getBusiNo();
        String appCode = busExtendDTO.getAppCode();
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppType(appCode, busiNo);
        // 添加日志信息
        ecmBusiLog.setBusiNo(busiNo)
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperateContent(operate + ": " + busiNo)
                .setOperatorType(operationType);
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 保存编辑业务属性日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveEditLogByDTO(EcmBusiLog ecmBusiLog, String operate, EditBusiAttrDTO editBusiAttrDTO,Integer operationType) {
        String busiNo = editBusiAttrDTO.getBusiNo();
        String appCode = editBusiAttrDTO.getAppCode();
        // 获取主键业务属性
        EcmAppAttr ecmAppAttr = ecmAppAttrMapper.selectOne(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, editBusiAttrDTO.getAppCode())
                .eq(EcmAppAttr::getIsKey, IcmsConstants.ONE));
        // 设置业务属性主键
        if (ObjectUtil.isNotNull(ecmAppAttr)) {
            for (EcmBusiAttrDTO attrDTO : editBusiAttrDTO.getEcmBusiAttrDTOList()) {
                if (attrDTO.getAttrCode().equals(ecmAppAttr.getAttrCode())) {
                    busiNo = attrDTO.getAppAttrValue();
                }
            }
        }
        // 获取业务及类型信息
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppType(appCode, busiNo);
        ecmBusiLog.setBusiNo(busiNo)
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(editBusiAttrDTO.getEcmBaseInfoDTO().getUserCode())
                .setOperator(editBusiAttrDTO.getEcmBaseInfoDTO().getUserName())
                .setOperatorType(operationType)
                .setOperateContent(operate + ": " + busiNo);
        ecmBusiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 根据业务ID保存业务日志
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveLogByBusiId(EcmBusiLog ecmBusiLog, String operate, Long busiId, AccountToken token,Integer operatorType) {
        // 获取业务信息实体
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        // 若业务新增失败未入库，则从缓存获取
        if (ObjectUtils.isEmpty(ecmBusiInfoDTO)) {
            AccountTokenExtendDTO accountTokenExtendDTO = new AccountTokenExtendDTO();
            BeanUtils.copyProperties(token,accountTokenExtendDTO);
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(accountTokenExtendDTO,busiId);
            this.saveAddLogByDTO(ecmBusiLog, operate, ecmBusiInfoRedisDTO, token,operatorType);
        } else {
            ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                    .setAppName(ecmBusiInfoDTO.getAppTypeName())
                    .setAppCode(ecmBusiInfoDTO.getAppCode())
                    .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                    .setOperatorId(token.getUsername())
                    .setOperator(token.getName())
                    .setOperateContent(operate + ":" + ecmBusiInfoDTO.getBusiNo())
                    .setOperatorType(operatorType)
                    .setId(null);
            ecmBusiLogMapper.insert(ecmBusiLog);
        }
    }

    /**
     * 根据业务id列表批量保存日志（查看业务）
     */
    @Async(BUSI_LOG_TASK_EXECUTOR)
    public void saveBatchLogByBusiId(EcmBusiLog ecmBusiLog, String operate, List<Long> busiIdList, AccountToken token,Integer operatorType) {
        busiIdList.forEach(busiId -> {
            saveLogByBusiId(ecmBusiLog, operate, busiId, token,operatorType);
        });
    }

    /**
     * 批量插入文件操作日志
     */
    private void insertFileBatchOperation(List<Long> fileIdList, Long busiLogId) {
        List<EcmFileBatchOperation> operationList = new LinkedList<>();
        for (Long fileId : fileIdList) {
            EcmFileBatchOperation batchOperation = new EcmFileBatchOperation();
            batchOperation.setFileId(fileId);
            batchOperation.setEcmBusiLogId(busiLogId);
            operationList.add(batchOperation);
        }
        // 批量插入操作日志
        insertEcmFileBatchOperations(operationList);
    }

    private void insertEcmFileBatchOperations(List<EcmFileBatchOperation> operationList) {
        MybatisBatch<EcmFileBatchOperation> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, operationList);
        MybatisBatch.Method<EcmFileBatchOperation> method = new MybatisBatch.Method<>(EcmFileBatchOperationMapper.class);
        mybatisBatch.execute(method.insert());
    }

}
