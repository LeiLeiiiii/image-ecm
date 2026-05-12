package com.sunyard.module.storage.ecm.webservice.dto;

import com.sunyard.module.storage.dto.webservice.BusinessRespBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 删除接口
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delBusiness", propOrder = {"arg0"}, namespace = "com.sunyard.sunam.service.impl.sunecm.webservice")
public class DelBusiness {

    protected BusinessRespBean arg0;

    /**
     *
     * @return Result possible object is {@link BusinessRespBean }
     */
    public BusinessRespBean getArg0() {
        return arg0;
    }

    /**
     *
     * @param value allowed object is {@link BusinessRespBean }
     */
    public void setArg0(BusinessRespBean value) {
        this.arg0 = value;
    }

}
