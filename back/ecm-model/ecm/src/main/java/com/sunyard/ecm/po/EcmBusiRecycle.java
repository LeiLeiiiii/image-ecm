package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 影像业务回收表
 * </p>
 *
 * @author wzz
 * @since 2024-06-06
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmBusiRecycle对象", description = "影像业务回收表")
public class EcmBusiRecycle implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "回收业务主键")
    @TableId(value = "recycle_id", type = IdType.ASSIGN_ID)
    private Long recycleId;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "业务主索引")
    private String busiNo;

    @ApiModelProperty(value = "回收人")
    private String recycleUser;

    @ApiModelProperty(value = "回收时间")
    @TableField(fill = FieldFill.INSERT)
    private Date recycleTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
