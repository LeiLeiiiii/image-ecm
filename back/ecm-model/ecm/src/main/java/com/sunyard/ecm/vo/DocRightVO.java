package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmDimensionDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
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
public class DocRightVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务资料权限版本id")
    private Long id;

    @ApiModelProperty(value = "角色id")
    private Long roleId;

    @ApiModelProperty(value = "业务资料权限管理列表(角色维度)")
    private List<EcmDocrightDefDTO> docRightList;

    @ApiModelProperty(value = "当前用户人")
    private String currentUser;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "版本号")
    private Integer rightVer;

    @ApiModelProperty(value = "关联维度列表")
    private List<EcmDimensionDefDTO> relateDimList;

    @ApiModelProperty(value = "业务资料权限管理列表(业务多维度)")
    private List<List<EcmDocrightDefDTO>> lotDimDocRightList;

}
