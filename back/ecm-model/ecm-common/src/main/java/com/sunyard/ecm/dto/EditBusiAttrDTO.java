package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/9/11 17:08
 * @desc 业务属性DTO
 */
@Data
public class EditBusiAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 基本信息
     */
    private EcmUserDTO ecmBaseInfoDTO;

    /**
     * 业务编号
     */
    private String busiNo;

    /**
     * 业务类型代码
     */
    private String appCode;

    /**
     * 业务状态
     */
    private Integer status;

    /**
     * 业务属性
     */
    private List<EcmBusiAttrDTO> ecmBusiAttrDTOList;

    /**
     * 错误编码(0:不展示 1：展示)
     */
    private Integer errNo;

    /**
     * 错误信息
     */
    private String remark;
}
