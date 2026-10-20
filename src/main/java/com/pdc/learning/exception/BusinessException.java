package com.pdc.learning.exception;

/**
 * 可预期的业务错误。UI 捕获后显示消息，不打印异常栈。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
