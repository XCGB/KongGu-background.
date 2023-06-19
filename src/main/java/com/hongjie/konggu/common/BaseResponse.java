package com.hongjie.konggu.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 20:03
 * @description: 通用返回类
 */
@Data
public class BaseResponse<T> implements Serializable {
    /**
     * 状态码
     */
    private int code;

    /**
     * 数据
     */
    private T data;

    /**
     * 状态码信息
     */
    private String message;

    /**
     * 状态码描述（详情）
     */
    private String description;


    public BaseResponse(int code, T data, String message,String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message){
        this(code, data,message,"");
    }

    public BaseResponse(int code, T data){
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null, errorCode.getMessage(), errorCode.getDescription());
    }
}
