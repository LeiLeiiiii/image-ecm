package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author P-JWei
 * @date 2023/5/15 15:55 @title：
 * @description:
 */
@FeignClient(value = ApiConstants.NAME)
public interface ParamApi {

    String PREFIX = ApiConstants.PREFIX + "/param/";

    /**
     * 根据key查询系统配置
     * 
     * @param key key
     * @return Result
     */
    @PostMapping(PREFIX + "searchValueByKey")
    Result<SysParamDTO> searchValueByKey(@RequestParam("key") String key);

    /**
     *  更新配置
     * @param key key
     * @param value 更新值
     * @return Result
     */
    @PostMapping(PREFIX + "updateValueByKey")
    Result<Boolean> updateValueByKey(@RequestParam("key") String key, @RequestParam("value") String value);

    /**
     * 修改状态和值
     * @param key key
     * @param value 值
     * @param status 状态
     * @return Result
     */
    @PostMapping(PREFIX + "updateValueAndStatusByKey")
    Result<Boolean> updateValueAndStatusByKey(@RequestParam("key")String key, @RequestParam("value") String value, @RequestParam("status") Integer status);
}
