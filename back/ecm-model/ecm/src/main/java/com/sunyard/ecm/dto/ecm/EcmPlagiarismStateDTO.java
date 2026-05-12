
package com.sunyard.ecm.dto.ecm;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ljw
 * @since 2025/2/24
 * @Description 查重配置开关DTO类
 */
@Data
public class EcmPlagiarismStateDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "动态资料id")
    private Long id;

    @ApiModelProperty(value = "资料code")
    private String docCode;

    @ApiModelProperty(value = "查询范围：资料类型code")
    private String relDocCode;

    @ApiModelProperty(value = "时间范围（近N年），默认值为0表示不限制")
    private Integer frameYear;

    @ApiModelProperty(value = "相似度阈值")
    private Double fileSimilarity;

    @ApiModelProperty(value = "类型：0：静态树；1：自定义类型（动态树）")
    private Integer docType;

    @ApiModelProperty(value = "图像查重检测配置开关(关:0,开:1)")
    private Integer isPlagiarism;

    @ApiModelProperty(value = "资料类型code集合")
    private List<String> docCodes;

    @ApiModelProperty(value = "关联资料类型名称集合")
    private List<String> relDocNames;

    @ApiModelProperty(value = "资料名称")
    private String docName;

    @ApiModelProperty(value = "查询资料节点类型：0：当前资料节点，1：选中资料节点，2：全部资料节点")
    private Integer queryType;

    @ApiModelProperty(value = "关联查重资料类型")
    private Integer relType;

    @ApiModelProperty(value = "关联code类型集合")
    private List<EcmDocPlaRelNameDTO> relDocCodes;
}
