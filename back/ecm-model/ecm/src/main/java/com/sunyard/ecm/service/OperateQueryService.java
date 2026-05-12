package com.sunyard.ecm.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.config.MqConfig;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiVersionDTO;
import com.sunyard.ecm.dto.ecm.EcmFileHistoryDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiOperationService;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.TaskSwitchService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.mapper.EcmFileCommentMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiVersion;
import com.sunyard.ecm.po.EcmFileComment;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.service.mq.RabbitMQProducer;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author： ty
 * @create： 2023/5/9 11:19
 * @desc: 影像查询业务实现类
 */
@Service
public class OperateQueryService  {
    public final static List<String> IMGS = Arrays.asList("JPG", "jpg", "JPEG", "jpeg", "png", "PNG", "psd", "PSD", "bmp", "BMP");
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmAsyncTaskMapper asyncTaskMapper;
    @Resource
    private EcmFileCommentMapper ecmFileCommentMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private FileHandleApi fileHandleApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private CommonService commonService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private TaskSwitchService taskSwitchService;
    @Resource
    private RabbitMQProducer rabbitMQProducer;
    @Resource
    private MqConfig mqConfig;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private ModelPermissionsService modelPermissionsService;

    /**
     * 查看影像文件信息 基本信息+EXIF
     */
    public Object getFileInfo(Long busiId, Long fileId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiId, "参数错误");
        AssertUtils.isNull(fileId, "参数错误");
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return null;
        }
        Map<Long, List<FileInfoRedisDTO>> collect = fileInfoRedisEntities.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getFileId));
        List<FileInfoRedisDTO> fileInfoRedisEntities1 = collect.get(fileId);
        if (CollectionUtils.isEmpty(fileInfoRedisEntities1)) {
            return null;
        }
        return fileInfoRedisEntities1.get(0);
    }

    /**
     * 业务轨迹
     */
    public Object busiTrajectory(Long busiId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiId, "参数错误");
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,busiId);
        List<EcmBusiVersion> ecmBusiVersions = ecmBusiInfoRedisDTO.getEcmBusiVersions();
        if (CollectionUtils.isEmpty(ecmBusiVersions)) {
            return Collections.emptyList();
        }
        List<EcmBusiVersionDTO> ecmBusiVersionDTOS = PageCopyListUtils.copyListProperties(ecmBusiVersions, EcmBusiVersionDTO.class);
        //添加用户名称
        addUserName(ecmBusiVersionDTOS);
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return ecmBusiVersionDTOS;
        }
        //上一个节点的创建时间
        Long beforeTime = 0L;
        for (EcmBusiVersionDTO ecmBusiVersionDTO : ecmBusiVersionDTOS) {
            List<FileInfoRedisDTO> fileInfoRedisDTOS = new ArrayList<>();
            fileInfoRedisDTOS.addAll(fileInfoRedisEntities);
            Iterator<FileInfoRedisDTO> iterator = fileInfoRedisDTOS.iterator();
            while (iterator.hasNext()){
                FileInfoRedisDTO p = iterator.next();
                p.setNewTag(StateConstants.ZERO);
                //根据修改时间和是否删除来判断是否移除
                if (!ObjectUtils.isEmpty(p.getUpdateTime()) && p.getUpdateTime().getTime() < ecmBusiVersionDTO.getCreateTime().getTime() && StateConstants.COMMON_ONE.equals(p.getState())) {
                    iterator.remove();
                    continue;
                }
                //时间在上一个节点的时间~本节点的时间之间的文件设置为new
                if (p.getCreateTime().getTime() > beforeTime && ecmBusiVersionDTO.getCreateTime().getTime() > p.getCreateTime().getTime()) {
                    p.setNewTag(StateConstants.COMMON_ONE);
                }
                //文件上传时间大于本节点的时间的就删除
                if (p.getCreateTime().getTime() > ecmBusiVersionDTO.getCreateTime().getTime()){
                    iterator.remove();
                    continue;
                }
                //设置文件路径
//                if (StrUtil.isNotBlank(IcmsConstants.NEW_FILE_URL) && p.getNewFileId() != null) {
//                    StringBuffer buf = new StringBuffer();
//                    buf.append(fileFullPath).append(IcmsConstants.NEW_FILE_URL).append("?").append("fileId").append("=").append(p.getNewFileId());
//                    p.setFileFullPath(buf.toString());
//                    StringBuffer buf1 = new StringBuffer();
//                    buf1.append(fileFullPath).append(IcmsConstants.NEW_FILE_URL+"Cache").append("?").append("fileId").append("=").append(p.getNewFileId());
//                    p.setFileFullPathCache(buf1.toString());
//                    StringBuffer buf2 = new StringBuffer();
//                    buf2.append(fileFullPath).append(IcmsConstants.THUMBNAIL_URL).append("?").append("fileId").append("=").append(p.getNewFileId());
//                    p.setFileFullPathCacheThumbnail(buf2.toString());
//                }
                //文件后缀改为小写
                p.setFormat(p.getFormat().toLowerCase());
            }
            List<FileInfoRedisDTO> fileList = BeanUtil.copyToList(fileInfoRedisDTOS, FileInfoRedisDTO.class);
//            //业务轨迹文件顺序和节点文件顺序保持一致
            List<FileInfoRedisDTO> sortFileList = operateCaptureService.doSort(fileList, ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS());
            ecmBusiVersionDTO.setFileInfoRedisEntities(sortFileList);
//            ecmBusiVersionDTO.setFileInfoRedisEntities(fileList);
            beforeTime = ecmBusiVersionDTO.getCreateTime().getTime();
        }
        //排序
        ecmBusiVersionDTOS = ecmBusiVersionDTOS.stream().sorted(Comparator.comparing(EcmBusiVersionDTO::getCreateTime)).collect(Collectors.toList());
        return ecmBusiVersionDTOS;
    }

    /**
     * 影像文件历史
     */
    public Object getFileHistory(FileInfoVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        AssertUtils.isNull(vo.getFileId(), "参数错误");
//        String s = redisUtil.get(RedisConstants.BUSI_PREFIX + vo.getBusiId());
//        if (ObjectUtils.isEmpty(s)) {
//            return null;
//        }
//        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = JSONObject.parseObject(s, EcmBusiInfoRedisDTO.class);
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(vo.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return null;
        }
        //分组
        Map<Long, List<FileInfoRedisDTO>> groupedByFileId = fileInfoRedisEntities.stream().collect(Collectors.groupingBy(EcmFileInfoDTO::getFileId));
        List<FileInfoRedisDTO> fileInfoRedisEntities1 = groupedByFileId.get(vo.getFileId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities1)) {
            return null;
        }
        List<EcmFileHistory> fileHistories = fileInfoRedisEntities1.get(0).getFileHistories();
        if (CollectionUtils.isEmpty(fileHistories)) {
            return null;
        }
        List<EcmFileHistory> collect = fileHistories.stream().filter(f -> f.getFileId().equals(vo.getFileId())).collect(Collectors.toList());
        List<EcmFileHistoryDTO> fileHistoryExtends = PageCopyListUtils.copyListProperties(collect, EcmFileHistoryDTO.class);
        //根据时间排序 逆序
        fileHistoryExtends = fileHistoryExtends.stream().sorted(Comparator.comparing(EcmFileHistory::getCreateTime).reversed()).collect(Collectors.toList());
        //添加序号
        if(token.isOut()){
            Integer number = fileHistoryExtends.size();
            for (EcmFileHistoryDTO extend : fileHistoryExtends) {
                //添加创建人
                extend.setCreateUserName(extend.getCreateUser());
                extend.setNumber(number);
                number--;
            }
        }else{
            addUserName2(fileHistoryExtends);
        }
        return fileHistoryExtends;
    }

    /**
     * 文件还原
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#ecmFileHistoryDTO.busiId + '_' + #ecmFileHistoryDTO.fileId")
    @WebsocketNoticeAnnotation(busiId = "#ecmFileHistoryDTO.busiId")
    public void restoreFile(EcmFileHistoryDTO ecmFileHistoryDTO, AccountTokenExtendDTO token) {
        ecmFileHistoryDTO.setCurrentUserId(token.getUsername());
        //入参校验
        checkParam(ecmFileHistoryDTO);
        //获取还原文件信息
        SysFileDTO targetFileInfo = getTargetFileInfo(ecmFileHistoryDTO);
        //检验md5
        checkMd5(ecmFileHistoryDTO, targetFileInfo);
        //更新新旋转前文件插入文件历史记录表
        EcmFileHistory ecmFileHistory = commonService.insertFileHistory(ecmFileHistoryDTO.getBusiId(), ecmFileHistoryDTO.getFileId(),
                ecmFileHistoryDTO.getNewFileId(), IcmsConstants.REVERT_FILE_STRING +"<div class=\"dot\">"+ecmFileHistoryDTO.getNumber()+"</div>", ecmFileHistoryDTO.getCurrentUserId(),ecmFileHistoryDTO.getNewFileSize(),targetFileInfo.getExt());
        //持久化数据库逻辑
        restoreFileToDb(ecmFileHistoryDTO, targetFileInfo,token);
        //redis逻辑
        restoreFileToRedis(ecmFileHistoryDTO, targetFileInfo, ecmFileHistory);
        //处理智能化
        asyTask(ecmFileHistoryDTO,token);
    }

    private void checkMd5(EcmFileHistoryDTO ecmFileHistoryDTO, SysFileDTO targetFileInfo) {
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(ecmFileHistoryDTO.getBusiId());
        if (!CollectionUtils.isEmpty(fileInfoRedis)) {
            //要还原的文件信息
            List<FileInfoRedisDTO> restoreFileInfoList = fileInfoRedis.stream()
                    .filter(p -> p.getFileId().equals(ecmFileHistoryDTO.getFileId())).collect(Collectors.toList());
            AssertUtils.isNull(restoreFileInfoList, "参数错误");
            //得到节点id
            String docCode = restoreFileInfoList.get(0).getDocCode();
            if (!IcmsConstants.UNCLASSIFIED_ID.equals(docCode)) {
                List<String> fileMd5 = fileInfoRedis.stream()
                        .filter(p -> p.getDocCode().equals(docCode)
                                && StateConstants.ZERO.equals(p.getState()))
                        .map(FileInfoRedisDTO::getFileMd5).collect(Collectors.toList());
                AssertUtils.isTrue(fileMd5.contains(targetFileInfo.getFileMd5()),
                        "还原失败,节点中已存在要还原的文件");
            }
        }
    }


    private void asyTask(EcmFileHistoryDTO ecmFileHistoryDTO,AccountTokenExtendDTO token) {
        if (IcmsConstants.ADD_FILE_OPERATION_STRING.equals(ecmFileHistoryDTO.getFileOperation())
                ||IcmsConstants.REPLACE_FILE_STRING.equals(ecmFileHistoryDTO.getFileOperation())) {
            //判断是否是图片类型
            if (IMGS.contains(ecmFileHistoryDTO.getNewFileExt())) {
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmFileHistoryDTO.getBusiId());
                Integer treeType = ecmBusiInfoRedisDTO1.getTreeType();
                FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                        .getFileInfoRedisSingle(ecmFileHistoryDTO.getBusiId(), ecmFileHistoryDTO.getFileId());
        /*
            这里写配置检测,异步任务类型000000100九个字长,1位单证识别，2位自动转正，3模糊检测，4查重检测，5拆分合并，6位表示翻拍检测,7es,8反光,9缺角
            其中每位上 0表示无该类型，1表示处理中，2失败，3成功,对于模糊查重以及翻拍多2个状态，4表示排除异常，5表示确认异常
            如果开启了单证识别配置,则初始化taskType100000,把对应位置置为1即可
            */
                Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(fileInfoRedisSingle.getDocCode());
                //单证识别
                SysStrategyDTO vo1 = isAutoGroup(ecmBusiInfoRedisDTO1.getAppCode());
                String taskType = getTaskType(result, vo1);
                //如果未开启任何配置则不初始化
                if (!IcmsConstants.ASYNC_TASK_STATUS_INIT.equals(taskType)) {
                    //物理删除之前的数据
                    asyncTaskMapper.deleteBatchByFileId(Collections.singletonList(ecmFileHistoryDTO.getFileId()));
                    EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                    ecmAsyncTask.setTaskType(taskType);
                    ecmAsyncTask.setBusiId(ecmFileHistoryDTO.getBusiId());
                    ecmAsyncTask.setFileId(ecmFileHistoryDTO.getFileId());
                    asyncTaskService.insert(ecmAsyncTask);
                    //查重新增文本查重
//                judgeAfmText(ecmFileInfoDTO);
                    //MQ处理智能检测
                    checkDetectionByMq(fileInfoRedisSingle, ecmAsyncTask, taskType);
                }
            }
        }
    }

    /**
     * 上传文件检测处理-MQ
     */
    private void checkDetectionByMq(EcmFileInfoDTO ecmFileInfoDTO, EcmAsyncTask ecmAsyncTask, String taskType) {
        String exchange = mqConfig.getExchangeEcmIntelligent();
        // 定义类型和路由键的映射
        Map<Integer, String> typeRoutingKeyMap = new HashMap<>();
        typeRoutingKeyMap.put(IcmsConstants.TYPE_ONE, "docOcr");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_TWO, "regularize");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_THREE, "obscure");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_FOUR, "afm");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_SIX, "remake");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_SEVEN, "esContext");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_EIGHT, "reflective");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_NINE, "missCorner");


        char targetChar = EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0);
        // 检查转正和缺角是否同时开启
        boolean isTypeTwoActive = taskType.charAt(IcmsConstants.TYPE_TWO - 1) == targetChar;
        boolean isTypeNineActive = taskType.charAt(IcmsConstants.TYPE_NINE - 1) == targetChar;
        boolean useSpecialRouting = isTypeTwoActive && isTypeNineActive;

        // 遍历映射，检查并发送消息
        for (Map.Entry<Integer, String> entry : typeRoutingKeyMap.entrySet()) {
            int position = entry.getKey();
            String routingKey = entry.getValue();

            if (taskType.charAt(position - 1) == targetChar) {
                // 当转正和缺角同时开启时，使用special路由键
                String actualRoutingKey = (useSpecialRouting &&
                        (position == IcmsConstants.TYPE_TWO ||
                                position == IcmsConstants.TYPE_NINE)) ?
                        "special" : routingKey;

                if (position == IcmsConstants.TYPE_FOUR) {
                    // 查重10小时过期
                    rabbitMQProducer.sendMessageWithTTL(JSONObject.toJSONString(ecmFileInfoDTO),
                            exchange, actualRoutingKey, 1000 * 60 * 60 * 10);
                } else {
                    // 其余的1小时过期
                    rabbitMQProducer.sendMessageWithTTL(JSONObject.toJSONString(ecmFileInfoDTO),
                            exchange, actualRoutingKey, 1000 * 60 * 60);
                }
            }
        }
        // 修改异步任务状态为已存入队列
        taskType = taskType.replace(EcmCheckAsyncTaskEnum.PROCESSING.description(),
                EcmCheckAsyncTaskEnum.IN_MQ.description());
        ecmAsyncTask.setTaskType(taskType);
        asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
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

    /**
     * 根据开关状态获取taskType
     */
    private String getTaskType(Map<Integer, Boolean> result, SysStrategyDTO vo) {
        String taskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
        //单证识别
        //OCR识别开关
        if (vo.getOcrConfigStatus()) {
            taskType = updateStatus(taskType, IcmsConstants.TYPE_ONE,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
        }
        //判断是否开启翻拍检测
        if (!CollectionUtils.isEmpty(result)) {
            boolean isReShootEnabled = result.get(IcmsConstants.REMAKE);
            if (isReShootEnabled) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_SIX,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }
            //判断是否开启模糊检测
            boolean isObscure = result.get(IcmsConstants.OBSCURE);
            if (isObscure) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_THREE,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启了转正检测
            boolean isRegularize = result.get(IcmsConstants.REGULARIZE);
            if (isRegularize) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_TWO,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启查重检测
            boolean plagiarism = result.get(IcmsConstants.PLAGIARISM);
            if (plagiarism) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_FOUR,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启反光检测
            boolean reflective = result.get(IcmsConstants.REFLECTIVE);
            if (reflective) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启缺角检测
            boolean missCorner = result.get(IcmsConstants.MISS_CORNER);
            if (missCorner) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_NINE,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }
        }
        return taskType;
    }

    /**
     * 更新 RemakeStatus 指定位置的值
     * @param status   原始状态字符串
     * @param position 需要更新的位置
     * @param newValue
     * @return 更新后的状态字符串
     */
    private String updateStatus(String status, Integer position, char newValue) {
        if (position < 1 || position > IcmsConstants.LENGTH) {
            throw new IllegalArgumentException(
                    String.format("只能更新 1 到 %d 位", IcmsConstants.LENGTH)
            );
        }
        StringBuilder sb = new StringBuilder(status);
        sb.setCharAt(position - 1, newValue);
        return sb.toString();

    }

        /**
         * 还原操作
         */
    private void restoreFileToDb(EcmFileHistoryDTO ecmFileHistoryDTO, SysFileDTO targetFileInfo, AccountTokenExtendDTO token) {
        if (IcmsConstants.DELETE_FILE_STRING.equals(ecmFileHistoryDTO.getFileOperation())) {
            //还原已删除操作文件操作
            ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                    .set("state", IcmsConstants.ZERO)
                    .eq("file_id", ecmFileHistoryDTO.getFileId()));
        } else {
            //其他还原操作 重命名、旋转、压缩等
            ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                    .set("new_file_id", ecmFileHistoryDTO.getNewFileId())
                    .set("new_file_size",ecmFileHistoryDTO.getNewFileSize())
                    .set("new_file_name", targetFileInfo.getOriginalFilename())
                    .eq("file_id", ecmFileHistoryDTO.getFileId()));
        }
        busiOperationService.addOperation(ecmFileHistoryDTO.getBusiId(), IcmsConstants.REVERT_FILE, token, "修改文件-还原");
    }

    /**
     * 缓存更新
     */
    private void restoreFileToRedis(EcmFileHistoryDTO ecmFileHistoryDTO, SysFileDTO targetFileInfo, EcmFileHistory ecmFileHistory) {
//        String s = redisUtil.get(RedisConstants.BUSI_PREFIX + ecmFileHistoryDTO.getBusiId());
//        if (ObjectUtils.isEmpty(s)) {
//            return;
//        }
//        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = JSONObject.parseObject(s, EcmBusiInfoRedisDTO.class);
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(ecmFileHistoryDTO.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return;
        }
        if (IcmsConstants.DELETE_FILE_STRING.equals(ecmFileHistoryDTO.getFileOperation())) {
            //还原已删除操作文件操作
            fileInfoRedisEntities.stream().filter(p -> ecmFileHistoryDTO.getFileId().equals(p.getFileId())).forEach(p -> {
                //文件状态改为正常
                p.setState(IcmsConstants.ZERO);
                //添加还原文件历史
                List<EcmFileHistory> fileHistories = p.getFileHistories();
                fileHistories.add(ecmFileHistory);
                p.setFileHistories(fileHistories);
            });
        } else {
            //其他还原操作 重命名、旋转、压缩等
            for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisEntities) {
                if (fileInfoRedisDTO.getFileId().equals(ecmFileHistoryDTO.getFileId())) {
                    fileInfoRedisDTO.setNewFileId(ecmFileHistoryDTO.getNewFileId());
                    fileInfoRedisDTO.setNewFileName(targetFileInfo.getOriginalFilename());
                    fileInfoRedisDTO.setNewFileUrl(IcmsConstants.NEW_FILE_URL);
                    fileInfoRedisDTO.setSize(ecmFileHistoryDTO.getNewFileSize());
                    //批注数量重新计算
                    //获取文件批注数量
                    Long commentCount = ecmFileCommentMapper.selectCount(new LambdaQueryWrapper<EcmFileComment>()
                            .eq(EcmFileComment::getBusiId, ecmFileHistoryDTO.getBusiId())
                            .eq(EcmFileComment::getFileId, ecmFileHistoryDTO.getFileId())
                            .eq(EcmFileComment::getNewFileId, ecmFileHistoryDTO.getNewFileId()));
                    if (commentCount!=null) {
                        fileInfoRedisDTO.setFileCommentCount(commentCount.intValue());
                    }else{
                        fileInfoRedisDTO.setFileCommentCount(0);
                    }
                    //添加还原文件历史
                    List<EcmFileHistory> fileHistories = fileInfoRedisDTO.getFileHistories();
                    ArrayList<EcmFileHistory> ecmFileHistories = new ArrayList<>(fileHistories);
                    ecmFileHistories.add(ecmFileHistory);
                    fileInfoRedisDTO.setFileHistories(ecmFileHistories);
                }
            }
        }
        busiCacheService.updateFileInfoRedis(fileInfoRedisEntities);
//        ecmBusiInfoRedisDTO.setFileInfoRedisEntities(fileInfoRedisEntities);
//        //更新缓存
//        redisUtil.set(RedisConstants.BUSI_PREFIX + ecmFileHistoryDTO.getBusiId(), JSONObject.toJSONString(ecmBusiInfoRedisDTO), TimeOutConstants.ONE_DAY);
    }
    /**
     * 获取文件轨迹
     */
    private SysFileDTO getTargetFileInfo(EcmFileHistoryDTO ecmFileHistoryDTO) {
        List<Long> fileIds = new ArrayList<>();
        fileIds.add(ecmFileHistoryDTO.getNewFileId());
        Result<List<SysFileDTO>> details = fileHandleApi.details(fileIds);
        if (!details.isSucc()) {
            throw new SunyardException(details.getMsg());
        }
        List<SysFileDTO> fileDetails = details.getData();
        if (!CollectionUtils.isEmpty(fileDetails)) {
            return fileDetails.get(0);
        } else {
            return new SysFileDTO();
        }
    }

    /**
     * 校验参数
     */
    private void checkParam(EcmFileHistoryDTO ecmFileHistoryDTO) {
        AssertUtils.isNull(ecmFileHistoryDTO.getBusiId(), "参数错误");
        AssertUtils.isNull(ecmFileHistoryDTO.getFileId(), "参数错误");
        AssertUtils.isNull(ecmFileHistoryDTO.getNewFileId(), "参数错误");
        AssertUtils.isNull(ecmFileHistoryDTO.getFileOperation(), "参数错误");
    }

    /**
     * 添加创建人
     */
    private void addUserName2(List<EcmFileHistoryDTO> fileHistoryExtends) {
        Integer number = fileHistoryExtends.size();
        List<String> userIds = new ArrayList<>();
        for (EcmFileHistoryDTO extend : fileHistoryExtends) {
            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
                userIds.add(extend.getCreateUser());
            }
        }
        Map<String, List<SysUserDTO>> groupedByUserId = modelPermissionsService.getUserListByUserIds(userIds);
        for (EcmFileHistoryDTO extend : fileHistoryExtends) {
            extend.setNumber(number);
            number--;
            //添加创建人
            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
                if (!CollectionUtils.isEmpty(groupedByUserId.get(extend.getCreateUser()))) {
                    extend.setCreateUserName(groupedByUserId.get(extend.getCreateUser()).get(0).getName());
                } else {
                    extend.setCreateUserName(extend.getCreateUser());
                }
            }
        }
    }
    /**
     * 添加创建人
     */
    private void addUserName(List<EcmBusiVersionDTO> ecmBusiVersionDTOS) {
//        List<String> userIds = new ArrayList<>();
//        for (EcmBusiVersionDTO extend : ecmBusiVersionDTOS) {
//            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
//                userIds.add(extend.getCreateUser());
//            }
//        }
//        Map<String, List<SysUserDTO>> groupedByUserId = modelPermissionsService.getUserListByUserIds(userIds);
        for (EcmBusiVersionDTO extend : ecmBusiVersionDTOS) {
            //添加创建人
//            if (!ObjectUtils.isEmpty(extend.getCreateUser())) {
//                if (!CollectionUtils.isEmpty(groupedByUserId.get(extend.getCreateUser()))) {
                    extend.setCreateUserName(extend.getCreateUser());
                }
//            }
//        }
    }
}
