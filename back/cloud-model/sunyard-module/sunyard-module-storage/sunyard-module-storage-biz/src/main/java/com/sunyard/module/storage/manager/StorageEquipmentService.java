package com.sunyard.module.storage.manager;

import static com.sunyard.module.storage.config.factory.impl.FileStorageServiceBuilder.buildLocalFileStorage;
import static com.sunyard.module.storage.config.factory.impl.FileStorageServiceBuilder.buildSftpFileStorage;
import static com.sunyard.module.storage.config.factory.impl.FileStorageServiceBuilder.buildSoftWarnStorage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.storage.constant.RedisTopicConstant;
import com.sunyard.module.storage.dto.FileStorageEquipmentTopicDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FtpUtils;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.impl.FileStorageService;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.mapper.StEquipmentMapper;
import com.sunyard.module.storage.mapper.StFileMapper;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.vo.StEquipmentVO;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * 分片上传
 *
 * @author zyl
 * @Description
 * @since 2023/7/20 10:05
 */
@Service
@Slf4j
public class StorageEquipmentService {


    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private StEquipmentMapper stEquipmentMapper;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private StFileMapper stFileMapper;
    @Resource
    private LockTemplate lockTemplate;



    /**
     * 列表查询页
     * @param stEquipment 存储设备
     * @param page 分页参数
     * @return Result
     */
    public PageInfo query(StEquipmentVO stEquipment, PageForm page) {
        if (stEquipment.getStatusList().isEmpty()) {
            return new PageInfo<>();
        }
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<StEquipment> stEquipments = stEquipmentMapper
                .selectList(new LambdaQueryWrapper<StEquipment>()
                        .in(StEquipment::getStatus, stEquipment.getStatusList())
                        .eq(stEquipment.getStorageType() != null, StEquipment::getStorageType,
                                stEquipment.getStorageType())
                        .like(StringUtils.hasText(stEquipment.getEquipmentName()),
                                StEquipment::getEquipmentName, stEquipment.getEquipmentName())
                        .orderByDesc(StEquipment::getCreateTime));
        return new PageInfo<StEquipment>(stEquipments);
    }

    /**
     * 新增页
     * @param stEquipment 存储设备
     */
    public void add(StEquipment stEquipment) {
        AssertUtils.isNull(stEquipment.getEquipmentName(), "参数有误！");
        AssertUtils.isNull(stEquipment.getStorageType(), "参数有误！");
        AssertUtils.isNull(stEquipment.getDomainName(), "参数有误！");
        AssertUtils.isNull(stEquipment.getStatus(), "参数有误！");
        //本地存储的需要把base_path赋值到桶名
        if (0 == stEquipment.getStorageType()) {
            stEquipment.setBucket(stEquipment.getBasePath());
        }
        stEquipmentMapper.insert(stEquipment);
        List<StEquipment> list = new ArrayList<>();
        list.add(stEquipment);
        CopyOnWriteArrayList<FileStorage> old = fileStorageService.getFileStorageList();
        //更新及通知所有节点
        updateFileStorageListWithLockAndNotify(stEquipment.getStorageType(), list, old);
    }

    /**
     * 修改页
     * @param stEquipment 存储设备
     */
    public void update(StEquipment stEquipment) {
        AssertUtils.isNull(stEquipment.getId(), "参数有误！");
        StEquipment stEquipment1 = stEquipmentMapper.selectById(stEquipment.getId());
        //本地存储的需要把base_path赋值到桶名
        if (0 == stEquipment1.getStorageType()) {
            stEquipment.setBucket(stEquipment1.getBasePath());
        }
        //查询设备是否有文件,有的话不允许修改
        Long count = stFileMapper.selectCount(new LambdaQueryWrapper<StFile>().eq(StFile::getEquipmentId,stEquipment.getId()));
        AssertUtils.isTrue(count!=0, "该存储设备已有文件,无法编辑");
        stEquipmentMapper.updateById(stEquipment);
        //修改当前服务的设备信息
        CopyOnWriteArrayList<FileStorage> old = fileStorageService.getFileStorageList();
        // 删除平台 ID 等于给定值的 FileStorage
        old.removeIf(fileStorage -> fileStorage.getPlatform()
                .equals(Long.toString(stEquipment.getId())));
        List<StEquipment> list = new ArrayList<>();
        list.add(stEquipment);
        //更新及通知所有节点
        updateFileStorageListWithLockAndNotify(stEquipment.getStorageType(), list, old);
    }

    /**
     * 删除
     * @param stEquipment 存储设备
     */
    public void del(StEquipment stEquipment) {
        AssertUtils.isNull(stEquipment.getId(), "参数有误！");
        //查询设备是否有文件,有的话不允许修改
        Long count = stFileMapper.selectCount(new LambdaQueryWrapper<StFile>().eq(StFile::getEquipmentId,stEquipment.getId()));
        AssertUtils.isTrue(count!=0, "该存储设备已有文件,无法编辑");
        stEquipmentMapper.deleteById(stEquipment.getId());
    }

    /**
     * 获取信息
     * @param stEquipment 存储设备
     * @return Result
     */
    public StEquipment getInfo(StEquipment stEquipment) {
        AssertUtils.isNull(stEquipment.getId(), "参数有误！");
        return stEquipmentMapper.selectById(stEquipment.getId());
    }

    /**
     * 测试连接
     * @param vo 存储设备
     * @return Result
     */
    public Result testConnect(StEquipment vo) {
        if (StateConstants.COMMON_STORAGE_TYPE_LOCAL.equals(vo.getStorageType())) {
            //本地存储
            return localTest(vo);
        } else if (StateConstants.COMMON_STORAGE_TYPE_OBJ.equals(vo.getStorageType())) {
            //对象存储
            return objTest(vo);
        } else {
            return Result.error("服务器连接失败。", ResultCode.SYSTEM_BUSY_ERROR);
        }
    }

    /**
     * 本地测试连接
     *
     * @param stEquipment 设备id
     * @return Result
     */
    private Result localTest(StEquipment stEquipment) {
        try {
            ChannelSftp connect = FtpUtils.getConnect(stEquipment.getDomainName().split(":")[0],
                    Integer.parseInt(stEquipment.getDomainName().split(":")[1]),
                    stEquipment.getAccessKey(), stEquipment.getAccessSecret());
            if (!connect.isConnected()) {
                log.info("服务器连接失败。");
                return Result.error("连接出错，请核对相关配置", ResultCode.SYSTEM_BUSY_ERROR);
            }
            SftpATTRS lstat = connect.lstat(stEquipment.getBasePath());
            if (!lstat.isDir()) {
                connect.disconnect();
                log.info("服务器连接失败,指定路径不存在");
                return Result.error("指定路径不存在", ResultCode.SYSTEM_BUSY_ERROR);
            }
        } catch (Exception e) {
            log.error("服务器连接失败。", e);
            throw new RuntimeException(e);
        }
        log.info("服务器连接成功！");
        return Result.success("服务器连接成功！");
    }

    /**
     * 对象存储测试连接
     *
     * @param stEquipment 设备id
     * @return Result
     */
    private Result objTest(StEquipment stEquipment) {
        String endpoint = stEquipment.getStorageAddress();
        String accessKeyId = stEquipment.getAccessKey();
        String accessKeySecret = stEquipment.getAccessSecret();
        String bucketName = stEquipment.getBucket();
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, accessKeySecret);

        // 注意：对于非AWS标准服务（如MinIO等），你可能需要自定义endpoint和region
        S3Client s3Client = S3Client.builder().endpointOverride(URI.create(endpoint))
                .credentialsProvider(() -> credentials)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true)//启用路径模式
                        .chunkedEncodingEnabled(false)//启用块传输
                        .build())
                // 注意：如果你使用的是标准的AWS S3服务，你可能需要指定一个Region
                .region(Region.of(stEquipment.getDomainName())).build();

        try {
            // 使用HeadBucket来检查存储桶是否存在
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            // 如果代码执行到这里，说明存储桶存在
            return Result.success("");

        } catch (S3Exception e) {
            // 如果存储桶不存在，会抛出NoSuchBucketException
            if (e.awsErrorDetails().errorCode().equals("NoSuchBucket")) {
                throw new RuntimeException("存储桶不存在", e);
            } else {
                log.error("系统异常", e);
                throw new RuntimeException("存储桶不存在", e);
            }
        }
    }

    /**
     * 增加存储设备到服务
     *
     * @param storageType 设备类型
     * @param list 新增设备列表
     * @param fileStorageList 当前存储设备列表
     * @return Result
     */
    public CopyOnWriteArrayList<FileStorage> getFileStorageList(Integer storageType,
                                                                List<StEquipment> list,
                                                                CopyOnWriteArrayList<FileStorage> fileStorageList) {
        switch (storageType) {
            case 0:
                fileStorageList.addAll(buildLocalFileStorage(
                        list.stream().filter(p -> FileConstants.NAS.equals(p.getStorageType()))
                                .collect(Collectors.toList()),
                        applicationContext));
                break;
            case 1:
                fileStorageList.addAll(buildSoftWarnStorage(
                        list.stream().filter(p -> FileConstants.OSS.equals(p.getStorageType()))
                                .collect(Collectors.toList())));
                break;
            case 2:
                fileStorageList.addAll(buildSftpFileStorage(
                        list.stream().filter(p -> FileConstants.SFTP.equals(p.getStorageType()))
                                .collect(Collectors.toList()),
                        applicationContext));
                break;
            default:
                break;
        }
        return fileStorageList;
    }

    /**
     * 通用方法：加分布式锁更新本地缓存 + 发送Redis广播（仅发新列表）
     * 全量基于 LockTemplate 实现分布式锁，保证锁操作一致性
     * @param storageType 存储类型
     * @param stEquipmentList 新增/修改的设备列表
     * @param oldFileStorageList 旧的FileStorage列表
     * @return 新构建的FileStorage列表
     * @throws SunyardException 业务异常（锁获取失败/参数异常/执行中断等）
     */
    private CopyOnWriteArrayList<FileStorage> updateFileStorageListWithLockAndNotify(
            Integer storageType,
            List<StEquipment> stEquipmentList,
            CopyOnWriteArrayList<FileStorage> oldFileStorageList) {

        // 锁对象
        LockInfo lockInfo = null;

        try {
            int retryCount = RedisTopicConstant.LOCK_RETRY_TIMES;
            long retryInterval = RedisTopicConstant.LOCK_RETRY_INTERVAL;
            while (retryCount-- > 0 && lockInfo == null) {
                // 获取锁
                lockInfo = lockTemplate.lock(
                        RedisTopicConstant.FILE_STORAGE_LOCK,
                        RedisTopicConstant.LOCK_TIMEOUT_SECONDS * 1000L,
                        RedisTopicConstant.EXPIRE
                );

                if (lockInfo != null) {
                    log.info("成功获取分布式锁，剩余重试次数：{}，锁Key：{}，锁信息：{}",
                            retryCount, RedisTopicConstant.FILE_STORAGE_LOCK, lockInfo);
                    break;
                }

                log.warn("获取分布式锁失败，剩余重试次数：{}，锁Key：{}", retryCount, RedisTopicConstant.FILE_STORAGE_LOCK);
                // 重试间隔
                Thread.sleep(retryInterval);
            }

            // 锁获取失败
            if (lockInfo == null) {
                String errorMsg = String.format("获取分布式锁失败（重试%s次），更新FileStorage列表失败",
                        RedisTopicConstant.LOCK_RETRY_TIMES);
                log.error(errorMsg + "，锁Key：{}", RedisTopicConstant.FILE_STORAGE_LOCK);
                throw new SunyardException("更新存储配置失败，请稍后重试");
            }

            // 构建新的FileStorage列表
            CopyOnWriteArrayList<FileStorage> newFileStorageList = getFileStorageList(
                    storageType, stEquipmentList, oldFileStorageList);
            log.info("成功构建新FileStorage列表，列表大小：{}", newFileStorageList.size());

            // 更新
            fileStorageService.setFileStorageList(newFileStorageList);
            log.info("成功更新本地FileStorage缓存");

            // 发送Redis广播
            sendRedisBroadcast(storageType, stEquipmentList);

            return newFileStorageList;

        } catch (InterruptedException e) {
            // 线程中断：恢复中断状态 + 抛业务异常
            Thread.currentThread().interrupt();
            log.error("更新FileStorage列表时线程被中断，锁Key：{}", RedisTopicConstant.FILE_STORAGE_LOCK, e);
            throw new SunyardException("更新存储配置失败，操作被中断");
        } catch (Exception e) {
            // 其他异常：兜底日志 + 抛业务异常
            log.error("更新FileStorage列表异常，锁Key：{}，锁信息：{}",
                    RedisTopicConstant.FILE_STORAGE_LOCK, lockInfo, e);
            throw new SunyardException("更新存储配置失败：" + e.getMessage());
        } finally {
            // 5. 释放锁（基于LockInfo判空，传入LockInfo解锁，符合主流模板规范）
            if (lockInfo != null) {
                try {
                    lockTemplate.releaseLock(lockInfo);
                    log.info("成功释放分布式锁，锁Key：{}，锁信息：{}",
                            RedisTopicConstant.FILE_STORAGE_LOCK, lockInfo);
                } catch (Exception e) {
                    log.error("释放分布式锁失败，锁Key：{}，锁信息：{}",
                            RedisTopicConstant.FILE_STORAGE_LOCK, lockInfo, e);
                }
            }
        }
    }

    /**
     * 通用方法：发送Redis广播（仅发送新列表）
     */
    private void sendRedisBroadcast(Integer storageType, List<StEquipment> stEquipmentList) {
        try {
            FileStorageEquipmentTopicDTO message = new FileStorageEquipmentTopicDTO();
            message.setStEquipmentList(stEquipmentList);
            message.setStorageType(storageType);
            redisUtils.convertAndSend(RedisTopicConstant.FILE_STORAGE_EQUIPMENT_TOPIC, objectMapper.writeValueAsString(message));
            log.info("Redis广播发送成功，新列表大小：{}", stEquipmentList.size());
        } catch (Exception e) {
            log.error("存储设备更新发送Redis广播失败", e);
        }
    }


}
