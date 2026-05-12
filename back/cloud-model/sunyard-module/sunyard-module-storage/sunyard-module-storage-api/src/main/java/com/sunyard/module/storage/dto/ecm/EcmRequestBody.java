package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author 朱山成
 * @time 2023/5/29 22:29
 **/
@Data
public class EcmRequestBody implements Serializable {
    /**
     * 影像ip
     */
    private String ip;

    /**
     * 端口
     */

    private String port;
    /**
     * 拼接地址
     */

    private String address;

    /**
     * key
     */
    private String key;
    /**
     * 业务类型
     */
    private String appCode;
    /**
     * 批次号
     */
    private String busiNo;
    /**
     * 操作人ID
     */
    private String userId;
    /**
     * 操作人姓名
     */
    private String userName;
    /**
     * 本次操作使用角色
     */
    private String roleNo;
    /**
     * 动态树节点
     */
    private EcmVtree vtree;
    /**
     * 业务属性
     */
    private Map<String, String> appAttrs;
    /**
     * 档案题名
     */
    private String busiTitle;
    /**
     * 是否为扫描接口
     */
    private Boolean isScan;

    private Integer isScanning;
    /**
     * 版本
     */
    private String version;

    private List<EcmVtreeNode> vtreeNodes;

    private Integer isStatic;

    private String arcNo;

    /**
     * 批量查询
     */
    private List<EcmBatchBusiInfo> ecmBatchBusiInfoList;
}
