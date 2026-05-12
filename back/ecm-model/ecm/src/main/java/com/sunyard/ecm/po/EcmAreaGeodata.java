package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 中国地理经纬度坐标表
 * </p>
 *
 * @author yzy
 * @since 2025-5-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmAreaGeodata对象", description = "中国地理经纬度坐标表")
public class EcmAreaGeodata implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "code")
    @ApiModelProperty(value = "区域编码")
    private Integer code;

    @ApiModelProperty(value = "父级区域编码")
    private Integer parentCode;

    @ApiModelProperty(value = "区域名称")
    private String name;

    @ApiModelProperty(value = "区域级别")
    private Integer level;

    @ApiModelProperty(value = "中心点坐标")
    private String center;

    @ApiModelProperty(value = "多边形边界坐标")
    private String polyline;
}