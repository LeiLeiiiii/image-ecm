package com.sunyard.ecm.task;

import com.sunyard.ecm.service.OperateDestroyService;
import com.sunyard.framework.quartz.annotation.QuartzLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 @Author ypy
 @time 2025/07/04
 **/
@Slf4j
@DisallowConcurrentExecution
public class EcmDestroyTask extends QuartzJobBean {
    @Resource
    private OperateDestroyService operateDestroyService;

    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("##########  影像销毁任务开始执行销毁 ##########");
        operateDestroyService.destroyEcm();
        log.info("##########  影像销毁任务执行销毁结束 ##########");
    }

}
