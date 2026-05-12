package com.sunyard.module.system.api.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author liugang
 * @since 2021-12-02
 */
@Data
public class SysLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 机构id
     */
    private Long instId;
    /**
     * 操作人id
     */
    private String userName;

    /**
     * 请求-ip
     */
    private String requestIp;

    /**
     * 请求-接口url
     */
    private String requestUrl;

    /**
     * 请求-接口-中文功能描述
     */
    private String requestDesc;

    /**
     * 请求-入参
     */
    private String requestParams;

    /**
     * 日志状态(成功:0,失败:1,异常:2)
     */
    private Integer responseCode;

    /**
     * 异常信息
     */
    private String exceptionMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 日志开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date logsStartDate;

    /**
     * 日志结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date logsEndDate;

    /**
     * 报表统计计数
     */

    private Integer num;
    /**
     * 用户id集合
     */
    private List<Long> userIds;

    /**
     * 报表统计
     */
    private String groupStr;
    /**
     * 分页计算字段
     */
    private Integer size;
    /**
     * 页数
     */
    private Integer pageSize;

    /**
     * 系统类型
     */
    private String logSystem;
}
