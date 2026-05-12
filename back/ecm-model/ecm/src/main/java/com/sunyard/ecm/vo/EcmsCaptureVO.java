package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.redis.FileDocDTO;
import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/26 11:23
 * @desc：影像采集VO
 */
@Data
public class EcmsCaptureVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "资料树主键")
    private String docId;

    @ApiModelProperty(value = "资料树标记主键")
    private Long markDocId;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "采集人")
    private String captureUser;

    @ApiModelProperty(value = "采集日期始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date captureTimeStart;

    @ApiModelProperty(value = "采集日期终")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date captureTimeEnd;

    @ApiModelProperty(value = "批注：0无，1有")
    private Integer isComment;

    @ApiModelProperty(value = "是否过期：0未过期，1过期")
    private Integer isExpire;

    @ApiModelProperty(value = "显示全部：0否，1是")
    private Integer showAll;

    @ApiModelProperty(value = "文件id(数组)")
    private List<Long> fileIds;

    @ApiModelProperty(value = "从归类的文件列表")
    private List<FileDocDTO> fileInfoVOS;

    @ApiModelProperty(value = "资料类型id")
    private String docCode;

    @ApiModelProperty(value = "批注对应的json字符串")
    private List<String> commentStr;

    @ApiModelProperty(value = "原业务id")
    private Long oldBusiId;

    @ApiModelProperty(value = "待归类业务id")
    private Long newBusiId;

    @ApiModelProperty(value = "资料节点")
    private EcmBusiStructureTreeDTO docNode;

    @ApiModelProperty(value = "目标业务类型id")
    private Long targetAppTypeId;

    @ApiModelProperty(value = "老资料类型code")
    private List<String> oldDocCode;

    @ApiModelProperty(value = "排序类型,asc升序,desc降序")
    private String sortType;

    @ApiModelProperty(value = "排序字段")
    private String sortField;

    @ApiModelProperty(value = "关联标签ids")
    private List<String> labelNames;
    @ApiModelProperty(value = "关联标签ids")
    private List<Long> labelIds;
    @ApiModelProperty(value = "是否自动归类手动处理,0否,1是")
    private Integer isAutoCheck;

}
