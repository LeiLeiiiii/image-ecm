package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc
 * @date 14:25 2021/9/30
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmBaseData implements Serializable {
    /**
     * 操作人代码
     */
    @XmlElement(name = "USER_CODE")
    private String userCode;

    /**
     * 操作人姓名
     */
    @XmlElement(name = "USER_NAME")
    private String userName;

    /**
     * 业务归属地机构代码
     */
    @XmlElement(name = "ORG_CODE")
    private String orgCode;

    /**
     * 操作员所属分公司
     */
    @XmlElement(name = "COM_CODE")
    private String comCode;

    /**
     * 机构名称
     */
    @XmlElement(name = "ORG_NAME")
    private String orgName;

    /**
     * 操作员角色
     */
    @XmlElement(name = "ROLE_CODE")
    private String roleCode;

    /**
     * 操作员角色
     */
    @XmlElement(name = "BIZ_CONTROL")
    private String bizControl;
}
