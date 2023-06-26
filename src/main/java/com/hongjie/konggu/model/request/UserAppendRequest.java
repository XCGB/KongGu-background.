package com.hongjie.konggu.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-06-20 14:25
 * @description: 添加用户封装类
 */
@Data
public class UserAppendRequest implements Serializable {
    private static final long serialVersionUID = -4630208710230489573L;

    /**
     * 用户姓名
     */
    private String username;

    /**
     * 用户学号
     */
    private String userAccount;


    /**
     * 用户头像
     */
    private String avatar;

}
