package com.sunyard.ecm.task;

import com.sunyard.ecm.service.OperateExpireService;
import com.sunyard.framework.quartz.annotation.QuartzLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 @Author ypy
 @time 2025/11/06
 **/
@Slf4j
@DisallowConcurrentExecution
public class EcmFileExpireTask extends QuartzJobBean {
    @Resource
    private OperateExpireService operateExpireService;

    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("##########  影像文件到期任务开始执行 ##########");
        operateExpireService.updateExpireStatus();
        log.info("##########  影像文件到期任务执行结束 ##########");
    }

}
