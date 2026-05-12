package com.sunyard.sunafm.task;

import com.sunyard.framework.quartz.annotation.QuartzLog;
import com.sunyard.sunafm.service.AfmApiDataService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@Slf4j
public class AfmRetryTask {

    @Resource
    private AfmApiDataService afmApiDataService;

    /**
     * 重试任务
     */
    @QuartzLog
    public void retryTask() {
        log.info("查重重试任务开启");
        try {
            afmApiDataService.checkRepeatRetry();
        }catch (Exception e){
            log.error("查重重试任务错误");
        }
        log.info("查重重试任务结束");
    }
}
