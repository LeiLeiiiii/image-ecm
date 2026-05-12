package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 影像文件信息表
 * </p>
 *
 * @author zyl
 * @since 2023-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmFileInfo对象", description = "影像文件信息表")
public class EcmFileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件id")
    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    private Long fileId;
    @ApiModelProperty(value = "最新的文件大小")
    private Long newFileSize;
    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "资料树主键")
    private String docCode;

    @ApiModelProperty(value = "资料树标记主键")
    private Long markDocId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "文件名称")
    private String newFileName;

    @ApiModelProperty(value = "文件唯一md5（可查重使用）")
    private String fileMd5;

    @ApiModelProperty(value = "是否复用（默认0，1:复用）")
    private Integer fileReuse;

    @ApiModelProperty(value = "顺序（在doc_id下排序）")
    private Double fileSort;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "备注")
    @TableField("\"COMMENT\"")
    private String comment;

    @ApiModelProperty(value = "状态(默认正常展示:0,已删除:1)")
    private Integer state;

    @ApiModelProperty(value = "创建者名称")
    private String createUserName;

    @ApiModelProperty(value = "更新者名称")
    private String updateUserName;

    @ApiModelProperty(value = "拓展名,同st_file")
    private String newFileExt;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    @TableLogic
    @TableField("is_deleted") // 保持最简单
    private Integer isDeleted;

    @ApiModelProperty(value = "上传机构")
    private String orgName;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "文件唯一标识")
    private String pageId;

    @ApiModelProperty(value = "文件来源")
    private String fileSource;

    @ApiModelProperty(value = "文件是否有密码,true/false")
    private Boolean isFilePassword;
}
