package com.sunyard.framework.common.util;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @date 2022/3/18 14:31
 * @Desc
 */
@Slf4j
public class IoUtils {

    /**
     * 做一个关闭流接口的形参，里面的参数代表可变参数，不管传进来多少个参数都会放到数组里面
     * @param io io
     */
    public static void close(Closeable... io) {
        for (Closeable closeable : io) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * byte转输入流
     * @param buf bate数组
     * @return InputStream
     */
    public static final InputStream byte2Input(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }

    /**
     * 输入流转byte
     * @param inStream 输出流
     * @return byte
     * @throws IOException 异常
     */
    public static final byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    /**
     * 关闭输入流
     * @param in 输入流
     */
    public static void closeInputStream(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            } finally {
                in = null;
            }
        }
    }

    /**
     * 关闭输出流
     * @param out 输出流
     */
    public static void closeOutputStream(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            } finally {
                out = null;
            }
        }
    }

    /**
     * 关闭channel连接
     * @param channel channel连接
     */
    public static void closeFileChannel(FileChannel channel) {
        if (channel != null) {
            try {
                if (channel.isOpen()) {
                    channel.close();
                }
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            } finally {
                channel = null;
            }
        }
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/3/18 zhouleibin creat
 */
