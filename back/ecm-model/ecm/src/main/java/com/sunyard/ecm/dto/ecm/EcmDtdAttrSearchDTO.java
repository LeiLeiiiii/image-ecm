package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.vo.SearchOptionVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zyl
 * @since 2023/8/8 15:42
 * @Description 单证属性查询DTO类
 */
@Data
public class EcmDtdAttrSearchDTO implements Serializable {
    @ApiModelProperty(value = "前端列表的code")
    private String code;

    @ApiModelProperty(value = "前端列表的name")
    private String label;

    @ApiModelProperty(value = "前端列表的placeholder")
    private String placeholder;

    @ApiModelProperty(value = "前端列表的type")
    private String type;

    @ApiModelProperty(value = "前端列表的option")
    private List<SearchOptionVO> option;

    @ApiModelProperty(value = "前端列表的datetype")
    private String datetype;

    @ApiModelProperty(value = "前端列表的valueFormat")
    private String valueFormat;

    @ApiModelProperty(value = "业务属性字段id")
    private Long appAttrId;
}
