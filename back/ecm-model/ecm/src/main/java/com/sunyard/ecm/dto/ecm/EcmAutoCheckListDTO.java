package com.sunyard.ecm.dto.ecm;

import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/3/19
 */
@Data
public class EcmAutoCheckListDTO {

    private Long fileId;
    private Long newFileId;
    private List<String> docCodes;
    private String fileName;
}
