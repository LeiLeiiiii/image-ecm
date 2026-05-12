package com.sunyard.module.auth.api.dto;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.module.auth.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author P-JWei
 * @date 2023/11/6 13:46:15
 * @title 对外接口认证
 * @description
 */
@FeignClient(name = ApiConstants.NAME)
public interface SignAuthApi {
    String PREFIX = ApiConstants.PREFIX + "/signAuth/";

    /**
     * 对外api授权校验
     *
     * @param openAuthDTO appId对象
     * @return Result
     */
    @PostMapping(value = PREFIX + "openAuthToApi")
    JSONObject signAuthToApi(@RequestBody OpenAuthDTO openAuthDTO);

}
