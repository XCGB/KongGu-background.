package com.hongjie.konggu.constant;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 14:32
 * @description: 常量接口类
 */
public interface UserConstant {
    /**
     * 用户登录态的键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;

    /**
     * 昵称前缀
     */
    String NICKNAME_PREFIX = "KG-";

    /**
     * 昵称长度
     */
    int NICKNAME_LENGTH = 23;
}
