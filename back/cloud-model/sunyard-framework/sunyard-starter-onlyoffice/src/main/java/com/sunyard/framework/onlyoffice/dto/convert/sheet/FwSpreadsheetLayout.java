package com.sunyard.framework.onlyoffice.dto.convert.sheet;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;

/**
 * @author 朱山成
 */
@Data
public class FwSpreadsheetLayout {
    /**设置转换区域的高度（以页数为单位）。 默认值为0。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.fitToHeight:0}")
    private Integer fitToHeight = 0;
    /**设置转换区域的宽度（以页数为单位）。 默认值为0。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.fitToWidth:0}")
    private Integer fitToWidth = 0;
    /**允许是否在输出PDF文件中包含网格线。 默认值为false。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.gridLines:false}")
    private Boolean gridLines = false;
    /**允许是否包含输出PDF文件的标题。 默认值为false。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.headings:false}")
    private Boolean headings = false;
    /**确定是否忽略为电子表格文件选择的打印区域。 默认值为true。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.ignorePrintArea:true}")
    private Boolean ignorePrintArea = true;

    private FwMargins fwMargins;
    /**设置输出 PDF 文件的方向。 可能是landscape, portrait.  默认值为纵向(portrait)。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.orientation:portrait}")
    private String orientation ="portrait";

    private FwPageSize pageSize;
    /**允许设置输出PDF文件的比例。 默认值为100。*/
    @Value("${onlyoffice.convert.spreadsheetLayout.scale:100}")
    private Integer scale =100;
}
