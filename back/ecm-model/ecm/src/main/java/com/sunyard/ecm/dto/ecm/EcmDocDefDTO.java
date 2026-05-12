package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/14 19:56
 * @desc: 资料定义DTO类
 */
@Data
public class EcmDocDefDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "资料代码")
    private String docCode;

    @ApiModelProperty(value = "资料名称")
    private String docName;

    @ApiModelProperty(value = "父资料类型id")
    private String parent;

    @ApiModelProperty(value = "资料顺序")
    private Float docSort;

    /*@ApiModelProperty(value = "资料条码")
    private String barcode;*/

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建人")
    String createUserName;
    @ApiModelProperty(value = "最近人修改人")
    String updateUserName;
    @ApiModelProperty(value = "前端显示")
    private String label;
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "父资料类型ids")
    private List<String> parents;
    @ApiModelProperty(value = "父资料类型名称")
    private String parentName;
    @ApiModelProperty("是否是子节点")
    private Integer type;
    /*@ApiModelProperty("是否加密，0：不加密，1：加密")
    private Integer isEncrypt;*/
    @ApiModelProperty("是否加密")
    private String isEncryptStr;
    @ApiModelProperty(value = "资料类型分类标识")
    private String docTypeSign;
    @ApiModelProperty(value = "资料类型标识起始位置")
    private Integer typeSignStart;
    @ApiModelProperty(value = "资料类型标识结束位置")
    private Integer typeSignEnd;

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

    @ApiModelProperty(value = "是否父级节点(0:否   1:是)")
    private Integer isParent;

    @ApiModelProperty(value = "自动分类检测开关（关：0，开：1）")
    private Integer isAutoClassified;

    @ApiModelProperty(value = "自动分类标识")
    private String autoClassificationId;
}
