package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.EcmAppAttrDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author XQZ
 * @date 2023/4/26
 * @describe 业务信息VO
 */
@Data
public class BusiInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     **/
    private String appCode;

    /**
     * 业务类型
     **/
    private List<String> appCodes;

    /**
     * 机构号
     **/
    private String orgCode;

    /**
     * 创建人
     **/
    private String createUser;

    /**
     * 最新修改人
     **/
    private String updateUser;

    /**
     * 业务属性列表
     **/
    private List<EcmAppAttrDTO> attrList;

    /**
     * 创建日期 起
     **/
    private Date createTimeStart;

    /**
     * 创建日期 止
     **/
    private Date createTimeEnd;

    /**
     * 最近修改日期 起
     **/
    private Date updateTimeStart;

    /**
     * 最近修改日期 止
     **/
    private Date updateTimeEnd;

    /**
     * 业务编号
     **/
    private String busiNo;

    /**
     * 页面唯一标识
     **/
    private String pageFlag;
}
