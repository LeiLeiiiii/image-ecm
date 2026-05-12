package com.sunyard.framework.common.excel;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

/**
 * @author zhouleibin
 * @date 2021/8/10 16:39
 * @Desc
 */

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouleibin
 * @date 2021/8/10 16:27
 * @Desc
 */
@Slf4j
public class DefaultExcelListener<T> extends AnalysisEventListener<T> {
    private final List<T> rows = new ArrayList<T>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        log.info("解析到一条头数据:{}", JSON.toJSONString(headMap));
    }

    @Override
    public void invoke(T object, AnalysisContext context) {
        rows.add(object);
        // 实际数据量比较大时，rows里的数据可以存到一定量之后进行批量处理（比如存到数据库），
        // 然后清空列表，以防止内存占用过多造成OOM
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("read {} rows", rows.size());
    }

    /**
     * 在转换异常 获取其他异常下会调用本接口。抛出异常则停止读取。如果这里不抛出异常则 继续读取下一行。
     * 
     * @param exception 异常
     * @param context 内容
     * @throws Exception 异常
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.debug("解析失败，但是继续解析下一行:{}", exception.toString());
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            log.debug("第{}行，第{}列解析异常，数据为:{}", excelDataConvertException.getRowIndex(),
                excelDataConvertException.getColumnIndex(), excelDataConvertException.getCellData());
        }
    }

    public List<T> getRows() {
        return rows;
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/8/10 zhouleibin creat
 */
