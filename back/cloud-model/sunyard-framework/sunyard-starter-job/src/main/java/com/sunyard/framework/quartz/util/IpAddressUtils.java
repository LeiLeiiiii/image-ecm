package com.sunyard.framework.quartz.util;
/*
 * Project: Sunyard
 *
 * File Created at 2026/1/15
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

/**
 * @author Leo
 * @Desc
 * @date 2026/1/15 14:56
 */

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;

/**
 * IP地址获取工具类
 * 用于获取真实的服务器IP地址，避免获取到127.0.0.1等本地地址
 */
@Slf4j
public class IpAddressUtils {

    /**
     * 获取本机真实IP地址
     *
     * @return 真实IP地址，如果获取失败则返回localhost
     */
    public static String getRealIpAddress() {
        try {
            // 首先尝试通过Socket连接获取
            String ip = getIpBySocket();
            if (isValidIpAddress(ip)) {
                return ip;
            }

            // 如果Socket方式失败，则遍历网卡获取
            ip = getIpByNetworkInterface();
            if (isValidIpAddress(ip)) {
                return ip;
            }

            // 最后的备选方案
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.error("获取IP地址失败", e);
            return "127.0.0.1";
        }
    }

    /**
     * 通过Socket连接方式获取本机IP
     * 这种方式可以获取到真正对外通信的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    private static String getIpBySocket() throws SocketException {
        try (DatagramSocket socket = new DatagramSocket()) {
            // 尝试连接到一个远程地址来确定本地IP
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String localIpAddress = socket.getLocalAddress().getHostAddress();
            return localIpAddress;
        } catch (Exception e) {
            log.debug("通过Socket获取IP失败", e);
            return null;
        }
    }

    /**
     * 通过遍历网络接口获取IP地址
     * 排除回环、虚拟等非真实网卡
     *
     * @return IP地址
     * @throws SocketException
     */
    private static String getIpByNetworkInterface() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            // 跳过回环接口、虚拟接口和未激活的接口
            if (networkInterface.isLoopback() || networkInterface.isVirtual()
                    || !networkInterface.isUp()) {
                continue;
            }

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                // 只考虑IPv4地址，排除回环地址、链路本地地址
                if (address instanceof Inet4Address && !address.isLoopbackAddress()
                        && !address.isLinkLocalAddress()) {

                    String ip = address.getHostAddress();
                    if (isValidIpAddress(ip)) {
                        return ip;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 检查IP地址是否有效（不是本地回环地址）
     *
     * @param ip IP地址字符串
     * @return 是否为有效IP地址
     */
    private static boolean isValidIpAddress(String ip) {
        return ip != null && !ip.isEmpty() && !"127.0.0.1".equals(ip) && !"localhost".equals(ip)
                && !ip.startsWith("169.254"); // 排除APIPA地址
    }

    /**
     * 获取完整的主机标识（IP:端口格式）
     *
     * @param port 端口号
     * @return 完整的主机标识
     */
    public static String getHostWithPort(int port) {
        String ip = getRealIpAddress();
        return ip + ":" + port;
    }
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2026/1/15 Leo creat
 */
