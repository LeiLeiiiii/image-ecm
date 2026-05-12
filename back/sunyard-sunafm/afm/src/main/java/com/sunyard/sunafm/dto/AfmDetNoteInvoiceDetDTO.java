package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 检测记录-发票检测-结果返回参
 * @author P-JWei
 * @date 2024/3/11 16:56:28
 * @title
 * @description
 */
@Data
public class AfmDetNoteInvoiceDetDTO implements Serializable {

    /**
     * 是否验真（0通过 1不通过）
     */
    private Integer isVerify;

    /**
     * 发票查重结果list（验真不通过，此list为空）
     */
    private List<AfmDetNoteResultDetailsDTO> invoiceDupList;

    /**
     * 发票连续结果list（验真不通过，此list为空）
     */
    private List<AfmDetNoteResultDetailsDTO> invoiceLinkList;
}
