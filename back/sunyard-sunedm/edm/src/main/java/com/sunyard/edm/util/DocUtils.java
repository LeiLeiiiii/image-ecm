package com.sunyard.edm.util;

import com.sunyard.edm.constant.DocConstants;

import java.text.DecimalFormat;

/**
 * @Author PJW 2023/2/2 16:26
 * 文档公用方法
 */
public class DocUtils {

    /**
     * 计算文件大小
     *
     * @return 1GB
     * @Author PJW
     */
    public static String getFilseSize(Long size) {
        if (size == null) {
            return null;
        }
        //定义GB的计算常量
        int gb = DocConstants.FILESIZE * DocConstants.FILESIZE * DocConstants.FILESIZE;
        //定义MB的计算常量
        int mb = DocConstants.FILESIZE * DocConstants.FILESIZE;
        //定义KB的计算常量
        int kb = DocConstants.FILESIZE;
        try {

            // 格式化小数
            DecimalFormat df = new DecimalFormat("0.00");
            String resultSize = "";
            if (size / gb >= 1) {
                //如果当前Byte的值大于等于1GB
                resultSize = df.format(size / (float) gb) + "GB";
            } else if (size / mb >= 1) {
                //如果当前Byte的值大于等于1MB
                resultSize = df.format(size / (float) mb) + "MB";
            } else if (size / kb >= 1) {
                //如果当前Byte的值大于等于1KB
                resultSize = df.format(size / (float) kb) + "KB";
            } else {
                resultSize = size + "B";
            }
            return resultSize;
        } catch (Exception e) {
            return null;
        }
    }
}
