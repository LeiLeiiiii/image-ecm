package com.sunyard.ecm.dto.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zyl
 * @since 2023/8/7 17:12
 * @Description ES业务信息参数DTO
 */
@Data
@Accessors(chain = true)
public class EcmBusiInfoEsDTO implements Serializable {
    /******************************* 业公共查询 *******************************/

    @ApiModelProperty(value = "唯一id")
    private Long busiId;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "业务主索引")
    private String busiNo;

    @ApiModelProperty(value = "创建人名称")
    private String creatUserName;

    @ApiModelProperty(value = "最近修改人名称")
    private String updateUserName;

    @ApiModelProperty(value = "业务类型的属性")
    private String appAttrs;

    @ApiModelProperty(value = "业务类型的属性Map")
    private List<Map<String , String>> appAttrMap;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createDate;


    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateTime;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    private Integer isDeleted;

    /**
     * 机构号
     */
    @ApiModelProperty(value = "机构号")
    private String orgCode;

}
