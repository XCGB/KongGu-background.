package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.mapper.PostMapper;
import com.hongjie.konggu.mapper.TagMapper;
import com.hongjie.konggu.model.domain.Post;
import com.hongjie.konggu.model.domain.PostTag;
import com.hongjie.konggu.mapper.PostTagMapper;
import com.hongjie.konggu.model.domain.Tag;
import com.hongjie.konggu.service.PostTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author WHJ
* @description 针对表【post_tag(帖子标签关联表)】的数据库操作Service实现
* @createDate 2023-06-23 20:21:43
*/
@Service
@Slf4j
public class PostTagServiceImpl extends ServiceImpl<PostTagMapper, PostTag>
    implements PostTagService {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private PostTagMapper postTagMapper;

    @Override
    public void addPostTag(Long tagId, Long postId) {
        // 1. 校验参数合法性
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签不存在");
        }
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子不存在");
        }

        // 2. 插入参数
        PostTag postTag = new PostTag();
        postTag.setPostId(postId);
        postTag.setTagId(tagId);
        postTagMapper.insert(postTag);
    }
}




