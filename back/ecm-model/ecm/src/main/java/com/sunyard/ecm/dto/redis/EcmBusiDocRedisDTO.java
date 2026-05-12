package com.sunyard.ecm.dto.redis;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 业务资料缓存数据DTO
 */
@Data
public class EcmBusiDocRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资料树子节点")
    private List<EcmBusiDocRedisDTO> children;

    @ApiModelProperty(value = "文件数量")
    private Integer fileCount=0;

    @ApiModelProperty(value = "1叶子节点，0非叶子节点")
    private Integer nodeType;

    @ApiModelProperty(value = "资料类型父节点id")
    private String parent;

    @ApiModelProperty(value = "资料类型父节点名称")
    private String parentName;

    @ApiModelProperty(value = "影像业务目录树表主键")
    private Long docId;

    @ApiModelProperty(value = "可上传文件最大个数")
    private Integer maxLen;

    @ApiModelProperty(value = "可上传文件最小个数")
    private Integer minLen;

    @ApiModelProperty(value = "限制文档格式及大小")
    private String officeLimit;

    @ApiModelProperty(value = "限制图片格式及大小")
    private String imgLimit;

    @ApiModelProperty(value = "限制音频格式及大小")
    private String audioLimit;

    @ApiModelProperty(value = "限制视频格式及大小")
    private String videoLimit;

    @ApiModelProperty(value = "限制其他格式及大小")
    private String otherLimit;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "资料代码")
    private String docCode;

    @ApiModelProperty(value = "资料名称")
    private String docName;

    @ApiModelProperty(value = "资料顺序（DOC_TYPE_ID下排序）")
    private Float docSort;

    @ApiModelProperty(value = "标记标志（默认0，标记1）")
    private Integer docMark;


    @ApiModelProperty(value = "父节点id")
    private Long parentId;

    @ApiModelProperty(value = "所有父节点名称")
    private String allParentName;

    @ApiModelProperty("是否加密，0：不加密，1：加密")
    private Integer isEncrypt;

    @ApiModelProperty(value = "是否父级目录(0:否   1:是)")
    private Integer isParent;

    @ApiModelProperty(value = "自动分类检测开关（关：0，开：1）")
    private Integer isAutoClassified;

    @ApiModelProperty(value = "自动分类标识")
    private String autoClassificationId;
}
