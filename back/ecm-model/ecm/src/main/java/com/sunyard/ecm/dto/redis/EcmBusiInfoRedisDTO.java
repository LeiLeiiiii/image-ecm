package com.sunyard.ecm.dto.redis;

import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiVersion;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 业务信息缓存数据DTO
 */
@Data
public class EcmBusiInfoRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttrDTO> attrList;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "是否压缩（0：否，1：是）")
    private Integer isQulity;

    @ApiModelProperty(value = "压缩比例（ 例：800长宽都大于上述值时进行压缩，有一个不大于就不压缩）")
    private Integer resiz;

    @ApiModelProperty(value = "压缩质量(默认0.5)")
    private Float qulity;

    @ApiModelProperty(value = "业务对应的资料树")
    private List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS;

    @ApiModelProperty(value = "业务轨迹")
    private List<EcmBusiVersion> ecmBusiVersions;

    @ApiModelProperty(value = "资料标记")
    private List<EcmBusiDoc> ecmBusiDocs;

//    @ApiModelProperty(value = "资料权限列表")
//    private List<EcmDocrightDefDTO> docRightList;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "业务号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "资料权限版本")
    private Integer rightVer;

    @ApiModelProperty(value = "树标志(0静态树，1动态树)")
    private Integer treeType;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    private Integer isDeleted;

    @ApiModelProperty(value = "页面唯一标识")
    private String pageFlag;

    @ApiModelProperty(value = "扫描模式（1-单扫 2-批扫）")
    private Integer modelType;

    @ApiModelProperty(value = "是否是查看页面，0：查看，1：采集")
    private Integer isShow;

    @ApiModelProperty(value = "设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "树是否有数据标识 true-有数据 false-无数据")
    private Boolean treeDataFlag;

    @ApiModelProperty(value = "创建者名称")
    private String createUserName;

    @ApiModelProperty(value = "更新者名称")
    private String updateUserName;

    @ApiModelProperty(value = "机构名称")
    private String orgName;

    @ApiModelProperty(value = "文件总数量")
    private Long totalFileSize;

    @ApiModelProperty(value = "业务状态0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结")
    private Integer status;

    @ApiModelProperty(value = "流程类型ID")
    private String delegateType;

    @ApiModelProperty(value = "业务类型ID")
    private String typeBig;

    @ApiModelProperty(value = "业务系统来源")
    private String sourceSystem;

    /**
     * 重写equals方法
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        EcmBusiInfoRedisDTO appInfo = (EcmBusiInfoRedisDTO) o;
        return appCode.equals(appInfo.getAppCode());
    }

    /**
     * 重写hashCode
     * @param
     * @return
     */
    @Override
    public int hashCode() {
        return 31 * appCode.hashCode();
    }

}
