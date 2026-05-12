package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： zyl
 * @create： 2023/5/8 15:50
 * @Description：影像文件批注评论数据
 */
@Data
public class EcmFileMarkCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "批注内容")
    private String commentContent;

}
