package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.EcmBusiAttrQueryDataDTO;
import com.sunyard.ecm.dto.QueryDataTreeDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 业务redis
 * </p>
 *
 * @author zyl
 * @since 2023-04-17
 */
@Data
public class QueryDataVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /****业务属性列表**/
    private List<EcmBusiAttrQueryDataDTO> attrList;
//
//    /****业务类型名称**/
//    private String appTypeName;
//
//    /****是否压缩（0：否，1：是）**/
//    private Integer isQulity;
//
//    /****压缩比例（ 例：800长宽都大于上述值时进行压缩，有一个不大于就不压缩）**/
//    private Integer resiz;
//
//    /****压缩质量(默认0.5)**/
//    private Float qulity;

    /****业务对应的资料静态树**/
    private List<QueryDataTreeDTO> ecmBusiDocRedisDTOS;

//    /****业务轨迹**/
//    private List<EcmBusiVersion> ecmBusiVersions;

    /****文件对象**/
    private List<QueryDataFileVO> fileInfoRedisEntities;

//    /****资料标记**/
//    private List<EcmBusiDoc> ecmBusiDocs;

//    /****资料权限列表**/
//    private List<EcmDocrightDefDTO> docRightList;
//
//    /****资料权限列表(动态树资料权限列表)**/
//    private List<EcmBusiDoc> dynamicTreeRightList;
//
//    /****资料权限列表(多维度资料权限列表)**/
//    private List<EcmDocrightDef> dimensionRightList;

    /****业务表主键**/
//    private String busiId;

    /****业务号**/
    private String busiNo;

    /****业务类型**/
    private String appCode;

    /****资料权限版本**/
//    private Integer rightVer;
//
//    /****树标志(0静态树，1动态树)**/
//    private Integer treeType;
//
//    /****机构号**/
//    private String orgCode;
//
//    /****创建人**/
//    private String createUser;
//
//    /****创建时间**/
//    private Date createTime;
//
//    /****最新修改人**/
//    private String updateUser;
//
//    /****最新修改时间**/
//    private Date updateTime;

//    /****删除状态(否:0,是:1)**/
//    private Integer isDeleted;

//    /****页面唯一标识**/
//    private String pageFlag;
//
//    /****扫描模式（1-单扫 2-批扫）**/
//    private Integer modelType;

//    /****设备id**/
//    private Long equipmentId;

//    /****树是否有数据标识 true-有数据 false-无数据**/
//    private Boolean treeDataFlag;

//    /****创建者名称**/
//    private String createUserName;
//
//    /****更新者名称**/
//    private String updateUserName;
//
//    /****机构名称**/
//    private String orgName;


//    /**
//     * 重写equals方法
//     * @param o
//     * @return
//     */
//    @Override
//    public boolean equals(Object o) {
//        if (this == o){
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()){
//            return false;
//        }
//        QueryDataVo appInfo = (QueryDataVo) o;
//        return appCode.equals(appInfo.getAppCode());
//    }

//    /**
//     * 重写hashCode
//     * @param
//     * @return
//     */
//    @Override
//    public int hashCode() {
//        return 31 * appCode.hashCode();
//    }

}
