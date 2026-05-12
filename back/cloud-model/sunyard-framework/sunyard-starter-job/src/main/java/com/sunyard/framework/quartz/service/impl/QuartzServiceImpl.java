package com.sunyard.framework.quartz.service.impl;

import javax.annotation.Resource;

import com.sunyard.framework.quartz.util.IpAddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.quartz.config.properties.QuartzProperties;
import com.sunyard.framework.quartz.response.RespEnum;
import com.sunyard.framework.quartz.service.QuartzService;
import com.sunyard.framework.quartz.util.QuartzUtils;
import com.sunyard.framework.quartz.vo.QuartzVO;
import com.sunyard.module.system.api.TaskApi;
import com.sunyard.module.system.api.dto.SysTimingTaskDTO;

import cn.hutool.core.bean.BeanUtil;

/**
 * @author P-JWei
 * @date 2024/1/19 17:02:51
 * 使用Component注解，跳过service日志aop切面日志输出
 */
@Slf4j
@Component
public class QuartzServiceImpl implements QuartzService {
    @Resource
    private QuartzProperties quartzProperties;
    @Resource(name = "SunyardQuartzScheduler")
    private Scheduler scheduler;
    @Resource
    private TaskApi taskApi;

    @Override
    public Result begin(Long id) {
        Assert.notNull(id, "参数错误");
        Result<SysTimingTaskDTO> stringResult = taskApi.selectTask(String.valueOf(id));
        QuartzVO quartzVo = new QuartzVO();
        BeanUtil.copyProperties(stringResult.getData(), quartzVo);
        RespEnum resp = QuartzUtils.createScheduleJob(scheduler, quartzVo,
                quartzVo.getClassAbsolutePath());
        if (RespEnum.SUCCESS.getCode().equals(resp.getCode())) {
            taskApi.updateTask(quartzVo.getId(), 0, this.getHost());
            return Result.success(resp.getMsg());
        }
        return Result.error(resp.getMsg(), resp.getCode());
    }

    @Override
    public Result update(QuartzVO quartzVo) {
        Assert.notNull(quartzVo.getId(), "参数错误");
        Assert.notNull(quartzVo.getCronExpression(), "参数错误");
        RespEnum resp = QuartzUtils.deleteScheduleJob(scheduler, quartzVo.getId() + "");
        if (RespEnum.SUCCESS.getCode().equals(resp.getCode())) {
            taskApi.updateTaskCronExpression(quartzVo.getId() + "", quartzVo.getCronExpression());
            if (quartzVo.getStatus().equals(0)) {
                resp = QuartzUtils.createScheduleJob(scheduler, quartzVo,
                        String.valueOf(quartzVo.getClassAbsolutePath()));
            }
            return Result.success(resp.getMsg());
        }
        return Result.error(resp.getMsg(), resp.getCode());
    }

    @Override
    public Result stop(Long id) {
        Assert.notNull(id, "参数错误");
        RespEnum resp = QuartzUtils.deleteScheduleJob(scheduler, String.valueOf(id));
        if (RespEnum.SUCCESS.getCode().equals(resp.getCode())) {
            taskApi.updateTask(id, 1, this.getHost());
            return Result.success(resp.getMsg());
        }
        return Result.error(resp.getMsg(), resp.getCode());
    }

    @Override
    public Result runNow(Long id) {
        Assert.notNull(id, "参数错误");
        RespEnum resp = QuartzUtils.runOnce(scheduler, String.valueOf(id));
        if (RespEnum.SUCCESS.getCode().equals(resp.getCode())) {
            return Result.success(resp.getMsg());
        }
        return Result.error(resp.getMsg(), resp.getCode());
    }

    private String getHost() {
        String host = IpAddressUtils.getHostWithPort(quartzProperties.getPort());
        log.debug("定时任务注册IP地址：{}", host);
        return host;
    }
}
