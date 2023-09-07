package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Follow;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.service.FollowService;
import com.hongjie.konggu.mapper.FollowMapper;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author WHJ
* @description 针对表【follow(关注表)】的数据库操作Service实现
* @createDate 2023-09-06 13:07:02
*/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
    implements FollowService{

    @Resource
    private UsersService usersService;

    @Resource
    private FollowMapper followMapper;

    @Override
    public void followUser(Long followUserId) {
        // 1. 校验参数合法性
        QueryWrapper<Users> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", followUserId);
        Users followUser = usersService.getOne(userQueryWrapper);
        if (followUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = user.getId();
        // 修改数据库
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("followUserId", followUserId);
        Follow follow = getOne(queryWrapper);
        if (follow == null) {
            Follow followed = new Follow();
            followed.setFollowUserId(followUserId);
            followed.setUserId(userId);
            this.save(followed);
        } else {
            Integer isDelete = follow.getLogicDelete();
            if (isDelete == 0) {
                this.update().eq("id", follow.getId())
                        .set("logicDelete", 1).update();
            } else {
                this.update().eq("id", follow.getId())
                        .set("logicDelete", 0).update();
            }
        }
    }

    @Override
    public List<UserDTO> listFans(Long followUserId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followUserId", followUserId);
        List<Follow> list = this.list(queryWrapper);
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        List<Users> usersList = list.stream().map((follow -> usersService.getById(follow.getUserId()))).filter(Objects::nonNull).collect(Collectors.toList());
        return usersList.stream().map((item) -> {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(item, userDTO);
            QueryWrapper<Follow> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", followUserId).eq("followUserId", item.getId());
            long count = this.count(wrapper);
            userDTO.setIsFollow(count > 0);
            return userDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> listMyFollow(Long userId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<Follow> list = this.list(queryWrapper);
        List<Users> usersList = list.stream().map((follow -> usersService.getById(follow.getFollowUserId()))).collect(Collectors.toList());

        return usersList.stream().map((user) -> {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            userDTO.setIsFollow(true);
            return userDTO;
        }).collect(Collectors.toList());
    }


}




