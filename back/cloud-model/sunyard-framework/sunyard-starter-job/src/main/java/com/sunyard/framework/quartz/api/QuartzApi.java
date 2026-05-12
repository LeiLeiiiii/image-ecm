package com.sunyard.framework.quartz.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.quartz.vo.QuartzVO;

/**
 * @author P-JWei
 * @date 2023/5/24 9:34
 * @title：公共feign接口
 * @description: 服务间互相调用
 */
public interface QuartzApi {
    /**
     * 启动定时任务
     * 
     * @param id 实例id
     * @return Result 无
     */
    @PostMapping("begin")
    Result begin(@RequestParam("id") Long id);

    /**
     * 修改定时任务
     * 
     * @param quartzVo 实例对象
     * @return Result 无
     */
    @PostMapping("update")
    Result update(@RequestBody QuartzVO quartzVo);

    /**
     * 根据id删除定时任务
     * 
     * @param id 实例id
     * @return Result 无
     */
    @PostMapping("stop")
    Result stop(@RequestParam("id") Long id);

    /**
     * 根据id立即执行一次定时任务
     * 
     * @param id 实例id
     * @return Result 无
     */
    @PostMapping("runNow")
    Result runNow(@RequestParam("id") Long id);
}
