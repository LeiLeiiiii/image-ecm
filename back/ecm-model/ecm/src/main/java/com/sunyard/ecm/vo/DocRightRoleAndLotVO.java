package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmAppDocRelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 16:56
 * @desc 资料权限VO
 */
@Data
public class DocRightRoleAndLotVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色纬度")
    private DocRightVO roleDocRight;

    @ApiModelProperty(value = "资料节点树")
    private List<EcmAppDocRelDTO> ecmDocTreeVO;

    @ApiModelProperty(value = "多维度")
    private DocRightVO lotDicRight;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "版本")
    private  Integer rightVer;

    @ApiModelProperty(value = "多维度是否使用")
    private Integer lotDimUse;

}
