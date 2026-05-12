package com.sunyard.mytool.config;

import com.sunyard.mytool.constant.RedisKeyConstant;
import com.sunyard.mytool.dto.EcmAppDefDto;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.entity.ecm.EcmAppAttr;
import com.sunyard.mytool.entity.ecm.EcmAppDef;
import com.sunyard.mytool.entity.ecm.EcmDocDef;
import com.sunyard.mytool.entity.ecm.EcmSysLabel;
import com.sunyard.mytool.service.st.StEquipmentService;
import com.sunyard.mytool.service.ecm.EcmAppAttrService;
import com.sunyard.mytool.service.ecm.EcmAppDefService;
import com.sunyard.mytool.service.ecm.EcmDocDefService;
import com.sunyard.mytool.service.ecm.EcmSysLabelService;
import com.sunyard.mytool.service.file.impl.FileStroageServiceManager;
import com.sunyard.mytool.until.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 初始化config
 */
@Configuration
public class InitConfig {
    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private StEquipmentService stEquipmentService;

    @Autowired
    private EcmDocDefService ecmDocDefService;
    @Autowired
    private EcmAppDefService ecmAppDefService;
    @Autowired
    private EcmAppAttrService ecmAppAttrService;
    @Resource
    private RedisUtil redisUtil;
    @Autowired
    private EcmSysLabelService ecmSysLabelService;
    @Bean
    public void s3Client() {
        long start = System.currentTimeMillis();
        FileStroageServiceManager fileStroageServiceManager = new FileStroageServiceManager();
        List<StEquipment> allStEquipments = stEquipmentService.findAll();
        List<StEquipment> collect1 = allStEquipments.stream().filter(s -> s.getIsDeleted().equals(0)).collect(Collectors.toList());
        //文件上传初始化（s3）
        fileStroageServiceManager.initStorageServices(collect1);
        //基础业务初始化
        //缓存业务类型信息
        handleAppCodeRedisData();
        //缓存资料类型信息
        handleDocCodeRedisData();
        //缓存业务属性信息
        handleMetaDataRedisData();
        //缓存标签信息
        handleLabelRedisData();
        logger.info("*初始化基础信息完成*耗时:{}",  System.currentTimeMillis() - start);
    }

    //资料类型信息存入redis中
    private void handleDocCodeRedisData() {
        List<EcmDocDef> allDoc = ecmDocDefService.findAll();
        Map<String, List<EcmDocDef>> docCollect = allDoc.stream().collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        Map<String, Object> map = new HashMap();
        for (String docCode:docCollect.keySet()) {
            List<EcmDocDef> ecmDocDefList = docCollect.get(docCode);
            EcmDocDef ecmDocDef = ecmDocDefList.get(0);
            map.put(docCode, ecmDocDef);
        }
        redisUtil.hmset(RedisKeyConstant.REDIS_DOC_DEF,map);
    }

    //业务类型信息存入redis中
    private void handleAppCodeRedisData() {
        List<EcmAppDef> allAppdef = ecmAppDefService.findAll();
        Map<String, List<EcmAppDef>> appDefCollect = allAppdef.stream().collect(Collectors.groupingBy(EcmAppDef::getAppCode));

        //设备---考虑到后期可能会便，这边可以通过每日刷新或者别的方式，更新设备信息
        List<StEquipment> allEquipments = stEquipmentService.findAll();
        Map<Long, List<StEquipment>> stEcollect = allEquipments.stream().collect(Collectors.groupingBy(StEquipment::getId));
        Map<String, Object> map = new HashMap();
        for (String s:appDefCollect.keySet()) {
            List<EcmAppDef> ecmAppDefEntities = appDefCollect.get(s);
            EcmAppDefDto dto = new EcmAppDefDto();
            BeanUtils.copyProperties(ecmAppDefEntities.get(0),dto);
            List<StEquipment> stEquipmentEntities = stEcollect.get(dto.getEquipmentId());
            if (!CollectionUtils.isEmpty(stEquipmentEntities)) {
                dto.setStEquipment(stEquipmentEntities.get(0));
            }
            map.put(s,dto);
        }

        redisUtil.hmset(RedisKeyConstant.REDIS_APP_DEF,map);
    }

    //业务属性信息存入redis中
    private void handleMetaDataRedisData() {
        List<EcmAppAttr> allAppAttr = ecmAppAttrService.findAll();
        //根据appCode和attrCode进行分组 key为appCode:attrCode
        Map<String, List<EcmAppAttr>> appAttrCollect = allAppAttr.stream().collect(
                Collectors.groupingBy(e -> e.getAppCode() + ":" + e.getAttrCode())
        );
        Map<String, Object> map = new HashMap();
        for (String s:appAttrCollect.keySet()) {
            List<EcmAppAttr> ecmAppAttrEntities = appAttrCollect.get(s);
            EcmAppAttr ecmAppAttr = ecmAppAttrEntities.get(0);
            map.put(s, ecmAppAttr);
        }
        redisUtil.hmset(RedisKeyConstant.REDIS_APP_ATTR,map);
    }

    //系统标签信息存入redis中
    private void handleLabelRedisData() {
        List<EcmSysLabel> allSysLabel = ecmSysLabelService.findAll();
        //key为labelName
        Map<String, List<EcmSysLabel>> sysLabelCollect = allSysLabel.stream().collect(
                Collectors.groupingBy(EcmSysLabel::getLabelName)
        );
        Map<String, Object> map = new HashMap();
        for (String s:sysLabelCollect.keySet()) {
            List<EcmSysLabel> ecmSysLabelEntities = sysLabelCollect.get(s);
            EcmSysLabel ecmSysLabel = ecmSysLabelEntities.get(0);
            map.put(s, ecmSysLabel);
        }
        redisUtil.hmset(RedisKeyConstant.REDIS_SYS_LABEL,map);
    }
}
