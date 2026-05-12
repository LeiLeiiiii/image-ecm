package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.EcmBusiAttrQueryDataDTO;
import com.sunyard.ecm.dto.QueryDataTreeDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 获取业务类型及文档列表vo
 * </p>
 *
 * @author ypy
 * @since 2025-09-19
 */
@Data
public class QueryBusiInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /****业务属性列表**/
    private List<EcmBusiAttrQueryDataDTO> attrList;

    /****业务对应的资料静态树**/
    private List<QueryDataTreeDTO> ecmBusiDocRedisDTOS;


    /****文件对象**/
    private List<QueryBusiFileVO> fileInfoRedisEntities;

    /****业务号**/
    private String busiNo;

    /****业务类型**/
    private String appCode;

    /****业务状态**/
    private Integer status;

}
