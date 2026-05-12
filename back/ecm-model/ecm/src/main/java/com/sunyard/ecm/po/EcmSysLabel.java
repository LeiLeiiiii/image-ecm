package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author Wenbiwen
 * @since 2025/2/18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmSysLabel对象", description = "影像标签定义表")
public class EcmSysLabel {

    @ApiModelProperty(value = "标签id")
    @TableId(value = "label_id", type = IdType.ASSIGN_ID)
    private Long labelId;

    @ApiModelProperty(value = "标签名称")
    private String labelName;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "上级标签id")
    private Long parentId;

    @ApiModelProperty(value = "是否是最后层级，0：是，1：否")
    private Integer lastLevel;

}
