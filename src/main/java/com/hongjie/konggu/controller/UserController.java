package com.hongjie.konggu.controller;

import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.*;
import com.hongjie.konggu.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.List;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 14:06
 * @description: 用户控制层
 */
@RequestMapping("/user")
@RestController
@Slf4j
public class UserController {
    @Resource
    private UsersService usersService;

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
     * 用户登录
     * @param loginRequest 用户登录封装类
     * @param session 存放登录态
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest loginRequest, HttpSession session){
        if (loginRequest == null) {
            return null;
        }
        String token = usersService.userLogin(loginRequest,session);

        return ResultUtil.success(token);
    };

    /**
     * 用户登出
     * @param request 请求对象
     * @return boolean
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            return ResultUtil.error(ErrorCode.NULL_ERROR);
        }
        Boolean result = usersService.userLogout(request);
        if (result){
            return ResultUtil.success(result);
        }
        return ResultUtil.error(ErrorCode.INSERT_ERROR);
    }

    /**
     * 获取当前用户登录态
     * @param request HTTP请求(包含登录态）
     * @return 脱敏后用户信息
     */
    @GetMapping("/current")
    public BaseResponse<UserDTO> getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户登录态
        UserDTO safetyUser = usersService.getLoginUser(request);

        return ResultUtil.success(safetyUser);
    }

    /**
     * 搜索用户信息（ 如果 username 为空，则搜索全部信息）
     * @param username 姓名
     * @param request HTTP请求
     * @return 列表组成的用户信息
     */
    @GetMapping("/search")
    public BaseResponse<List<Users>> searchUsers(@RequestParam(required = false) String username,
                                                 HttpServletRequest request) {
        // 1. 获取用户鉴权信息
        if (!usersService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        List<Users> list = usersService.searchUsers(username);
        return ResultUtil.success(list);
    }

    /**
     * 管理员（新增用户）
     * @param userAppendRequest 新增用户类
     * @return 新增用户ID
     */
    @PostMapping("/append")
    public BaseResponse<Long> adminAddUser(@RequestBody UserAppendRequest userAppendRequest) {
        if (userAppendRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long result = usersService.appendUser(userAppendRequest);
        return ResultUtil.success(result);
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
        if (!usersService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 检查参数是否为空
        Long id = deleteRequest.getId();
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = usersService.removeById(id);
        return ResultUtil.success(result);
    }

    /**
     * 更新当前用户信息
     * @param id 用户ID
     * @param updateUser 更新信息
     * @param request 请求对象
     * @return 是否更新成功
     */
    @PutMapping("/update/{id}")
    public BaseResponse<Boolean> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest updateUser, HttpServletRequest request) {
        // 1. 获取用户登录态
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Boolean result = usersService.updateUser(id, updateUser,request);
        return ResultUtil.success(result);
    }


}
