package com.sunyard.framework.onlyoffice.dto;


import java.io.Serializable;

import lombok.Data;

/**
 * only office 配置信息
 * 以下参数必须填写
 * 1、docServiceApiUrl
 * 2、fileType
 * 3、key
 * 4、title
 * 5、url
 * 6、callbackUrl
 *
 * @author PJW
 */
@Data
public class FwFileConfig implements Serializable {
    /**
     * 类型 embedded/desktop
     * 默认为desktop
     */
    private String type = "desktop";


    /**
     * 文档类型 word/cell/slide
     * open a word document (.doc, .docm, .docx, .dot, .dotm, .dotx, .epub, .fodt, .htm, .html, .mht, .odt, .ott, .pdf, .rtf, .txt, .djvu, .xps)
     * open a cell (.csv, .fods, .ods, .ots, .xls, .xlsm, .xlsx, .xlt, .xltm, .xltx)
     * open a slide (.fodp, .odp, .otp, .pot, .potm, .potx, .pps, .ppsm, .ppsx, .ppt, .pptm, .pptx)
     *
     */
    private String documentType;

    /**
     * 自定义签名
     */
    private String token;

    /**
     * 文档配置信息
     */
    private FwDocumentConfig document;

    /**
     * 编辑配置
     */
    private FwEditorConfig fwEditorConfig;

    /**
     * 访问api 路径
     */
    private String docServiceApiUrl;

}
