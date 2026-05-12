package com.sunyard.ecm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.SysBusiLogDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmFileBatchOperationMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmFileBatchOperation;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.util.EasyExcelUtils;
import com.sunyard.ecm.vo.SearchBusiLogVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author scm
 * @since 2023/8/1 14:22
 * @desc 业务日志实现类
 */
@Slf4j
@Service
public class LogBusiService  {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmFileBatchOperationMapper ecmFileBatchOperationMapper;
    @Resource
    private SysBusiLogMapper busiLogMapper;
    @Resource
    private EcmBusiInfoMapper busiInfoMapper;
    @Resource
    private EcmAppDefMapper appDefMapper;
    @Resource
    private EcmFileInfoMapper fileInfoMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private SysBusiLogMapper sysBusiLogMapper;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    /**
     * 分页查询业务日志列表
     */
    public Object queryBusiLog(SearchBusiLogVO vo) {
        List<EcmBusiLog> ecmBusiLogs = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        if (vo.getAppType() == null) {
            PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
            ecmBusiLogs = sysBusiLogMapper.selectList(vo.queryWrapper());
        } else {
            LambdaQueryWrapper<EcmBusiLog> queryWrapper = vo.queryWrapper();
            List<String> appTypeIds = vo.getAppType();
            getSysBusiLogs(ids, appTypeIds);
            ids.addAll(appTypeIds);
            queryWrapper.in(EcmBusiLog::getAppCode, ids);
            PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
            ecmBusiLogs = sysBusiLogMapper.selectList(queryWrapper);
        }
        PageInfo pageInfo = new PageInfo<>(ecmBusiLogs);
        if (CollectionUtils.isEmpty(ecmBusiLogs)) {
            return pageInfo;
        }
        List<SysBusiLogDTO> sysBusiLogDTOS = PageCopyListUtils.copyListProperties(ecmBusiLogs, SysBusiLogDTO.class);
        handBusiLogOperatorTypeStr(sysBusiLogDTOS);
        pageInfo.setList(sysBusiLogDTOS);
        return pageInfo;
    }

    public void handBusiLogOperatorTypeStr(List<SysBusiLogDTO> sysBusiLogDTOS) {
        Map<Integer, String> operatorTypeMap = new HashMap<>();
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_ZERO,BusiLogConstants.OPERATION_TYPE_ZERO_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_ONE, BusiLogConstants.OPERATION_TYPE_ONE_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_TWO,BusiLogConstants.OPERATION_TYPE_TWO_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_THREE, BusiLogConstants.OPERATION_TYPE_THREE_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_FOUR, BusiLogConstants.OPERATION_TYPE_FOUR_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_FIVE, BusiLogConstants.OPERATION_TYPE_FIVE_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_SIX, BusiLogConstants.OPERATION_TYPE_SIX_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_SEVEN, BusiLogConstants.OPERATION_TYPE_SEVEN_STR);
        operatorTypeMap.put(BusiLogConstants.OPERATION_TYPE_EIGHT, BusiLogConstants.OPERATION_TYPE_EIGHT_STR);
        for (SysBusiLogDTO dto : sysBusiLogDTOS) {
            if(!ObjectUtils.isEmpty(dto.getOperatorType())){
                dto.setOperatorTypeStr(operatorTypeMap.get(dto.getOperatorType()));
            }
        }
    }

    /**
     * 查询业务日志中全部业务类型
     */
    public void exportBusiLog(HttpServletResponse response, List<Long> logIds) {
        //获取业务日志数据
        LambdaQueryWrapper<EcmBusiLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(EcmBusiLog::getCreateTime);
        List<EcmBusiLog> ecmBusiLogs;
        if (ObjectUtils.isEmpty(logIds)) {
            ecmBusiLogs = sysBusiLogMapper.selectList(null);
        } else {
            queryWrapper.in(EcmBusiLog::getId, logIds);
            ecmBusiLogs = sysBusiLogMapper.selectList(queryWrapper);
        }
        try {
            EasyExcelUtils.writeListTo(response, ecmBusiLogs, BusiLogConstants.BUSILOG);
        } catch (IOException e) {
            log.error("导出失败", e);
        }
    }

    /**
     * 添加影像日志记录
     * @param type 0：打印操作，1：下载操作，2：其他操作
     * @param fileIds 操作的文件id集合
     * @param busiId 业务id
     * @param token
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addEcmLog(Integer type, List<Long> fileIds, Long busiId, AccountTokenExtendDTO token) {
        long id;
        //判断操作类型
        if (IcmsConstants.ZERO.equals(type)) {
            //打印
            //-插入影像操作日志表
            id = addEcmBusiLogToDb(busiId, token, "打印", fileIds.size());
        } else if (IcmsConstants.ONE.equals(type)) {
            //下载
            //-插入影像操作日志表
            id = addEcmBusiLogToDb(busiId, token, "下载", fileIds.size());
        } else {
            return Result.error("type有误", ResultCode.PARAM_ERROR);
        }
        //-插入影像文件批量操作记录表
        addEcmFileBatchOperationToDb(fileIds, id);
        return Result.success(true);
    }

    /**
     * 查询日志详情信息
     */
    public String searchLogDetails(Long ecmBusiLogId, String operateContent, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmBusiLogId, "ecmBusiLogId: 影像操作日志主键id不能未空");
        //查询影像文件批量操作记录表
        LambdaQueryWrapper<EcmFileBatchOperation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmFileBatchOperation::getEcmBusiLogId, ecmBusiLogId);
        List<EcmFileBatchOperation> ecmFileBatchOperationList = ecmFileBatchOperationMapper.selectList(queryWrapper);
        //返回详情
        String info;
        String remark = operateContent.split(":")[0];
        if ("合并文件".equals(remark)) {
            //合并
            //处理 operateContent
            //将文件id转为文件名称
            ArrayList<String> fileNamelist = getNamelist(ecmFileBatchOperationList);
            info = remark + fileNamelist + operateContent.split("至")[1];
        } else if ("归类文件".equals(remark)) {
            //归类
            //将文件id转为文件名称
            ArrayList<String> fileNamelist = getNamelist(ecmFileBatchOperationList);
            info = remark + fileNamelist + operateContent.split(remark)[1];
        } else if ("复用文件".equals(remark)) {
            //复用
            //反推业务
            info = backsteppingInfo(remark, operateContent, ecmFileBatchOperationList,token);
        } else if ("拆分文件".equals(remark)) {
            //拆分
            //将文件id转为文件名称
            ArrayList<String> fileNamelist = getNamelist(ecmFileBatchOperationList);
            info = operateContent + "=>" + fileNamelist;
        } else {
            //将文件id转为文件名称
            ArrayList<String> fileNamelist = getNamelist(ecmFileBatchOperationList);
            info = remark + fileNamelist + "";
        }
        return info;
    }

    /**
     * 反推业务
     */
    private String backsteppingInfo(String remark, String operateContent, List<EcmFileBatchOperation> ecmFileBatchOperationList, AccountTokenExtendDTO token) {
        //返回的信息
        String info;
        info = remark + ":";
        //得到文件id集合
        List<Long> fileIdList = ecmFileBatchOperationList.stream().map(EcmFileBatchOperation::getFileId).collect(Collectors.toList());
        //根据文件id找到业务id和doc_code
        LambdaQueryWrapper<EcmFileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(EcmFileInfo::getFileId, fileIdList);
        List<EcmFileInfo> ecmFileInfos = fileInfoMapper.selectList(queryWrapper);
        //根busiId分组
        Map<Long, List<EcmFileInfo>> busiIdMap = ecmFileInfos.stream().collect(Collectors.groupingBy(EcmFileInfo::getBusiId));
        AssertUtils.isNull(busiIdMap, "参数错误");
        //提取busiId集合
        List<Long> busiIdList = ecmFileInfos.stream().map(EcmFileInfo::getBusiId).collect(Collectors.toList());
        AssertUtils.isNull(busiIdList, "参数错误");
        //提取doc_code
        List<String> docCodeList = ecmFileInfos.stream().map(EcmFileInfo::getDocCode).collect(Collectors.toList());
        AssertUtils.isNull(docCodeList, "参数错误");
        //根据doc_code分组
        Map<String, List<EcmFileInfo>> docCodeMap = ecmFileInfos.stream().collect(Collectors.groupingBy(EcmFileInfo::getDocCode));
        AssertUtils.isNull(docCodeMap, "参数错误");
        //判断是否从持久化数据库中查询
        Boolean isDb = false;
        for (Entry<Long, List<EcmFileInfo>> entry : busiIdMap.entrySet()) {
            Long k = entry.getKey();
            List<EcmFileInfo> v = entry.getValue();
            //根据业务id得到业务名称
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, k);
            if (ecmBusiInfoRedisDTO!=null) {
                //资料树子节点
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS();
                AssertUtils.isNull(ecmBusiDocRedisDTOS, "参数错误");
                //得到子节点名称
                info = info + ecmBusiInfoRedisDTO.getAppTypeName() + "下的";
                for (EcmBusiDocRedisDTO p : ecmBusiDocRedisDTOS) {
                    if (docCodeList.contains(p.getDocCode())) {
                        List<String> fileNameList = docCodeMap.get(p.getDocCode()).stream().map(EcmFileInfo::getNewFileName).collect(Collectors.toList());
                        info = info + p.getDocName() + "中的:" + fileNameList.toString() + ",";
                    }
                }
            } else {
                isDb = true;
                break;
            }
        }
        //-从持久化数据库中获取
        if (isDb) {
            //查询业务名称
            LambdaQueryWrapper<EcmBusiInfo> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(EcmBusiInfo::getBusiId, busiIdList);
            List<EcmBusiInfo> ecmBusiInfos = busiInfoMapper.selectList(queryWrapper1);
            AssertUtils.isNull(ecmBusiInfos, "参数错误");
            //查询节点名称
            LambdaQueryWrapper<EcmBusiDoc> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.in(EcmBusiDoc::getDocCode, docCodeList);
            List<EcmBusiDoc> docList = ecmBusiDocMapper.selectList(queryWrapper2);
            AssertUtils.isNull(docList, "参数错误");
            for (EcmBusiInfo ecmBusiInfo : ecmBusiInfos) {
                info = info + ecmBusiInfo.getBusiNo() + "下的";
                for (EcmBusiDoc ecmBusiDoc : docList) {
                    if (ecmBusiInfo.getBusiId().equals(ecmBusiDoc.getBusiId())) {
                        List<String> fileNameList = docCodeMap.get(ecmBusiDoc.getDocCode()).stream().map(EcmFileInfo::getNewFileName).collect(Collectors.toList());
                        info = info + ecmBusiDoc.getDocName() + "中的:" + fileNameList.toString() + ",";
                    }
                }
            }
        }
        info = info + "复用至" + operateContent.split("至")[1];
        return info;
    }

    /**
     * 将文件id转为文件名称
     */
    private ArrayList<String> getNamelist(List<EcmFileBatchOperation> ecmFileBatchOperationList) {
        ArrayList<String> fileNamelist = new ArrayList<>();
        for (EcmFileBatchOperation ecmFileBatchOperation : ecmFileBatchOperationList) {
            LambdaQueryWrapper<EcmFileInfo> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(EcmFileInfo::getFileId, ecmFileBatchOperation.getFileId());
            EcmFileInfo fileInfo = fileInfoMapper.selectOne(queryWrapper2);
            fileNamelist.add(fileInfo.getNewFileName());
        }
        return fileNamelist;
    }

    /**
     * 插入影像文件批量操作记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void addEcmFileBatchOperationToDb(List<Long> fileIds, long ecmBusiLogId) {
        AssertUtils.isNull(ecmBusiLogId, "ecmBusiLogId: 影像操作日志id不能未空");
        List<EcmFileBatchOperation> ecmFileBatchOperationList = new ArrayList<>();
        for (Long fileId : fileIds) {
            EcmFileBatchOperation ecmFileBatchOperation = new EcmFileBatchOperation();
            ecmFileBatchOperation.setEcmBusiLogId(ecmBusiLogId);
            ecmFileBatchOperation.setId(snowflakeUtil.nextId());
            ecmFileBatchOperation.setFileId(fileId);
            ecmFileBatchOperationList.add(ecmFileBatchOperation);
        }


        insertEcmFileBatchOperations(ecmFileBatchOperationList);
        //ecmFileBatchOperationMapper.insertList(ecmFileBatchOperationList);
    }

    /**
     * 批量插入影像文件批量操作记录
     */
    private void insertEcmFileBatchOperations(List<EcmFileBatchOperation> ecmFileBatchOperationList) {
        MybatisBatch<EcmFileBatchOperation> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileBatchOperationList);
        MybatisBatch.Method<EcmFileBatchOperation> method = new MybatisBatch.Method<>(EcmFileBatchOperationMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 添加日志
     */
    public Result addLog(Long busiId, String operateContent, String errorInfo, AccountTokenExtendDTO tokenExtendDTO,Integer operatorType) {
        Assert.notNull(busiId, "业务ID不能为空!");
        EcmBusiInfo ecmBusiInfo =  busiInfoMapper.selectByIdWithDeleted(busiId);
        EcmAppDef ecmAppDef = appDefMapper.selectOne(new LambdaQueryWrapper<EcmAppDef>()
                .select(EcmAppDef::getAppCode, EcmAppDef::getAppName)
                .eq(EcmAppDef::getAppCode, ecmBusiInfo.getAppCode()));
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setBusiNo(ecmBusiInfo.getBusiNo())
                .setAppCode(ecmAppDef.getAppCode())
                .setAppName(ecmAppDef.getAppName())
                .setOperator(tokenExtendDTO.getName())
                .setOperatorId(tokenExtendDTO.getUsername())
                .setOperateContent(operateContent)
                .setOrgCode(ecmBusiInfo.getOrgCode())
                .setCreateTime(DateUtil.date())
                .setOperatorType(operatorType)
                .setErrorInfo(errorInfo);

        return busiLogMapper.insert(ecmBusiLog) == 1 ? Result.success() : Result.error("日志添加失败!", 411);
    }

    /**
     * 插入业务日志
     */
    private long addEcmBusiLogToDb(Long busiId, AccountTokenExtendDTO token, String operate, int size) {
        //获取业务信息实体
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                .getEcmBusiInfoRedisDTO(token, busiId);
        //获取业务类型实体
        LambdaQueryWrapper<EcmAppDef> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(EcmAppDef::getAppCode, ecmBusiInfoRedisDTO.getAppCode());
        EcmAppDef ecmAppDef = appDefMapper.selectOne(queryWrapper1);
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
        ecmBusiLog.setAppName(ecmAppDef.getAppName());
        ecmBusiLog.setAppCode(ecmAppDef.getAppCode());
        ecmBusiLog.setOrgCode(ecmBusiInfoRedisDTO.getOrgCode());
        ecmBusiLog.setOperatorId(token.getUsername());
        ecmBusiLog.setOperator(token.getName());
        ecmBusiLog.setOperatorType(BusiLogConstants.OPERATION_TYPE_FIVE);
        ecmBusiLog.setOperateContent(operate + ":" + size + "个文件");
        busiLogMapper.insert(ecmBusiLog);
        return ecmBusiLog.getId();
    }

    //判断传入的业务类型ID是否为叶子节点ID
    private void getSysBusiLogs(List<String> ids, List<String> appTypeIds) {
        for (String a : appTypeIds) {
            LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppDef::getParent, a);
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(wrapper);
            if (ecmAppDefs.size() == IcmsConstants.ZERO) {
                ids.add(a);
            } else {
                List<String> list = new ArrayList<>();
                ecmAppDefs.forEach(e -> {
                    list.add(e.getAppCode());
                });
                getSysBusiLogs(ids, list);
            }
        }
    }


}
