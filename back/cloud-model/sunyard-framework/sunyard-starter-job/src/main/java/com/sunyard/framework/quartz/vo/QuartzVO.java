package com.sunyard.framework.quartz.vo;

import lombok.Data;

/**
 * @author P-JWei
 * @date 2023/4/11 15:45 @title：
 * @description:
 */

@Data
public class QuartzVO {
    /**
     * 任务id
     */
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务组名
     */
    private String serviceName;

    /**
     * 任务状态 启动还是暂停
     */
    private Integer status;

    /**
     * 初始化策略
     */
    private Integer initType;

    /**
     * 调用实例
     */
    private String classAbsolutePath;

    /**
     * 任务运行时间表达式
     */
    private String cronExpression;
}
