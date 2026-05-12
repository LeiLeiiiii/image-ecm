package com.sunyard.ecm.oldToNew.config;

import com.sunyard.ecm.po.Grant;
import com.sunyard.ecm.oldToNew.mapper.BGrantTableMapper;
import com.sunyard.ecm.oldToNew.po.BGrantTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;

@Slf4j
@Configuration
public class GrantConfig {

    @Autowired
    private BGrantTableMapper bGrantTableMapper;

    @Bean
    public Grant grant() {
        // 从数据库中查询授权配置
//        BGrantTable bGrantTable = bGrantTableMapper.selectOne(null);
        Grant grant = new Grant();

//        try {
//            Blob grantService = new SerialBlob(bGrantTable.getGrantService());
//            Blob grantAccess = new SerialBlob(bGrantTable.getGrantAccess());
//            grant.setGrantService(grantService);
//            grant.setGrantAccess(grantAccess);
//        } catch (Exception e) {
//            log.error("初始化老影像授权文件转Blob异常",e);
//        }
//
//        grant.setId(bGrantTable.getId());
        return grant;
    }
}
