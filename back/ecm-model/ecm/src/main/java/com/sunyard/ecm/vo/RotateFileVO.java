package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @Description：关联文件VO
 * @create： 2023/5/17 14:05
 */
@Data
public class RotateFileVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "旋转后的文件信息集合")
    List<EcmFileInfoDTO> ecmFileInfoExtendsNew;
    @ApiModelProperty(value = "业务表主键")
    private Long busiId;
    @ApiModelProperty(value = "资料树主键")
    private String docId;
    @ApiModelProperty(value = "资料树标记主键")
    private Long markDocId;
    @ApiModelProperty(value = "业务批次号")
    private String busiBatchNo;
    @ApiModelProperty(value = "最新的文件名称")
    private String newFileName;
    @ApiModelProperty(value = "用户id")
    private String createUser;
    @ApiModelProperty(value = "用户名称")
    private String createUserName;
    @ApiModelProperty(value = "自动转正(1:是)")
    private Integer regularize;
}
