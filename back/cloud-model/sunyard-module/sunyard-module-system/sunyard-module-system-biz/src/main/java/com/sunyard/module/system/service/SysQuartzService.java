package com.sunyard.module.system.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.quartz.api.QuartzApi;
import com.sunyard.framework.quartz.util.QuartzFeignBuilderUtils;
import com.sunyard.framework.quartz.vo.QuartzVO;
import com.sunyard.module.system.api.dto.SysTimingTaskLogDTO;
import com.sunyard.module.system.enums.QuartzEnum;
import com.sunyard.module.system.mapper.SysTimingTaskLogMapper;
import com.sunyard.module.system.mapper.SysTimingTaskMapper;
import com.sunyard.module.system.po.SysTimingTask;
import com.sunyard.module.system.po.SysTimingTaskLog;

import cn.hutool.core.bean.BeanUtil;

/**
 * @author P-JWei
 * @date 2023/4/11 16:57s
 */
@Service
public class SysQuartzService {

    @Resource
    private SysTimingTaskMapper sysTimingTaskMapper;
    @Resource
    private SysTimingTaskLogMapper sysTimingTaskLogMapper;

    /**
     * 查询所有的定时任务
     *
     * @param name 实例名称
     * @param serviceName 组名
     * @param status 状态
     * @param pageForm 分页
     * @return Result 实例集合
     */
    public Result search(String name, String serviceName, Integer status, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysTimingTask> sysTimingTasks = sysTimingTaskMapper
                .selectList(
                        new LambdaQueryWrapper<SysTimingTask>()
                                .like(StringUtils.hasText(name), SysTimingTask::getName, name)
                                .eq(StringUtils.hasText(serviceName), SysTimingTask::getServiceName,
                                        serviceName)
                                .eq(null != status, SysTimingTask::getStatus, status));
        return Result.success(new PageInfo<>(sysTimingTasks));
    }

    /**
     * 查询指定实例日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @param pageForm 分页
     * @return Result 实例集合
     */
    public Result searchLog(Long taskId, Date startTime, Date endTime, Integer status, PageForm pageForm) {
        SysTimingTask sysTimingTasks = sysTimingTaskMapper.selectById(taskId);
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysTimingTaskLog> sysTimingTaskLogs = sysTimingTaskLogMapper
                .selectList(new LambdaQueryWrapper<SysTimingTaskLog>()
                        .eq(null != status, SysTimingTaskLog::getStatus, status)
                        .ge(!ObjectUtils.isEmpty(startTime), SysTimingTaskLog::getRunStartTime, startTime)
                        .le(!ObjectUtils.isEmpty(endTime), SysTimingTaskLog::getRunOverTime, endTime)
                        .eq(SysTimingTaskLog::getTaskId, taskId)
                        .orderByDesc(SysTimingTaskLog::getRunStartTime));
        PageInfo<SysTimingTaskLogDTO> pageInfo = PageCopyListUtils
                .getPageInfo(new PageInfo<>(sysTimingTaskLogs), SysTimingTaskLogDTO.class);
        pageInfo.getList().forEach(item -> {
            item.setJobName(sysTimingTasks.getName());
            item.setServiceName(sysTimingTasks.getServiceName());
            item.setClassAbsolutePath(sysTimingTasks.getClassAbsolutePath());
        });
        return Result.success(pageInfo);
    }

    /**
     * 添加定时任务
     *
     * @param quartzVO
     * @return Result
     */
    public Result insert(QuartzVO quartzVO) {
        Assert.notNull(quartzVO.getName(), "参数错误");
        Assert.notNull(quartzVO.getServiceName(), "参数错误");
        Assert.notNull(quartzVO.getInitType(), "参数错误");
        Assert.notNull(quartzVO.getClassAbsolutePath(), "参数错误");
        Assert.notNull(quartzVO.getCronExpression(), "参数错误");

        Long count = sysTimingTaskMapper.selectCount(new LambdaQueryWrapper<SysTimingTask>()
                .eq(SysTimingTask::getClassAbsolutePath, quartzVO.getClassAbsolutePath()));
        Assert.isTrue(count == 0, "已存在调用实例，无法添加");

        SysTimingTask sysTimingTask = new SysTimingTask();
        BeanUtil.copyProperties(quartzVO, sysTimingTask);
        // 初始为 1 未运行
        sysTimingTask.setStatus(1);
        sysTimingTaskMapper.insert(sysTimingTask);
        return Result.success("");
    }

    /**
     * 删除定时任务
     *
     * @param id
     * @return Result
     */
    public Result deleted(Long[] id) {
        Assert.notEmpty(id, "参数错误");
        // 删除task 且删除taskLog
        sysTimingTaskMapper.delete(new LambdaUpdateWrapper<SysTimingTask>().in(SysTimingTask::getId,
                Arrays.asList(id)));
        sysTimingTaskLogMapper.delete(new LambdaUpdateWrapper<SysTimingTaskLog>()
                .in(SysTimingTaskLog::getId, Arrays.asList(id)));
        return Result.success("");
    }

    /**
     * 删除日志
     *
     * @param id
     * @return Result 实例集合
     */
    public Result deletedLog(Long[] id) {
        Assert.notEmpty(id, "参数错误");
        sysTimingTaskLogMapper.delete(new LambdaUpdateWrapper<SysTimingTaskLog>()
                .in(SysTimingTaskLog::getId, Arrays.asList(id)));
        return Result.success("");
    }

    /**
     * 启动定时任务
     *
     * @param id 实例id
     * @return Result 实例集合
     */
    public Result begin(Long id) {
        Assert.notNull(id, "参数错误");
        // 调用对应的服务，启动定时任务
        SysTimingTask sysTimingTask = sysTimingTaskMapper.selectById(id);
        QuartzApi feignClient = QuartzFeignBuilderUtils.getFeignClient(QuartzApi.class,
                QuartzEnum.getSystemNameStr(sysTimingTask.getClassAbsolutePath()));
        return feignClient.begin(sysTimingTask.getId());
    }

    /**
     * 修改定时任务
     *
     * @param quartzVo 实例对象
     * @return Result 实例集合
     */
    public Result update(QuartzVO quartzVo) {
        Assert.notNull(quartzVo.getId(), "参数错误");
        Assert.notNull(quartzVo.getName(), "参数错误");
        Assert.notNull(quartzVo.getCronExpression(), "参数错误");
        Assert.notNull(quartzVo.getInitType(), "参数错误");
        Assert.notNull(quartzVo.getClassAbsolutePath(), "参数错误");

        Long count = sysTimingTaskMapper.selectCount(new LambdaQueryWrapper<SysTimingTask>()
                .eq(SysTimingTask::getClassAbsolutePath, quartzVo.getClassAbsolutePath())
                .ne(SysTimingTask::getId, quartzVo.getId()));
        Assert.isTrue(count == 0, "已存在调用实例，无法添加");

        SysTimingTask sysTimingTask = new SysTimingTask();
        BeanUtil.copyProperties(quartzVo, sysTimingTask);
        sysTimingTaskMapper.updateById(sysTimingTask);
        // 调用对应的服务，更新定时任务
        SysTimingTask sysTimingTaskResult = sysTimingTaskMapper.selectById(quartzVo.getId());
        QuartzApi feignClient = QuartzFeignBuilderUtils.getFeignClient(QuartzApi.class,
                QuartzEnum.getSystemNameStr(sysTimingTask.getClassAbsolutePath()));
        return feignClient.update(quartzVo);
    }

    /**
     * 根据id删除定时任务
     *
     * @param id 实例id
     * @return Result 实例集合
     */
    public Result delete(Long id) {
        Assert.notNull(id, "参数错误");
        // 调用对应的服务，删除定时任务(停止)
        SysTimingTask sysTimingTask = sysTimingTaskMapper.selectById(id);
        QuartzApi feignClient = QuartzFeignBuilderUtils.getFeignClient(QuartzApi.class,
                QuartzEnum.getSystemNameStr(sysTimingTask.getClassAbsolutePath()));
        return feignClient.stop(id);
    }

    /**
     * 根据id立即执行一次定时任务
     *
     * @param id 实例id
     * @return Result 实例集合
     */
    public Result runNow(Long id) {
        Assert.notNull(id, "参数错误");
        // 调用对应的服务，立即运行一次定时任务
        SysTimingTask sysTimingTask = sysTimingTaskMapper.selectById(id);
        QuartzApi feignClient = QuartzFeignBuilderUtils.getFeignClient(QuartzApi.class,
                QuartzEnum.getSystemNameStr(sysTimingTask.getClassAbsolutePath()));
        return feignClient.runNow(id);
    }

}
