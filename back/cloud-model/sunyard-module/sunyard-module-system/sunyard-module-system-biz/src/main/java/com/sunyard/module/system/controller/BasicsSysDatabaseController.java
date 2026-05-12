package com.sunyard.module.system.controller;

import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.SysDataBaseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 通用管理/系统初始化/数据库文档
 *
 * @Author 朱山成
 * @time 2023/5/22 15:08
 **/
@RestController
@RequestMapping("basics/sysDataBase/doc")
public class BasicsSysDatabaseController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_MONITOR + "-数据库文档->";
    @Resource
    private SysDataBaseService sysDataBaseService;

    /**
     * 查询列表
     *
     * @param systemType 系统类型
     * @param fileType   文件类型
     */
    @OperationLog(BASELOG + "查询列表")
    @PostMapping("search")
    public void search(Integer systemType, Integer fileType) {
        sysDataBaseService.search(response, systemType, fileType);
    }

}
