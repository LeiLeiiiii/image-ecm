package com.sunyard.module.auth.util;

import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSON;
import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.util.encryption.SignatureUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.auth.config.properties.SignAuthProperties;
import com.sunyard.module.auth.constant.CachePrefixConstants;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author P-JWei
 * @date 2024/4/10 15:25:29
 * @title
 * @description
 */
@Slf4j
@Component
public class OAuthHandleUtils {

    /**忽略时间戳filter的redis的key*/
    public final static String TIMESTAMPIGNORE = "timestampIgnore";
    /**忽略refererFilter的redis的key*/
    public final static String REFERERIGNORE = "refererIgnore";
    /**忽略验签的redis的key*/
    public final static String SIGNIGNORE = "signIgnore";

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SignAuthProperties signAuthProperties;

    /**
     * 时间戳校验
     * @param timestamp 时间戳
     * @param url 请求url
     * @return bool
     */
    public boolean timestampFilter(String timestamp, String url) {
        return isIgnore(url, TIMESTAMPIGNORE) || validTimestamp(timestamp);

    }

    /**
     * referer校验
     * @param oldReferer 运行referer
     * @param referer 传入referer
     * @param url 请求url
     * @return bool
     */
    public boolean refererFilter(String oldReferer, String referer, String url) {
        return isIgnore(url, REFERERIGNORE) || validReferer(referer, oldReferer);
    }

    /**
     * 验签
     * @param appId appId
     * @param sign 签名
     * @param timestamp 时间戳
     * @param params 参数
     * @param url 请求url
     * @return bool
     */
    public boolean verifySignatureFilter(String appId, String sign, String timestamp, String params, SysApiSystemDTO sysApiSystem, String url) {
        return isIgnore(url, SIGNIGNORE) || validSign(appId, sign, timestamp, params, sysApiSystem);
    }

    /**
     * 缓存校验结果
     * @param key key
     * @param value 值
     * @param seconds 过期时间
     */
    public void toRedisPassResult(String key, String value, Long seconds) {
        redisUtils.set(CachePrefixConstants.AUTH + key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取检测结果
     * @param key key
     * @return String
     */
    public String getRedisPassResult(String key) {
        return redisUtils.get(CachePrefixConstants.AUTH + key);
    }

    /**
     * 判断是否跳过此Filter
     *
     * @param url 请求url
     * @return Result
     */
    private boolean isIgnore(String url, String redisKey) {
        String ignoreJson = redisUtils.get(CachePrefixConstants.AUTH + redisKey);
        if (!StringUtils.hasText(ignoreJson)) {
            return false;
        }
        List<String> ignoreList = JSON.parseArray(ignoreJson, String.class);
        return ignoreList.stream().anyMatch(url::matches);
    }

    /**
     * 校验时间区间是否在60S内
     *
     * @param timestamp 时间戳
     * @return Result
     */
    private boolean validTimestamp(String timestamp) {
        try {
            if(StringUtils.isEmpty(timestamp)){
                return false;
            }
            long now = Instant.now().toEpochMilli();
            return now - Long.parseLong(timestamp) < Long.valueOf(signAuthProperties.getApiSignTimeOut());
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验referer
     *
     * @param referer referer
     * @return Result
     */
    private boolean validReferer(String referer, String oldReferer) {
        boolean contains = false;
        try {
            if (!"".equals(referer)) {
                return oldReferer.contains(checkRefererIsIp(referer));
            }
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        return contains;
    }

    /**
     * 验签
     *
     * @param appId     appId
     * @param sign      签名
     * @param timestamp 时间戳
     * @param params    参数
     * @return Result
     */
    private boolean validSign(String appId, String sign, String timestamp, String params, SysApiSystemDTO sysApiSystem) {
        try {
            if(StringUtils.isEmpty(params)){
                return true;
            }
            String decrypt = new String(Base64Utils.decodeFromString(params));
            //拿到appid、timestamp、body验签。
            if (sysApiSystem.getSignType().contains(LoginEncryptionConstant.SM2.toUpperCase())) {
                return Sm2Util.sm2CheckContent(appId + decrypt, sign, sysApiSystem.getPublicKey());
            } else if (sysApiSystem.getSignType().contains(LoginEncryptionConstant.RSA.toUpperCase())) {
                return SignatureUtils.rsaCheckContent(appId + decrypt, sign, sysApiSystem.getPublicKey());
            }
            return false;
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 检测是http还是https
     * @param referer referer
     * @return String
     */
    private String checkRefererIsIp(String referer) {
        String refererIp = "";
        try {
            if (referer.matches("^https?://.*$")) {
                URL url = URLUtil.url(referer);
                refererIp = url.getHost();
            } else {
                refererIp = referer;
            }
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        return refererIp;
    }

    /**
     * 验签

     * @param url 请求url
     * @return bool
     */
    public boolean signVerifyFilter(String url) {
        return isIgnore(url, SIGNIGNORE);
    }
}
