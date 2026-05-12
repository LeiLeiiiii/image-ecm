package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @Description：文件差分VO
 * @create： 2023/5/17 14:05
 */
@Data
public class SplitFileVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "被拆分的文件信息")
    EcmFileInfoDTO ecmFileInfoDTO;
    @ApiModelProperty(value = "拆分后的文件信息集合")
    List<EcmFileInfoDTO> ecmFileInfoDTOS;
    @ApiModelProperty(value = "业务表主键")
    private Long busiId;
    @ApiModelProperty(value = "资料类型id")
    private String docCode;
    @ApiModelProperty(value = "业务批次号")
    private String busiBatchNo;
    @ApiModelProperty(value = "最新的文件名称")
    private String newFileName;
    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;
    @ApiModelProperty(value = "业务编号")
    private String busiNo;
    @ApiModelProperty(value = "是否加密")
    private Integer isEncrypt;
    @ApiModelProperty(value = "每页大小")
    private Integer splitPageSize;
    @ApiModelProperty(value = "页码")
    private Integer splitPageNum;

    @ApiModelProperty(value = "批注")
    private List<EcmCommentVO> commentList;
}
