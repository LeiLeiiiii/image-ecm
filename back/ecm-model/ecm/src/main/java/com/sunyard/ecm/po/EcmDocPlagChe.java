package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 资料类型查重表
 * </p>
 *
 * @author ljw
 * @since 2025-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmDocPlagChe对象", description = "资料类型查重配置表")
public class EcmDocPlagChe implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "资料code")
    private String docCode;

    @ApiModelProperty(value = "查询范围：资料类型code")
    private String relDocCode;

    @ApiModelProperty(value = "时间范围(近N年)")
    private Integer frameYear;

    @ApiModelProperty(value = "相似度阈值")
    private Double fileSimilarity;

//    @ApiModelProperty(value = "类型：0：静态树；1：自定义类型（动态树）; 2:全局配置")
//    private Integer type;

    @ApiModelProperty(value = "查询资料节点类型：0：当前资料节点，1：选中资料节点，2：全部资料节点")
    private Integer queryType;

//    @ApiModelProperty(value = "关联资料类型：0，静态树；1，动态树")
//    private Integer relType;

}

