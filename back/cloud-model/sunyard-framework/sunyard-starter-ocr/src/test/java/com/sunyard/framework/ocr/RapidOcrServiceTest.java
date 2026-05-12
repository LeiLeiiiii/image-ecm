package com.sunyard.framework.ocr;
/*
 * Project: Sunyard
 *
 * File Created at 2026/1/20
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import com.benjaminwan.ocrlibrary.OcrResult;
import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;

/**
 * @author Leo
 * @Desc
 * @date 2026/1/20 10:13
 */
public class RapidOcrServiceTest {

    public static void main(String[] args) {
        // 初始化推理引擎（推荐使用 ONNX 模型）
        InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V3);
        String path = "/1.pdf";
        OcrResult result = engine.runOcr(path);
        if (result != null) {
            System.out.println(result.getStrRes());
        }else {
            System.out.println("result.getStrRes()");
        }

    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2026/1/20 Leo creat
 */
