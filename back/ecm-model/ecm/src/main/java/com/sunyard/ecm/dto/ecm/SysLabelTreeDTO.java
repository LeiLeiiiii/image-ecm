package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wenbiwen
 * @since 2025/2/19
 * @desc 标签树形结构DTO类
 */
@Data
public class SysLabelTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "标签id")
    private Long labelId;

    @ApiModelProperty(value = "标签名称")
    private String labelName;

    @ApiModelProperty(value = "父级标签代码")
    private Long parentId;

    @ApiModelProperty(value = "子级标签")
    private List<SysLabelTreeDTO> children;


    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "本节点所在的资料所有节点id")
    private List<Long> labelTreeIds;

    @ApiModelProperty(value = "本节点与父节点的所有名称")
    private List<String> labelParentNames;
}
