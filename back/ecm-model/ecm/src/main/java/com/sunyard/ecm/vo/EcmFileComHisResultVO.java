package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 影像文件批注记录返回数据
 * </p>
 *
 * @since 2023-12-12
 */
@Data
public class EcmFileComHisResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "批注记录(一次记录包含多个批注)")
    private String commentRecord;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "批注内容集合")
    private List<String> commentValueList;

    @ApiModelProperty(value = "类型(1-批注 2-评论)")
    private Integer commentType;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建人姓名")
    private String createUserName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
