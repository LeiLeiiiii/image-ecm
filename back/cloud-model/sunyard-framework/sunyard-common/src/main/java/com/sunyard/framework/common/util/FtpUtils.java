package com.sunyard.framework.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author P-JWei
 * @date 2023/9/8 11:12:04
 * @title
 * @description
 */
@Slf4j
public class FtpUtils {

    @Value("${upload.file_home:/home/temp/}")
    private static String fileHome;

    public static ChannelSftp getConnect(String host, int port, String username, String password) throws Exception {
        Session sshSession = null;
        ChannelSftp channel = null;
        JSch jsch = new JSch();
        jsch.getSession(username, host, port);
        //根据用户名，密码，端口号获取session
        sshSession = jsch.getSession(username, host, port);
        sshSession.setPassword(password);
        //修改服务器/etc/ssh/sshd_config 中 GSSAPIAuthentication的值yes为no，解决用户不能远程登录
        sshSession.setConfig("userauth.gssapi-with-mic", "no");
        //为session对象设置properties,第一次访问服务器时不用输入yes
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
        //获取sftp通道
        channel = (ChannelSftp) sshSession.openChannel("sftp");
        channel.connect();
        return channel;
    }

    /**
     * 获取SFTP服务器的文件流
     *
     * @param url
     * @param host
     * @param port
     * @param username
     * @param password
     * @return Result
     */
    public static InputStream getSFTPFileStream(String url, String host, Integer port, String username, String password) {
        ChannelSftp connect = null;
        UUID uuid = UUID.randomUUID();
        String remotePath = fileHome + uuid + "." + url.replaceFirst("^.*[.]", "").toLowerCase();
        File file = new File(remotePath);
        try(FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)) {
            //获取连接
            connect = FtpUtils.getConnect(host, port, username, password);
            InputStream in = connect.get(url);
            byte[] one = new byte[1024];
            int len = -1;
            while ((len = in.read(one)) != -1) {
                bos.write(one, 0, len);
            }
            //确保缓冲区数据被立即刷新到文件
            bos.flush();
            return new FileInputStream(file);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            // 关闭连接和删除临时文件
            if (connect != null) {
                connect.disconnect();
            }
            FileUtil.del(file);
        }
    }

    /**
     * @param channel   连接对象
     * @param directory 上传ftp的目录
     * @param inputStream     文件流
     * @param fileName  文件名称
     */
    public static void upload(ChannelSftp channel, String directory, InputStream inputStream, String fileName) {
        try {
            //执行列表展示ls 命令
            //执行盘符切换cd 命令
            String[] urlParts = directory.split(File.separatorChar == '\\' ? "\\\\" : "/");
            mkdirDir(channel, urlParts,"",urlParts.length,0);
            channel.ls(directory);
            channel.cd(directory);
            channel.put(inputStream, fileName);
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error(fileName + "关闭文件时.....异常!",e);
            }
        } catch (Exception e) {
            log.error("【子目录创建中】：", e);
            //创建子目录
            try {
                channel.mkdir(directory);
            } catch (SftpException ex) {
                log.error("错误信息为:{}",ex);
            }
        }
    }

    /**
     * ChannelSftp 在文件末尾追加数据
     * @param channel   连接对象
     * @param directory 上传ftp的目录
     * @param input     文件流
     * @param fileName  文件名称
     */
    public static void shardUpload(ChannelSftp channel, String directory, InputStream input, String fileName) {
        try {
            //执行列表展示ls 命令
            //执行盘符切换cd 命令
            String[] urlParts = directory.split(File.separatorChar == '\\' ? "\\\\" : "/");
            mkdirDir(channel, urlParts,"",urlParts.length,0);
            channel.ls(directory);
            channel.cd(directory);
            channel.put(input, fileName, ChannelSftp.APPEND);
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                log.error(fileName + "关闭文件时.....异常!",e);
            }
        } catch (Exception e) {
            log.error("【子目录创建中】：", e);
            //创建子目录
            try {
                channel.mkdir(directory);
            } catch (SftpException ex) {
                log.error("错误信息为:{}",ex);
            }
        }
    }


    /**
     * 递归根据路径创建文件夹
     *
     * @param dirs     根据 / 分隔后的数组文件夹名称
     * @param tempPath 拼接路径
     * @param length   文件夹的格式
     * @param index    数组下标
     */
    public static void mkdirDir(ChannelSftp channel, String[] dirs, String tempPath, int length, int index) {
        // 以"/a/b/c/d"为例按"/"分隔后,第0位是"";顾下标从1开始
        index++;
        if (index < length) {
            // 目录不存在，则创建文件夹
            tempPath += "/" + dirs[index];
        }
        try {
            log.info("检测目录[" + tempPath + "]");
            channel.cd(tempPath);
            if (index < length) {
                mkdirDir(channel,dirs, tempPath, length, index);
            }

        } catch (SftpException ex) {
            log.warn("创建目录[" + tempPath + "]");
            try {
                channel.mkdir(tempPath);
                channel.cd(tempPath);
            } catch (SftpException e) {
                log.error("创建目录[" + tempPath + "]失败,异常信息[" + e.getMessage() + "]",e);
            }
            log.info("进入目录[" + tempPath + "]");
            mkdirDir(channel,dirs, tempPath, length, index);
        }

    }
}
