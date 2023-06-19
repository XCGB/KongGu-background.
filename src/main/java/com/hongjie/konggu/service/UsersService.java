package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.domain.request.UserLoginRequest;
import com.hongjie.konggu.model.domain.request.UserRegisterRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author WHJ
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2023-06-17 16:27:43
*/
public interface UsersService extends IService<Users> {

    /**
     * 用户登录
     * @param loginRequest 学号 + 密码
     * @param request 存放登录态
     * @return 脱敏后的用户信息
     */
    Users userLogin(UserLoginRequest loginRequest, HttpServletRequest request);

    /**
     * 用户注册
     * @param registerRequest 用户注册封装类
     * @return 注册成功的用户ID
     */
    Long userRegister(UserRegisterRequest registerRequest);

    /**
     * 获取当前用户登录态
     * @param currentObj 包含用户类的信息
     * @return 脱敏后的用户
     */
    Users getCurrentUser(Users currentObj);

    /**
     * 搜索用户信息
     * @param username 可能为空
     * @return 用户列表
     */
    List<Users> searchUsers(String username);


    /**
     * 用户脱敏
     * @param originUser 脱敏前用户
     * @return 脱敏后的用户信息（密码隐藏）
     */
    Users getSafetyUser(Users originUser);

}