package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmAsyncTaskGroupDTO;
import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/3/3
 */
@Data
public class EcmAsyncTaskVO {

    /**
     * 业务ID
     */
    private Long busiId;

    /**
     * 任务分组列表
     */
    private List<EcmAsyncTaskGroupDTO> ecmAsyncTaskGroupDTOList;


}
