package com.pdc.learning.exception;

/**
 * 数据文件读写错误。启动阶段安全退出，运行阶段由 UI 提示。
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
