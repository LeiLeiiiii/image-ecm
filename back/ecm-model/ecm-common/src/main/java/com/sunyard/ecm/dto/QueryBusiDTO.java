package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ypy
 * @since 2025/9/19 17:08
 * @desc 查询业务DTO
 */
@Data
public class QueryBusiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 基本信息
     */
    private EcmBaseInfoDTO ecmBaseInfoDTO;
    
    /**
     * 业务类型代码
     */
    private String appCode;

    /**
     * 业务编号
     */
    private String busiNo;

    /**
     * 查询业务创建开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startBusiCreate;

    /**
     * 查询业务创建时间结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endBusiCreate;

    /**
     * 业务属性
     */
    private List<EcmBusiAttrDTO> queryCriteria;
}
