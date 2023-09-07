package com.hongjie.konggu.controller;

import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.service.FollowService;
import com.hongjie.konggu.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.annotation.Resource;

/**
 * @author: WHJ
 * @createTime: 2023-09-06 13:08
 * @description: 关注控制器
 */
@RequestMapping("/follow")
@Api(tags = "关注管理模块")
@RestController
@Slf4j
public class FollowController {
    /**
     * 关注服务
     */
    @Resource
    private FollowService followService;

    /**
     * 关注用户
     *
     * @param followUserId 关注用户Id
     * @return {@link BaseResponse}<{@link String}>
     */
    @PostMapping("/{followUserId}")
    @ApiOperation(value = "关注用户")
    public BaseResponse<String> followUser(@PathVariable Long followUserId) {
        if (followUserId == null || followUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        followService.followUser(followUserId);
        return ResultUtil.success("ok");
    }

    /**
     * 获取我的粉丝
     *
     * @return {@link BaseResponse}<{@link List}<{@link UserDTO}>>
     */
    @GetMapping("/fans")
    public BaseResponse<List<UserDTO>> listFans(){
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<UserDTO> userDTOList = followService.listFans(user.getId());
        return ResultUtil.success(userDTOList);
    }


    /**
     * 获取我的关注
     *
     * @return {@link BaseResponse}<{@link List}<{@link UserDTO}>>
     */
    @GetMapping("/my")
    @ApiOperation(value = "获取我的关注")
    public BaseResponse<List<UserDTO>> listMyFollow(){
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<UserDTO> userDTOList = followService.listMyFollow(user.getId());
        return ResultUtil.success(userDTOList);
    }
}
