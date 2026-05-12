package com.sunyard.ecm.dto.ecm;

import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/3/3
 */
@Data
public class EcmAsyncTaskGroupDTO {

    /**
     * 类型
     */
    private String taskName;

    /**
     * 类型ID
     */
    private Integer taskId;

    /**
     * 总数量
     */
    private Integer total;

    /**
     * 处理中数量
     */
    private Integer processingCount;

    /**
     * 处理中数量
     */
    private List<EcmAsyncTaskDTO> data;

}
