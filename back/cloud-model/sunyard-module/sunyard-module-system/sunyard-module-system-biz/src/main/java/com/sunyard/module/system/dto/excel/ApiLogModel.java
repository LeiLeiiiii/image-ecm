package com.sunyard.module.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author P-JWei
 * @date 2023/8/21 13:51:23
 * @title
 * @description
 */
@Data
public class ApiLogModel {

    /**
     * 请求-接口-中文功能描述
     */
    @ExcelProperty("接口名称")
    private String requestDesc;

    /**
     * 请求地址
     */
    @ExcelProperty("请求地址")
    private String requestIp;

    /**
     * 请求内容
     */
    @ExcelProperty("请求内容")
    private String requestParams;

    /**
     * 操作时间
     */
    @ExcelProperty("操作时间")
    private Date createTime;

    /**
     * 日志状态
     */
    @ExcelProperty("日志状态")
    private String responseCodeStr;

    /**
     * 异常信息
     */
    @ExcelProperty("异常信息")
    private String exceptionMsg;
}
