
package com.sunyard.module.storage.ecm.webservice.dto;

import com.sunyard.module.storage.dto.webservice.BusinessRespBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 删除接口回传数据
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delBusinessResponse", propOrder = {"returnBus"},
    namespace = "com.sunyard.sunam.service.impl.sunecm.webservice")
public class DelBusinessResponse {

    @XmlElement(name = "return")
    protected BusinessRespBean returnBus;

    /**
     * 活动回传
     * 
     * @return Result possible object is {@link BusinessRespBean }
     *
     */
    public BusinessRespBean getReturn() {
        return returnBus;
    }

    /**
     * 注入回传
     * 
     * @param value allowed object is {@link BusinessRespBean }
     *
     */
    public void setReturn(BusinessRespBean value) {
        this.returnBus = value;
    }

}
