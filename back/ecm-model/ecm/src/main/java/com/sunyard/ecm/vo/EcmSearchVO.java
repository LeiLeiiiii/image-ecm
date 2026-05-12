package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.ecm.enums.EcmElasticsearchQueryTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zyl
 * @since 2023/8/7 10:17
 * @Description 影像查询VO
 */
@Data
@Accessors(chain = true)
public class EcmSearchVO implements Serializable {
    /******************************* 业公共查询 *******************************/
    @ApiModelProperty(value = "页数")
    private Integer pageNum ;
    @ApiModelProperty(value = "每页大小")
    private Integer pageSize;
    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;
    @ApiModelProperty(value = "标签名称")
    private String fileLabel;
    @ApiModelProperty(value = "业务主索引")
    private String busiNo;

    @ApiModelProperty(value = "创建人")
    private String creatUserName;

    @ApiModelProperty(value = "最近修改人")
    private String updateUserName;

    @ApiModelProperty(value = "业务类型的属性")
    private String appAttrs;

    @ApiModelProperty(value = "查询类型")
    private EcmElasticsearchQueryTypeEnum type;

    @ApiModelProperty(value = "搜索关键字")
    private String key;

    @ApiModelProperty(value = "查询来源(0 正常 ; 1 代表单独单证查询页)")
    private Integer searchSource;
    /**
     * 修改开始时间
     */
    @ApiModelProperty(value = "修改开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateStartDate;

    /**
     * 修改结束时间
     */
    @ApiModelProperty(value = "修改结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateEndDate;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;


    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "业务类型属性Map")
    private List<Map<String ,String>> appAttrMap;
    @ApiModelProperty(value = "单证类型属性Map")
    private List<Map<String ,String>> dtdAttrMap;
    /******************************* 业务查询 *******************************/

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "上传开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createStartDate;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "上传结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createEndDate;


    /******************************* 文档查询 *******************************/

    @ApiModelProperty(value = "资料类型id")
    private String docCode;

    @ApiModelProperty(value = "资料类型名称")
    private Long docTypeName;

    @ApiModelProperty(value = "单证类型id")
    private String dtdCode;

    @ApiModelProperty(value = "单证类型名称")
    private Long dtdTypeName;

    @ApiModelProperty(value = "业务id集合")
    private List<Long> busiIdList;

    @ApiModelProperty(value = "机构号")
    private List<String> orgCodeList;

    @ApiModelProperty(value = "默认有权限的业务类型列表")
    private Set<String> appCodeSet;

    @ApiModelProperty(value = "默认有权限的资料类型列表")
    private Set<String> docCodeSet;
}
