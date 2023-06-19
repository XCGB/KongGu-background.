package com.hongjie.konggu.common;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 20:08
 * @description: 返回结果工具类
 */
public class ResultUtil {
    /**
     * 成功
     * @param data 数据
     * @param <T> 泛型
     * @return 通用返回类
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(200, data, "ok");
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return 通用返回类
     */
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code 错误码
     * @param message 状态码信息
     * @param description 状态码描述（详情）
     * @return 通用返回类
     */
    public static BaseResponse error(int code,String message, String description){
        return new BaseResponse<>(code,null,message,description);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @param message 状态码信息
     * @param description 状态码描述（详情）
     * @return 通用返回类
     */
    public static BaseResponse error(ErrorCode errorCode,String message, String description){
        return new BaseResponse<>(errorCode.getCode(),message,description);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @param description 状态码描述（详情）
     * @return 通用返回类
     */
    public static BaseResponse error(ErrorCode errorCode, String description){
        return new BaseResponse<>(errorCode.getCode(),null,errorCode.getMessage(),description);
    }

}
