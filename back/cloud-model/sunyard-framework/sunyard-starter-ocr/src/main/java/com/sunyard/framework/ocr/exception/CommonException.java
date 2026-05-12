package com.sunyard.framework.ocr.exception;


import lombok.Getter;
import lombok.Setter;

/**
 * @author PJW
 */
@Setter
@Getter
public class CommonException extends RuntimeException {
    private String errorCode = "-1";

    public CommonException(String errorCode, String errorDes) {
        super(errorDes);
        this.errorCode = errorCode;
    }

    public CommonException(String errorDes) {
        super(errorDes);
    }
}
