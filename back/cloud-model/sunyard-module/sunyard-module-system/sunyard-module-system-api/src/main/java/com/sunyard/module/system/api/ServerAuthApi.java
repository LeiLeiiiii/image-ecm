package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description: 服务授权校验
 */
@FeignClient(value = ApiConstants.NAME)
public interface ServerAuthApi {

    String PREFIX = ApiConstants.PREFIX + "/serverAuth/";

    /**
     * 校验服务器授权
     *
     * @return Result
     */
    @PostMapping(PREFIX + "verifyServerAuth")
    Result<Boolean> verifyServerAuth(@RequestParam("onlyFrontDate") String onlyFrontDate,@RequestParam("onlyDate") String onlyDate);

}
