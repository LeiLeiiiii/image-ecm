package com.sunyard.framework.quartz.aop;

import javax.annotation.Resource;

import com.sunyard.framework.common.result.Result;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.quartz.JobExecutionContext;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.quartz.constant.LogConstants;
import com.sunyard.module.system.api.TaskApi;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author P-JWei
 * @date 2023/4/13 15:51 @title：
 * @description:
 */
@Slf4j
@Aspect
@Component
public class QuartzAspect extends BaseAspect{
    private static final ThreadLocal<Long> BEGIN_TIME_THREAD_LOCAL =
            new NamedThreadLocal<>("DatabaseJob ThreadLocal beginTime");
    private static final ThreadLocal<Long> NOW_JOB_ID =
            new NamedThreadLocal<>("Job ThreadLocal ID");

    @Resource
    private TaskApi timingTaskLogApi;

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.sunyard.framework.quartz.annotation.QuartzLog)")
    public void pointCut() {}

    /**
     * aop方法
     * @param point 切入点
     * @throws Throwable 异常
     */
    @Around("pointCut()")
    public void around(ProceedingJoinPoint point) throws Throwable {
        BEGIN_TIME_THREAD_LOCAL.set(System.nanoTime());
        // 获取任务ID
        Long jobId = this.getRealJobId(point);
        try {
            // 定时任务开始执行
            Result<Long> longResult = timingTaskLogApi.insertTaskLog(jobId + "");
            // 记录数据id到NOW_JOB_ID，用于更新日志信息
            if (longResult != null && longResult.getData() != null) {
                NOW_JOB_ID.set(longResult.getData());
            }
            super.inputConsoleLogs(point, LogConstants.STOREY_JOB, null);
            // 执行目标方法并获取返回值
            point.proceed();
            //更新定时任务结束时间
            timingTaskLogApi.updateTaskLastRunTime(NOW_JOB_ID.get()+"");
            long elapsedTimeInMillis = (System.nanoTime() - BEGIN_TIME_THREAD_LOCAL.get()) / 1000000;
            super.outputConsoleLogs(point, LogConstants.STOREY_JOB, null, elapsedTimeInMillis);
        } catch (Throwable e) {
            log.error("定时任务执行异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 从JobExecutionContext中获取真实的任务ID
     * @param point 切入点
     * @return 任务ID
     */
    private Long getRealJobId(ProceedingJoinPoint point) {
        // 获取方法参数
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            JobExecutionContext context = (JobExecutionContext) args[0];
            // 从JobDetail的key中获取任务ID
            String jobKey = context.getJobDetail().getKey().getName();
            try {
                return Long.parseLong(jobKey);
            } catch (NumberFormatException e) {
                log.error("获取任务ID失败，使用雪花算法生成新ID", e);
            }
        }
        // 如果无法获取真实任务ID，回退到使用雪花算法生成
        return IdUtil.getSnowflake(1, 1).nextId();
    }

    /**
     * 切入方法执行异常后执行的操作，用于记录定时任务失败时的异常信息并存储到数据库
     * @param joinPoint 切入点
     * @param exception 异常信息
     */
    @AfterThrowing(value = "pointCut()", throwing = "exception")
    public void doAfterThrowingAdvice(JoinPoint joinPoint, Throwable exception) {
        int responseCode = LogConstants.EXCEPTION;
        String mgs = SunyardException.printToStr(exception);
        // 根据异常类型确定响应代码
        if (exception instanceof SunyardException) {
            SunyardException e = (SunyardException)exception;
            if (e.getResultCode() == null) {
                responseCode = LogConstants.FAIL;
                mgs = exception.getMessage();
            } else if (!e.getResultCode().getCode().equals(ResultCode.SYSTEM_BUSY_ERROR.getCode())) {
                responseCode = LogConstants.FAIL;
                mgs = exception.getMessage();
            }
        } else if (exception instanceof IllegalArgumentException) {
            responseCode = LogConstants.FAIL;
            mgs = exception.getMessage();
        } else {
            // 处理其他类型的异常
            responseCode = LogConstants.EXCEPTION;
            mgs = exception.getMessage() != null ? exception.getMessage() : "未知异常";
        }
        // 记录异常日志到控制台/日志文件
        log.warn("Job Exception: {}", mgs, exception);
        // 获取当前任务ID并更新异常信息到数据库
        Long jobId = NOW_JOB_ID.get();
        try {
            // 调用API更新任务日志的错误信息到数据库
            timingTaskLogApi.updateTaskLogError(jobId, exception.toString());
            log.debug("定时任务[{}]异常信息已成功记录到数据库", jobId);
        } catch (Exception e) {
            // 记录数据库操作失败的日志
            log.error("更新定时任务异常日志到数据库失败", e);
        }
    }

}
