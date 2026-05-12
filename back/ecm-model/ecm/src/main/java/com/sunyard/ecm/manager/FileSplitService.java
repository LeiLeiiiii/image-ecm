package com.sunyard.ecm.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.service.ModelPermissionsService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.ecm.util.CheckDetectionUtils;
import com.sunyard.ecm.util.CommonUtils;
import com.sunyard.ecm.vo.SplitFileVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yzy
 * @desc
 * @since 2026/2/3
 */
@Slf4j
@Service
public class FileSplitService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private FileStorageApi fileStorageApi;
    @Resource
    private InstApi instApi;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private TaskSwitchService taskSwitchService;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private ModelPermissionsService modelPermissionsService;
    @Resource
    private CheckDetectionService checkDetectionService;

    /**
     * 拆分
     */
    @LogManageAnnotation("拆分文件")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public void backSplitFileAsn(SplitFileVO vo, AccountTokenExtendDTO token, FileSplitPdfVO fileSplitPdfVO, EcmAsyncTask ecmAsyncTask) {
        AccountToken accountToken = new AccountToken();
        BeanUtils.copyProperties(token, accountToken);
        String taskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
        fileSplitPdfVO.setToken(JSONObject.toJSONString(accountToken));
        Result<List<SysFileDTO>> listResult = fileStorageApi.splitFile(fileSplitPdfVO);
        if (!listResult.isSucc() || CollectionUtil.isEmpty(listResult.getData())) {
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
            ecmAsyncTask.setTaskType(taskType);
            asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
            AssertUtils.isTrue(true, "文件有误，未找到分页信息");
        }
        List<EcmFileInfoDTO> ecmFileInfoDTOS = PageCopyListUtils
                .copyListProperties(listResult.getData(), EcmFileInfoDTO.class);
        vo.setEcmFileInfoDTOS(ecmFileInfoDTOS);
        getSplitFileEcm(vo, token);
        String flagId = token.getFlagId();
        UserBusiRedisDTO userPageRedis = busiCacheService.getUserPageRedis(flagId, token);
        HashSet<Long> splitFileId = userPageRedis.getSplitFileId();
        splitFileId.remove(Long.parseLong(fileSplitPdfVO.getNewFileId()));
        userPageRedis.setSplitFileId(splitFileId);
        busiCacheService.saveOrUpdateUser(flagId, userPageRedis);
        //修改任务状态,拆分成功
        taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                EcmCheckAsyncTaskEnum.SPLIT_SUCCESS.description().charAt(0));
        ecmAsyncTask.setTaskType(taskType);
        asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
        //设置redis检测完成
        busiCacheService.setNeedPushBusiSync(
                RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + vo.getBusiId(),
                IcmsConstants.DETECTION_COMPLETE, TimeOutConstants.ONE_HOURS);

    }

    @LogManageAnnotation("拆分文件")
    public Result<Boolean> backSplitFile(SplitFileVO vo, AccountTokenExtendDTO token, FileSplitPdfVO fileSplitPdfVO) {
        AccountToken accountToken = new AccountToken();
        BeanUtils.copyProperties(token, accountToken);
        fileSplitPdfVO.setToken(JSONObject.toJSONString(accountToken));
        Result<List<SysFileDTO>> listResult = fileStorageApi.splitFile(fileSplitPdfVO);
        if (!listResult.isSucc() || CollectionUtil.isEmpty(listResult.getData())) {
            AssertUtils.isTrue(true, "文件有误，未找到分页信息");
        }
        List<EcmFileInfoDTO> ecmFileInfoDTOS = PageCopyListUtils
                .copyListProperties(listResult.getData(), EcmFileInfoDTO.class);
        vo.setEcmFileInfoDTOS(ecmFileInfoDTOS);
        Result<Boolean> splitFileEcm = getSplitFileEcm(vo, token);
        return splitFileEcm;
    }

    private Result<Boolean> getSplitFileEcm(SplitFileVO vo, AccountTokenExtendDTO token) {
        List<EcmFileInfoDTO> ecmFileInfoDTOS = vo.getEcmFileInfoDTOS();
        AssertUtils.isNull(ecmFileInfoDTOS, "拆分后的文件列表不能为空");
        //被拆分的文件信息
        EcmFileInfoDTO ecmFileInfoDTO = vo.getEcmFileInfoDTO();
        List<EcmFileInfo> listEcmFile = new ArrayList<>();
        List<EcmAsyncTask> ecmAsyncTaskList = new ArrayList<>();
        Date date = new Date();
        if (!ObjectUtils.isEmpty(ecmFileInfoDTOS)) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, vo.getBusiId());
//            EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectById(ecmFileInfoDTO.getDocId());
            EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(ecmFileInfoDTO.getDocId());
            List<EcmFileHistory> ecmFileHistoryList = new ArrayList<>();
//            String docCode = ecmBusiDoc == null ? ecmFileInfoDTO.getDocId() : ecmBusiDoc.getDocCode();
            String docCode = ecmFileInfoDTO.getDocId();
            Map<String, String> map = null;
            //查找创建人中文名和机构号

            map = searchUserNameById(token);
            String userName = map.get("userName");
            String orgCode = map.get("orgCode");
            String orgName = map.get("orgName");
            //查询资料类型名称
            EcmDocDef ecmDocDefs = ecmDocDefMapper.selectById(ecmFileInfoDTO.getDocCode());
            long docFileSort = System.currentTimeMillis();
            long f = 10;
            //获取资料智能化检测开关
            Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(ecmFileInfoDTO.getDocCode());
            SysStrategyDTO sysStrategyDTO = isAutoGroup(ecmFileInfoDTO.getAppCode());
            List<String> enumConfigList = sysStrategyService.queryEcmEnumConfig();
            String taskType = CheckDetectionUtils.getTaskType(result, sysStrategyDTO, enumConfigList);
            for (EcmFileInfoDTO e : ecmFileInfoDTOS) {
                e.setDocFileSort(String.valueOf(docFileSort + f));
                e.setDocCode(vo.getDocCode());
                //处理前端传来的数据
                dealEcmFileInfoExtend(e);
                addFileInfoToSplitFile(token, ecmFileInfoDTO.getBusiId(),
                        ecmFileInfoDTO.getDocId() == null ? ecmFileInfoDTO.getDocCode()
                                : ecmFileInfoDTO.getDocId(),
                        e, ecmBusiInfoRedisDTO.getTreeType(), docCode, ecmDocDef);
                //将拆分后生成的新文件插入到文件信息表
                EcmFileInfo ecmFileInfo = new EcmFileInfo();
                BeanUtils.copyProperties(e, ecmFileInfo);
                ecmFileInfo.setNewFileSize(e.getSize());
                ecmFileInfo.setCreateUser(token.getUsername());
                ecmFileInfo.setCreateUserName(token.getName());
                ecmFileInfo.setNewFileExt(CommonUtils.getExt(e));
                ecmFileInfo.setOrgName(orgName);
                ecmFileInfo.setOrgCode(orgCode);
                listEcmFile.add(ecmFileInfo);
                //生成异步任务列表
                if (!IcmsConstants.ASYNC_TASK_STATUS_INIT.equals(taskType)) {
                    EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                    ecmAsyncTask.setTaskType(taskType);
                    ecmAsyncTask.setBusiId(vo.getBusiId());
                    ecmAsyncTask.setFileId(e.getFileId());
                    ecmAsyncTaskList.add(ecmAsyncTask);
                }
                //将拆分后生成的新文件插入文件历史记录表
                EcmFileHistory ecmFileHistory = new EcmFileHistory();
                ecmFileHistory.setFileId(e.getFileId());
                ecmFileHistory.setBusiId(e.getBusiId());
                ecmFileHistory.setNewFileId(e.getNewFileId());
                ecmFileHistory.setNewFileSize(e.getSize());
                ecmFileHistory.setFileOperation(IcmsConstants.ADD_FILE_OPERATION_STRING);
                ecmFileHistory.setCreateUser(token.getUsername());
                ecmFileHistory.setCreateTime(new Date());
                ecmFileHistory.setNewFileExt(CommonUtils.getExt(e));
                ecmFileHistoryList.add(ecmFileHistory);
                //更新redis数据(添加新的文件)
                e.setAppTypeName(vo.getAppTypeName());
                e.setBusiNo(vo.getBusiNo());
                e.setCreateUserName(userName);
                e.setOrgCode(orgCode);
                e.setOrgName(orgName);
                e.setAppCode(ecmFileInfoDTO.getAppCode());
                e.setNewFileSize(e.getSize());
                addDocTypeName(e, ecmDocDefs);
                FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
                BeanUtils.copyProperties(e, fileInfoRedisDTO);
                //新增拆分后新文件的es信息
                operateFullQueryService.addEsFileInfo(fileInfoRedisDTO, token.getId());
                f += 10; // 每次增加10
            }
            log.info("历史：ecmFileHistoryList :{}", ecmFileHistoryList);
            //添加拆分前的文件状态（历史记录）
            EcmFileHistory ecmFileHistory = new EcmFileHistory();
            ecmFileHistory.setFileId(ecmFileInfoDTO.getFileId());
            ecmFileHistory.setBusiId(ecmFileInfoDTO.getBusiId());
            ecmFileHistory.setNewFileId(ecmFileInfoDTO.getNewFileId());
            ecmFileHistory.setNewFileSize(ecmFileInfoDTO.getSize());
            ecmFileHistory.setFileOperation(IcmsConstants.MSPILT_FILE_STRING);
            ecmFileHistory.setCreateUser(token.getUsername());
            ecmFileHistory.setCreateTime(date);
            ecmFileHistory.setNewFileExt(CommonUtils.getExt(ecmFileInfoDTO));
            ecmFileHistoryList.add(ecmFileHistory);
            //更新redis
            updateFileByBusiInfoRedisToSplitFile(ecmFileInfoDTOS, ecmFileHistoryList,
                    ecmFileInfoDTO.getFileId(), map, ecmDocDefs);
            //批量插入历史记录表
            //ecmFileHistoryMapper.insertBatch(ecmFileHistoryList);
            insertBatchEcmFileHistory(ecmFileHistoryList);
            if (!CollectionUtils.isEmpty(listEcmFile)) {
                insertEcmFileInfos(listEcmFile);
                //ecmFileInfoMapper.insertEcm(listEcmFile);
            }
            if (!CollectionUtils.isEmpty(ecmAsyncTaskList)) {
                asyncTaskService.batchInsert(ecmAsyncTaskList);
                //往redis写入key,表示检测中,需要定时任务刷新
                busiCacheService.setNeedPushBusiSync(RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + vo.getBusiId(), IcmsConstants.DETECTING, TimeOutConstants.ONE_HOURS);
                List<FileInfoRedisDTO> fileInfoRedisDTOList = busiCacheService.getFileInfoRedis(vo.getBusiId());
                Map<Long, FileInfoRedisDTO> fileInfoMap = fileInfoRedisDTOList.stream()
                        .collect(Collectors.toMap(FileInfoRedisDTO::getFileId, file -> file));
                for (EcmAsyncTask ecmAsyncTask : ecmAsyncTaskList) {
                    try {
                        checkDetectionService.checkDetectionByMq(fileInfoMap.get(ecmAsyncTask.getFileId()),ecmAsyncTask, taskType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        //删除redis缓存
        //        delBusiBatchNoRedies(vo.getBusiBatchNo());
        //添加业务操作记录表
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.MSPILT_FILE, token,
                "修改文件-拆分: " + vo.getNewFileName());
        //更新持久化数据(拆分前的文件)
        ecmFileInfoMapper.update(null,
                new UpdateWrapper<EcmFileInfo>().set("update_user", token.getUsername())
                        .set("update_time", date)
                        .eq("file_id", vo.getEcmFileInfoDTO().getFileId()));

        return Result.success(true);
    }

    private void insertEcmFileInfos(List<EcmFileInfo> ecmFileInfos) {
        MybatisBatch<EcmFileInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmFileInfos);
        MybatisBatch.Method<EcmFileInfo> method = new MybatisBatch.Method<>(
                EcmFileInfoMapper.class);
        mybatisBatch.execute(method.insert());
    }

    private Map<String, String> searchUserNameById(AccountTokenExtendDTO token) {
        Map<String, String> map = new HashMap();
        if (token.isOut()) {
            map.put("userName", token.getName());
            map.put("orgCode", token.getOrgCode());
            map.put("orgName",token.getOrgName());
            return map;
        } else {
            String userId = token.getUsername();
            List<String> userIds = new ArrayList<>();
            if (!ObjectUtils.isEmpty(userId)) {
                userIds.add(userId);
            }
            Map<String, List<SysUserDTO>> groupedByUserId = modelPermissionsService
                    .getUserListByUserIds(userIds);

            if (groupedByUserId != null) {
                //添加创建人名称
                if (!ObjectUtils.isEmpty(userId)) {
                    if (!CollectionUtils.isEmpty(groupedByUserId.get(userId))) {
                        map.put("userName", groupedByUserId.get(userId).get(0).getName());
                        //根据机构id获取机构号
                        SysInstDTO sysInstDTO = instApi
                                .getInstByInstId(groupedByUserId.get(userId).get(0).getInstId())
                                .getData();
                        AssertUtils.isNull(sysInstDTO, "参数错误");
                        map.put("orgCode", sysInstDTO.getInstNo());
                        map.put("orgName",sysInstDTO.getName());
                        token.setOrgName(sysInstDTO.getName());
                        token.setOrgCode(sysInstDTO.getInstNo());
                    }
                }
            } else {
                //对外接口的数据
                //添加创建人名称
                if (!ObjectUtils.isEmpty(userId)) {
                    map.put("userName", userId);
                }
            }
            return map;
        }
    }

    public SysStrategyDTO isAutoGroup(String appCode) {
        //查询OCR识别总配置（0为未配置）
        SysStrategyDTO sysStrategyDTO = sysStrategyService.queryConfig();
        //OCR识别业务类型ID
        List<String> ocrConfigIds = searchChildren(sysStrategyDTO.getOcrConfigIds());
        if (!ocrConfigIds.contains(appCode)) {
            sysStrategyDTO.setOcrConfigStatus(false);
        }
        //混贴拆分业务类型ID
        List<String> splitIds = searchChildren(sysStrategyDTO.getSplitIds());
        if (!splitIds.contains(appCode)) {
            sysStrategyDTO.setSplitStatus(false);
        }
        return sysStrategyDTO;
    }

    private List<String> searchChildren(List<String> ocrIdentifyIds) {
        //查询所有
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(null);
        //根据父节点分组
        Map<String, List<EcmAppDef>> listMap = ecmAppDefs.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getParent));
        List<String> allOcrIdentifyIds = new ArrayList();
        //将数据库存的id的所有子类存入一个新的数组
        addAllId(listMap, ocrIdentifyIds, allOcrIdentifyIds);
        return allOcrIdentifyIds;
    }

    private void addAllId(Map<String, List<EcmAppDef>> listMap, List<String> ocrIdentifyIds,
                          List<String> allOcrIdentifyIds) {
        if (!CollectionUtils.isEmpty(ocrIdentifyIds)) {
            for (String id : ocrIdentifyIds) {
                List<EcmAppDef> ecmAppDefs = listMap.get(id);
                if (CollectionUtils.isEmpty(ecmAppDefs)) {
                    allOcrIdentifyIds.add(id);
                } else {
                    //得到该子类的id
                    List<String> list = ecmAppDefs.stream().map(EcmAppDef::getAppCode)
                            .collect(Collectors.toList());
                    addAllId(listMap, list, allOcrIdentifyIds);
                }
            }
        }
    }

    private void dealEcmFileInfoExtend(EcmFileInfoDTO e) {
        e.setFileId(e.getId());
        e.setNewFileLock(StateConstants.ZERO);
        e.setFileReuse(StateConstants.ZERO);
        e.setFormat(e.getExt());
        e.setFileMd5(e.getSourceFileMd5());
        e.setNewFileName(e.getOriginalFilename());
        e.setNewFileLock(StateConstants.ZERO);
    }

    private EcmFileInfoDTO addFileInfoToSplitFile(AccountTokenExtendDTO token, Long busiId, String docId, EcmFileInfoDTO e, Integer treeType, String docCode, EcmDocDef ecmDocDef) {
        AssertUtils.isNull(e.getFileReuse(), "是否复用字段（fileReuse）不能为空{是否复用（默认0，1:复用）}");
        AssertUtils.isNull(busiId, "业务主键（busiId）不能为空");
        e.setCreateUser(token.getUsername());
        e.setCreateTime(new Date());
        e.setFileSort(Double.valueOf(e.getDocFileSort()));
        e.setNewFileId(e.getId());
        e.setFileId(snowflakeUtil.nextId());
        e.setBusiId(busiId);
        e.setDocId(docId);
        e.setNewFileUrl(IcmsConstants.NEW_FILE_URL);
        e.setState(StateConstants.ZERO);
        if (IcmsConstants.DYNAMIC_TREE.equals(treeType)) {
            if (IcmsConstants.UNCLASSIFIED_ID.equals(docId)) {
                e.setDocCode(docId);
            } else {
                e.setDocCode(docCode);
            }
        } else {
            e.setDocCode(docId);
        }
        //默认备注为当前节点名称
        e.setComment(ObjectUtils.isEmpty(ecmDocDef) ? "" : ecmDocDef.getDocName());
        e.setDocName(ObjectUtils.isEmpty(ecmDocDef) ? "" : ecmDocDef.getDocName());
        return e;
    }

    private List<FileInfoRedisDTO> updateFileByBusiInfoRedisToSplitFile(List<EcmFileInfoDTO> ecmFileInfoDTO,
                                                                        List<EcmFileHistory> ecmFileHistoryList,
                                                                        Long oleFileId,
                                                                        Map<String, String> map,
                                                                        EcmDocDef ecmDocDef) {

        EcmFileInfoDTO ecmFileInfoDTO1 = ecmFileInfoDTO.get(0);
        //将历史记录根据文件id分组
        Map<Long, List<EcmFileHistory>> listMap = ecmFileHistoryList.stream()
                .collect(Collectors.groupingBy(EcmFileHistory::getFileId));

        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                .getFileInfoRedisSingle(ecmFileInfoDTO1.getBusiId(), oleFileId);
        //批注数量清零
        fileInfoRedisSingle.setFileCommentCount(IcmsConstants.ZERO);
        //添加文件历史记录
        //更新最新修改人
        fileInfoRedisSingle.setUpdateUser(ecmFileInfoDTO1.getCreateUser());
        fileInfoRedisSingle.setUpdateUserName(map.get("userName"));
        fileInfoRedisSingle.setUpdateTime(new Date());
        //编辑文件，添加new标签标识(针对移动端功能)
        fileInfoRedisSingle.setSignFlag(IcmsConstants.ONE);
        List<EcmFileHistory> ecmFileHistories = fileInfoRedisSingle.getFileHistories();
        List<EcmFileHistory> newList = new ArrayList<>(ecmFileHistories);
        newList.add(listMap.get(oleFileId).get(0));
        fileInfoRedisSingle.setFileHistories(newList);
        //更新es信息
        operateFullQueryService.editEsFileInfo(oleFileId, fileInfoRedisSingle.getUpdateUserName(),
                fileInfoRedisSingle.getUpdateTime(), null, null, null);

        //添加新增的文件信息
        ecmFileInfoDTO.forEach(p -> {
            FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
            BeanUtils.copyProperties(p, fileInfoRedisDTO);
            //新增一条文件历史记录
            fileInfoRedisDTO.setFileHistories(listMap.get(p.getFileId()));
            busiCacheService.updateFileInfoRedis(fileInfoRedisDTO);
        });
        return null;
    }

    private void addDocTypeName(EcmFileInfoDTO fileInfoRedisDTO, EcmDocDef ecmDocDef) {
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getDocName())) {
            fileInfoRedisDTO.setDocName("未归类");
        }
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getDocCode())) {
            fileInfoRedisDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
        }
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getComment())) {
            if (!ObjectUtils.isEmpty(ecmDocDef)) {
                fileInfoRedisDTO.setDocName(ecmDocDef.getDocName());
                fileInfoRedisDTO.setComment(ecmDocDef.getDocName());
            }
        }
    }

    private void insertBatchEcmFileHistory(List<EcmFileHistory> ecmFileHistoryList) {
        MybatisBatch<EcmFileHistory> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmFileHistoryList);
        MybatisBatch.Method<EcmFileHistory> method = new MybatisBatch.Method<>(
                EcmFileHistoryMapper.class);
        mybatisBatch.execute(method.insert());
    }
}
