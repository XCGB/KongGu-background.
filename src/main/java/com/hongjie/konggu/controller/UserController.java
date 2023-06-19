package com.hongjie.konggu.controller;

import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.domain.request.UserDeleteRequest;
import com.hongjie.konggu.model.domain.request.UserLoginRequest;
import com.hongjie.konggu.model.domain.request.UserRegisterRequest;
import com.hongjie.konggu.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.hongjie.konggu.constant.UserConstant.ADMIN_ROLE;
import static com.hongjie.konggu.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 14:06
 * @description: 用户控制层
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UsersService usersService;

    /**
     * 用户登录
     * @param loginRequest 用户登录封装类
     * @param request 存放登录态
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<Users> userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request){
        if (loginRequest == null) {
            return null;
        }
        Users user = usersService.userLogin(loginRequest,request);
        return ResultUtil.success(user);
    };

    /**
     * 用户注册
     * @param registerRequest 用户注册封装类
     * @return 注册成功的用户ID
     */
    @PostMapping("/register")
    public  BaseResponse<Long>  register(@RequestBody UserRegisterRequest registerRequest){
        if (registerRequest == null) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR);
        }
        Long result = usersService.userRegister(registerRequest);
        return ResultUtil.success(result);
    }

    /**
     * 获取当前用户登录态
     * @param request HTTP请求(包含登录态）
     * @return 脱敏后用户信息
     */
    @GetMapping("/current")
    public BaseResponse<Users> getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 向下转型
        Users currentObj = (Users) userObj;
        // 根据ID查询数据库
        Users safetyUser = usersService.getCurrentUser(currentObj);
        return ResultUtil.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<Users>> searchUsers(@RequestParam(required = false) String username,
                                                 HttpServletRequest request) {
        // 1. 获取用户鉴权信息
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        List<Users> list = usersService.searchUsers(username);
        return ResultUtil.success(list);
    }

    /**
     * 删除用户
     * @param deleteRequest 删除用户封装类
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest deleteRequest, HttpServletRequest request){
        // 检查是否有删除权限
        if (!isAdmin(request)){
            throw new RuntimeException("权限不足");
        }

        // 检查参数是否为空
        Long id = deleteRequest.getId();
        if(id <= 0){
            throw new RuntimeException("参数错误");
        }
        boolean result = usersService.removeById(id);
        return ResultUtil.success(result);
    }


    /**
     * 获取用户鉴权
     *
     * @param request HTTP请求
     * @return 是否有权限
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userRole = request.getSession().getAttribute(USER_LOGIN_STATE);
        Users user = (Users) userRole;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
