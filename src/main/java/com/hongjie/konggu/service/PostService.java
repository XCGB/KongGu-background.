package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongjie.konggu.model.request.PostAddRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author WHJ
* @description 针对表【post(帖子表)】的数据库操作Service
* @createDate 2023-06-17 16:31:33
*/
public interface PostService extends IService<Post> {
    Long addPost(PostAddRequest post);

    List<Post> listByUser();

    List<Post> searchPosts(Long userId);

    void validPost(Post post);

    boolean containsProfanity(String content);
}
