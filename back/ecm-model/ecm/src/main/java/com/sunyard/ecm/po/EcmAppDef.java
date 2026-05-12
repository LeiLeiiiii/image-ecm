package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 影像业务类型定义表
 * </p>
 *
 * @author zyl
 * @since 2023-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmAppDef对象", description = "影像业务类型定义表")
public class EcmAppDef implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "app_code")
    @ApiModelProperty(value = "业务代码")
    private String appCode;

    @ApiModelProperty(value = "业务名称")
    private String appName;

    @ApiModelProperty(value = "设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "是否开启到达通知，0未开启，1开启")
    private Integer arriveInform;

    @ApiModelProperty(value = "关联的消息队列名称")
    private String queueName;

    @ApiModelProperty(value = "父业务类型id")
    private String parent;

    @ApiModelProperty(value = "顺序")
    private Float appSort;

    @ApiModelProperty(value = "是否压缩（0：否，1：是）")
    private Integer isResize;

    @ApiModelProperty(value = "压缩比例（ 例：800长宽都大于上述值时进行压缩，有一个不大于就不压缩）")
    private Integer resize;

    @ApiModelProperty(value = "压缩质量(默认0.5)")
    private Float qulity;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "分类标识")
    private String appTypeSign;

    @ApiModelProperty(value = "业务类型标识起始位置")
    private Integer typeSignStart;

    @ApiModelProperty(value = "业务类型标识结束位置")
    private Integer typeSignEnd;

    @ApiModelProperty(value = "业务编号标识起始位置")
    private Integer busiNoStart;

    @ApiModelProperty(value = "业务编号标识起始位置")
    private Integer busiNoEnd;

    @ApiModelProperty(value = "是否父级目录(0:否   1:是)")
    private Integer isParent;

    @ApiModelProperty(value = "接口归档 (0否  1是)")
    private Integer isApiArchived;

    @ApiModelProperty(value = "归档业务类型")
    private String archiveAppCode;

    @ApiModelProperty(value = "已关户权限角色列表")
    private String roleIds;

    @ApiModelProperty(value = "是否共享树(0:否   1:是)")
    private Integer isShareTree;
}
