package com.sunyard.framework.quartz.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.sunyard.framework.quartz.config.properties.QuartzProperties;
import com.sunyard.framework.quartz.service.QuartzService;
import com.sunyard.framework.quartz.util.IpAddressUtils;
import com.sunyard.framework.quartz.util.QuartzUtils;
import com.sunyard.module.system.api.TaskApi;
import com.sunyard.module.system.api.dto.SysTimingTaskDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author LQ
 * @date 2025/7/7
 * @describe 国银金租同步机构部门用户
 */
@Slf4j
@RefreshScope
@Component
public class SurvivalJob {

    @Resource
    private QuartzProperties quartzProperties;
    @Resource(name = "SunyardQuartzScheduler")
    private Scheduler scheduler;
    @Resource
    private QuartzService quartzService;
    @Resource
    private TaskApi taskApi;

    /**
     * 10秒执行一次 用于对当前应用执在计划内的任务进行心跳更新
     */
    @Scheduled(fixedDelay = 10000)
    public void survival() {
        if (!quartzProperties.getEnable()) {
            return;
        }
        try {
            HashMap<Long, String> map = this.getAllJob();
            HashMap<Long, SysTimingTaskDTO> mapDb = new HashMap<>();
            log.warn("现注册定时任务数量：{}", map.size());
            String host = this.getHost();
            List<SysTimingTaskDTO> listResult = taskApi.searchTask(quartzProperties.getAppName())
                    .getData();

            for (SysTimingTaskDTO dto : listResult) {
                String jobGroup = map.get(dto.getId());
                //处理数据库中停止的任务和(追加掉线任务，更新心跳)
                if (QuartzUtils.isRunning(dto)) {
                    if (QuartzUtils.isOnline(dto)) {
                        if (!ObjectUtils.isEmpty(dto.getSurvivalServer())
                                && dto.getSurvivalServer().equals(host)) {
                            if (!ObjectUtils.isEmpty(jobGroup)) {
                                taskApi.updateTask(dto.getId(), 0, host);
                            } else {
                                quartzService.begin(dto.getId());
                            }
                        }
                    } else {
                        quartzService.begin(dto.getId());
                    }
                } else {
                    if (!ObjectUtils.isEmpty(jobGroup)) {
                        quartzService.stop(dto.getId());
                    }
                }
                mapDb.put(dto.getId(), dto);
            }
            //清理本机的脏任务
            for (Map.Entry<Long, String> entry : map.entrySet()) {
                SysTimingTaskDTO dto = mapDb.get(entry.getKey());
                if (QuartzUtils.isRunning(dto)) {
                    if (!ObjectUtils.isEmpty(dto.getSurvivalServer())
                            && !dto.getSurvivalServer().equals(host)) {
                        QuartzUtils.deleteScheduleJob(scheduler, String.valueOf(entry.getKey()));
                    }
                } else if (ObjectUtils.isEmpty(dto)) {
                    quartzService.stop(entry.getKey());
                }
            }
        } catch (Exception e) {
            log.error("定时任务调用System调度中心:心跳失败", e);
        }
    }

    private String getHost() {
        String host = IpAddressUtils.getHostWithPort(quartzProperties.getPort());
        log.debug("定时任务注册IP地址：{}", host);
        return host;
    }

    public HashMap<Long, String> getAllJob() throws SchedulerException {
        HashMap<Long, String> map = new HashMap<>();
        List<JobKey> jobKeys = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            jobKeys.addAll(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)));
        }
        for (JobKey jobKey : jobKeys) {
            map.put(Long.valueOf(jobKey.getName()), jobKey.getGroup() + ":" + jobKey.getName());
        }
        return map;
    }

}
