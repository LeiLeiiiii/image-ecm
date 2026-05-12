package com.sunyard.module.system.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.framework.quartz.vo.QuartzVO;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.SysQuartzService;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用管理/系统初始化/定时任务
 *
 * @author P-JWei
 * @date 2023/4/11 17:19
 */
@Slf4j
@RestController
@RequestMapping("basics/sysConfig/quartz")
public class BasicsSysConfigQuartzController {
    private static final String BASELOG = LogsPrefixConstants.MENU_SYSTEM + "-定时任务->";
    @Resource
    private SysQuartzService quartzService;

    /**
     * 查询所有的定时任务
     *
     * @param name   实例名称
     * @param status 状态
     * @return Result 实例集合
     */
    @OperationLog(BASELOG + "查询所有的定时任务")
    @PostMapping("search")
    public Result search(String name, String serviceName, Integer status, PageForm pageForm) {
        return quartzService.search(name, serviceName, status, pageForm);
    }

    /**
     * 查询指定实例日志
     *
     * @param startTime        实例id
     * @param endTime 实例id
     * @param status      实例id
     * @param pageForm    分页
     * @return Result 日志集合
     */
    @OperationLog(BASELOG + "查询指定实例日志")
    @PostMapping("searchLog")
    public Result searchLog(Long taskId,
                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                            Integer status, PageForm pageForm) {
        return quartzService.searchLog(taskId, startTime, endTime, status, pageForm);
    }

    /**
     * 插入定时任务
     *
     * @param quartzVO 实例对象
     * @return result
     */
    @OperationLog(BASELOG + "插入定时任务")
    @PostMapping("insert")
    public Result insert(QuartzVO quartzVO) {
        return quartzService.insert(quartzVO);
    }

    /**
     * 删除定时任务
     *
     * @param id 实例id集
     * @return result
     */
    @OperationLog(BASELOG + "删除定时任务")
    @PostMapping("deleted")
    public Result deleted(Long[] id) {
        return quartzService.deleted(id);
    }

    /**
     * 删除日志
     *
     * @param id 日志id集
     * @return result
     */
    @OperationLog(BASELOG + "删除日志")
    @PostMapping("deletedLog")
    public Result deletedLog(Long[] id) {
        return quartzService.deletedLog(id);
    }

    /**
     * 启动定时任务
     *
     * @param id 实例id
     * @return result
     */
    @OperationLog(BASELOG + "启动定时任务")
    @PostMapping("begin")
    public Result begin(Long id) {
        return quartzService.begin(id);
    }

    /**
     * 修改定时任务
     *
     * @param quartzVo 实例对象
     * @return result
     */
    @OperationLog(BASELOG + "修改定时任务")
    @PostMapping("update")
    public Result update(QuartzVO quartzVo) {
        return quartzService.update(quartzVo);
    }

    /**
     * 根据id暂停定时任务
     *
     * @param id 实例id
     * @return result
     */
    @OperationLog(BASELOG + "根据id暂停定时任务")
    @PostMapping("stop")
    public Result stop(Long id) {
        return quartzService.delete(id);
    }

    /**
     * 根据id立即执行一次定时任务
     *
     * @param id 实例id
     * @return result
     */
    @OperationLog(BASELOG + "根据id立即执行一次定时任务")
    @PostMapping("runNow")
    public Result runNow(Long id) {
        return quartzService.runNow(id);
    }

}
