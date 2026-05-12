package com.sunyard.ecm.service.mq;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.util.EsAsysUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author yzy
 * @desc
 * @since 2025/5/22
 */
@Slf4j
@Service
public class RabbitMQConsumer {

    // 锁获取时间超时为30秒
    private final Long acquireTimeout = 30 * 1000L;
    // 锁自动失效时间为10秒
    private final Long expire = 10 * 1000L;

    @Value("${storage.url:http://172.1.1.210:28083}")
    private String storageUrl;
    @Value("${spring.rabbitmq.environment:}")
    private String environment;

    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private LockTemplate lockTemplate;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private EsAsysUtils esAsysUtils;

    // 消费 ecm_task_docOcr 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_docOcr')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveDocOcrMessage(String message) {
        processMessage(message, Arrays.asList(IcmsConstants.TYPE_ONE,IcmsConstants.TYPE_TEN), (info, taskTypeOld) -> {
            SysStrategyDTO vo = fileInfoService.isAutoGroup(info.getAppCode());
            return fileInfoService.handleDocOcr(info, getToken(info), taskTypeOld, vo);
        }, getName(IcmsConstants.QUEUE_DOC_OCR));
    }

    // 消费 ecm_task_afm 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_afm')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveAfmMessage(String message) {
        processMessage(message, Collections.singletonList(IcmsConstants.TYPE_FOUR), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            return fileInfoService.handleAfm(info, taskTypeOld, fileUrl);
        }, getName(IcmsConstants.QUEUE_AFM));
    }

    // 消费 ecm_task_obscure 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_obscure')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveObscureMessage(String message) {
        processMessage(message, Collections.singletonList(IcmsConstants.TYPE_THREE), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            JSONObject requestBody = createRequestBody(fileUrl);
            return fileInfoService.obscureHandle(info.getFileId(), taskTypeOld, requestBody);
        }, getName(IcmsConstants.QUEUE_OBSCURE));
    }

    // 消费 ecm_task_regularize 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_regularize')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveRegularizeMessage(String message) {
        processMessage(message, Collections.singletonList(IcmsConstants.TYPE_TWO), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            JSONObject requestBody = createRequestBody(fileUrl);
            return fileInfoService.regularizeHandle(info.getFileId(), taskTypeOld, requestBody, info, getToken(info));
        }, getName(IcmsConstants.QUEUE_REGULARIZE));
    }

    // 消费 ecm_task_remake 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_remake')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveRemakeMessage(String message) {
        processMessage(message, Collections.singletonList(IcmsConstants.TYPE_SIX), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            JSONObject requestBody = createRequestBody(fileUrl);
            return fileInfoService.remakeHandle(info.getFileId(), taskTypeOld, requestBody);
        }, getName(IcmsConstants.QUEUE_REMAKE));
    }

    // 消费 ecm_task_esContext 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_esContext')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveEsContextMessage(String message) {
        processMessage(message, Arrays.asList(IcmsConstants.TYPE_SEVEN,IcmsConstants.TYPE_TEN), (info, taskTypeOld) ->{
                    String fileUrl = getFileUrl(info);
                    return esAsysUtils.updateEsImgContext(info, taskTypeOld,fileUrl,getToken(info));
        }, getName(IcmsConstants.QUEUE_ES_CONTEXT));
    }

    // 消费 ecm_task_reflective 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_reflective')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveReflectiveMessage(String message) {
        processMessage(message, Collections.singletonList(IcmsConstants.TYPE_EIGHT), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            JSONObject requestBody = createRequestBody(fileUrl);
            return fileInfoService.reflectiveHandle(info.getFileId(), taskTypeOld, requestBody);
        }, getName(IcmsConstants.QUEUE_REFLECTIVE));
    }

    // 消费 ecm_task_missCorner 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_missCorner')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveMissCornerMessage(String message) {
        processMessage(message, Collections.singletonList(IcmsConstants.TYPE_NINE), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            JSONObject requestBody = createRequestBody(fileUrl);
            return fileInfoService.missCornerHandle(info.getFileId(), taskTypeOld, requestBody);
        }, getName(IcmsConstants.QUEUE_MISS_CORNER));
    }

    // 消费 ecm_task_special 队列的消息
    @RabbitListener(
            queues = "#{@mqConfig.getName('ecm_task_special')}",
            concurrency = "${spring.rabbitmq.theadNumber:3-5}")
    public void receiveSpecialMessage(String message) {
        processMessage(message, Arrays.asList(IcmsConstants.TYPE_TWO,IcmsConstants.TYPE_NINE), (info, taskTypeOld) -> {
            String fileUrl = getFileUrl(info);
            JSONObject requestBody = createRequestBody(fileUrl);
            return fileInfoService.specialHandle(info.getFileId(), taskTypeOld, requestBody, info, getToken(info));
        }, getName(IcmsConstants.QUEUE_SPECIAL));
    }

    private void processMessage(String message, List<Integer> positions, BiFunction<EcmFileInfoDTO, String, String> processor, String queueName) {
        LockInfo lockInfo = null;
        try {
            log.info("消费{}：{}", queueName,message);
            EcmFileInfoDTO ecmFileInfoDTO = JSONObject.parseObject(message, EcmFileInfoDTO.class);
            log.info("消费{}队列消息，业务ID: {}, 文件ID: {}", queueName, ecmFileInfoDTO.getBusiId(), ecmFileInfoDTO.getFileId());
            //设置处理中需要推送
            busiCacheService.setNeedPushBusiSync(
                    RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId(),
                    IcmsConstants.DETECTING, TimeOutConstants.ONE_HOURS);
            String key = RedisConstants.BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId();
            Long fileId = ecmFileInfoDTO.getFileId();
            EcmAsyncTask ecmAsyncTask =new EcmAsyncTask();
            // 加锁
            lockInfo = lockTemplate.lock(key + fileId, expire, acquireTimeout);
            if (lockInfo != null) {
                //修改异步任务为处理中
                ecmAsyncTask = busiCacheService.getEcmAsyncTask(key, fileId.toString());
                String taskType = ecmAsyncTask.getTaskType();
                taskType=updateStatus(taskType,positions,EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
                ecmAsyncTask.setTaskType(taskType);
                asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
                //修改完毕释放锁
                lockTemplate.releaseLock(lockInfo);
            }
            ecmAsyncTask=busiCacheService.getEcmAsyncTask(key, fileId.toString());
            String taskTypeOld = ecmAsyncTask.getTaskType();

            taskTypeOld = processor.apply(ecmFileInfoDTO, taskTypeOld);

            // 加锁
            lockInfo = lockTemplate.lock(key + fileId, expire, acquireTimeout);
            if (lockInfo != null) {
                // 获取最新的taskType
                ecmAsyncTask = busiCacheService.getEcmAsyncTask(key, fileId.toString());
                String taskTypeNew = ecmAsyncTask.getTaskType();
                taskTypeNew = getNewTaskType(taskTypeOld, taskTypeNew, positions);
                //如果有某一位的值是2 则代表智能化处理需要重试 isFail置为失败
                if (taskTypeNew.contains(String.valueOf(IcmsConstants.TWO))){
                    ecmAsyncTask.setIsFail(IcmsConstants.TWO);
                }else {
                    ecmAsyncTask.setIsFail(IcmsConstants.ONE);
                }
                ecmAsyncTask.setTaskType(taskTypeNew);
                ecmAsyncTask.setIsCompensate(IcmsConstants.ZERO);
                asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
                //处理完成
                busiCacheService.setNeedPushBusiSync(
                        RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId(),
                        IcmsConstants.DETECTION_COMPLETE, TimeOutConstants.ONE_HOURS);
            }
        } catch (Exception e) {
            log.error("消费{}队列异常,消息内容: {}", queueName, message, e);
            try {
                EcmFileInfoDTO ecmFileInfoDTO = JSONObject.parseObject(message, EcmFileInfoDTO.class);
                String key = RedisConstants.BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId();
                Long fileId = ecmFileInfoDTO.getFileId();
                // 加锁
                lockInfo = lockTemplate.lock(key + fileId, expire, acquireTimeout);
                if (lockInfo != null) {
                    EcmAsyncTask ecmAsyncTask = busiCacheService.getEcmAsyncTask(key, fileId.toString());
                    String taskTypeOld = ecmAsyncTask.getTaskType();
                    String taskTypeNew = getNewTaskTypeFail(taskTypeOld, ecmAsyncTask.getTaskType(), positions);
                    ecmAsyncTask.setTaskType(taskTypeNew);
                    //智能化处理失败直接置为2
                    ecmAsyncTask.setIsFail(IcmsConstants.TWO);
                    asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);

                    // 处理完成
                    busiCacheService.setNeedPushBusiSync(
                            RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId(),
                            IcmsConstants.DETECTION_COMPLETE,
                            TimeOutConstants.ONE_HOURS
                    );
                }
            } catch (Exception ex) {
                log.error("消费{}队列异常处理时加锁更新数据库失败", queueName, ex);
            } finally {
                lockTemplate.releaseLock(lockInfo);
            }
        } finally {
            lockTemplate.releaseLock(lockInfo);
        }
    }

    private String getFileUrl(EcmFileInfoDTO ecmFileInfoDTO) {
        return storageUrl + "/storage/deal/getFileByFileId?fileId=" + ecmFileInfoDTO.getNewFileId();
    }

    private JSONObject createRequestBody(String fileUrl) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("file_url", fileUrl);
        requestBody.put("threshold", 10000);
        return requestBody;
    }

    private AccountTokenExtendDTO getToken(EcmFileInfoDTO ecmFileInfoDTO) {
        AccountTokenExtendDTO loginToken = new AccountTokenExtendDTO();
        loginToken.setName(ecmFileInfoDTO.getCreateUserName());
        loginToken.setUsername(ecmFileInfoDTO.getCreateUser());
        return loginToken;
    }

    private String getNewTaskType(String taskTypeOld, String taskTypeNew, List<Integer> positions) {
        // 获取处理的后的值
        StringBuilder sb = new StringBuilder(taskTypeNew);
        positions.forEach(position->{
            char result = taskTypeOld.charAt(position - 1);
            sb.setCharAt(position - 1, result);
        });
        return sb.toString();
    }

    private String getNewTaskTypeFail(String taskTypeOld, String taskTypeNew, List<Integer> positions) {
        StringBuilder sb = new StringBuilder(taskTypeNew);
        positions.forEach(position->{
            sb.setCharAt(position - 1, EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        });
        return sb.toString();
    }

    public String getName(String baseName) {
        if (StringUtils.hasText(environment)) {
            return environment + "_" + baseName;
        }
        return baseName;
    }

    /**
     * 更新 RemakeStatus 指定位置的值
     * @param status   原始状态字符串
     * @param positions 需要更新的位置
     * @param newValue
     * @return 更新后的状态字符串
     */
    private String updateStatus(String status, List<Integer> positions, char newValue) {
        StringBuilder sb = new StringBuilder(status);
        positions.forEach(position->{
            if (position < 1 || position > IcmsConstants.LENGTH) {
                throw new IllegalArgumentException(
                        String.format("只能更新 1 到 %d 位", IcmsConstants.LENGTH)
                );
            }
            if (sb.charAt(position -1) == EcmCheckAsyncTaskEnum.IN_MQ
                    .description().charAt(0)){
                sb.setCharAt(position - 1, newValue);
            }
        });
        return sb.toString();
    }

}