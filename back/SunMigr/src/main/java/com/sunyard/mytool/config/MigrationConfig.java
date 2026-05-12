package com.sunyard.mytool.config;


import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.service.file.FileStroageService;
import com.sunyard.mytool.until.ThreadPoolUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;


/**
 * 迁移功能务相关配置初始化类
 */
@Configuration
public class MigrationConfig {

    //存储服务初始化
    public static HashMap<Long, FileStroageService> fileStroageMap = new HashMap<>();

    //存储设备初始化
    public static HashMap<Long, StEquipment> equipmentMap = new HashMap<>();

    //文档数据迁移线程池参数
    @Value("${edm-migration.thread-pool.migrate.core-pool-size}")
    Integer EdmMigrateCorePoolSize;
    @Value("${edm-migration.thread-pool.migrate.max-pool-size}")
    Integer EdmMigrateMaxPoolSize;
    @Value("${edm-migration.thread-pool.migrate.queue-capacity}")
    Integer EdmMigrateQueueCapacity;
    @Value("${edm-migration.thread-pool.migrate.keep-alive-seconds}")
    Integer EdmMigrateKeepAliveSeconds;


    //影像数据迁移线程池参数
    @Value("${ecm-migration.thread-pool.migrate.core-pool-size}")
    Integer EcmMigrateCorePoolSize;
    @Value("${ecm-migration.thread-pool.migrate.max-pool-size}")
    Integer EcmMigrateMaxPoolSize;
    @Value("${ecm-migration.thread-pool.migrate.queue-capacity}")
    Integer EcmMigrateQueueCapacity;
    @Value("${ecm-migration.thread-pool.migrate.keep-alive-seconds}")
    Integer EcmMigrateKeepAliveSeconds;

    //es线程池参数
    @Value("${ecm-migration.thread-pool.es.core-pool-size}")
    Integer esCorePoolSize;
    @Value("${ecm-migration.thread-pool.es.max-pool-size}")
    Integer esMaxPoolSize;
    @Value("${ecm-migration.thread-pool.es.queue-capacity}")
    Integer esQueueCapacity;
    @Value("${ecm-migration.thread-pool.es.keep-alive-seconds}")
    Integer esKeepAliveSeconds;

    //文件上传线程池参数
    @Value("${ecm-migration.thread-pool.upload.core-pool-size}")
    Integer migrateUploadCorePoolSize;
    @Value("${ecm-migration.thread-pool.upload.max-pool-size}")
    Integer migrateUploadMaxPoolSize;
    @Value("${ecm-migration.thread-pool.upload.queue-capacity}")
    Integer migrateUploadQueueCapacity;
    @Value("${ecm-migration.thread-pool.upload.keep-alive-seconds}")
    Integer migrateUploadKeepAliveSeconds;


    /**
     * 文档迁移线程池
     */
    @Bean(name = "EdmMigrateExecutor")
    public ThreadPoolTaskExecutor getEDMMigrateExecutor() {
        return ThreadPoolUtil.getThreadPoolTaskExecutor(EdmMigrateCorePoolSize, EdmMigrateMaxPoolSize, EdmMigrateQueueCapacity, "EDM-migrateExecutor-", EdmMigrateKeepAliveSeconds);
    }

    /**
     * 影像迁移线程池
     */
    @Bean(name = "EcmMigrateExecutor")
    public ThreadPoolTaskExecutor getECMMigrateExecutor() {
        return ThreadPoolUtil.getThreadPoolTaskExecutor(EcmMigrateCorePoolSize, EcmMigrateMaxPoolSize, EcmMigrateQueueCapacity, "ECM-migrateExecutor-", EcmMigrateKeepAliveSeconds);
    }

    /**
     * 影像es线程池
     */
    @Bean("EsExecutor")
    public ThreadPoolTaskExecutor getEsExecutor() {
        return ThreadPoolUtil.getThreadPoolTaskExecutor(esCorePoolSize, esMaxPoolSize, esQueueCapacity, "ECM-EsExecutor-", esKeepAliveSeconds);
    }

    /**
     * 文件上传线程池
     */
    @Bean(name = "uploadExecutor")
    public ThreadPoolTaskExecutor uploadExecutor() {
        return ThreadPoolUtil.getThreadPoolTaskExecutor(migrateUploadCorePoolSize, migrateUploadMaxPoolSize, migrateUploadQueueCapacity, "migrateUploadExecutor-", migrateUploadKeepAliveSeconds);
    }
}
