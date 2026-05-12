package com.sunyard.ecm.dto.ecm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/5/5 14:51
 * @Desc: 业务结构树DTO类
 */
@Data
public class EcmBusiStructureTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "当前节点id")
    private String id;

    @ApiModelProperty(value = "节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除")
    private Integer type;

    @ApiModelProperty(value = "当前节点名称")
    private String name;

    @ApiModelProperty(value = "孩子节点")
    private List<EcmBusiStructureTreeDTO> children;

    @ApiModelProperty(value = "资料类型id")
    private String docCode;

    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttrDTO> attrList;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "文件数量")
    private Integer fileCount;

    @ApiModelProperty(value = "1叶子节点，0非叶子节点")
    private Integer nodeType;

    @ApiModelProperty(value = "父节点id")
    private String pid;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "树类型：0静态树，1动态树")
    private Integer treeType;

    @ApiModelProperty(value = "MD5列表")
    private List<String> md5List;

    @ApiModelProperty(value = "是否压缩（0：否，1：是）")
    private Integer isResize;

    @ApiModelProperty(value = "压缩比例（ 例：800长宽都大于上述值时进行压缩，有一个不大于就不压缩）")
    private Integer resize;
    @ApiModelProperty(value = "压缩质量(默认0.5)")
    private Integer isQulity;
    @ApiModelProperty(value = "压缩质量(默认0.5)")
    private Float qulity;

    @ApiModelProperty(value = "资料权限列表")
    @JsonInclude
    private EcmDocrightDefDTO docRight;

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

    @ApiModelProperty(value = "父节点名称")
    private String pName;

    @ApiModelProperty(value = "创建人id")
    private String createUser;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "扫描模式（1-单扫 2-批扫）")
    private Integer modelType;

    @ApiModelProperty(value = "存储设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "队列名称")
    private String queueName;

    @ApiModelProperty(value = "是否锁定")
    private boolean lock = false;

    @ApiModelProperty(value = "需要采集文件数量(移动端)")
    private Integer needCaptureFileCount;

    @ApiModelProperty(value = "是否偏离矫正")
    private Boolean isFlat;

    @ApiModelProperty(value = "是否有资料节点数据（true-有，false-没有）")
    private Boolean haveNodeData;

    @ApiModelProperty(value = "所有父节点名称")
    private String fatherNodeName;

    @ApiModelProperty("是否加密，0：不加密，1：加密;默认为0")
    private Integer isEncrypt =0;


    @ApiModelProperty(value = "当为标记节点时，这里显示父级资料节点的数量")
    private Integer docFileSize;
    @ApiModelProperty(value = "文件列表")
    List<FileInfoRedisDTO> resultFiles;

    @ApiModelProperty(value = "是否父级目录(0:否   1:是)")
    private Integer isParent;

    @ApiModelProperty(value = "业务状态0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结")
    private Integer status;
}
