package com.sunyard.framework.quartz.api.impl;


import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.quartz.api.QuartzApi;
import com.sunyard.framework.quartz.service.QuartzService;
import com.sunyard.framework.quartz.vo.QuartzVO;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2023/5/24 10:03 @title：
 * @description:
 */
@RestController
public class QuartzApiImpl implements QuartzApi {

    @Resource
    private QuartzService quartzService;

    @Override
    public Result begin(Long id) {
        return quartzService.begin(id);
    }

    @Override
    public Result update(QuartzVO quartzVo) {
        return quartzService.update(quartzVo);
    }

    @Override
    public Result stop(Long id) {
        return quartzService.stop(id);
    }

    @Override
    public Result runNow(Long id) {
        return quartzService.runNow(id);
    }
}
