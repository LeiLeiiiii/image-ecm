package com.sunyard.module.storage.config.factory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

import com.jcraft.jsch.ChannelSftp;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.FileStorageClientFactory;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.po.StEquipment;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zyl
 * @Description
 * @since 2024/3/19 9:45
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class FileStorageServiceBuilder {

    /**
     * 配置参数
     */
    private  List<StEquipment> stEquipments;
    /**
     * 存储平台
     */
    private List<FileStorage> fileStorageList = new ArrayList<>();

    public FileStorageServiceBuilder(List<StEquipment> stEquipments) {
        this.stEquipments = stEquipments;
    }

    /**
     * 创建一个 FileStorageService 的构造器
     * @param stEquipments 存储设备
     * @return FileStorageServiceBuilder
     */
    public static FileStorageServiceBuilder create(List<StEquipment> stEquipments) {
        return new FileStorageServiceBuilder(stEquipments);
    }

    /**
     * 创建
     */
    public FileStorageService build(ApplicationContext applicationContext) {
//        if (CollectionUtil.isEmpty(stEquipments)){
//            throw new SunyardException("stEquipments 不能为 null");
//        }
        // 初始化各个存储平台
        fileStorageList.addAll(buildLocalFileStorage(stEquipments.stream().filter(p-> FileConstants.NAS.equals(p.getStorageType())).collect(Collectors.toList()),applicationContext));
        fileStorageList.addAll(buildSoftWarnStorage(stEquipments.stream().filter(p-> FileConstants.OSS.equals(p.getStorageType())).collect(Collectors.toList())));
        fileStorageList.addAll(buildSftpFileStorage(stEquipments.stream().filter(p-> FileConstants.SFTP.equals(p.getStorageType())).collect(Collectors.toList()),applicationContext));
        // 本体
        FileStorageService service = new FileStorageService();
        service.setSelf(service);
        service.setFileStorageList(new CopyOnWriteArrayList<>(fileStorageList));

        return service;
    }

    /**
     * 根据配置文件创建本地文件存储平台
     * @param list 存储设备list
     * @return LocalFileStorage
     */
    public static List<LocalFileStorage> buildLocalFileStorage(List<StEquipment> list,ApplicationContext applicationContext) {
        if (CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        return list.stream()
                .map(config -> {
                    log.info("加载本地存储平台：{}", config.getId());
                    return new LocalFileStorage(config,applicationContext);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据配置文件创建又 Amazon S3 存储平台
     * @param list 存储设备list
     * @return AmazonS3FileStorage
     */
//    public static List<AmazonS3FileStorage> buildAmazonS3FileStorage(
//            List<StEquipment> list) {
//        if (CollUtil.isEmpty(list)){
//            return Collections.emptyList();
//        }
//        buildFileStorageDetect(list, "Amazon S3", "com.amazonaws.services.s3.AmazonS3");
//
//        return list.stream()
//                .map(config -> {
//                    log.info("加载 Amazon S3 存储平台：{}", config.getId());
//                    FileStorageClientFactory<AmazonS3> clientFactory = new AmazonS3FileStorageClientFactory(config);
//                    return new AmazonS3FileStorage(config, clientFactory);
//                })
//                .collect(Collectors.toList());
//    }

    /**
     * 根据配置文件创建又 Amazon S3 存储平台
     * @param list 存储设备list
     * @return AmazonS3FileStorage
     */
    public static List<SoftWarnS3FileStorage> buildSoftWarnStorage(
            List<StEquipment> list) {
        if (CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        buildFileStorageDetect(list, "s3", "software.amazon.awssdk.services.s3.S3Client");

        return list.stream()
                .map(config -> {
                    log.info("加载 Amazon S3 存储平台：{}", config.getId());
                    return new SoftWarnS3FileStorage(config);
                })
                .collect(Collectors.toList());
    }


    /**
     * 根据配置文件创建 SFTP 存储平台
     * @param list 存储设备
     * @return SftpFileStorage
     */
    public static List<SftpFileStorage> buildSftpFileStorage(
            List<StEquipment> list,ApplicationContext applicationContext) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        buildFileStorageDetect(
                list,
                "SFTP",
                "com.jcraft.jsch.ChannelSftp");
        return list.stream()
                .map(config -> {
                    log.info("加载 SFTP 存储平台：{}", config.getId());
                    FileStorageClientFactory<ChannelSftp> clientFactory = new SftpFileStorageClientFactory(config);
                    return new SftpFileStorage(config, clientFactory,applicationContext);
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断是否没有引入指定 Class
     * @param name 名字
     * @return boolean
     */
    public static boolean doesNotExistClass(String name) {
        try {
            Class.forName(name);
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    /**
     * 创建存储平台时的依赖检查
     * @param list list
     * @param platformName platformName
     * @param classNames classNames
     */
    public static void buildFileStorageDetect(List<?> list, String platformName, String... classNames) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (String className : classNames) {
            if (doesNotExistClass(className)) {
                throw new SunyardException(
                        "检测到【" + platformName + "】配置，但是没有找到对应的依赖类：【" + className
                                + "】，所以无法加载此存储平台！配置参考地址：https://x-file-storage.xuyanwu.cn/2.1.0/#/%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8");
            }
        }
    }

}
