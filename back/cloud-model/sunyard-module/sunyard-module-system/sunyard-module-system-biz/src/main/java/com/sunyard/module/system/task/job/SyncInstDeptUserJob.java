package com.sunyard.module.system.task.job;

import javax.annotation.Resource;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.sunyard.framework.quartz.annotation.QuartzLog;
import com.sunyard.module.system.task.service.SyncInstDeptUserService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author LQ
 * @date 2025/7/7
 * @describe 国银金租同步机构部门用户
 */
@Slf4j
@DisallowConcurrentExecution
public class SyncInstDeptUserJob extends QuartzJobBean {

    @Resource
    private SyncInstDeptUserService syncInstDeptUserService;

    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context){
        syncInstDeptUserService.synchronizationInstDeptUser();
    }

}
