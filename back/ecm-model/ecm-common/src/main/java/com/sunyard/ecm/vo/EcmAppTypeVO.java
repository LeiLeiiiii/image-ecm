package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;


/**
 * @author scm
 * @date 2023/4/26
 * @describe 新增返回业务类型参数
 */
@Data
public class EcmAppTypeVO implements   Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 业务类型
     */
    private String appCode;

    /**
     * 业务主索引名称
     */
    private String mainIndexName;

    /**
     * 业务主索引值
     */
    private String mainIndexValue;

    /**
     * 业务ID
     */
    private Long busiId;

    /**
     * 页面flag
     */
    private String pageFlag;


}
