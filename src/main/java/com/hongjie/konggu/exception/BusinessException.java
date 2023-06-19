package com.hongjie.konggu.exception;

import com.hongjie.konggu.common.ErrorCode;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 20:16
 * @description: 异常类
 */
public class BusinessException extends RuntimeException{
    /**
     * 状态码
     */
    private final int code;
    /**
     * 状态码描述（详情）
     */
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

