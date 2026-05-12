package com.sunyard.framework.common.exception;
/*
 * Project: com.sunyard.am.exception
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.PrintWriter;
import java.io.StringWriter;

import com.sunyard.framework.common.result.ResultCode;

/**
 * @author zhouleibin
 * @date 2021/6/30 8:28
 */
public class SunyardException extends RuntimeException {

    private static final long serialVersionUID = 604122701395795861L;

    private ResultCode resultCode;

    public SunyardException() {
        super();
    }

    public SunyardException(String message) {
        super(String.format("====[errorMessage ：  %s]", message));
    }

    public SunyardException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public SunyardException(ResultCode code) {
        super(code.getMsg());
        this.resultCode = code;
    }

    /**
     * 异常详情打印到字符串
     * 
     * @param e
     * @return Result
     */
    public static String printToStr(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 异常详情打印到字符串
     * 
     * @param e
     * @return Result
     */
    public static String printToStr(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
