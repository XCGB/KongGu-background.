package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.mapper.PostTagMapper;
import com.hongjie.konggu.mapper.TagMapper;
import com.hongjie.konggu.model.domain.PostTag;
import com.hongjie.konggu.model.domain.PostThumb;
import com.hongjie.konggu.model.domain.Tag;
import com.hongjie.konggu.service.PostTagService;
import com.hongjie.konggu.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author WHJ
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2023-06-23 14:18:58
*/
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    @Resource
    PostTagService postTagService;

    @Override
    public List<Tag> getTagList(String tagName) {
        if (tagName == null){
            // 1.1 返回所有标签
            return list();
        }
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("tagName", tagName);

        List<Tag> tagList = list(queryWrapper);
        return tagList;
    }

    @Override
    public boolean deleteTag(Long tagId) {
        // 1. 校验参数是否合法
        if (tagId == null || tagId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 删除标签记录
        this.removeById(tagId);

        // 3. 删除标签帖子关联记录
        QueryWrapper<PostTag> postTagQueryWrapper = new QueryWrapper<>();
        postTagQueryWrapper.eq("tagId", tagId);
        boolean tagResult = postTagService.remove(postTagQueryWrapper);
        if (!tagResult) {
            log.error("postTag delete failed, tagId = {}", tagId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 4. 返回结果
        return true;
    }
}




