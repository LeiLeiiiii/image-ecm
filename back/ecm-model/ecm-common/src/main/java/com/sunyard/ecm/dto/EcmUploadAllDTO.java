package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 文件上传DTO
 */
@Data
public class EcmUploadAllDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private AddBusiDTO ecmRootDataDTO;
    /**
     * 业务资料文件信息（docNo为空，标识文件上传至业务节点）
     */
    private List<EcmUploadFileDTO> splitDTO;

    /**
     * 资料节点
     */
    private String docNo;
}
