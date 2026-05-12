package com.sunyard.framework.common.util.ip;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/23
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/23 15:30
 */
@Slf4j
public class IpUtils {

    /**
     * localAddresses 减少运行耗时，初始化时吧本机地址缓存起来 缺点，网络改变时，需要重启
     */
    private static List<String> localAddresses = new ArrayList<>();
    static {
        try {
            Enumeration<NetworkInterface> enumNi = NetworkInterface.getNetworkInterfaces();
            while (enumNi.hasMoreElements()) {
                NetworkInterface ifc = enumNi.nextElement();
                if (ifc.isUp()) {
                    Enumeration<InetAddress> enumAdds = ifc.getInetAddresses();
                    while (enumAdds.hasMoreElements()) {
                        InetAddress addr = enumAdds.nextElement();
                        localAddresses.add(addr.getHostAddress());
                    }
                }
            }
            localAddresses.add("localhost");
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断是否同一个地址，无论是主机名还是ip地址
     *
     * @param host1 地址1
     * @param host2 地址2
     * @return Result
     */
    public static boolean sameAddress(String host1, String host2) {
        if (StrUtil.isBlank(host1) || StrUtil.isBlank(host2)) {
            return false;
        }
        try {
            if (isLocal(host1)) {
                host1 = InetAddress.getLocalHost().getHostName();
            }
            if (isLocal(host2)) {
                host2 = InetAddress.getLocalHost().getHostName();
            }
            InetAddress addr1 = InetAddress.getByName(host1);
            InetAddress addr2 = InetAddress.getByName(host2);
            return addr1.getHostAddress().equals(addr2.getHostAddress());
        } catch (UnknownHostException e) {
            log.error("系统异常",e);
            return false;
        }
    }

    public static boolean isLocal(String host) {
        return localAddresses.contains(host);
    }

    /***
     * 获取客户端ip地址(可以穿透代理)
     *
     * @param request 请求头
     * @return Result
     */
    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!isValidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_VIA");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取客户端ip
     * 
     * @param request 请求头
     * @return Result
     */
    public static String getClientIpAddr(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_FORWARDED_FOR");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_FORWARDED");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("HTTP_VIA");
        }
        if (!isValidIp(ip)) {
            ip = request.getHeaders().getFirst("REMOTE_ADDR");
        }
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddress().getHostName();
        }
        return ip;
    }

    /**
     * 判断ip是否为空
     * 
     * @param ip ip地址
     * @return boo
     */
    private static boolean isValidIp(String ip) {
        return !(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip));
    }

    /**
     * 获取tomcat内置服务监听端口
     *
     * @return Result
     */
    public static int getTomcatPort() {
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            String port = objectNames.iterator().next().getKeyProperty("port");
            return Integer.parseInt(port);
        } catch (Exception e) {
            log.error("get tomcat port fail", e);
        }
        return 0;
    }

    /**
     * 获取客户端IP
     *
     * @param request 请求对象
     * @return IP地址
     * @author haod.liu
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     * @return 是否未知
     */
    public static boolean isUnknown(String checkString) {
        return isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen == 0) {
            return true;
        } else {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    /***/
    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }
    /**
     * @throws SocketException
     * @Title: getMacAddressByIp
     * @Description: 根据ip获取mac地址
     * @param @param ip
     * @param @return
     * @return String
     * @throws
     */
    public static String getMacAddressByIp(InetAddress ip) throws Exception{
        StringBuilder sb = new StringBuilder();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        if (network != null) {
            byte[] mac = network.getHardwareAddress();
            if (mac != null) {
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
            }
        }
        return sb.toString();
    }

    /*mac比较*/
    public static boolean matchMacEqual(String localMac, String encryptMac){
        boolean result = false;
        if(localMac.toUpperCase(Locale.ENGLISH).equals(encryptMac.toUpperCase(Locale.ENGLISH))){
            result = true;
        }else{
            localMac = localMac.replaceAll("-|:", "");
            encryptMac = encryptMac.replaceAll("-|:", "");
            if(localMac.toUpperCase(Locale.ENGLISH).equals(encryptMac.toUpperCase(Locale.ENGLISH))){
                result = true;
            }
        }
        return result;
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/23 Leo creat
 */
