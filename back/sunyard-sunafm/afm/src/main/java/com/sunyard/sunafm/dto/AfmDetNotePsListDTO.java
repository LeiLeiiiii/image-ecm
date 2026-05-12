package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测记录-篡改列表返回参
 * @author P-JWei
 * @date 2024/3/8 10:32:55
 * @title
 * @description
 */
@Data
public class AfmDetNotePsListDTO implements Serializable {


    /**
     * 记录id
     */
    private Long id;

    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 主索引
     */
    private String businessIndex;

    /**
     * 资料类型
     */
    private String materialType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 上传人
     */
    private String uploadUser;

    /**
     * 检测时间
     */
    private Date psDetTime;

    /**
     * 检测结果（字典：0、1）
     */
    private Integer psDetResult;

    /**
     * 检测结果（字典值：正常、疑似篡改）
     */
    private String psDetResultStr;

}
