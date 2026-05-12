package com.sunyard.module.storage.ecm.webservice.dto;

import com.sunyard.module.storage.dto.webservice.BusinessRespBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 修改接口回传数据
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modBusinessResponse", propOrder = {"returnBus"},
    namespace = "com.sunyard.sunam.service.impl.sunecm.webservice")
public class ModBusinessResponse {

    @XmlElement(name = "return")
    protected BusinessRespBean returnBus;

    /**
     * 获取接口参数
     * 
     * @return Result possible object is {@link BusinessRespBean }
     */
    public BusinessRespBean getReturn() {
        return returnBus;
    }

    /**
     * 注入接口参数
     * 
     * @param value allowed object is {@link BusinessRespBean }
     */
    public void setReturn(BusinessRespBean value) {
        this.returnBus = value;
    }

}
