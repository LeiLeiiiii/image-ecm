package com.sunyard.ecm.task;

import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.framework.quartz.annotation.QuartzLog;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;

/**
 @Author yzy
 @desc 每天刷新异步任务栏中异常的一直在处理中的为处理失败
 @time 2025/05/13 16:05
 **/
@DisallowConcurrentExecution
public class EcmBusiAsyncCleanTask extends QuartzJobBean {
    @Resource
    private AsyncTaskService asyncTaskService;

    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<EcmAsyncTask> list= asyncTaskService.getEcmAsyncTaskListInProcessing();
        list.forEach(task -> {
            String taskType = task.getTaskType();
            if (taskType != null && taskType.contains(EcmCheckAsyncTaskEnum.PROCESSING.description())) {
                task.setTaskType(taskType.replace(EcmCheckAsyncTaskEnum.PROCESSING.description(), EcmCheckAsyncTaskEnum.FAILED.description()));
            }
        });
        asyncTaskService.batchUpdateTask(list);
    }

}
