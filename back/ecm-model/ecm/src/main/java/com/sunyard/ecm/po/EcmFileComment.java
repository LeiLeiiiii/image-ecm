package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * 影像文件批注表(批次文件信息记录表)
 * </p>
 *
 * @author zyl
 * @since 2023-06-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmFileComment对象", description = "影像文件批注表(批次文件信息记录表)")
public class EcmFileComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "comment_id", type = IdType.ASSIGN_ID)
    private Long commentId;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "批注标记js ")
    private String commentValue;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "批注页数")
    private Integer filePage;

    @ApiModelProperty(value = "是否是pdf文件")
    private Boolean isPdf;


}
