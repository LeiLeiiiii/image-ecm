package com.sunyard.framework.onlyoffice.dto.convert;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.sunyard.framework.onlyoffice.dto.convert.document.FwDocumentLayout;
import com.sunyard.framework.onlyoffice.dto.convert.document.FwDocumentRenderer;
import com.sunyard.framework.onlyoffice.dto.convert.sheet.FwSpreadsheetLayout;
import com.sunyard.framework.onlyoffice.dto.convert.thumbnail.FwThumbnail;

import lombok.Data;


/**
 * @author 朱山成
 */
@Data
@Configuration
public class FwConvertBody {
    /**
     * 定义转换请求类型：异步或非异步。
     *  默认值为false。
     */
    @Value("${onlyoffice.convert.async:false}")
    private Boolean async;
    /**
     * 定义从csv或txt格式转换时的文件编码。
     * 支持的主要值：
     * 932- 日语 （移位 JIS），
     * 950-繁体中文（大5），
     * 1250- 中欧 （视窗），
     * 1251-西里尔文（视窗），
     * 65001- 统一码 （UTF-8）。
     */
    @Value("${onlyoffice.convert.codePage:}")
    private Integer codePage;
    /**
     * 定义从csv格式转换时用于分隔值的分隔符。
     * 0- 无分隔符，
     * 1-标签，
     * 2-分号，
     * 3-冒号，
     * 4-逗号，
     * 5-空间。
     */
    @Value("${onlyoffice.convert.delimiter:}")
    private Integer delimiter;
    /**
     * 必填 文件类型
     */
    private String filetype;
    /**
     * 必填 文件key
     */
    private String key;
    /**
     * 必填 输出类型
     */
    private String outputtype;
    /**
     * 密码
     */
    private String password;
    /**
     * 地区 默认值为en-US
     */
    @Value("${onlyoffice.convert.region:zh-CN}")
    private String region;

    /**
     * 定义转换后的文件名
     */
    private String title;

    /**
     * 必填 定义要转换的文档的绝对 URL。
     */
    private String url;

    /**
     * token
     */
    private String token;

    /**
     * 文档布局
     */
    private FwDocumentLayout fwDocumentLayout;
    /**
     * 文档渲染器
     */
    private FwDocumentRenderer fwDocumentRenderer;
    /**
     * 表格布局
     */
    private FwSpreadsheetLayout fwSpreadsheetLayout;

    private FwThumbnail fwThumbnail;
}
