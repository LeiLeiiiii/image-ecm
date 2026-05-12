package com.sunyard.module.storage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存工具类
 * 
 * @author PJW
 */
@Slf4j
public class CacheFileUtils {

    /**
     * 上传本地文件到服务器目录下
     * 
     * @param conn Connection对象
     * @param fileName 本地文件
     * @param remotePath 服务器目录
     */
    public static void putFile(Connection conn, String fileName, String remotePath) {
        SCPClient sc = new SCPClient(conn);
        try {
            // 将本地文件放到远程服务器指定目录下，默认的文件模式为 0600，即 rw，
            // 如要更改模式，可调用方法 put(fileName, remotePath, mode),模式须是4位数字且以0开头
            sc.put(fileName, remotePath);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 服务器目录下载到本地文件
     * 
     * @param conn Connection对象
     * @param fileName 远程文件
     * @param remotePath 本地服务器目录
     */
    public static void get(Connection conn, String fileName, String remotePath) {
        SCPClient sc = new SCPClient(conn);
        try {
            sc.get(fileName, remotePath);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 流式输出，用于浏览器下载
     * 
     * @param conn 连接
     * @param fileName 文件名
     * @param outputStream 输出流
     */
    public static void copyFile(Connection conn, String fileName, ByteArrayOutputStream outputStream) {
        SCPClient sc = new SCPClient(conn);
        try {
            sc.get(fileName, outputStream);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
    }

    public static String getIpAddress() {
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        String ipAddress = localHost.getHostAddress();
        return ipAddress;
    }

}
