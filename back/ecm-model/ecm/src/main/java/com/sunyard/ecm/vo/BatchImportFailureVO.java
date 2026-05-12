package com.sunyard.ecm.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 批量导入失败信息vo
 */
@Data
@Builder
public class BatchImportFailureVO implements Serializable {

    /**
     * 业务类型
     */
    private String appName;
    /**
     * 业务主索引
     */
    private String busiIndex;
    /**
     * 失败原因（如：主索引重复）
     */
    private String reason;
}
