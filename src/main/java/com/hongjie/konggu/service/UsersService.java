package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.UserAppendRequest;
import com.hongjie.konggu.model.request.UserLoginRequest;
import com.hongjie.konggu.model.request.UserRegisterRequest;
import com.hongjie.konggu.model.request.UserUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
* @author WHJ
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2023-06-17 16:27:43
*/
public interface UsersService extends IService<Users> {

    String userLogin(UserLoginRequest loginRequest);

    Boolean userLogout(HttpServletRequest request);

    Long userRegister(UserRegisterRequest registerRequest);

    UserDTO getLoginUser(HttpServletRequest request);

    List<Users> searchUsers(String username);

    Long appendUser(UserAppendRequest userAppendRequest);

    Boolean updateUser(Long id, UserUpdateRequest updateUser, HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    Users getSafetyUser(Users originUser);


}
