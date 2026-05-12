package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zyl
 * @since 2023/8/1 17:36
 * @Description 业务类型树VO
 */
@Data
public class QueryDataTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
//
///**业务类型id**/
//    private String appCode;
//
///**当前节点id**/
//    private String id;

///**节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除**/
//    private Integer type;

/**当前节点名称**/
    private String name;

/**孩子节点**/
    private List<QueryDataTreeDTO> children;

/**资料类型id**/
    private String docCode;
//
///**业务属性列表**/
//    private List<EcmAppAttrDTO> attrList;
//
///**业务编号**/
//    private String busiNo;
//
///**业务类型名称**/
//    private String appTypeName;

/**文件数量**/
    private Integer fileCount;
//
///**1叶子节点，0非叶子节点**/
//    private Integer nodeType;
//
///**父节点id**/
//    private String pid;
//
///**业务id**/
//    private Long busiId;
//
///**树类型：0静态树，1动态树**/
//    private Integer treeType;

/**MD5列表**/
//    private List<String> md5List;


}
