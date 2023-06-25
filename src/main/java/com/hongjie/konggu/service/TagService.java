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

    /**
     * 获取标签列表
     * @param tagName 标签名
     * @return
     */
    List<Tag> getTagList(String tagName);

    /**
     * 删除标签
     * @param tagId 标签ID
     * @return
     */
    boolean deleteTag(Long tagId);

    /**
     * 添加引用次数
     * @param tagId 标签ID
     */
    void addPostNum(Long tagId);
}
