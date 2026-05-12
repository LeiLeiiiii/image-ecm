package com.sunyard.framework.onlyoffice.dto.convert.sheet;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;


/**
 * @author 朱山成
 */
@Data
public class FwPageSize {
    /** 设置输出 PDF 文件的页面高度。默认 297mm*/
    @Value("${onlyoffice.convert.spreadsheetLayout.pageSize.height:297mm}")
    private String height = "297mm";
    /** 设置输出 PDF 文件的页面宽度。默认 210mm*/
    @Value("${onlyoffice.convert.spreadsheetLayout.pageSize.height:210mm}")
    private String width = "500mm";
}
