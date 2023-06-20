package com.hongjie.konggu.service;

import com.hongjie.konggu.model.domain.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author WHJ
* @description 针对表【post(帖子表)】的数据库操作Service
* @createDate 2023-06-17 16:31:33
*/
public interface PostService extends IService<Post> {

    /**
     * 添加帖子
     * @param post 帖子对象
     * @param request 请求对象
     * @return 帖子ID
     */
    Long addPost(Post post, HttpServletRequest request);

    List<Post> listByUser();

    /**
     * 检查帖子是否非法，若非法则直接抛出异常
     * @param post 帖子对象
     */
    void validPost(Post post);

}
