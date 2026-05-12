package com.sunyard.ecm.vo;

import com.sunyard.ecm.po.EcmBusiInfo;
import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/12/1
 */
@Data
public class EcmScanDownLoadVO {

    /**
     * 业务信息
     */
    private List<EcmBusiInfo> busiInfos;

    /**
     * 是否打包下载
     */
    private Integer isPack;

    /**
     * 资料列表
     */
    private List<String> docCodes;
}
