package com.sunyard.framework.licence.task.service;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.common.util.ip.IpUtils;
import com.sunyard.framework.licence.config.ServerAuthProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * @Type
 * @Desc 服务器授权
 * 使用Component注解，跳过service日志aop切面日志输出
 */
@Slf4j
@Component
public class ServerAuthService {

    @Resource
    private ServerAuthProperties serverAuthProperties;

    public Result<Boolean> verifyServerAuth(String onlyFrontDate, String onlyDate) {
        Boolean isAuth = false;//授权校验开关
        if (!isAuth) {
            return Result.success(false);
        }
        Boolean result = false;
        //解密
        byte[] file = Sm2Util.decrypt(Hex.decode(serverAuthProperties.getServerLIC()),
                serverAuthProperties.getPrivateKey());
        String string_file = new String(file, StandardCharsets.UTF_8);
        JSONObject jsonObject = JsonUtils.parseObject(string_file);
        int maxInstances = jsonObject.getInteger("maxInstances");
        String expireDate = jsonObject.getString("expireDate");
        List<JSONObject> allowedMacsList = jsonObject.getJSONArray("allowedIpMacs")
                .toJavaList(JSONObject.class);
        LocalDate today = LocalDate.now(); // 获取当前日期
        LocalDate exDate = LocalDate.parse(expireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));//授权到期时间
        if ("1".equals(onlyFrontDate)) {//1.前端进行授权提醒
            LocalDate pastDate = exDate.minus(30, ChronoUnit.DAYS); //获取N天前的日期
            if (today.isAfter(pastDate)) {
                result = true;//需要提醒
                log.info("服务器授权快到期!");
                return Result.success(result);
            }
        } else {
            if ("1".equals(onlyDate)) {//2.晚上定时任务校验时间
                if (today.isAfter(exDate)) {
                    result = true;//授权到期
                    log.info("服务器授权已到期!");
                    return Result.success(result);
                }
            } else {//3.启动校验
                if (today.isAfter(exDate)) {
                    result = true;//授权到期
                    log.info("服务器授权已到期!");
                    return Result.success(result);
                }
                AssertUtils.isNull(serverAuthProperties.getAppName(), "服务名不能为空！");
                Properties properties = new Properties();
                properties.put("serverAddr", serverAuthProperties.getNacosUrl());
                properties.put("namespace", serverAuthProperties.getNacosNamespace());
                NamingService namingService = null;
                try {
                    namingService = NacosFactory.createNamingService(properties);
                    // 指定服务名称
                    String serviceName = serverAuthProperties.getAppName();
                    // 获取所有实例
                    List<Instance> instances = namingService.getAllInstances(serviceName);
                    if (instances.size() > maxInstances) {
                        result = true;//实例超过最大值
                        log.info("服务器授权实例数超过授权最大值,请确认!");
                        return Result.success(result);
                    }
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
                if (serverAuthProperties.getAppName().toUpperCase().indexOf("SYSTEM") > -1) {//system系统需要校验mac
                    boolean macFlag = false;
                    for (JSONObject macInfo : allowedMacsList) {
                        try {
                            String localMac = IpUtils.getMacAddressByIp(
                                    InetAddress.getByName(macInfo.getString("ip")));
                            if (IpUtils.matchMacEqual(localMac, macInfo.getString("mac"))) {
                                macFlag = true;
                                break;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (!macFlag) {
                        result = true;
                        log.info("服务器授权未授权该IP,MAC,请确认!");
                        return Result.success(result);
                    }
                }

            }
        }
        return Result.success(result);
    }

}
