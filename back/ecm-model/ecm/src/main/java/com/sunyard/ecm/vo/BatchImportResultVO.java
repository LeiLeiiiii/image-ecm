package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量导入返回vo
 */
@Data
public class BatchImportResultVO implements Serializable {
    /**
     * 总条数
     */
    private Integer total;
    /**
     * 成功数量
     */
    private Integer successCount;
    /**
     * 失败数量
     */
    private Integer failCount;
    /**
     * 失败明细
     */
    private List<BatchImportFailureVO> failures;
}
