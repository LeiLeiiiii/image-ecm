package com.sunyard.mytool.service.file.impl;


import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.service.file.FileStroageService;
import com.sunyard.mytool.service.st.StEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileStroageServiceManager {
    @Autowired
    private StEquipmentService stEquipmentService;

    // 存储服务实例的map，key是设备id，value是对应的FileStroageService实例
    private Map<Long, FileStroageService> FileStroageServiceMap = new HashMap<>();

    // 存储服务实例的map，key是bucket，value是对应的FileStroageService实例
    private Map<String, FileStroageService> bucketFileStroageServiceMap = new HashMap<>();

    // 存储服务实例的map，key是设备id，value是对应的StEquipment实例
    private Map<Long, StEquipment> stEquipmentMap = new HashMap<>();

    // 初始化方法，在项目启动时调用
    public void initStorageServices(List<StEquipment> stEquipments) {
        if (CollectionUtils.isEmpty(stEquipments)) {
            return;
        }
        for (StEquipment stEquip : stEquipments) {
            FileStroageService service = createStorageService(stEquip);
            FileStroageServiceMap.put(stEquip.getId(), service);
        }
        for (StEquipment stEquip : stEquipments) {
            FileStroageService service = createStorageService(stEquip);
            bucketFileStroageServiceMap.put(stEquip.getBucket(), service);
        }
    }

    public FileStroageService initStorageServices(StEquipment stEquip) {
        FileStroageService service = createStorageService(stEquip);
        FileStroageServiceMap.put(stEquip.getId(), service);
        return service;
    }

    public FileStroageService initStorageServicesbyBucket(String bucket) {
        StEquipment stEquip = stEquipmentService.findByBucket(bucket);
        FileStroageService service = createStorageService(stEquip);
        bucketFileStroageServiceMap.put(bucket, service);
        return service;
    }


    // 辅助方法，根据类型创建LogService实例
    private FileStroageService createStorageService(StEquipment stEquip) {
        switch (stEquip.getStorageType()) {
            case 0:
                return new LocalFileStroageServiceImpl(stEquip);
            case 1:
                return new ObjFileStroageServiceImpl(stEquip);
            default:
                throw new IllegalArgumentException("Unsupported log service type: " + stEquip);
        }
    }

    public FileStroageService getFileStroage(StEquipment stEquip) {
        FileStroageService fileStroageService = FileStroageServiceMap.get(stEquip.getId());
        if (fileStroageService == null) {
            fileStroageService = initStorageServices(stEquip);
        }
        return fileStroageService;
    }

    public FileStroageService getFileStroagebyBucket(String bucket) {
        FileStroageService fileStroageService = bucketFileStroageServiceMap.get(bucket);
        if (fileStroageService == null) {
            fileStroageService = initStorageServicesbyBucket(bucket);
        }
        return fileStroageService;
    }

    public StEquipment getStEquipment(Long id) {
        StEquipment stEquipment = stEquipmentMap.get(id);
        if (stEquipment == null) {
            stEquipment = stEquipmentService.findById(id);
            if (stEquipment == null){
                throw new RuntimeException("不存在的设备id: " + id);
            }
            stEquipmentMap.put(stEquipment.getId(), stEquipment);
        }
        return stEquipment;
    }


}
