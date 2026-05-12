package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author HRH
 * @date 2024/11/1
 * @describe 用于批量查询
 */
@Data
public class EcmBatchBusiInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     */
    private String appCode;

    /**
     * 业务类型名称
     */
    private String appName;

    /**
     * 索引信息
     */
    private String busiNo;

    /**
     * 档案号
     */
    private String busiArcno;

    /**
     * 版本号
     */
    private String queryVer;

    /**
     *
     * 索引信息（合约再保、临分再保特例）
     */
    private String businessNo;

    /**
     * 档案标题
     */
    private String busiTitle;

    /**
     * 缓存机构的机构号
     */
    private String comCode;

    /**
     * 图片信息(PAGES，最多一组，非必填，包含多组NODE，NODE下包含多组PAGE，PAGE下可传递PAGE_EXT)
     */
    private EcmPages pages;

    private EcmVtree ecmVtree;

    /**
     * 是否使用ES，0否 1是
     */
    private String isToEs;

    /**
     * 业务属性
     */
    private Map<String, String> appAttrs;

    private List<EcmVtreeNode> vtreeNodes;

    private String arcNo;

}
