package com.hongjie.konggu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.domain.PostTag;

/**
* @author WHJ
* @description 针对表【post_tag(帖子标签关联表)】的数据库操作Service
* @createDate 2023-06-23 20:21:43
*/
public interface PostTagService extends IService<PostTag> {

    void addPostTag(Long tagId, Long postId);
}
