package com.sunyard.module.storage.ecm.webservice.dto;


import com.sunyard.module.storage.dto.webservice.BusinessRespBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 添加接口
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addBusiness", propOrder = {"arg0"}, namespace = "com.sunyard.sunam.service.impl.sunecm.webservice")
public class AddBusiness {

    protected BusinessRespBean arg0;

    public BusinessRespBean getArg0() {
        return arg0;
    }

    public void setArg0(BusinessRespBean value) {
        this.arg0 = value;
    }

}
