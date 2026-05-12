package com.sunyard.module.system.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysTimingTaskDTO;
import com.sunyard.module.system.constant.ApiConstants;

/**
 * @author P-JWei
 * @date 2023/5/24 15:50 @title：
 * @description:
 */
@FeignClient(value = ApiConstants.NAME)
public interface TaskApi {

    String PREFIX = ApiConstants.PREFIX + "/task/";

    /**
     * 插入定时任务日志
     *
     * @param jobId 定时任务id
     * @return Result
     */
    @PostMapping(PREFIX + "insertTaskLog")
    Result<Long> insertTaskLog(@RequestParam("jobId") String jobId);

    /**
     * 更新日志记录的结束时间
     *
     * @param id 日志id
     * @return Result
     */
    @PostMapping(PREFIX + "updateTaskLogRunOverTime")
    Result<Boolean> updateTaskLogRunOverTime(@RequestParam("id") Long id);

    /**
     * 记录异常日志信息
     *
     * @param id    日志id
     * @param exStr 异常信息
     * @return Result
     */
    @PostMapping(PREFIX + "updateTaskLogError")
    Result<Boolean> updateTaskLogError(@RequestParam("id") Long id,
                                       @RequestParam("exStr") String exStr);

    /**
     * 更新定时任务最后运行时间
     *
     * @param jobId 定时任务id
     * @return Result
     */
    @PostMapping(PREFIX + "updateTaskLastRunTime")
    Result<Boolean> updateTaskLastRunTime(@RequestParam("jobId") String jobId);

    /**
     * 查询定时任务
     *
     * @param jobId 定时任务id
     * @return Result
     */
    @PostMapping(PREFIX + "selectTask")
    Result<SysTimingTaskDTO> selectTask(@RequestParam("jobId") String jobId);

    /**
     * 查询当前应用的所有任务
     * @return Result
     */
    @PostMapping(PREFIX + "searchTask")
    Result<List<SysTimingTaskDTO>> searchTask(@RequestParam("serviceName") String serviceName);

    /**
     * 更新定时任务状态
     *
     * @param jobId  定时任务id
     * @param status 状态
     * @return Result
     */
    @PostMapping(PREFIX + "updateTask")
    Result<Boolean> updateTask(@RequestParam("jobId") Long jobId,
                               @RequestParam("status") Integer status,
                               @RequestParam("survivalServer") String survivalServer);

    /**
     * 更新定时任务cronExpression
     *
     * @param jobId          定时任务id
     * @param cronExpression 频率表达式
     * @return Result
     */
    @PostMapping(PREFIX + "updateTaskCronExpression")
    Result<Boolean> updateTaskCronExpression(@RequestParam("jobId") String jobId,
                                             @RequestParam("cronExpression") String cronExpression);

    /**
     * 初始化指定模块的定时任务
     *
     * @param serviceName 服务名
     * @return Result
     */
    @PostMapping(PREFIX + "initTask")
    Result<List<SysTimingTaskDTO>> initTask(@RequestParam("serviceName") String serviceName);

    /**
     * 在各自模块关闭前修改Task的状态
     *
     * @param serviceName 服务名
     * @return Result
     */
    @PostMapping(PREFIX + "destroyTask")
    Result<Boolean> destroyTask(@RequestParam("serviceName") String serviceName);
}
