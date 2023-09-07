package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.PostComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.request.AddCommentRequest;
import com.hongjie.konggu.model.vo.PostCommentsVO;

import java.util.List;

/**
* @author WHJ
* @description 针对表【post_comments(帖子评论表)】的数据库操作Service
* @createDate 2023-09-05 09:30:07
*/
public interface PostCommentsService extends IService<PostComments> {

    Long addComment(AddCommentRequest addCommentRequest);

    List<PostCommentsVO> getListComments(Long postId);

    int thumbComment(Long userId, Long commentId);
}
