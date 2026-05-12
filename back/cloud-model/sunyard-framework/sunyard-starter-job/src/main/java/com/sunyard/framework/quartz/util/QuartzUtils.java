package com.sunyard.framework.quartz.util;

import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.util.ObjectUtils;

import com.sunyard.framework.quartz.response.RespEnum;
import com.sunyard.framework.quartz.vo.QuartzVO;
import com.sunyard.module.system.api.dto.SysTimingTaskDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author P-JWei
 * @date 2023/4/11 15:52 @title：
 * @description:
 */
@Slf4j
public class QuartzUtils {

    /**
     * 创建定时任务 定时任务创建之后默认启动状态
     *
     * @param scheduler  调度器
     * @param quartzBean 定时任务信息类
     * @return RespEnum
     */
    public static RespEnum createScheduleJob(Scheduler scheduler, QuartzVO quartzBean,
                                             String jobClassStr) {
        try {
            // 获取到定时任务的执行类 必须是类的绝对路径名称
            // 定时任务类需要是job类的具体实现 QuartzJobBean是job的抽象类。
            Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(jobClassStr);

            // 创建JobKey和TriggerKey
            JobKey jobKey = new JobKey(quartzBean.getId() + "");
            TriggerKey triggerKey = new TriggerKey(quartzBean.getId() + "");

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey)
                    .withDescription(quartzBean.getName())
                    .build();

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                    .cronSchedule(quartzBean.getCronExpression());

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(scheduleBuilder)
                    .build();

            // 检查Job是否已存在
            if (scheduler.checkExists(jobKey)) {
                // 如果Job已存在，更新trigger
                scheduler.rescheduleJob(triggerKey, trigger);
                log.info("更新任务: {}", jobKey);
            } else {
                // 如果Job不存在，创建新的
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("创建任务: {}", jobKey);
            }

        } catch (ClassNotFoundException e) {
            log.error(RespEnum.FAIL_NO_FOUND_CLASS.getMsg(), e);
            return RespEnum.FAIL_NO_FOUND_CLASS;
        } catch (SchedulerException e) {
            log.error(RespEnum.FAIL_CREATE_JOB.getMsg(), e);
            return RespEnum.FAIL_CREATE_JOB;
        }
        return RespEnum.SUCCESS;
    }
    /**
     * 根据任务Id暂停定时任务
     *
     * @param scheduler 调度器
     * @param jobId     定时任务名称
     * @return RespEnum
     */
    public static RespEnum pauseScheduleJob(Scheduler scheduler, String jobId) {
        JobKey jobKey = JobKey.jobKey(jobId);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            log.error(RespEnum.FAIL_PAUSE_JOB.getMsg(), e);
            return RespEnum.FAIL_PAUSE_JOB;
        }
        return RespEnum.SUCCESS;
    }

    /**
     * 根据任务Id恢复定时任务
     *
     * @param scheduler 调度器
     * @param jobId     定时任务id
     * @return RespEnum
     */
    public static RespEnum resumeScheduleJob(Scheduler scheduler, String jobId) {
        JobKey jobKey = JobKey.jobKey(jobId);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            log.error(RespEnum.FAIL_START_JOB.getMsg(), e);
            return RespEnum.FAIL_START_JOB;
        }
        return RespEnum.SUCCESS;
    }

    /**
     * 根据任务id立即运行一次定时任务
     *
     * @param scheduler 调度器
     * @param jobId     定时任务id
     * @return RespEnum
     */
    public static RespEnum runOnce(Scheduler scheduler, String jobId) {
        JobKey jobKey = JobKey.jobKey(jobId);
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
            if (jobKeys.isEmpty()) {
                log.info("不存在定时任务！");
                return RespEnum.FAIL_NOT_FOUND_JOB;
            }
            for (JobKey k : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(k);
            }
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            log.error(RespEnum.FAIL_RUN_ONE_JOB.getMsg(), e);
            return RespEnum.FAIL_RUN_ONE_JOB;
        }
        return RespEnum.SUCCESS;
    }

    /**
     * 更新定时任务
     *
     * @param scheduler  调度器
     * @param quartzBean 定时任务信息类
     * @return RespEnum
     */
    public static RespEnum updateScheduleJob(Scheduler scheduler, QuartzVO quartzBean) {
        try {
            // 获取到对应任务的触发器
            TriggerKey triggerKey = TriggerKey.triggerKey(quartzBean.getId()+"");
            // 设置定时任务执行方式
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                    .cronSchedule(quartzBean.getCronExpression());
            // 重新构建任务的触发器trigger
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(scheduleBuilder).build();
            // 重置对应的job
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            log.error(RespEnum.FAIL_UPDATE_JOB.getMsg(), e);
            return RespEnum.FAIL_UPDATE_JOB;
        }
        return RespEnum.SUCCESS;
    }

    /**
     * 根据定时任务id从调度器当中删除定时任务
     *
     * @param scheduler 调度器
     * @param jobId     定时任务名称
     * @return RespEnum
     */
    public static RespEnum deleteScheduleJob(Scheduler scheduler, String jobId) {
        JobKey jobKey = JobKey.jobKey(jobId);
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error(RespEnum.FAIL_DELETE_JOB.getMsg(), e);
            return RespEnum.FAIL_DELETE_JOB;
        }
        return RespEnum.SUCCESS;
    }

    /**
     * 判断任务是否在执行状态
     * */
    public static boolean isRunning(SysTimingTaskDTO dto) {
        if (!ObjectUtils.isEmpty(dto)) {
            return dto.getStatus().intValue() == 0;
        } else {
            return false;
        }
    }

    /**
     * 判断任务是否掉线
     * */
    public static boolean isOnline(SysTimingTaskDTO dto) {
        if (!ObjectUtils.isEmpty(dto.getSurvivalTime())) {
            Long now = System.currentTimeMillis();
            Long survivalTime = dto.getSurvivalTime().getTime();
            Long time = now - survivalTime;
            //允许服务器节点时间相差5分钟内
            return time < 5 * 60 * 1000L;
        } else {
            return false;
        }
    }

}
