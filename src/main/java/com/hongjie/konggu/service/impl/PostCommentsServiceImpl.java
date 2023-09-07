package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.mapper.PostCommentsMapper;
import com.hongjie.konggu.model.domain.*;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.AddCommentRequest;
import com.hongjie.konggu.model.vo.PostCommentsVO;
import com.hongjie.konggu.service.CommentThumbsService;
import com.hongjie.konggu.service.PostCommentsService;
import com.hongjie.konggu.service.PostService;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author WHJ
* @description 针对表【post_comments(帖子评论表)】的数据库操作Service实现
* @createDate 2023-09-05 09:30:07
*/
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments>
    implements PostCommentsService{
    /**
     * 帖子服务
     */
    @Resource
    private PostService postService;

    /**
     * 用户服务
     */
    @Resource
    private UsersService usersService;

    /**
     * 评论点赞服务
     */
    @Resource
    private CommentThumbsService commentThumbsService;

    /**
     * 发布评论
     *
     * @param addCommentRequest 发布评论封装类
     * @return {@link Long}
     */
    @Override
    public Long addComment(AddCommentRequest addCommentRequest) {
        // 1. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 校验帖子ID是否合法
        Long postId = addCommentRequest.getPostId();
        if (postId == null || postId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子信息错误");
        }
        // 检查帖子是否非空
        Post postById = postService.getById(postId);
        if (postById == null) {
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }
        // 检查评论内容
        String content = addCommentRequest.getContent();
        if (content == null || postService.containsProfanity(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请文明发言");
        }
        // 更新评论信息
        PostComments postComments = new PostComments();
        postComments.setPostId(postId);
        postComments.setContent(content);
        postComments.setUserId(user.getId());
        boolean saveResult = this.save(postComments);
        // 更新帖子评论数
        Post post = postService.getById(postId);
        postService.update().eq("id", postId)
                .set("commentsNum", post.getCommentsNum() + 1).update();
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子信息保存失败");
        }
        return postComments.getId();
    }

    /**
     * 获取帖子评论列表
     *
     * @param postId 帖子ID
     * @return {@link List}<{@link PostComments}>
     */
    @Override
    public List<PostCommentsVO> getListComments(Long postId) {
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<PostComments> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("postId",postId);
        List<PostComments> postComments = list(queryWrapper);
        return postComments.stream().map((comment) -> {
            PostCommentsVO postCommentsVO = new PostCommentsVO();
            BeanUtils.copyProperties(comment, postCommentsVO);
            Users user = usersService.getById(comment.getUserId());
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            postCommentsVO.setCommentUser(userDTO);
            return postCommentsVO;
        }).collect(Collectors.toList());
    }

    /**
     * 评论点赞
     *
     * @param userId     用户Id
     * @param commentId  评论Id
     * @return {@link Integer}
     */
    @Override
    public int thumbComment(Long userId, Long commentId) {
        //判断帖子是否存在
        PostComments comments = this.getById(commentId);
        if(comments == null){
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }

        //判断是否已点赞
        synchronized (String.valueOf(userId).intern()){
            return doThumbInner(userId, commentId);
        }
    }

    /**
     * 封装了事务方法
     * @param userId     用户ID
     * @param commentId  评论ID
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public int doThumbInner(Long userId,Long commentId){
        CommentThumbs commentThumbs = new CommentThumbs();
        commentThumbs.setUserId(userId);
        commentThumbs.setCommentId(commentId);
        QueryWrapper<CommentThumbs> queryWrapper = new QueryWrapper<>(commentThumbs);
        int count = (int) commentThumbsService.count(queryWrapper);
        //已点赞
        if(count > 0){
            boolean result = commentThumbsService.remove(queryWrapper);
            if(result){
                //帖子点赞数量-1
                this.update()
                        .eq("id",commentId)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return -1;
            } else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }else{
            //未点赞
            boolean result = commentThumbsService.save(commentThumbs);
            if(result){
                //帖子点赞数量+1
                this.update()
                        .eq("id",commentId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                return 1;
            } else{
                throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

}




