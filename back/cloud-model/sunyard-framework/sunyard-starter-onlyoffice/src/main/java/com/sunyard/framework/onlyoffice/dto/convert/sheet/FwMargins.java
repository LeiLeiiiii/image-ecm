package com.sunyard.framework.onlyoffice.dto.convert.sheet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author PJW
 * @BelongsProject: leaf-onlyoffice
 * @BelongsPackage: com.ideayp.leaf.onlyoffice.dto.convert.sheet
 * @Description: 边距
 */
@Data
@Configuration
public class FwMargins {
    /**设置输出 PDF 文件的边距 默认 19.1mm */
    @Value("${onlyoffice.convert.spreadsheetLayout.margins.bottom:19.1mm}")
    private String bottom ="19.1mm";
    @Value("${onlyoffice.convert.spreadsheetLayout.margins.left:17.8mm}")
    /** 默认 17.8mm */
    private String left = "17.8mm";
    @Value("${onlyoffice.convert.spreadsheetLayout.margins.right:17.8mm}")
    /** 默认 17.8mm */
    private String right = "17.8mm";
    @Value("${onlyoffice.convert.spreadsheetLayout.margins.top:19.1mm}")
    /** 默认 19.1mm */
    private String top = "19.1mm";
}
