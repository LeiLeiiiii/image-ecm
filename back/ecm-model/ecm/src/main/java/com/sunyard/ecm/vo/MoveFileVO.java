package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/18 15:38
 * @Description：文件移动VO
 */
@Data
public class MoveFileVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "选中节点后所有文件id列表")
    private List<Long> allFileId;
    @ApiModelProperty(value = "要移动的文件id的busiId")
    private Long busiId;
    @ApiModelProperty(value = "移动的文件id")
    private List<Long> fileId;
    @ApiModelProperty(value = "移动的文件id列表")
    private List<Long> moveFilesId;
}
