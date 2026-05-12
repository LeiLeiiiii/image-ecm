package com.sunyard.sunafm.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2024/3/11 9:23:32
 * @title
 * @description
 */
@Data
public class AfmDetOnlineListVO implements Serializable {

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
     * 上传时间左区间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeStart;

    /**
     * 上传时间右区间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeEnd;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 上传人
     */
    private String uploadUser;

}
