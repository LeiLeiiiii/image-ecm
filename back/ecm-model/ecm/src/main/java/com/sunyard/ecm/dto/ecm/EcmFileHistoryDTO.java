package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.po.EcmFileHistory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author： ty
 * @create： 2023/5/10 16:19
 * @desc: 文件历史DTO类
 */
@Data
public class EcmFileHistoryDTO extends EcmFileHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "影像文件历史版本表主键")
    private Long id;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "主体id,文件id（第一份文件的文件id，即需要记录的此文件的生命周期）")
    private Long fileId;

    @ApiModelProperty(value = "修改后的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "文件大小")
    private Long newFileSize;

    @ApiModelProperty(value = "操作")
    private String fileOperation;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "当前用户")
    private String currentUserId;

    @ApiModelProperty(value = "序号")
    private Integer number;

}
