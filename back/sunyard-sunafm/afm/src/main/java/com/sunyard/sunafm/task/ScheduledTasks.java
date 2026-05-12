package com.sunyard.sunafm.task;


import com.sunyard.framework.quartz.annotation.QuartzLog;
import com.sunyard.sunafm.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;

import javax.annotation.Resource;

/**
 * 自动分配服务器定时任务
 */
@DisallowConcurrentExecution
@Slf4j
public class ScheduledTasks {

    @Resource
    private CommonService commonService;

    /**
     * 自动切换
     */
    @QuartzLog
    public void queryServer() {
        log.info("执行服务器内存重新计算逻辑");
        try {
            commonService.queryServer();
        }catch (Exception e){
            log.error("执行服务器内存重新计算逻辑错误");
        }
    }

}