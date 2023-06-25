package com.hongjie.konggu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.domain.PostTag;
import com.hongjie.konggu.model.domain.Tag;

import java.util.List;

/**
* @author WHJ
* @description 针对表【post_tag(帖子标签关联表)】的数据库操作Service
* @createDate 2023-06-23 20:21:43
*/
public interface PostTagService extends IService<PostTag> {
    /**
     * 添加关联记录
     * @param tagId 标签ID
     * @param postId 帖子ID
     */
    void addPostTag(Long tagId, Long postId);

    /**
     * 根据帖子ID查找标签
     * @param postId 帖子ID
     * @return
     */
    List<Tag> searchByPostId(Long postId);
}
