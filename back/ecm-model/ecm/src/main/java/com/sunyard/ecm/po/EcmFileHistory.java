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
 * 影像文件历史版本表(批次文件信息记录表)
 * </p>
 *
 * @author zyl
 * @since 2023-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmFileHistory对象", description = "影像文件历史版本表(批次文件信息记录表)")
public class EcmFileHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "主体id,文件id（第一份文件的文件id，即需要记录的此文件的生命周期）")
    private Long fileId;

    @ApiModelProperty(value = "文件大小")
    private Long newFileSize;

    @ApiModelProperty(value = "修改后的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "操作")
    private String fileOperation;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "扩展名;同st_file")
    private String newFileExt;

}
