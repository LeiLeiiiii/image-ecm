package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zyl
 * @Description 影像复制VO
 * @since 2023/8/1 17:36
 */
@Data
public class EcmMultiplexFileVO implements Serializable {

    /**
     * 目标资料节点id
     */
    private List<Long> targetDocId;

    /**
     * 目标资料节点的业务id
     */
    private Long targetBusiId;

    /**
     * 目标资料类型id
     */
    private List<Long> targetDocTypeId;
}
