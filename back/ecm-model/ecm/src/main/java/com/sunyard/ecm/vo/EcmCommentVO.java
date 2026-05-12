package com.sunyard.ecm.vo;


import com.sunyard.ecm.po.EcmFileComment;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/13 14:51
 * @desc: 文件批注VO
 */
@Data
public class EcmCommentVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "批注列表")
    List<EcmFileComment> fileCommentList;
    @ApiModelProperty(value = "批注列表Str")
    String fileCommentListStr;
    @ApiModelProperty(value = "业务表主键")
    private Long busiId;
    @ApiModelProperty(value = "文件id")
    private Long fileId;
    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;
    @ApiModelProperty(value = "最新的文件名称")
    private String newFileName;
    @ApiModelProperty(value = "最新备注")
    private String comment;
    @ApiModelProperty(value = "批注页数")
    private Integer filePage;
    @ApiModelProperty(value = "是否是pdf文件")
    private Boolean isPdf;
}
