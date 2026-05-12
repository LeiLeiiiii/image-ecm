package com.sunyard.ecm.vo;

import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/13 14:51
 * @desc: 业务类型定义VO
 */
@Data
public class EcmAppDefAttrVO extends EcmAppDef implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务主键")
    private String id;
    @ApiModelProperty(value = "创建人")
    String createUserName;
    @ApiModelProperty(value = "最近人修改人")
    String updateUserName;
    /**
     * 业务属性列表
     */
    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttr> ecmAppAttrList;
    @ApiModelProperty(value = "父节点业务类型id")
    private String parent;
    @ApiModelProperty(value = "父节点业务类型ids")
    private List<String> parents;
    @ApiModelProperty(value = "父节点业务类型名称")
    private String parentName;
    @ApiModelProperty(value = "父节点业务类型名称")
    private List<EcmAppDefAttrVO> children;
    @ApiModelProperty(value = "0最的节点，1有字节的节点")
    private Integer type;
    @ApiModelProperty(value = "前端所用")
    private String label;

    @ApiModelProperty(value = "前端禁用所用")
    private Boolean disabled;

    @ApiModelProperty(value = "设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "队列名称")
    private String queueName;

    @ApiModelProperty(value = "分类标识")
    private String appTypeSign;

    @ApiModelProperty(value = "业务类型标识起始位置")
    private Integer typeSignStart;

    @ApiModelProperty(value = "业务类型标识结束位置")
    private Integer typeSignEnd;

    @ApiModelProperty(value = "业务编号标识起始位置")
    private Integer busiNoStart;

    @ApiModelProperty(value = "业务编号标识起始位置")
    private Integer busiNoEnd;

    @ApiModelProperty(value = "已关户权限角色列表")
    private List<String> roleIdList;

}
