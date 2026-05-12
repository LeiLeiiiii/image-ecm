package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/14 16:25
 * @desc：资料树DTO类
 */
@Data
public class    EcmDocTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "资料代码")
    private String docCode;

    @ApiModelProperty(value = "资料名称")
    private String docName;

    @ApiModelProperty(value = "父资料类型id")
    private String parent;

    @ApiModelProperty(value = "资料顺序")
    private Float docSort;

    @ApiModelProperty(value = "资料条码")
    private String barcode;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    private List<EcmDocTreeDTO> children;

    /**
     * 0最的节点，1有字节的节点
     */
    @ApiModelProperty(value = "0最的节点，1有字节的节点")
    private Integer type;

    @ApiModelProperty(value = "父节点资料类型名称")
    private String parentName;

    /**
     * 节点值
     */
    @ApiModelProperty(value = "节点值")
    private String id;

    /**
     * 节点名称
     */
    @ApiModelProperty(value = "节点名称")
    private String label;

    @ApiModelProperty(value = "前端禁用所用")
    private Boolean disabled;

    @ApiModelProperty(value = "文稿类识别转正配置开关(关:0,开:1)")
    private Integer isRegularized;

    @ApiModelProperty(value = "图像模糊检测配置开关(关:0,开:1)")
    private Integer isObscured;

    @ApiModelProperty(value = "图像翻拍检测配置开关(关:0,开:1)")
    private Integer isRemade;

    @ApiModelProperty(value = "图像反光检测配置开关(关:0,开:1)")
    private Integer isReflective;

    @ApiModelProperty(value = "图像缺角检测配置开关(关:0,开:1)")
    private Integer isCornerMissing;

    @ApiModelProperty(value = "图像查重检测配置开关(关:0,开:1)")
    private Integer isPlagiarism;

    @ApiModelProperty(value = "文本查重检测配置开关(关:0,开:1)")
    private Integer isPlagiarismText;

    @ApiModelProperty(value = "时间范围(近N年)")
    private Integer frameYear;

    @ApiModelProperty(value = "相似度阈值")
    private Double fileSimilarity;

    @ApiModelProperty(value = "资料关联code名称集合")
    private List<String> relDocNames;

    @ApiModelProperty(value = "关联查重策略")
    private EcmDocPlaCheDTO plagiarismCheckPolicy;

    @ApiModelProperty(value = "是否父级目录(0:否   1:是)")
    private Integer isParent;

    @ApiModelProperty(value = "自动分类检测开关（关：0，开：1）")
    private Integer isAutoClassified;

    @ApiModelProperty(value = "自动分类标识")
    private String autoClassificationId;

    @ApiModelProperty(value = "自动分类字典名")
    private String autoClassificationName;

    @ApiModelProperty(value = "关联code集合")
    private List<String> docCodes;

    @ApiModelProperty(value = "开关状态 0关闭 1开启")
    private Integer state;

    @ApiModelProperty(value = "资料节点类型 0 静态 1动态 2全局")
    private Integer docType;

    @ApiModelProperty(value = "查询资料节点类型：0：当前资料节点，1：选中资料节点，2：全部资料节点")
    private Integer queryType;
    @ApiModelProperty(value = "回显查看")
    private List<EcmDocTreeDTO> all;

    @ApiModelProperty(value = "关联code类型集合")
    private List<EcmDocPlaRelNameDTO> relDocCodes;

    @ApiModelProperty(value = "开关状态筛选集合")
    private List<TypeStateDTO> typeStates;

}
