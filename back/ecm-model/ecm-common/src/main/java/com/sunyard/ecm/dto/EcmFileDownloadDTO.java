package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/21 16:55
 * @desc 文件下载DTo
 */
@Data
public class EcmFileDownloadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务id
     */
    private Long busiId;

    /**
     * 资料节点id
     */
    private Long docId;


}
