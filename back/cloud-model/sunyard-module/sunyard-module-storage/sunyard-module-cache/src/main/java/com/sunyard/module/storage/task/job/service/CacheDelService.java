package com.sunyard.module.storage.task.job.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.constant.SunCacheDelConstants;
import com.sunyard.module.storage.util.CacheFileUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author RAO
 */
@Slf4j
@Service
public class CacheDelService {
    @Resource
    private StorageUploadProperties storageUploadProperties;
    @Resource
    private ParamApi paramApi;

    /**
     * 清理缓存数据
     */
    public void delCache() {
        // 默认10天
        int maxDayThreshold = 10;
        Result<SysParamDTO> sysParamDtoResult1 = paramApi
                .searchValueByKey(SunCacheDelConstants.CACHE_CLEAR_DAY_THRESHOLD);
        // 优先获取配置中缓存保存最长时间
        if (sysParamDtoResult1.isSucc() && sysParamDtoResult1.getData() != null
                && sysParamDtoResult1.getData().getValue() != null) {
            maxDayThreshold = Integer.parseInt(sysParamDtoResult1.getData().getValue());
            log.info("缓存保存时间:{}天", maxDayThreshold);
        }
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取前N天日期
        LocalDate old = today.minusDays(maxDayThreshold);
        old.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        // 清理操作
        String ipAddress = CacheFileUtils.getIpAddress();
        if (storageUploadProperties.getSftpHost().equals(ipAddress) || !storageUploadProperties.getSftpEnable()) {
            // 清楚本地缓存
            this.delLocalCache();
        } else {
            // 清除远程服务器上的缓存
            // Connection conn = null;
            // conn = cacheFileUtils.pool.getConnection();
            // sftpConnectionService.getFileProperties(conn, sftpDirectory, timestamp, 0);
        }
    }

    /**
     * 清除存储服务本地缓存数据
     */
    public void delLocalCache() {
//        判断是否超过清理大小的阈值，如果不超过则不进行清理
        if (!isCacheSizeExceedThreshold()){
            log.info("还未达到清理阈值，跳过此次清理定时任务！");
            return;
        }

        // 默认10天
        int maxDayThreshold = 10;
        Result<SysParamDTO> sysParamDtoResult1 = paramApi
                .searchValueByKey(SunCacheDelConstants.CACHE_CLEAR_DAY_THRESHOLD);
        // 优先获取配置中缓存保存最长时间
        if (sysParamDtoResult1.isSucc() && sysParamDtoResult1.getData() != null
                && sysParamDtoResult1.getData().getValue() != null) {
            maxDayThreshold = Integer.parseInt(sysParamDtoResult1.getData().getValue());
            log.info("缓存保存时间:{}天", maxDayThreshold);
        }
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取前N天日期
        LocalDate old = today.minusDays(maxDayThreshold);
        log.info("缓存保存时间：{}", maxDayThreshold);
        long timestamp = old.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        // 清理操作
        File directory = new File(storageUploadProperties.getSftpDirectory());
        if (!directory.exists() || !directory.isDirectory()) {
            log.info("提供的路径不是有效的目录");
            return;
        }

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            List<Path> filesModifiedYesterday = paths.filter(Files::isRegularFile).filter(path -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(path,
                            BasicFileAttributes.class);
                    long millis = attrs.lastModifiedTime().toMillis();
                    return millis < timestamp;
                } catch (IOException e) {
                    log.error("{}读取文件属性时出错:{}", path, e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            log.info("要删除的文件集合:{}", filesModifiedYesterday);
            for (Path file : filesModifiedYesterday) {
                Files.delete(file);
                log.info("删除文件：{}", file.getFileName());
            }
        } catch (IOException e) {
            log.error("An error occurred while walking the directory tree: {}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断缓存目录总大小是否超过指定阈值（单位：GB）
     * @return true 如果当前总大小 > thresholdInGB，否则 false
     */
    public boolean isCacheSizeExceedThreshold() {
        //默认1G大小
        int thresholdInGB = 1;
        Result<SysParamDTO> sysParamDtoResult1 = paramApi
                .searchValueByKey(SunCacheDelConstants.CACHE_CLEAR_FILEMAX_THRESHOLD);
        // 优先获取配置中缓存保存最长时间
        if (sysParamDtoResult1.isSucc() && sysParamDtoResult1.getData() != null
                && sysParamDtoResult1.getData().getValue() != null) {
            thresholdInGB = Integer.parseInt(sysParamDtoResult1.getData().getValue());
            log.info("缓存清理阈值:{}G", thresholdInGB);
        }
        File dir = new File(storageUploadProperties.getSftpDirectory());
        if (!dir.exists() || !dir.isDirectory()) {
            // 目录不存在或不是目录，视为未超阈值
            return false;
        }

        try {
            long totalBytes = getTotalDirectorySize(dir.toPath());
            log.info("当前缓存目录总大小: {} bytes", totalBytes);
            long thresholdBytes = thresholdInGB * 1024L * 1024L * 1024L; // 转为字节

            return totalBytes > thresholdBytes;
        } catch (IOException e) {
            log.error("统计缓存目录大小失败，跳过清理判断", e);
            return false;
        }
    }

    /**
     * 递归计算目录下所有普通文件的总大小（字节）
     */
    private long getTotalDirectorySize(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            log.error("无法获取文件大小，跳过: {}", path, e);
                            return 0L;
                        }
                    })
                    .sum();
        }
    }
}
