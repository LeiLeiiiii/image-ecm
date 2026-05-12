package com.sunyard.ecm.exception;

/**
 * @author yzy
 * @desc
 * @since 2025/2/27
 */
public class OldToNewException extends RuntimeException {

    public OldToNewException(String message) {
        super(String.format("====[errorMessage ：  %s]", message));
    }
}
