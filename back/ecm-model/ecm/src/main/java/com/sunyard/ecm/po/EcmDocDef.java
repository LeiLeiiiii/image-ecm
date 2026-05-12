package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 资料类型定义表
 * </p>
 *
 * @author zyl
 * @since 2023-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmDocDef对象", description = "资料类型定义表")
public class EcmDocDef implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "doc_code")
    @ApiModelProperty(value = "资料代码")
    private String docCode;

    @ApiModelProperty(value = "资料名称")
    private String docName;

    @ApiModelProperty(value = "父资料类型id")
    private String parent;

    @ApiModelProperty(value = "资料顺序")
    private Float docSort;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "资料类型分类标识")
    private String docTypeSign;

    @ApiModelProperty(value = "资料类型标识起始位置")
    private Integer typeSignStart;

    @ApiModelProperty(value = "资料类型标识结束位置")
    private Integer typeSignEnd;

    @ApiModelProperty(value = "可上传文件最大个数")
    private Integer maxFiles;

    @ApiModelProperty(value = "可上传文件最小个数")
    private Integer minFiles;

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

    @ApiModelProperty(value = "文稿类识别转正配置开关(关:0,开:1)")
    private Integer isRegularized;

    @ApiModelProperty(value = "图像模糊检测配置开关(关:0,开:1)")
    private Integer isObscured;

    @ApiModelProperty(value = "图像反光检测配置开关(关:0,开:1)")
    private Integer isReflective;

    @ApiModelProperty(value = "图像缺角检测配置开关(关:0,开:1)")
    private Integer isCornerMissing;

    @ApiModelProperty(value = "图像翻拍检测配置开关(关:0,开:1)")
    private Integer isRemade;

    @ApiModelProperty(value = "自动分类检测开关（关：0，开：1）")
    private Integer isAutoClassified;

    @ApiModelProperty(value = "自动分类标识")
    private String autoClassificationId;

    @ApiModelProperty(value = "图像查重检测配置开关(关:0,开:1)")
    private Integer isPlagiarism;

    @ApiModelProperty(value = "是否父级节点(0:否   1:是)")
    private Integer isParent;

    @ApiModelProperty(value = "文本查重检测配置开关(关:0,开:1)")
    private Integer isPlagiarismText;
}
