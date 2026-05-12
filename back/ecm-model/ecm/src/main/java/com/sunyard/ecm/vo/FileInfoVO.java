package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/5/10 16:08
 * @desc: 文件基本信息VO
 */
@Data
public class FileInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "文件id列表")
    private List<Long> fileIdList;

    @ApiModelProperty(value = "文件新名称")
    private String newName;

    @ApiModelProperty(value = "当前用户id")
    private String curentUserId;

    @ApiModelProperty(value = "当前用户名称")
    private String curentUserName;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "老资料类型code")
    private List<String> oldDocCode;

}
