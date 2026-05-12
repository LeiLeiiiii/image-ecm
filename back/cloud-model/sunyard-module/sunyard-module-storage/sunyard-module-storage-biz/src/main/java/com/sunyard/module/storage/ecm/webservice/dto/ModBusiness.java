package com.sunyard.module.storage.ecm.webservice.dto;

import com.sunyard.module.storage.dto.webservice.BusinessRespBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 修改接口
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modBusiness", propOrder = {"arg0"}, namespace = "com.sunyard.sunam.service.impl.sunecm.webservice")
public class ModBusiness {

    protected BusinessRespBean arg0;

    /**
     * 获得接口参数
     * 
     * @return Result possible object is {@link BusinessRespBean }
     *
     */
    public BusinessRespBean getArg0() {
        return arg0;
    }

    /**
     * 注入接口参数
     * 
     * @param value allowed object is {@link BusinessRespBean }
     *
     */
    public void setArg0(BusinessRespBean value) {
        this.arg0 = value;
    }

}
