package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 影像业务目录树表（含动态树和标记节点）
 * </p>
 *
 * @author zyl
 * @since 2023-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmBusiDoc对象", description = "影像业务目录树表（含动态树和标记节点）")
public class EcmBusiDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "doc_id", type = IdType.ASSIGN_ID)
    private Long docId;

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

    @ApiModelProperty(value = "父节点资料节点code")
    private Long parentId;
}
