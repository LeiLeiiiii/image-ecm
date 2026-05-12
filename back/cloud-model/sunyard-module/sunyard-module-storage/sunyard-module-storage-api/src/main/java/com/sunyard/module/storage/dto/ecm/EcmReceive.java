package com.sunyard.module.storage.dto.ecm;

import lombok.Data;
import org.springframework.util.ObjectUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc
 * @date 9:54 2021/11/16
 */
@Data
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmReceive {

    /**
     * 返回结果代号
     */
    @XmlElement(name = "RESPONSE_CODE")
    private String responseCode;

    /**
     * 返回结果消息
     */
    @XmlElement(name = "RESPONSE_MSG")
    private String responseMsg;

    /**
     * 影像地址
     */
    @XmlElement(name = "PAGES")
    private EcmPageList pages;

    /**
     * 资料统计数据结构
     */
    @XmlElement(name = "RETURN_DATA")
    private EcmReturnData returnData;

    @XmlElement(name = "SYD")
    private EcmSyd syd;

    @XmlElement(name = "IMAGE_ZIP_URL")
    private String imageZipUrl;

    /**
     * 根据ID获取下载Page
     *
     * @param pageId
     * @return Result
     */
    public EcmPage getEcmPageOfId(String pageId) {
        if (ObjectUtils.isEmpty(pages) || ObjectUtils.isEmpty(pages.getPageList()) || ObjectUtils.isEmpty(pageId)) {
            return null;
        }
        for (EcmPage page : pages.getPageList()) {
            if (pageId.equals(page.getPageId())) {
                return page;
            }
        }
        return null;
    }

    /**
     * 根据ID获取page信息
     *
     * @param pageId
     * @return Result
     */
    public EcmPageInfo getEcmPageInfoOfId(String pageId) {
        if (ObjectUtils.isEmpty(syd) || ObjectUtils.isEmpty(syd.getDoc())
            || ObjectUtils.isEmpty(syd.getDoc().getPageInfo())
            || ObjectUtils.isEmpty(syd.getDoc().getPageInfo().getPageInfoList()) || ObjectUtils.isEmpty(pageId)) {
            return null;
        }
        for (EcmPageInfo pageInfo : syd.getDoc().getPageInfo().getPageInfoList()) {
            if (pageId.equals(pageInfo.getPageId())) {
                return pageInfo;
            }
        }
        return null;
    }
}
