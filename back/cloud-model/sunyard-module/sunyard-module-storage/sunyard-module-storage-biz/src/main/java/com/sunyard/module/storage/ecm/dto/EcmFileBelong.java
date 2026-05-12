package com.sunyard.module.storage.ecm.dto;

import lombok.Data;

/**
 * @author yzy
 * @desc
 * @since 2025/12/1
 */
@Data
public class EcmFileBelong {
    private String busiNo;
    private String docName;

    public EcmFileBelong(String busiNo, String docName) {
        this.busiNo = busiNo;
        this.docName = docName;
    }
}
