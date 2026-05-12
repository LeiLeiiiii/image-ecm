package com.sunyard.ecm.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author scm
 * @since 2023/8/21 16:55
 */
@Data
@ToString
@Accessors(chain = true)
public class EcmDownloadFileDTO{

    private EcmBaseInfoDTO ecmBaseInfoDTO;

    /**
     * 文件id
     */
    private List<Long> files;

    /**
     * 业务类型code
     */
    private String appCode;

    /**
     * 业务编号
     */
    private String busiNo;

    /**
     * 资料节点
     */
    private String docNo;

    /**
     * 下载的位置
     */
    private String path;


    /**
     * 是否打包，0：不打包，1：打包
     */
    private Integer isPack;


}
