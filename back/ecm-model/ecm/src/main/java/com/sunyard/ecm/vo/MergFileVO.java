package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @Description：文件合并VO
 * @create： 2023/5/16 11:19
 */
@Data
public class MergFileVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "合并后的文件信息")
    EcmFileInfoDTO ecmFileInfoDTO;
    @ApiModelProperty(value = "要合并的文件id集合")
    List<Long> fileIdList;
    @ApiModelProperty(value = "要合并的new文件id集合")
    List<Long> newFileIdList;
    @ApiModelProperty(value = "要合并的文件信息集合")
    List<String> newFileNames;
    @ApiModelProperty(value = "业务表主键")
    private Long busiId;
    @ApiModelProperty(value = "资料树主键")
    private String docId;
    @ApiModelProperty(value = "资料类型id")
    private String docCode;
    @ApiModelProperty(value = "资料树标记主键")
    private Long markDocId;

}
