package com.sunyard.module.auth.api;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.auth.api.dto.OpenAuthDTO;
import com.sunyard.module.auth.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author P-JWei
 * @date 2023/11/6 13:46:15
 * @title 对外接口认证
 * @description
 */
@FeignClient(name = ApiConstants.NAME)
public interface OpenAuthApi {
    String PREFIX = ApiConstants.PREFIX + "/openAuth/";

    /**
     * 注销token
     * 当appID权限发生变化时，注销掉当前token
     * @param appId appId
     * @return Result
     */
    @PostMapping(value = PREFIX + "revokeToken")
    Result<Boolean> revokeToken(@RequestParam("appId") String appId);

    /**
     * 对外api授权校验
     *
     * @param openAuthDTO appId对象
     * @return Result
     */
    @PostMapping(value = PREFIX + "openAuthToApi")
    JSONObject openAuthToApi(@RequestBody OpenAuthDTO openAuthDTO);

    @PostMapping(value = PREFIX + "openJwtToApi")
    JSONObject openJwtToApi(@RequestBody OpenAuthDTO openAuthDTO);

    @PostMapping(value = PREFIX + "getTokenJwt")
    Result<String> getTokenJwt(@RequestParam("flagId") String flagId);
}
