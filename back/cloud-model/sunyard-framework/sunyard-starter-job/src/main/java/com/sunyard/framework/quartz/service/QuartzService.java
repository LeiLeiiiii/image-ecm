package com.sunyard.framework.quartz.service;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.quartz.vo.QuartzVO;

/**
 * @author P-JWei
 * @date 2024/1/19 17:01:02
 * @title
 * @description
 */
public interface QuartzService {

    /**
     * 开启定时任务
     * @param id 实例id
     * @return Result
     */
    Result begin(Long id);

    /**
     * 更新定时任务
     * @param quartzVo 实例对象
     * @return Result
     */
    Result update(QuartzVO quartzVo);

    /**
     * 暂停定时任务
     * @param id 实例id
     * @return Result
     */
    Result stop(Long id);

    /**
     * 立即运行一次
     * @param id 实例id
     * @return Result
     */
    Result runNow(Long id);
}
