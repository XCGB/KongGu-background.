package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author WHJ
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2023-06-23 14:18:58
*/
public interface TagService extends IService<Tag> {
    List<Tag> getTagList(String tagName);
    boolean deleteTag(Long tagId);


}
