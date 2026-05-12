package com.sunyard.module.storage.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author lyq
 * @Description 水印配置相关内容
 * @Date 2023/8/4 13:55
 */
@Data
public class WaterMarkConfigVO {

    /**
     * 配置是否开启（下载 打印 查看）
     */
    public Boolean openFlag;

    /**
     * 水印内容
     */
    public String markValue;

    /**
     * 字体
     */
    public String waterStyleFamilyValue;


    /**
     * 颜色
     */
    public String color1;

    /**
     * 倾斜度
     */
    public Integer num;

    public Boolean switchValueInvisible;

    /**
     * 查看
     */
    public Boolean switchValueShow;
    /**
     * 下载
     */
    public Boolean switchValueDownload;
    /**
     * 打印
     */
    public Boolean switchValuePrint;
    public String contentValue;
    public List<String> checkList;
    /**
     * 字号
     */
    public String waterStyleSizeValue;


}
