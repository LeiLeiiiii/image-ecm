package com.sunyard.module.storage.dto.ecm;

/**
 * @author raochangmei
 * @Desc
 * @date 2022/6/10 11:10
 */

import lombok.Data;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc 下载内容
 * @date 9:57 2021/11/16
 */
@Data
public class QueryEcmPageVO {

    private String pageId;

    private String pageUrl;

    private String thumUrl;

    private String fileNo;

    private String pageVer;
}
