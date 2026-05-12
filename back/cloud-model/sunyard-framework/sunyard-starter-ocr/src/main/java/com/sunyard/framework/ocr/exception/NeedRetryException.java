package com.sunyard.framework.ocr.exception;

/**
 * @author PJW
 */
public class NeedRetryException extends CommonException {

    public NeedRetryException(String errorCode, String errorDes) {
        super(errorCode, errorDes);
    }

    public NeedRetryException(String errorDes) {
        super(errorDes);
    }
}
