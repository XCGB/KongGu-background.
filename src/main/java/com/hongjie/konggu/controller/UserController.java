package com.hongjie.konggu.controller;

import com.hongjie.konggu.annotation.AuthCheck;
import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.*;
import com.hongjie.konggu.service.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.hongjie.konggu.constant.UserConstant.ADMIN_ROLE;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 14:06
 * @description: 用户控制层
 */
@RequestMapping("/user")
@Api(tags = "用户端控制器")
@RestController
@Slf4j
public class UserController {
    /**
     * 用户服务
     */
    @Resource
    private UsersService usersService;

    /**
     * 用户登录
     *
     * @param loginRequest 用户登录封装类
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest loginRequest){
        if (loginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String token = usersService.userLogin(loginRequest);
        return ResultUtil.success(token);
    };

    /**
     * 用户注册
     *
     * @param registerRequest 用户注册封装类
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public  BaseResponse<Long>  register(@RequestBody UserRegisterRequest registerRequest){
        if (registerRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long result = usersService.userRegister(registerRequest);
        return ResultUtil.success(result);
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户登出")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = usersService.userLogout(request);
        if (!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(true);
    }

    /**
     * 获取当前用户登录态
     *
     * @param request HTTP请求
     * @return {@link BaseResponse}<{@link UserDTO}>
     */
    @GetMapping("/current")
    @ApiOperation(value = "获取登录态")
    public BaseResponse<UserDTO> getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户登录态
        UserDTO safetyUser = usersService.getLoginUser(request);
        return ResultUtil.success(safetyUser);
    }

    /**
     * 搜索用户信息（如果 username为空，则搜索全部信息）
     *
     * @param username 姓名
     * @return {@link BaseResponse}<{@link List}<{@link Users}>>
     */
    @GetMapping("/search")
    @ApiOperation(value = "搜索用户信息")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<List<Users>> searchUsers(@RequestParam(required = false) String username) {
        List<Users> list = usersService.searchUsers(username);
        return ResultUtil.success(list);
    }

    /**
     * 管理员（新增用户）
     *
     * @param userAppendRequest 新增用户类
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/append")
    @ApiOperation(value = "新增用户（管理员）")
    public BaseResponse<Long> adminAddUser(@RequestBody UserAppendRequest userAppendRequest) {
        if (userAppendRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long result = usersService.appendUser(userAppendRequest);
        return ResultUtil.success(result);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除用户封装类
     * @param request       HTTP请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除用户")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest deleteRequest, HttpServletRequest request){
//        // 检查是否有删除权限
//        if (!usersService.isAdmin(request)){
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }

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
     *
     * @param id          用户ID
     * @param updateUser  用户更新信息
     * @param request     HTTP请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PutMapping("/update/{id}")
    @ApiOperation(value = "更新用户信息")
    public BaseResponse<Boolean> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest updateUser, HttpServletRequest request) {
        // 1. 获取用户登录态
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = usersService.updateUser(id, updateUser,request);
        return ResultUtil.success(result);
    }


}
