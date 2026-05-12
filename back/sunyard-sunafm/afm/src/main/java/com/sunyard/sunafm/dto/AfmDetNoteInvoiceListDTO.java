package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测记录-发票检测列表返回参
 * @author P-JWei
 * @date 2024/3/11 16:08:39
 * @title
 * @description
 */
@Data
public class AfmDetNoteInvoiceListDTO implements Serializable {

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
    private Date invoiceDetTime;

    /**
     * 检测结果（字典：0、1、2、3）
     */
    private Integer invoiceDetResult;

    /**
     * 检测结果（字典值：正常、验真不通过、发票号重复、发票号连续）
     */
    private String invoiceDetResultStr;

}
