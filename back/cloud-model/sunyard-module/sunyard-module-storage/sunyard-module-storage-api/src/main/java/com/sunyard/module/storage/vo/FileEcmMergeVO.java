package com.sunyard.module.storage.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;


/**
 * @author： yzy
 * @create： 2025/3/25
 */
@Data
public class FileEcmMergeVO extends FileMergeVO implements Serializable {

    private Long userId;

    private List<Long> newFileIdList;

    private List<String> newFileNames;

    private String docCode;

    private String docId;

    private Object ecmFileInfoDTO;


}
