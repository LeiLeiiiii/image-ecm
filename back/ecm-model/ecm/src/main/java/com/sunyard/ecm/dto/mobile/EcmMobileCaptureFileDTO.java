package com.sunyard.ecm.dto.mobile;

import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 移动端获取资料列表入参
 */
@Data
public class EcmMobileCaptureFileDTO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务busiId")
    private Long busiId;

    @ApiModelProperty(value = "资料id")
    private String docId;

    @ApiModelProperty(value = "显示全部：0否，1是")
    private Integer showAll;

    @ApiModelProperty(value = "标记节点id")
    private Long markDocId;

}
