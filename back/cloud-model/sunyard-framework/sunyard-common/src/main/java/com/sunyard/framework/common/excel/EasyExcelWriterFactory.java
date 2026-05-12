package com.sunyard.framework.common.excel;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import javax.annotation.Resource;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * @author zhouleibin
 * @date 2021/8/10 16:27
 * @Desc
 */
public class EasyExcelWriterFactory {
    private int sheetNo = 0;
    @Resource
    private ExcelWriter excelWriter = null;

    public EasyExcelWriterFactory(OutputStream outputStream) {
        excelWriter = EasyExcel.write(outputStream).build();
    }

    public EasyExcelWriterFactory(File file) {
        excelWriter = EasyExcel.write(file).build();
    }

    public EasyExcelWriterFactory(String filePath) {
        excelWriter = EasyExcel.write(filePath).build();
    }

    /**
     * 链式模板表头写入
     * 
     * @param headClazz 表头格式
     * @param data 数据 List<ExcelModel> 或者List<List<Object>>
     * @return Result
     */
    public EasyExcelWriterFactory writeModel(Class headClazz, List data, String sheetName) {
        excelWriter.write(data, EasyExcel.writerSheet(this.sheetNo++, sheetName).head(headClazz).build());
        return this;
    }

    /**
     * 链式自定义表头写入
     * 
     * @param head 表头格式
     * @param data 数据 List<ExcelModel> 或者List<List<Object>>
     * @param sheetName sheet页
     * @return Result
     */
    public EasyExcelWriterFactory write(List<List<String>> head, List data, String sheetName,
        ExcelMergeStrategy excelMergeStrategy, ExcelStyle excelStyle) {
        ExcelWriterSheetBuilder excelWriterSheetBuilder = EasyExcel.writerSheet(this.sheetNo++, sheetName).head(head);
        if (excelMergeStrategy != null) {
            excelWriterSheetBuilder.registerWriteHandler(excelMergeStrategy);
        }
        if (excelStyle != null) {
            excelWriterSheetBuilder.registerWriteHandler(excelStyle);
        }
        WriteSheet build = excelWriterSheetBuilder.build();
        excelWriter.write(data, build);
        return this;
    }

    /***/
    public void finish() {
        excelWriter.finish();
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/8/10 zhouleibin creat
 */
