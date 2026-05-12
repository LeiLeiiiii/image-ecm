package com.sunyard.ecm.oldToNew.dto;

import lombok.Data;

/**
 * @author yzy
 * @desc
 * @since 2025/2/24
 */
@Data
public class FileNameWithNodeDTO {
    private String fileName;
    private String nodeId;

    public FileNameWithNodeDTO(String fileName, String nodeId) {
        this.fileName = fileName;
        this.nodeId = nodeId;
    }
}
