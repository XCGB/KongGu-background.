package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author WHJ
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service
* @createDate 2023-06-17 16:32:41
*/
public interface PostThumbService extends IService<PostThumb> {
    /**
     * 点赞/取消点赞
     * @param userId
     * @param postId
     * @return
     */
    long doThumb(Long userId, Long postId);
}
