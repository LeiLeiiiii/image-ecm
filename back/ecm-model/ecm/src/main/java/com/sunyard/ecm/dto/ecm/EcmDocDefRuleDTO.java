package com.sunyard.ecm.dto.ecm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lv
 * @since 2023-12-15
 * @desc 资料类型规则匹配数据
 */
@Data
public class EcmDocDefRuleDTO implements Serializable {

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
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @ApiModelProperty("是否加密，0：不加密，1：加密")
    private Integer isEncrypt;

    @ApiModelProperty(value = "资料类型分类标识")
    private String docTypeSign;

    @ApiModelProperty(value = "资料类型标识起始位置")
    private Integer typeSignStart;

    @ApiModelProperty(value = "资料类型标识结束位置")
    private Integer typeSignEnd;

    @ApiModelProperty(value = "截取后的资料类型code")
    private String subStrDocCodeSign;

    @ApiModelProperty(value = "截取后资料类型长度")
    private Integer subStrDocCodeLength;

}
