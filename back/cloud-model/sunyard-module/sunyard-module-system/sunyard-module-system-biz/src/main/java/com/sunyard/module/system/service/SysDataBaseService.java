package com.sunyard.module.system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.monitor.constant.DatabaseDocConstants;
import com.sunyard.framework.monitor.enums.DatabaseDocEnum;
import com.sunyard.framework.monitor.enums.DatabaseDocReportTypeEnum;
import com.sunyard.framework.monitor.screw.util.DatabaseDocUtils;
import com.sunyard.framework.redis.util.RedisUtils;

import cn.smallbun.screw.core.engine.EngineFileType;

/**
 * @Author 文档导出类型
 * @time 2023/5/22 20:22
 **/
@Service
public class SysDataBaseService {
    @Resource
    private RedisUtils redisUtils;

    /**
     * 查询数据库
     * @param response 响应头
     * @param systemType 系统类别
     * @param fileType 文件类别
     */
    public void search(HttpServletResponse response, Integer systemType, Integer fileType) {
        List<String> list = new ArrayList<>();
        list.add("act");
        list.add("ACT");
        list.add("flw");
        EngineFileType type = DatabaseDocReportTypeEnum.getEnum(fileType.toString()).getName();
        DatabaseDocUtils.setIgnorePrefix(list);
        String databaseDocInfo = redisUtils.get(DatabaseDocConstants.CACHE_NAME
                + DatabaseDocEnum.getEnum(systemType + "").getName());
        if (!ObjectUtils.isEmpty(databaseDocInfo)) {
            Map infoMap = JSONObject.parseObject(databaseDocInfo, Map.class);
            DatabaseDocUtils.exportDatabaseDoc(response, type, infoMap.get("url").toString(),
                    infoMap.get("username").toString(), infoMap.get("password").toString());
        }
    }
}
