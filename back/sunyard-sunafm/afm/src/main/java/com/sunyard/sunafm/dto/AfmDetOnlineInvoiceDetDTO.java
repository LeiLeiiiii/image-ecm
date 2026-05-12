package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 在线检测-发票检测-结果返回参
 * @author P-JWei
 * @date 2024/3/11 15:42:21
 * @title
 * @description
 */
@Data
public class AfmDetOnlineInvoiceDetDTO implements Serializable {

    /**
     * 源文件base64
     */
    private String sourceFileBase64;

    /**
     * 是否验真（0通过 1不通过）
     */
    private Integer isVerify;

    /**
     * 记录id
     */
    private Long noteId;

    /**
     * 发票查重结果list
     */
    private List<AfmDetOnlineResultDetailsDTO> invoiceDupList;

    /**
     * 发票连续结果list
     */
    private List<AfmDetOnlineResultDetailsDTO> invoiceLinkList;
}
