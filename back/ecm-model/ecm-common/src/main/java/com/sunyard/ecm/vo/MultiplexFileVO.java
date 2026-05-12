package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zyl
 * @since 2023/8/1 17:36
 * @Description 文件复制VO
 */
@Data
public class MultiplexFileVO implements Serializable {

    /**
     * 目标资料节点id
     */
    private List<String> targetDocId;

    /**
     * 目标资料节点的业务id
     */
    private Long targetBusiId;

    /**
     * 目标资料类型id
     */
    private List<String> targetDocTypeId;

    /**
     * 目标文件id
     */
    private List<Long> targetFileId;
}
