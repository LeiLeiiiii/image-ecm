package com.sunyard.module.auth.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.auth.api.dto.OpenAuthDTO;
import com.sunyard.module.auth.api.dto.SignAuthApi;
import com.sunyard.module.auth.oauth.response.ResponseCodeEnum;
import com.sunyard.module.auth.util.SignAuthHandleUtils;
import com.sunyard.module.system.api.SysAuthApi;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2023/11/6 13:50:49
 * @title
 * @description
 */
@RestController
public class SignAuthApiImpl implements SignAuthApi {
    @Resource
    private SignAuthHandleUtils signAuthHandleUtils;
    @Resource
    private SysAuthApi sysAuthApi;

    @Override
    public JSONObject signAuthToApi(OpenAuthDTO openAuthDTO) {
        return signAuthToApi(openAuthDTO.getAppid(),
                openAuthDTO.getTimestamp(),
                openAuthDTO.getSign(),
                openAuthDTO.getReferer(),
                openAuthDTO.getUrl(),
                openAuthDTO.getParam());
    }

    /**
     * api认证
     *
     * @param appid     apiId
     * @param timestamp 时间戳
     * @param sign      加签
     * @param referer   referer
     * @param url       url
     * @param param     参数
     * @return Result
     */
    public JSONObject signAuthToApi(String appid, String timestamp, String sign, String referer, String url, String param) {
        //先从缓存拿结果，看是否存在
        String redisPassResult = signAuthHandleUtils.getRedisPassResult("cache_" + appid + url);
        if (StringUtils.hasText(redisPassResult)) {
            return JSONObject.parseObject(redisPassResult);
        }
        Result<SysApiSystemDTO> systemDTOResult = sysAuthApi.getApiByAppId(appid);
        SysApiSystemDTO sysApiSystem = systemDTOResult.getData();
        if (!url.contains("/api/ws/triggerQuery")) {
            if (!signAuthHandleUtils.timestampFilter(timestamp, url)) {
                return JSONObject.parseObject(JSON
                        .toJSONString(com.sunyard.module.auth.oauth.response.Result.error(ResponseCodeEnum.FAIL_TIMESTAMP)));
            }
            if (!signAuthHandleUtils.refererFilter(sysApiSystem.getSystemReferer(), referer, url)) {
                return JSONObject.parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result.error(ResponseCodeEnum.FAIL_REFERER)));
            }
            if (!signAuthHandleUtils.verifySignatureFilter(appid, sign, timestamp, param, sysApiSystem, url)) {
                return JSONObject.parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result.error(ResponseCodeEnum.FAIL_SIGN)));
            }
        }

        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result.success("")));
        //验证通过后，缓存进redis，保证一定时间内无须重复验证
        signAuthHandleUtils.toRedisPassResult("cache_" + appid + url, jsonObject.toJSONString(), 10L);
        //修改成功返回
        return jsonObject;
    }

}
