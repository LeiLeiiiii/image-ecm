package com.sunyard.module.system.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.CloseLog;
import com.sunyard.module.system.api.dto.SysTimingTaskDTO;
import com.sunyard.module.system.enums.QuartzInitEnum;
import com.sunyard.module.system.mapper.SysTimingTaskLogMapper;
import com.sunyard.module.system.mapper.SysTimingTaskMapper;
import com.sunyard.module.system.po.SysTimingTask;
import com.sunyard.module.system.po.SysTimingTaskLog;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;

/**
 * @author P-JWei
 * @date 2023/5/24 16:00 @title：
 * @description:
 */
@CloseLog
@RestController
public class TaskApiImpl implements TaskApi {

    @Resource
    private SysTimingTaskMapper sysTimingTaskMapper;
    @Resource
    private SysTimingTaskLogMapper sysTimingTaskLogMapper;

    @Override
    public Result<Long> insertTaskLog(String jobId) {
        SysTimingTaskLog sysTimingTaskLog = new SysTimingTaskLog();
        sysTimingTaskLog.setTaskId(Long.valueOf(jobId));
        sysTimingTaskLog.setRunStartTime(new Date());
        // 默认成功，报错后再改为 1
        sysTimingTaskLog.setStatus(0);
        sysTimingTaskLogMapper.insert(sysTimingTaskLog);
        return Result.success(sysTimingTaskLog.getId());
    }

    @Override
    public Result<Boolean> updateTaskLogRunOverTime(Long id) {
        sysTimingTaskLogMapper.update(null, new LambdaUpdateWrapper<SysTimingTaskLog>()
                .set(SysTimingTaskLog::getRunOverTime, new Date()).eq(SysTimingTaskLog::getId, id));
        return Result.success(true);
    }

    @Override
    public Result<Boolean> updateTaskLogError(Long id, String exStr) {
        sysTimingTaskLogMapper.update(null,
                new LambdaUpdateWrapper<SysTimingTaskLog>()
                        .set(SysTimingTaskLog::getRunOverTime, new Date())
                        .set(SysTimingTaskLog::getStatus, 1)
                        .set(SysTimingTaskLog::getErrorMsg, exStr).eq(SysTimingTaskLog::getId, id));
        return Result.success(true);
    }

    @Override
    public Result<Boolean> updateTaskLastRunTime(String jobId) {
        sysTimingTaskMapper.update(null, new LambdaUpdateWrapper<SysTimingTask>()
                .set(SysTimingTask::getLastRunTime, new Date()).eq(SysTimingTask::getId, jobId));
        return Result.success(true);
    }

    @Override
    public Result<SysTimingTaskDTO> selectTask(String jobId) {
        SysTimingTask sysTimingTask = sysTimingTaskMapper.selectById(jobId);
        SysTimingTaskDTO sysTimingTaskDTO = new SysTimingTaskDTO();
        BeanUtil.copyProperties(sysTimingTask, sysTimingTaskDTO);
        return Result.success(sysTimingTaskDTO);
    }

    @Override
    public Result<List<SysTimingTaskDTO>> searchTask(String serviceName) {
        Assert.notNull(serviceName, "参数错误");
        Integer code = QuartzInitEnum.getCode(serviceName);
        //只初始化策略为 0 自动，并且开启的 的定时任务
        List<SysTimingTaskDTO> dto = new ArrayList<>();
        List<SysTimingTask> list = sysTimingTaskMapper
                .selectList(new LambdaQueryWrapper<SysTimingTask>()
                        .eq(SysTimingTask::getServiceName, code).eq(SysTimingTask::getStatus, 0));
        for (SysTimingTask sysTimingTasks : list) {
            SysTimingTaskDTO sysTimingTaskDTO = new SysTimingTaskDTO();
            BeanUtil.copyProperties(sysTimingTasks, sysTimingTaskDTO);
            dto.add(sysTimingTaskDTO);
        }
        return Result.success(dto);
    }

    @Override
    public Result<Boolean> updateTask(Long jobId, Integer status, String survivalServer) {
        String survivalServerPo = survivalServer;
        Date survivalTime = new Date();
        if (Integer.valueOf(1).equals(status)) {
            survivalServerPo = null;
            survivalTime = null;
        }
        sysTimingTaskMapper.update(null, new LambdaUpdateWrapper<SysTimingTask>()
                .set(!ObjectUtil.isEmpty(status), SysTimingTask::getStatus, status)
                .set(SysTimingTask::getSurvivalServer, survivalServerPo)
                .set(SysTimingTask::getSurvivalTime, survivalTime).eq(SysTimingTask::getId, jobId));
        return Result.success(true);
    }

    @Override
    public Result<Boolean> updateTaskCronExpression(String jobId, String cronExpression) {
        // 修改cron条件
        sysTimingTaskMapper.update(null,
                new LambdaUpdateWrapper<SysTimingTask>()
                        .set(SysTimingTask::getCronExpression, cronExpression)
                        .eq(SysTimingTask::getId, jobId));
        return Result.success(true);
    }

    @Override
    public Result<List<SysTimingTaskDTO>> initTask(String serviceName) {
        Assert.notNull(serviceName, "参数错误");
        Integer code = QuartzInitEnum.getCode(serviceName);
        //只初始化策略为 0 自动，并且开启的 的定时任务
        List<SysTimingTask> sysTimingTasks = sysTimingTaskMapper
                .selectList(new LambdaQueryWrapper<SysTimingTask>()
                        .eq(SysTimingTask::getServiceName, code).eq(SysTimingTask::getInitType, 0));
        if (sysTimingTasks.isEmpty()) {
            return Result.success(null);
        }
        List<SysTimingTaskDTO> list = sysTimingTasks.stream().map(item -> {
            SysTimingTaskDTO sysTimingTaskDTO = new SysTimingTaskDTO();
            BeanUtil.copyProperties(item, sysTimingTaskDTO);
            return sysTimingTaskDTO;
        }).collect(Collectors.toList());
        return Result.success(list);
    }

    @Override
    public Result<Boolean> destroyTask(String serviceName) {
        Assert.notNull(serviceName, "参数错误");
        Integer code = QuartzInitEnum.getCode(serviceName);
        //把指定模块的 task状态改为 1 没有运行
        sysTimingTaskMapper.update(null, new LambdaUpdateWrapper<SysTimingTask>()
                .set(SysTimingTask::getStatus, 1).set(SysTimingTask::getSurvivalServer, "")
                .set(SysTimingTask::getSurvivalTime, null).eq(SysTimingTask::getServiceName, code));
        return Result.success(true);
    }

}
