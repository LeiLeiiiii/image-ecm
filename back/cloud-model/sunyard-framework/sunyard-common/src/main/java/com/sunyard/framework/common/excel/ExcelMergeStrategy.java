package com.sunyard.framework.common.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author PJW 2021/11/23 9:26
 */
public class ExcelMergeStrategy extends AbstractMergeStrategy {

    /**
     * 合并坐标合集
     */
    private List<CellRangeAddress> cellRangeAddresses;

    /**
     * 私有构造
     * 
     * @param cellRangeAddresses 表格 单元格
     */
    public ExcelMergeStrategy(List<CellRangeAddress> cellRangeAddresses) {
        this.cellRangeAddresses = cellRangeAddresses;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (!CollectionUtils.isEmpty(cellRangeAddresses)) {
            if (cell.getRowIndex() == 1 && cell.getColumnIndex() == 0) {
                for (CellRangeAddress item : cellRangeAddresses) {
                    sheet.addMergedRegionUnsafe(item);
                }
            }
        }
    }
}
