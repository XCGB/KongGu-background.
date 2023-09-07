package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.Follow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.dto.UserDTO;

import java.util.List;

/**
* @author WHJ
* @description 针对表【follow(关注表)】的数据库操作Service
* @createDate 2023-09-06 13:07:02
*/
public interface FollowService extends IService<Follow> {

    void followUser(Long followUserId);

    List<UserDTO> listFans(Long followUserId);

    List<UserDTO> listMyFollow(Long userId);

}
