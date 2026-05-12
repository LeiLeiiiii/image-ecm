package com.sunyard.module.storage.config;

import com.sunyard.module.storage.config.factory.impl.FileStorageService;
import com.sunyard.module.storage.config.factory.impl.FileStorageServiceBuilder;
import com.sunyard.module.storage.mapper.StEquipmentMapper;
import com.sunyard.module.storage.po.StEquipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;


/**
 * 创建AmazonS3客户端
 *
 * @author zyl
 * @Desc
 * @since 2023/6/14 11:06
 */
@Slf4j
@Configuration
@ConditionalOnMissingBean(FileStorageService.class)
public class StorageConfig {

    @Resource
    private StEquipmentMapper stEquipmentMapper;

    @Resource
    private  ApplicationContext applicationContext;

    /**
     * 初始化存储对象
     * @return FileStorageService
     */
    @Bean
    public FileStorageService fileStorageService() {
        //初始化文件存储服务
        //从数据库获取存储设备新形象
        List<StEquipment> stEquipments = stEquipmentMapper.selectList(null);
        FileStorageServiceBuilder builder = FileStorageServiceBuilder.create(stEquipments);
        return builder.build(applicationContext);
    }

}
