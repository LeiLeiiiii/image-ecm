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
 * 资料类型定义必包表
 * </p>
 *
 * @author zyl
 * @since 2023-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmDocDefRelVer对象", description = "资料类型版本树表")
public class EcmDocDefRelVer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "资料代码")
    private String docCode;

    @ApiModelProperty(value = "父节点代码")
    private String parent;

    @ApiModelProperty(value = "版本")
    private Integer rightVer;

    @ApiModelProperty(value = "资料名称")
    private String docName;

    @ApiModelProperty("业务类型code")
    private String appCode;

    @ApiModelProperty(value = "资料顺序")
    private Float docSort;
    /*@ApiModelProperty(value = "资料条码")
    private String barcode;*/

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
