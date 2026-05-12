package com.sunyard.ecm.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zyl
 * @since 2023/11/22 17:15
 * @Description 业务日志VO
 */
@Data
public class EcmBusiLogPrintDownVO {
    /**
     * 0：打印操作，1：下载操作，2：其他操作
     */
    private Integer type ;
    /**
     * 操作的文件id集合
     */
    private List<Long> fileIds ;
    /**
     * 业务主键id
     */
    private Long busiId;
}
