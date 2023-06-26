package com.hongjie.konggu.utils;

import com.hongjie.konggu.model.dto.UserDTO;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 16:17
 * @description:
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
