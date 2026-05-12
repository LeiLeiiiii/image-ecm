
package com.sunyard.module.storage.dto.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 回传数据
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseBean", propOrder = {"respCode", "respMsg"})
@XmlSeeAlso({BusinessRespBean.class})
public class ResponseBean extends SunBean {

    protected String respCode;
    protected String respMsg;

    /**
     * 获取回传状态
     *
     * @return Result possible object is {@link String }
     */
    public String getRespCode() {
        return respCode;
    }

    /**
     * 注入回传状态
     *
     * @param value allowed object is {@link String }
     */
    public void setRespCode(String value) {
        this.respCode = value;
    }

    /**
     * 获取回传信息
     *
     * @return Result possible object is {@link String }
     */
    public String getRespMsg() {
        return respMsg;
    }

    /**
     * 注入回传信息
     *
     * @param value allowed object is {@link String }
     */
    public void setRespMsg(String value) {
        this.respMsg = value;
    }

    @Override
    public String toString() {
        return "ResponseBean{" + "respCode='" + respCode + '\'' + ", respMsg='" + respMsg + '\'' + '}';
    }
}
