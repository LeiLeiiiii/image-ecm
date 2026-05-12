package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： lw
 * @create： 2023/4/26 14:30
 * @desc：业务机构树入参DTO类
 */
@Data
@ApiModel(value = "业务结构树入参数", description = "业务结构树入参数")
public class EcmStructureTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务代码")
    private List<Long> busiIdList;

    @ApiModelProperty(value = "已删除节点：0无，1有")
    private Integer isDeleted;

    @ApiModelProperty(value = "页面标识")
    private String pageFlag;

    @ApiModelProperty(value = "是否是查看页面，0：查看，1：采集")
    private Integer isShow;

    @ApiModelProperty(value = "是否是归类查询资料节点,传1则表示是")
    private Integer isClassify;

    @ApiModelProperty(value = "资料代码")
    private String docCode;

    @ApiModelProperty(value = "是否展示全部树")
    private Integer isShowAll;

    @ApiModelProperty(value = "业务代码")
    private List<Long> nowBusiIdList;

}
