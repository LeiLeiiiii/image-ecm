package com.sunyard.framework.onlyoffice.dto.convert.document;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;

/**
 * @author 朱山成
 */
@Data
public class FwDocumentLayout {
    /**
     * 定义是否绘制占位符。
     */
    @Value("${onlyoffice.convert.documentLayout.drawPlaceHolders:false}")
    private Boolean drawPlaceHolders;
    /**
     * 定义是否突出显示表单。
     */
    @Value("${onlyoffice.convert.documentLayout.drawFormHighlight:false}")
    private Boolean drawFormHighlight;
    /***
     * 定义打印模式是打开还是关闭。
     * 此参数仅用于将docx/docxf转换为pdf。
     * 如果此参数等于true，则如上所述使用drawPlaceHolder和drawFormHighlight标志。
     * 如果此参数为false，则drawFormHighlight标志不起作用，
     * 并且drawPlaceHolder参数允许以pdf格式保存表单。 默认值为false。
     */
    @Value("${onlyoffice.convert.documentLayout.isPrint:false}")
    private Boolean isPrint;
}
