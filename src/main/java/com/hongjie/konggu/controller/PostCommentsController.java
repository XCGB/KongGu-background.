package com.hongjie.konggu.controller;

import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.PostComments;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.AddCommentRequest;
import com.hongjie.konggu.model.request.CommentThumbsRequest;
import com.hongjie.konggu.model.vo.PostCommentsVO;
import com.hongjie.konggu.service.PostCommentsService;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.common.BaseResponse;

import com.hongjie.konggu.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.annotation.Resource;

/**
 * @author: WHJ
 * @createTime: 2023-09-05 09:28
 * @description: 帖子评论控制器
 */
@RequestMapping("/comments")
@Api(tags = "帖子评论控制器")
@RestController
@Slf4j
public class PostCommentsController {
    @Resource
    private PostCommentsService postCommentsService;
    @Resource
    private UsersService usersService;

    /**
     * 发布评论
     *
     * @param addCommentRequest 发布评论封装类
     * @return {@link BaseResponse}<{@link String}>
     */
    @PostMapping("/add")
    @ApiOperation(value = "发布评论")
    public BaseResponse<String> addComment(@RequestBody AddCommentRequest addCommentRequest){
        // 1. 校验合法性
        if (addCommentRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long result = null;
        result = postCommentsService.addComment(addCommentRequest);
        if (result == null || result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtil.success("添加成功");
    }

    /**
     * 获取帖子评论列表
     *
     * @param postId 帖子ID
     * @return {@link BaseResponse}<{@link List}<{@link PostComments}>>
     */
    @GetMapping
    @ApiOperation(value = "获取帖子评论列表")
    public BaseResponse<List<PostCommentsVO>> getListComments(Long postId){
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<PostCommentsVO> commentsList = postCommentsService.getListComments(postId);
        if (commentsList == null) {
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }
        return ResultUtil.success(commentsList);
    }

    /**
     * 评论点赞
     *
     * @param commentThumbsRequest 评论点赞封装类
     * @return {@link BaseResponse}<{@link Integer}>
     */
    @PostMapping("/thumb")
    @ApiOperation(value = "点赞/取消点赞")
    public BaseResponse<Integer> thumbComment(@RequestBody CommentThumbsRequest commentThumbsRequest){
        // 1. 判断请求非空
        if (commentThumbsRequest == null || commentThumbsRequest.getCommentId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long userId = user.getId();
        Long commentId = commentThumbsRequest.getCommentId();
        int result = postCommentsService.thumbComment(userId, commentId);
        return ResultUtil.success(result);

    }


}
