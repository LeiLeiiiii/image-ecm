package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/5/5 14:51
 * @desc: 业务结构树DTO
 */
@Data
public class EcmBusiStructureTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

/**业务类型id**/
    private String appCode;

/**当前节点id**/
    private String id;

/**节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除**/
    private Integer type;

/**当前节点名称**/
    private String name;

/**孩子节点**/
    private List<EcmBusiStructureTreeDTO> children;

/**资料类型id**/
    private String docCode;
//
///**业务属性列表**/
//    private List<EcmAppAttrDTO> attrList;

/**业务编号**/
    private String busiNo;

/**业务类型名称**/
    private String appTypeName;

/**文件数量**/
    private Integer fileCount;

/**1叶子节点，0非叶子节点**/
    private Integer nodeType;

/**父节点id**/
    private String pid;

/**业务id**/
    private Long busiId;

/**树类型：0静态树，1动态树**/
    private Integer treeType;

/**MD5列表**/
    private List<String> md5List;

/**是否压缩（0：否，1：是）**/
    private Integer isQulity;

/**压缩比例（ 例：800长宽都大于上述值时进行压缩，有一个不大于就不压缩）**/
    private Integer resiz;

/**压缩质量(默认0.5)**/
    private Float qulity;
//
///**资料权限列表**/
//    @JsonInclude
//    private EcmDocrightDefDTO docRight;

/**父节点名称**/
    private String pName;

/**创建人id**/
    private String createUser;

/**创建人名称**/
    private String createUserName;

/**创建时间**/
    private Date createTime;

/**扫描模式（1-单扫 2-批扫）**/
    private Integer modelType;

/**存储设备id**/
    private Long equipmentId;

/**队列名称**/
    private String queueName;

/**需要采集文件数量(移动端)**/
    private Integer needCaptureFileCount;

/**是否偏离矫正**/
    private Boolean isFlat;

}
